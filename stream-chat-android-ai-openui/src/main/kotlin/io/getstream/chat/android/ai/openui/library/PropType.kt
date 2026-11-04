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

package io.getstream.chat.android.ai.openui.library

import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.openUIValueOf

/**
 * The type of a component prop: the Kotlin counterpart of the Zod schemas OpenUI component
 * libraries are defined with.
 *
 * A prop type produces both the JSON Schema the parser uses (positional mapping and validation)
 * and the type annotation shown to the model in the generated system prompt. Create one with the
 * factories in the companion, such as [PropType.string] or [PropType.array].
 */
internal class PropType private constructor(internal val kind: Kind) {

    internal sealed interface Kind {
        data object Str : Kind
        data object Num : Kind
        data object Bool : Kind
        data object Anything : Kind
        data object Action : Kind
        data class Enumeration(val values: List<String>) : Kind
        data class Literal(val value: String) : Kind
        data class ArrayOf(val items: PropType, val minItems: Int?) : Kind
        data class ObjectOf(val fields: List<PropField>) : Kind
        data class RecordOf(val value: PropType) : Kind
        data class Component(val name: String) : Kind
        data class Union(val options: List<PropType>) : Kind
        data class Actions(val names: List<String>) : Kind
    }

    /** The JSON Schema for this type (matches Zod's `toJSONSchema` output). */
    val jsonSchema: OpenUIValue
        get() = when (val kind = kind) {
            Kind.Str -> obj("type" to "string")
            Kind.Num -> obj("type" to "number")
            Kind.Bool -> obj("type" to "boolean")
            Kind.Anything -> OpenUIValue.Obj(OpenUIObject.Empty)
            Kind.Action -> obj("\$ref" to "#/\$defs/$ACTION_EXPRESSION")
            is Kind.Enumeration -> obj("type" to "string", "enum" to kind.values)
            is Kind.Literal -> obj("type" to "string", "const" to kind.value)
            is Kind.ArrayOf -> OpenUIValue.Obj(
                OpenUIObject.build {
                    kind.minItems?.let { this["minItems"] = OpenUIValue.of(it) }
                    this["type"] = OpenUIValue.Str("array")
                    this["items"] = kind.items.jsonSchema
                },
            )
            is Kind.ObjectOf -> PropField.objectSchema(kind.fields, description = null)
            is Kind.RecordOf -> obj(
                "type" to "object",
                "propertyNames" to obj("type" to "string"),
                "additionalProperties" to kind.value.jsonSchema,
            )
            is Kind.Component -> obj("\$ref" to "#/\$defs/${kind.name}")
            is Kind.Union -> obj("anyOf" to kind.options.map { it.jsonSchema })
            is Kind.Actions -> obj("anyOf" to kind.names.map { obj("\$ref" to "#/\$defs/$it") })
        }

    /** The type annotation used in prompt signatures (e.g. `string`, `Series[]`, `(A | B)[]`). */
    val signature: String
        get() = when (val kind = kind) {
            Kind.Str -> "string"
            Kind.Num -> "number"
            Kind.Bool -> "boolean"
            Kind.Anything -> "any"
            Kind.Action -> ACTION_EXPRESSION
            is Kind.Enumeration -> kind.values.joinToString(" | ") { "\"$it\"" }
            is Kind.Literal -> "\"${kind.value}\""
            is Kind.ArrayOf ->
                if (kind.items.kind is Kind.Union) "(${kind.items.signature})[]" else "${kind.items.signature}[]"
            is Kind.ObjectOf -> kind.fields.joinToString(", ", "{", "}") { it.signature }
            is Kind.RecordOf -> "Record<string, ${kind.value.signature}>"
            is Kind.Component -> kind.name
            is Kind.Union -> kind.options.joinToString(" | ") { it.signature }
            is Kind.Actions -> kind.names.joinToString(" | ") { "@$it" }
        }

    /** A `$defs` entry a type refers to: a component, or the JSON shape of an action. */
    internal sealed interface Definition {
        data class Component(val name: String) : Definition
        data class Action(val name: String) : Definition
    }

    /** The `$defs` entries this type refers to, in declaration order. */
    internal val definitionReferences: List<Definition>
        get() = when (val kind = kind) {
            is Kind.Component -> listOf(Definition.Component(kind.name))
            is Kind.ArrayOf -> kind.items.definitionReferences
            is Kind.Union -> kind.options.flatMap { it.definitionReferences }
            is Kind.ObjectOf -> kind.fields.flatMap { it.type.definitionReferences }
            is Kind.RecordOf -> kind.value.definitionReferences
            Kind.Action -> listOf(Definition.Action(ACTION_EXPRESSION))
            is Kind.Actions -> kind.names.map(Definition::Action)
            Kind.Str, Kind.Num, Kind.Bool, Kind.Anything, is Kind.Enumeration, is Kind.Literal -> emptyList()
        }

    /** Component names referenced by this type, in declaration order. */
    internal val componentReferences: List<String>
        get() = definitionReferences.mapNotNull { (it as? Definition.Component)?.name }

    /**
     * The type without [names] among the options of its unions. A union keeps them when they are
     * all it has.
     */
    internal fun removingComponents(names: Set<String>): PropType = when (val kind = kind) {
        is Kind.Union -> {
            val kept = kind.options.filter { option -> (option.kind as? Kind.Component)?.name !in names }
            if (kept.isEmpty()) this else union(kept.map { it.removingComponents(names) })
        }
        is Kind.ArrayOf -> array(kind.items.removingComponents(names), kind.minItems)
        is Kind.RecordOf -> record(kind.value.removingComponents(names))
        is Kind.ObjectOf -> objectOf(kind.fields.map { it.copy(type = it.type.removingComponents(names)) })
        else -> this
    }

    /**
     * The components this type can't do without when [names] are removed: a slot's only type, or
     * the types of a union that would be left with none.
     */
    internal fun neededComponents(names: Set<String>): List<String> = when (val kind = kind) {
        is Kind.Component -> listOf(kind.name)
        is Kind.Union -> {
            val components = kind.options.mapNotNull { (it.kind as? Kind.Component)?.name }
            val nested = kind.options.filter { it.kind !is Kind.Component }.flatMap { it.neededComponents(names) }
            val keepsOne = kind.options.size > components.size || components.any { it !in names }
            (if (keepsOne) emptyList() else components) + nested
        }
        is Kind.ArrayOf -> kind.items.neededComponents(names)
        is Kind.RecordOf -> kind.value.neededComponents(names)
        is Kind.ObjectOf -> kind.fields.flatMap { it.type.neededComponents(names) }
        else -> emptyList()
    }

    override fun equals(other: Any?): Boolean = other is PropType && kind == other.kind
    override fun hashCode(): Int = kind.hashCode()
    override fun toString(): String = signature

    /** Factories for prop types. */
    companion object {
        internal const val ACTION_EXPRESSION: String = "ActionExpression"

        /** A string. */
        val string: PropType = PropType(Kind.Str)

        /** A number. */
        val number: PropType = PropType(Kind.Num)

        /** A boolean. */
        val boolean: PropType = PropType(Kind.Bool)

        /** Anything (`{}` in JSON Schema). */
        val any: PropType = PropType(Kind.Anything)

        /** An action: one `@step`, or `Action([...])` with several (`ActionExpression` in signatures). */
        val action: PropType = PropType(Kind.Action)

        /** One of a fixed set of strings: `"a" | "b"`. */
        fun enumeration(values: List<String>): PropType = PropType(Kind.Enumeration(values))

        /** A single string literal: `"open_url"`. */
        fun literal(value: String): PropType = PropType(Kind.Literal(value))

        /** An array; [minItems] adds a JSON Schema length constraint. */
        fun array(items: PropType, minItems: Int? = null): PropType = PropType(Kind.ArrayOf(items, minItems))

        /** A nested object with named fields. */
        fun objectOf(fields: List<PropField>): PropType = PropType(Kind.ObjectOf(fields))

        /** `Record<string, Value>`. */
        fun record(value: PropType): PropType = PropType(Kind.RecordOf(value))

        /** A reference to another component in the library (its name in signatures). */
        fun component(name: String): PropType = PropType(Kind.Component(name))

        /** Any of several types: `A | B`. */
        fun union(options: List<PropType>): PropType = PropType(Kind.Union(options))

        /** A union of component references. */
        fun components(names: List<String>): PropType = union(names.map(::component))

        /**
         * An action slot that takes only the listed steps, such as `listOf("CopyToClipboard", "OpenUrl")`
         * (`@CopyToClipboard | @OpenUrl` in signatures). Built-in steps with a JSON shape are
         * `OpenUrl` and `ToAssistant`; any other name is one of the library's custom actions.
         */
        fun actions(names: List<String>): PropType = PropType(Kind.Actions(names))

        /** The JSON shape of `@OpenUrl` (`$defs.OpenUrl`). */
        internal val openUrlSchema: OpenUIValue
            get() = obj(
                "type" to "object",
                "properties" to obj(
                    "type" to obj("type" to "string", "const" to "open_url"),
                    "url" to obj("type" to "string"),
                ),
                "required" to listOf("type", "url"),
                "additionalProperties" to false,
            )

        /** The JSON shape of `@ToAssistant` (`$defs.ToAssistant`). */
        internal val toAssistantSchema: OpenUIValue
            get() = obj(
                "type" to "object",
                "properties" to obj(
                    "type" to obj("type" to "string", "const" to "continue_conversation"),
                    "context" to obj("type" to "string"),
                ),
                "required" to listOf("type"),
                "additionalProperties" to false,
            )

        /** The legacy action object contract (the JSON Schema side of `ActionExpression`). */
        internal val actionSchema: OpenUIValue
            get() = obj(
                "anyOf" to listOf(
                    openUrlSchema,
                    toAssistantSchema,
                    obj(
                        "type" to "object",
                        "properties" to obj(
                            "type" to obj("type" to "string"),
                            "params" to obj(
                                "type" to "object",
                                "propertyNames" to obj("type" to "string"),
                                "additionalProperties" to obj(),
                            ),
                        ),
                        "required" to listOf("type"),
                        "additionalProperties" to false,
                    ),
                ),
            )
    }
}

/** An ordered object value from Kotlin values. */
internal fun obj(vararg pairs: Pair<String, Any?>): OpenUIValue =
    OpenUIValue.Obj(OpenUIObject.build { for ((key, value) in pairs) this[key] = openUIValueOf(value) })

/** A named prop (or nested object field) with its type and modifiers. */
internal data class PropField(
    val name: String,
    val type: PropType,
    /** Optional props may be omitted from the end of a positional call. */
    val isOptional: Boolean = false,
    /** Schema default (used when the value is missing, `null` or invalid). */
    val defaultValue: OpenUIValue? = null,
    /** Reactive props accept a `$variable` for two-way binding (`$binding<T>` in signatures). */
    val isReactive: Boolean = false,
) {
    /** Present in the JSON Schema `required` list (required props and props with defaults). */
    internal val isSchemaRequired: Boolean get() = !isOptional || defaultValue != null

    /** The JSON Schema of this prop, with its default. */
    internal val jsonSchema: OpenUIValue
        get() {
            val default = defaultValue ?: return type.jsonSchema
            return OpenUIValue.Obj(
                OpenUIObject.build {
                    this["default"] = default
                    (type.jsonSchema as? OpenUIValue.Obj)?.fields?.forEach { (key, value) -> this[key] = value }
                },
            )
        }

    /** `name: type`, `name?: type`, or `name?: $binding<type>`. */
    val signature: String
        get() {
            val base = type.signature
            val annotation = if (isReactive) "\$binding<$base>" else base
            return if (isOptional) "$name?: $annotation" else "$name: $annotation"
        }

    /** Factories for props. */
    companion object {
        /** A required prop. */
        fun required(name: String, type: PropType): PropField = PropField(name, type)

        /** An optional prop. */
        fun optional(name: String, type: PropType): PropField = PropField(name, type, isOptional = true)

        /** A prop with a default (optional for the model, always present after parsing). */
        fun defaulted(name: String, type: PropType, defaultValue: Any?): PropField =
            PropField(name, type, isOptional = true, defaultValue = openUIValueOf(defaultValue))

        /** An optional prop that accepts a `$variable` binding (port of `reactive`). */
        fun reactive(name: String, type: PropType): PropField =
            PropField(name, type, isOptional = true, isReactive = true)

        internal fun objectSchema(fields: List<PropField>, description: String?): OpenUIValue = OpenUIValue.Obj(
            OpenUIObject.build {
                this["type"] = OpenUIValue.Str("object")
                this["properties"] =
                    OpenUIValue.Obj(OpenUIObject.build { fields.forEach { this[it.name] = it.jsonSchema } })
                val required = fields.filter { it.isSchemaRequired }.map { OpenUIValue.Str(it.name) }
                if (required.isNotEmpty()) this["required"] = OpenUIValue.Arr(required)
                this["additionalProperties"] = OpenUIValue.False
                if (!description.isNullOrEmpty()) this["description"] = OpenUIValue.Str(description)
            },
        )
    }
}

/** A portable component definition OpenUI can't read. */
internal class OpenUIPortableComponentException(message: String) : IllegalArgumentException(message)

/** The schema of a component: its name, description and ordered (positional) props. */
internal data class ComponentSchema(val name: String, val description: String, val props: List<PropField>) {
    /** Prompt signature: `Name(prop: type, optional?: type, ...)`. */
    val signature: String get() = props.joinToString(", ", "$name(", ")") { it.signature }

    /** The component's JSON Schema definition. */
    val jsonSchema: OpenUIValue get() = PropField.objectSchema(props, description)

    /** Names of reactive props. */
    val reactiveProps: Set<String> get() = props.filter { it.isReactive }.mapTo(LinkedHashSet()) { it.name }

    /** A copy with the prop named [name] transformed. */
    fun updatingProp(name: String, update: (PropField) -> PropField): ComponentSchema =
        copy(props = props.map { if (it.name == name) update(it) else it })

    /** Factories for schemas, such as [fromPortableDefinition]. */
    companion object
}
