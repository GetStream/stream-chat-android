/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ai.a2ui.core

/** Constants of the A2UI 1.0 protocol. */
internal object A2UIProtocol {
    /** The version every 1.0 message carries. */
    const val VERSION: String = "1.0"

    /** The id of every surface's top-level component. */
    const val ROOT_ID: String = "root"

    /** The reserved type of the implicit container that holds a surface's root. */
    const val SURFACE_TYPE: String = "Surface"

    /** The key of a data binding. */
    const val PATH_KEY: String = "@path"

    /** The key of a function call. */
    const val CALL_KEY: String = "@call"

    /** The prefix of the protocol's own functions, such as `@index`. */
    const val SYSTEM_PREFIX: String = "@"

    /** How deep components and JSON values may nest. */
    const val MAX_NESTING_DEPTH: Int = 50

    /**
     * The most components a surface may expand to, counting a component once for each place it
     * shows, so a component listed as a child several times on each level can't build millions.
     */
    const val MAX_EXPANDED_COMPONENTS: Int = 5000

    /** How deep `@call`s may nest through their arguments. */
    const val MAX_CALL_DEPTH: Int = 5

    /** The id of the A2UI 1.0 basic catalog. */
    const val BASIC_CATALOG_ID: String = "https://a2ui.org/specification/v1_0/catalogs/basic/catalog.json"
}

/** The spec's rule for ids, function names and extension keys: UAX #31 identifiers, which may start with `_`. */
internal object A2UIIdentifier {
    fun isValid(name: String): Boolean {
        if (name.isEmpty()) return false
        val first = name.codePointAt(0)
        if (first != '_'.code && !isStart(first)) return false
        var index = Character.charCount(first)
        while (index < name.length) {
            val codePoint = name.codePointAt(index)
            if (!isContinue(codePoint)) return false
            index += Character.charCount(codePoint)
        }
        return true
    }

    fun isStart(codePoint: Int): Boolean = Character.isUnicodeIdentifierStart(codePoint)

    fun isContinue(codePoint: Int): Boolean =
        Character.isUnicodeIdentifierPart(codePoint) && !Character.isIdentifierIgnorable(codePoint)
}

/** Escapes a key for a JSON Pointer. */
internal fun pointerEscape(key: String): String = key.replace("~", "~0").replace("/", "~1")

/**
 * A component property as the agent wrote it: a literal, a binding (`{"@path": "/a/b"}`) or a
 * function call (`{"@call": "formatString", "args": {...}}`).
 */
internal sealed interface DynamicValue {
    data class Literal(val value: Any?) : DynamicValue
    data class Binding(val path: DataPath) : DynamicValue
    data class Call(val call: FunctionCall) : DynamicValue
    data object Missing : DynamicValue

    companion object {
        /**
         * Reads a raw value. An object is a binding or a call only when it carries `@path` or
         * `@call`; any other object is a literal, with its `@@`-escaped keys unescaped. An object
         * that misuses the reserved `@` prefix reads as missing.
         */
        fun of(raw: Any?): DynamicValue {
            if (raw == null) return Missing
            val map = A2UIJson.map(raw) ?: return Literal(raw)
            if (map.containsKey(A2UIProtocol.CALL_KEY)) return FunctionCall.of(map)?.let(::Call) ?: Missing
            if (map.containsKey(A2UIProtocol.PATH_KEY)) {
                val path = map[A2UIProtocol.PATH_KEY] as? String
                return if (map.size == 1 && path != null) Binding(DataPath.parse(path)) else Missing
            }
            return unescapedLiteral(map)?.let(::Literal) ?: Missing
        }

        /** [map] with its `@@` keys unescaped, or `null` when a key uses the reserved single `@`. */
        fun unescapedLiteral(map: Map<String, Any?>): Map<String, Any?>? {
            val literal = LinkedHashMap<String, Any?>()
            for ((key, value) in map) {
                when {
                    key.startsWith("@@") -> literal[key.substring(1)] = value
                    key.startsWith("@") -> return null
                    else -> literal[key] = value
                }
            }
            return literal
        }

        /** How a raw value misuses the reserved `@` prefix, nested values included, or `null`. */
        @Suppress("ReturnCount")
        fun reservedKeyViolation(raw: Any?): String? {
            A2UIJson.list(raw)?.let { list -> return list.firstNotNullOfOrNull(::reservedKeyViolation) }
            val map = A2UIJson.map(raw) ?: return null
            if (map.containsKey(A2UIProtocol.PATH_KEY)) {
                val path = map[A2UIProtocol.PATH_KEY] as? String ?: return "'@path' must be a string."
                if (map.size != 1) return "A '@path' binding cannot carry other keys."
                if (!DataPath.isValidSyntax(path)) return "Invalid path syntax: '$path'."
                return null
            }
            if (map.containsKey(A2UIProtocol.CALL_KEY)) {
                val name = map[A2UIProtocol.CALL_KEY] as? String ?: return "'@call' must be a string."
                if (name.startsWith(A2UIProtocol.SYSTEM_PREFIX)) {
                    if (name != "@index") return "Unknown system function '$name'."
                } else if (!A2UIIdentifier.isValid(name)) {
                    return "The function name '$name' must be a valid UAX #31 identifier."
                }
                val args = map["args"] ?: return null
                val argsMap = A2UIJson.map(args) ?: return "'args' must be an object."
                return argsMap.values.firstNotNullOfOrNull(::reservedKeyViolation)
            }
            for ((key, value) in map) {
                if (key.startsWith("@") && !key.startsWith("@@")) {
                    return "The key '$key' uses the reserved '@' prefix; escape a literal '@' as '@@'."
                }
                reservedKeyViolation(value)?.let { return it }
            }
            return null
        }

        /** Every `@call` in a raw value, with the JSON Pointer to it from the value. */
        fun calls(raw: Any?, pointer: String = ""): List<Pair<String, Map<String, Any?>>> {
            A2UIJson.list(raw)?.let { list ->
                return list.flatMapIndexed { index, item -> calls(item, "$pointer/$index") }
            }
            val map = A2UIJson.map(raw) ?: return emptyList()
            if (map.containsKey(A2UIProtocol.PATH_KEY)) return emptyList()
            val own = if (map.containsKey(A2UIProtocol.CALL_KEY)) listOf(pointer to map) else emptyList()
            return own + map.keys.sorted().flatMap { key -> calls(map[key], "$pointer/${pointerEscape(key)}") }
        }

        /** How deeply `@call`s nest in a raw value, through their arguments. */
        fun callDepth(raw: Any?): Int {
            A2UIJson.list(raw)?.let { list -> return list.maxOfOrNull(::callDepth) ?: 0 }
            val map = A2UIJson.map(raw) ?: return 0
            val nested = map.values.maxOfOrNull(::callDepth) ?: 0
            return if (map.containsKey(A2UIProtocol.CALL_KEY)) nested + 1 else nested
        }

        /** A literal `formatString` template that doesn't parse, with the pointer to it, or `null`. */
        fun formatStringViolation(raw: Any?, pointer: String = ""): Pair<String, String>? {
            A2UIJson.list(raw)?.let { list ->
                list.forEachIndexed { index, item -> formatStringViolation(item, "$pointer/$index")?.let { return it } }
                return null
            }
            val map = A2UIJson.map(raw) ?: return null
            if (map.containsKey(A2UIProtocol.PATH_KEY)) return null
            val template = A2UIJson.map(map["args"])?.get("value") as? String
            if (map[A2UIProtocol.CALL_KEY] == "formatString" && template != null) {
                try {
                    ExpressionParser.parseTemplate(template)
                } catch (error: ExpressionException) {
                    return "$pointer/args/value" to (error.message ?: "Invalid template")
                }
            }
            for (key in map.keys.sorted()) {
                formatStringViolation(map[key], "$pointer/${pointerEscape(key)}")?.let { return it }
            }
            return null
        }
    }
}

/**
 * A function call in a component tree. It runs in the catalog it names, else in the surface's
 * default catalog. Names that start with `@` are the protocol's own functions.
 */
internal data class FunctionCall(
    val name: String,
    val catalogId: String? = null,
    val arguments: Map<String, Any?> = emptyMap(),
) {
    val isSystemFunction: Boolean get() = name.startsWith(A2UIProtocol.SYSTEM_PREFIX)

    /** The call as JSON. */
    fun toJson(): Map<String, Any?> = buildMap {
        put(A2UIProtocol.CALL_KEY, name)
        catalogId?.let { put("catalogId", it) }
        if (arguments.isNotEmpty()) put("args", arguments)
    }

    companion object {
        fun of(json: Map<String, Any?>): FunctionCall? {
            val name = (json[A2UIProtocol.CALL_KEY] as? String)?.takeIf { it.isNotEmpty() } ?: return null
            return FunctionCall(name, json["catalogId"] as? String, A2UIJson.map(json["args"]).orEmpty())
        }
    }
}

/** The children of a container: listed ids, or a template repeated for every item at a path. */
internal sealed interface ChildList {
    data class Ids(val ids: List<String>) : ChildList
    data class Template(val componentId: String, val path: DataPath) : ChildList
    data object None : ChildList

    companion object {
        fun of(raw: Any?): ChildList {
            A2UIJson.list(raw)?.let { list -> return Ids(list.filterIsInstance<String>()) }
            val map = A2UIJson.map(raw) ?: return None
            val componentId = map["componentId"] as? String ?: return None
            val path = map["path"] as? String ?: return None
            return Template(componentId, DataPath.parse(path))
        }
    }
}

/** What an interactive component does: send an event to the agent, or run a function here. */
internal sealed interface ActionDefinition {
    data class Event(
        val name: String,
        val userMessage: DynamicValue,
        val context: Map<String, DynamicValue>,
    ) : ActionDefinition
    data class Function(val call: FunctionCall) : ActionDefinition

    companion object {
        fun of(raw: Any?): ActionDefinition? {
            val map = A2UIJson.map(raw) ?: return null
            A2UIJson.map(map["event"])?.let { event ->
                val name = event["name"] as? String ?: return null
                return Event(
                    name = name,
                    userMessage = DynamicValue.of(event["userMessage"]),
                    context = A2UIJson.map(event["context"]).orEmpty().mapValues { DynamicValue.of(it.value) },
                )
            }
            A2UIJson.map(map["functionCall"])?.let { call -> return FunctionCall.of(call)?.let(::Function) }
            return null
        }
    }
}

/** A check of a component: a condition that must pass, with a message for when it doesn't. */
internal data class CheckRule(val condition: DynamicValue, val message: String?) {
    companion object {
        fun list(raw: Any?): List<CheckRule> = A2UIJson.list(raw).orEmpty().mapNotNull { entry ->
            val map = A2UIJson.map(entry) ?: return@mapNotNull null
            val condition = DynamicValue.of(map["condition"])
            if (condition == DynamicValue.Missing) null else CheckRule(condition, map["message"] as? String)
        }
    }
}

/** The outcome of a check. */
internal data class ValidationResult(
    val isValid: Boolean,
    val code: String? = null,
    val message: String? = null,
    val severity: String = SEVERITY_ERROR,
) {
    /** The result as JSON. */
    fun toJson(): Map<String, Any?> = buildMap {
        put("valid", isValid)
        code?.let { put("code", it) }
        message?.let { put("message", it) }
        put("severity", severity)
    }

    companion object {
        const val SEVERITY_ERROR: String = "error"
        private val SEVERITIES = setOf("error", "warning", "info")

        val Valid: ValidationResult = ValidationResult(isValid = true)

        /**
         * Reads an evaluated value as a result. A boolean passes or fails, so `and` works as a
         * condition, and a value still on its way (`null`) passes, so a form isn't blocked while
         * its data streams in.
         */
        fun of(value: Any?): ValidationResult {
            if (value == null) return Valid
            val map = A2UIJson.map(value)
            val valid = map?.get("valid")?.let(A2UIJson::bool)
            if (map != null && valid != null) {
                return ValidationResult(
                    isValid = valid,
                    code = map["code"] as? String,
                    message = map["message"] as? String,
                    severity = (map["severity"] as? String)?.takeIf { it in SEVERITIES } ?: SEVERITY_ERROR,
                )
            }
            return ValidationResult(isValid = A2UIJson.bool(value) ?: true)
        }
    }
}
