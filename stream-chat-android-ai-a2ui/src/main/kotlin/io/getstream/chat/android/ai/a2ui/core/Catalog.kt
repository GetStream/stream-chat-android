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

/**
 * A component type of a catalog: the JSON Schema of its properties, as the agent is told, and
 * where it may be placed.
 */
internal data class ComponentDefinition(
    val name: String,
    val description: String? = null,
    val properties: Map<String, Map<String, Any?>> = emptyMap(),
    val required: List<String> = emptyList(),
    val allowedParents: List<String>? = null,
    val allowedChildren: List<String>? = null,
) {
    /** The component's schema, as it appears in a catalog document. */
    fun toJson(): Map<String, Any?> = buildMap {
        put("type", "object")
        description?.let { put("description", it) }
        put("properties", properties + ("component" to mapOf("const" to name)))
        put("required", (required + "component").distinct())
        allowedParents?.let { put("allowedParents", it) }
        allowedChildren?.let { put("allowedChildren", it) }
    }

    /**
     * The ids [component] holds, with the JSON Pointer to each from the component, found from the
     * schema: a `ComponentId`, a `ChildList`, or either inside an array or object, such as each of
     * a `Tabs`' tabs.
     */
    fun childReferences(component: Component): List<Pair<String, String>> = properties.keys.sorted().flatMap { key ->
        references(component.properties[key], properties[key].orEmpty(), "/${pointerEscape(key)}")
    }

    /** The child lists of [component] that are neither ids nor a template. */
    fun childListViolations(
        component: Component,
    ): List<Pair<String, String>> = properties.keys.sorted().mapNotNull { key ->
        val reference = properties[key]?.get("\$ref") as? String
        if (reference?.endsWith("/ChildList") != true || !component.properties.containsKey(key)) return@mapNotNull null
        val value = component.properties[key]
        if (A2UIJson.list(value)?.all { it is String } == true) return@mapNotNull null
        val template = A2UIJson.map(value)
        if (template != null && template["componentId"] is String && template["path"] is String) return@mapNotNull null
        "/${pointerEscape(key)}" to
            "'$key' must be a list of component ids, or a template with a 'componentId' and a 'path'."
    }

    private fun references(value: Any?, schema: Map<String, Any?>, pointer: String): List<Pair<String, String>> {
        val reference = schema["\$ref"] as? String
        if (reference != null) {
            if (reference.endsWith("/ComponentId") || reference.endsWith("/Child")) {
                return (value as? String)?.let { listOf(pointer to it) }.orEmpty()
            }
            if (reference.endsWith("/ChildList")) {
                return when (val children = ChildList.of(value)) {
                    is ChildList.Ids -> children.ids.mapIndexed { index, id -> "$pointer/$index" to id }
                    is ChildList.Template -> listOf("$pointer/componentId" to children.componentId)
                    ChildList.None -> emptyList()
                }
            }
        }
        val items = A2UIJson.map(schema["items"])
        val list = A2UIJson.list(value)
        if (items != null && list != null) {
            return list.flatMapIndexed { index, item -> references(item, items, "$pointer/$index") }
        }
        val nested = A2UIJson.map(schema["properties"])
        val objectValue = A2UIJson.map(value)
        if (nested != null && objectValue != null) {
            return nested.keys.sorted().flatMap { key ->
                val schema = A2UIJson.map(nested[key]) ?: return@flatMap emptyList()
                references(objectValue[key], schema, "$pointer/${pointerEscape(key)}")
            }
        }
        return emptyList()
    }
}

/** A function call's arguments, already evaluated, and where it runs. */
internal class FunctionInvocation(
    val name: String,
    val arguments: Map<String, Any?>,
    val context: DataContext,
    val services: RendererServices,
    private val evaluator: Evaluator?,
) {
    fun value(key: String): Any? = arguments[key]

    fun string(key: String): String? = A2UIJson.string(value(key))

    fun double(key: String): Double? = A2UIJson.double(value(key))

    fun int(key: String): Int? = A2UIJson.int(value(key))

    /** A boolean argument; a validation result reads as its `valid` flag. */
    fun bool(key: String): Boolean? {
        val value = value(key) ?: return null
        A2UIJson.map(value)?.let { map -> if (map.containsKey("valid")) return A2UIJson.bool(map["valid"]) }
        return A2UIJson.bool(value)
    }

    fun list(key: String): List<Any?>? = A2UIJson.list(value(key))

    fun requireString(key: String): String = string(key) ?: throw FunctionException.missingArgument(key, name)

    fun requireDouble(key: String): Double = double(key) ?: throw FunctionException.missingArgument(key, name)

    /** Interpolates a `formatString` template in the call's scope. */
    fun interpolate(template: String): String =
        evaluator?.interpolate(template, context)
            ?: throw FunctionException(RendererError.EXECUTION_ERROR, "No evaluator for '$name'.")
}

/** A function that failed, with the code reported to the agent. */
internal class FunctionException(val code: String, message: String) : Exception(message) {
    companion object {
        fun missingArgument(key: String, function: String) =
            FunctionException(RendererError.INVALID_FUNCTION_CALL, "Function '$function' requires the argument '$key'.")

        fun invalidArgument(key: String, function: String, reason: String) =
            FunctionException(
                RendererError.INVALID_FUNCTION_CALL,
                "Function '$function' received an invalid value for '$key': $reason",
            )
    }
}

/** What a function does, given its call. */
internal typealias FunctionImplementation = (FunctionInvocation) -> Any?

/** A function a renderer evaluates, or the agent asks it to. */
internal data class FunctionDefinition(
    val name: String,
    val description: String = "",
    val argumentsSchema: Map<String, Any?>? = null,
    val returnType: String = "any",
    val allowedCallers: String = RENDERER_ONLY,
    val requiresUserActivation: Boolean = false,
    val implementation: FunctionImplementation? = null,
) {
    val isAgentCallable: Boolean get() = allowedCallers == AGENT_ONLY || allowedCallers == RENDERER_OR_AGENT
    val isRendererCallable: Boolean get() = allowedCallers == RENDERER_ONLY || allowedCallers == RENDERER_OR_AGENT

    /** The function's schema, as it appears in a catalog document. */
    fun toJson(): Map<String, Any?> = buildMap {
        put("type", "object")
        put("description", description)
        put("returnType", returnType)
        if (allowedCallers != RENDERER_ONLY) put("allowedCallers", allowedCallers)
        if (requiresUserActivation) put("requiresUserActivation", true)
        val properties = buildMap {
            put(A2UIProtocol.CALL_KEY, mapOf("const" to name))
            argumentsSchema?.let { put("args", it) }
        }
        put("properties", properties)
        put("required", listOfNotNull(A2UIProtocol.CALL_KEY, argumentsSchema?.let { "args" }))
    }

    companion object {
        const val RENDERER_ONLY: String = "rendererOnly"
        const val AGENT_ONLY: String = "agentOnly"
        const val RENDERER_OR_AGENT: String = "rendererOrAgent"
    }
}

/**
 * A set of components and functions a renderer supports, under an id the agent and the
 * renderer agree on. The id is conventionally a URI, never fetched.
 */
internal data class Catalog(
    val catalogId: String,
    val components: Map<String, ComponentDefinition> = emptyMap(),
    val functions: Map<String, FunctionDefinition> = emptyMap(),
    val title: String? = null,
    val description: String? = null,
    val instructions: String? = null,
    /** The `$defs` of the catalog document, such as `Weight`, which component schemas refer to. */
    val definitions: Map<String, Any?> = emptyMap(),
) {
    fun component(name: String): ComponentDefinition? = components[name]

    fun function(name: String): FunctionDefinition? = functions[name]

    /** A copy with more components, under another id when given. */
    fun adding(components: List<ComponentDefinition>, catalogId: String = this.catalogId): Catalog =
        copy(catalogId = catalogId, components = this.components + components.associateBy { it.name })

    /** The catalog as an A2UI 1.0 catalog document, which an agent writes against. */
    fun toJson(): Map<String, Any?> = buildMap {
        put("\$schema", "https://json-schema.org/draft/2020-12/schema")
        put("\$id", catalogId)
        put("protocolVersion", A2UIProtocol.VERSION)
        title?.let { put("title", it) }
        description?.let { put("description", it) }
        put("catalogId", catalogId)
        instructions?.let { put("instructions", it) }
        put("components", components.mapValues { it.value.toJson() })
        if (functions.isNotEmpty()) put("functions", functions.mapValues { it.value.toJson() })
        val defs = LinkedHashMap(definitions)
        defs["anyComponent"] = mapOf(
            "oneOf" to components.keys.map { mapOf("\$ref" to "#/components/$it") },
            "discriminator" to mapOf("propertyName" to "component"),
        )
        defs["anyFunction"] = mapOf("oneOf" to functions.keys.map { mapOf("\$ref" to "#/functions/$it") })
        put("\$defs", defs)
    }

    companion object {
        /**
         * Reads a catalog document, such as the published basic catalog. Component schemas are
         * flattened: the properties and requirements of `allOf` parts, and of the common types
         * they refer to, join the component's own.
         *
         * @param implementations What each function does, by name. A function without one is
         * routed to the agent.
         */
        fun fromJson(
            document: Map<String, Any?>,
            implementations: Map<String, FunctionImplementation> = emptyMap(),
        ): Catalog {
            val catalogId = document["catalogId"] as? String ?: document["\$id"] as? String ?: ""
            val components = A2UIJson.map(document["components"]).orEmpty().mapNotNull { (name, raw) ->
                A2UIJson.map(raw)?.let { name to component(name, it) }
            }.toMap()
            val functions = A2UIJson.map(document["functions"]).orEmpty().mapNotNull { (name, raw) ->
                A2UIJson.map(raw)?.let { name to function(name, it, implementations[name]) }
            }.toMap()
            val definitions = A2UIJson.map(document["\$defs"]).orEmpty()
                .filterKeys { it != "anyComponent" && it != "anyFunction" }
            return Catalog(
                catalogId = catalogId,
                components = components,
                functions = functions,
                title = document["title"] as? String,
                description = document["description"] as? String,
                instructions = document["instructions"] as? String,
                definitions = definitions,
            )
        }

        private fun component(name: String, schema: Map<String, Any?>): ComponentDefinition {
            val properties = LinkedHashMap<String, Map<String, Any?>>()
            val required = mutableListOf<String>()
            fun collect(part: Map<String, Any?>) {
                (part["\$ref"] as? String)?.let { reference -> CommonTypes.resolve(reference)?.let(::collect) }
                A2UIJson.map(part["properties"])?.forEach { (key, value) ->
                    A2UIJson.map(value)?.let { properties[key] = it }
                }
                A2UIJson.list(part["required"])?.filterIsInstance<String>()?.let(required::addAll)
                A2UIJson.list(part["allOf"])?.forEach { entry -> A2UIJson.map(entry)?.let(::collect) }
            }
            collect(schema)
            properties.remove("component")
            return ComponentDefinition(
                name = name,
                description = schema["description"] as? String,
                properties = properties,
                required = required.filter { it != "component" }.distinct(),
                allowedParents = A2UIJson.list(schema["allowedParents"])?.filterIsInstance<String>(),
                allowedChildren = A2UIJson.list(schema["allowedChildren"])?.filterIsInstance<String>(),
            )
        }

        private fun function(
            name: String,
            schema: Map<String, Any?>,
            implementation: FunctionImplementation?,
        ): FunctionDefinition {
            val properties = A2UIJson.map(schema["properties"]).orEmpty()
            return FunctionDefinition(
                name = name,
                description = schema["description"] as? String ?: "",
                argumentsSchema = A2UIJson.map(properties["args"]),
                returnType = schema["returnType"] as? String ?: "any",
                allowedCallers = schema["allowedCallers"] as? String ?: FunctionDefinition.RENDERER_ONLY,
                requiresUserActivation = A2UIJson.bool(schema["requiresUserActivation"]) ?: false,
                implementation = implementation,
            )
        }
    }
}

/**
 * The catalogs a renderer supports. A component or call resolves in the catalog it names, else
 * in the surface's default: there is deliberately no fallback to any other catalog.
 *
 * @param defaultCatalogId The catalog of a surface that names none. The spec requires one; this
 * keeps agents that leave it out working.
 */
internal class CatalogRegistry(catalogs: List<Catalog>, val defaultCatalogId: String? = null) {
    val catalogs: Map<String, Catalog> = catalogs.associateBy { it.catalogId }

    val supportedCatalogIds: List<String> get() = catalogs.keys.sorted()

    fun catalog(catalogId: String?): Catalog? = catalogId?.let(catalogs::get)

    fun resolve(entityCatalogId: String?, surfaceCatalogId: String?): Catalog? {
        if (entityCatalogId != null) return catalog(entityCatalogId)
        if (surfaceCatalogId != null) return catalog(surfaceCatalogId)
        return catalog(defaultCatalogId)
    }
}

/** The common types of A2UI 1.0, which catalogs refer to as `common_types.json#/$defs/...`. */
internal object CommonTypes {
    private val definitions: Map<String, Any?> by lazy {
        val text = Resources.read("common_types.json")
        A2UIJson.map(A2UIJson.map(A2UIJson.parse(text))?.get("\$defs")).orEmpty()
    }

    /** The schema a `$ref` to a common type names, or `null`. */
    fun resolve(reference: String): Map<String, Any?>? {
        if (!reference.contains("common_types.json#/\$defs/") && !reference.startsWith("#/\$defs/")) return null
        return A2UIJson.map(definitions[reference.substringAfterLast('/')])
    }
}

/** The JSON files bundled with the module. */
internal object Resources {
    fun read(name: String): String {
        val stream = Resources::class.java.getResourceAsStream(name)
            ?: error("The A2UI resource $name is missing")
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}

/**
 * Checks values against the JSON Schemas of catalogs: types, `enum`, `const`, required and
 * closed properties, array sizes, number bounds and the `oneOf`/`anyOf`/`allOf` combinators.
 * Common types resolve by name; a binding or a call stands for a value of any type.
 */
internal object SchemaValidator {

    /** The first problem of [value] against [schema], as a pointer and a message, or `null`. */
    @Suppress("ReturnCount")
    fun violation(
        value: Any?,
        schema: Map<String, Any?>,
        catalog: Catalog? = null,
        allowsDynamicValues: Boolean = false,
        pointer: String = "",
    ): Pair<String, String>? {
        if (allowsDynamicValues && isDynamic(value)) return null
        (schema["\$ref"] as? String)?.let { reference ->
            commonTypeViolation(reference, value, allowsDynamicValues, pointer)?.let { found ->
                return found.takeIf { it.second.isNotEmpty() }
            }
            val resolved = resolve(reference, catalog)
            if (resolved != null) violation(value, resolved, catalog, allowsDynamicValues, pointer)?.let { return it }
        }
        if (schema.containsKey("const") && !A2UIJson.isEqual(schema["const"], value)) {
            return pointer to "must be ${A2UIJson.write(schema["const"])}"
        }
        A2UIJson.list(schema["enum"])?.let { values ->
            if (values.none { A2UIJson.isEqual(it, value) }) {
                return pointer to "must be one of ${A2UIJson.write(values)}"
            }
        }
        (schema["type"] as? String)?.let { type ->
            if (!hasType(value, type)) return pointer to "is not of type '$type'"
        }
        typeSpecificViolation(value, schema, catalog, allowsDynamicValues, pointer)?.let { return it }
        A2UIJson.list(schema["allOf"])?.forEach { part ->
            A2UIJson.map(part)?.let { violation(value, it, catalog, allowsDynamicValues, pointer) }?.let { return it }
        }
        A2UIJson.list(schema["anyOf"])?.mapNotNull(A2UIJson::map)?.let { parts ->
            if (parts.isNotEmpty() && parts.all { fails(value, it, catalog, allowsDynamicValues) }) {
                return pointer to "does not match any allowed schema"
            }
        }
        A2UIJson.list(schema["oneOf"])?.mapNotNull(A2UIJson::map)?.let { parts ->
            if (parts.isNotEmpty() && parts.all { fails(value, it, catalog, allowsDynamicValues) }) {
                return pointer to "does not match any allowed schema"
            }
        }
        return null
    }

    @Suppress("NestedBlockDepth")
    private fun typeSpecificViolation(
        value: Any?,
        schema: Map<String, Any?>,
        catalog: Catalog?,
        allowsDynamicValues: Boolean,
        pointer: String,
    ): Pair<String, String>? {
        if (A2UIJson.isNumber(value)) {
            val number = (value as Number).toDouble()
            A2UIJson.double(schema["minimum"])?.let { minimum ->
                if (number < minimum) return pointer to "must be at least ${A2UIJson.stringify(minimum)}"
            }
            A2UIJson.double(schema["maximum"])?.let { maximum ->
                if (number > maximum) return pointer to "must be at most ${A2UIJson.stringify(maximum)}"
            }
        }
        A2UIJson.list(value)?.let { list ->
            A2UIJson.int(schema["minItems"])?.let { minimum ->
                if (list.size < minimum) return pointer to "must have at least $minimum items"
            }
            A2UIJson.int(schema["maxItems"])?.let { maximum ->
                if (list.size > maximum) return pointer to "must have at most $maximum items"
            }
            A2UIJson.map(schema["items"])?.let { items ->
                list.forEachIndexed { index, item ->
                    violation(item, items, catalog, allowsDynamicValues, "$pointer/$index")?.let { return it }
                }
            }
        }
        A2UIJson.map(value)?.let { map ->
            A2UIJson.list(schema["required"])?.filterIsInstance<String>()?.forEach { key ->
                if (!map.containsKey(key)) return pointer to "'$key' is a required property"
            }
            val properties = A2UIJson.map(schema["properties"]).orEmpty()
            for ((key, item) in map) {
                val propertySchema = A2UIJson.map(properties[key])
                if (propertySchema != null) {
                    val itemPointer = "$pointer/${pointerEscape(key)}"
                    violation(item, propertySchema, catalog, allowsDynamicValues, itemPointer)?.let { return it }
                } else if (schema["additionalProperties"] == false || schema["unevaluatedProperties"] == false) {
                    return "$pointer/${pointerEscape(key)}" to
                        "Additional properties are not allowed ('$key' was unexpected)"
                }
            }
        }
        return null
    }

    // A common type checked by name: `null` when it isn't one, an empty message when it passes.
    private fun commonTypeViolation(
        reference: String,
        value: Any?,
        allowsDynamicValues: Boolean,
        pointer: String,
    ): Pair<String, String>? {
        if (!reference.contains("common_types.json") && !reference.startsWith("#/\$defs/")) return null
        val name = reference.substringAfterLast('/')
        val dynamic = isDynamic(value)
        val ok = when (name) {
            "ComponentId", "Child", "CallId" -> value is String
            "DataBinding" -> A2UIJson.map(value)?.get(A2UIProtocol.PATH_KEY) is String
            "FunctionCall" -> A2UIJson.map(value)?.get(A2UIProtocol.CALL_KEY) is String
            "DynamicString" -> value is String || dynamic
            "DynamicNumber" -> A2UIJson.isNumber(value) || dynamic
            "DynamicBoolean" -> value is Boolean || dynamic
            "DynamicStringList" -> A2UIJson.list(value)?.all { it is String } == true || dynamic
            "DynamicValue" -> value != null
            "ChildList" -> ChildList.of(value) != ChildList.None
            "Action", "CheckRule", "AccessibilityAttributes" -> A2UIJson.map(value) != null
            else -> return null
        }
        return if (ok || allowsDynamicValues && dynamic) pointer to "" else pointer to "is not a valid $name"
    }

    private fun resolve(reference: String, catalog: Catalog?): Map<String, Any?>? {
        CommonTypes.resolve(reference)?.let { return it }
        if (reference.startsWith("#/\$defs/")) {
            return A2UIJson.map(catalog?.definitions?.get(reference.substringAfterLast('/')))
        }
        return null
    }

    private fun fails(
        value: Any?,
        schema: Map<String, Any?>,
        catalog: Catalog?,
        allowsDynamicValues: Boolean,
    ): Boolean =
        violation(value, schema, catalog, allowsDynamicValues) != null

    private fun isDynamic(value: Any?): Boolean {
        val map = A2UIJson.map(value) ?: return false
        return map.containsKey(A2UIProtocol.PATH_KEY) || map.containsKey(A2UIProtocol.CALL_KEY)
    }

    private fun hasType(value: Any?, type: String): Boolean = when (type) {
        "string" -> value is String
        "number" -> A2UIJson.isNumber(value)
        "integer" -> A2UIJson.isNumber(value) && (value as Number).toDouble().let { it == kotlin.math.floor(it) }
        "boolean" -> value is Boolean
        "array" -> value is List<*>
        "object" -> value is Map<*, *>
        "null" -> value == null
        else -> true
    }
}
