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

import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.arrayValue
import io.getstream.chat.android.ai.openui.lang.get
import io.getstream.chat.android.ai.openui.lang.objectValue
import io.getstream.chat.android.ai.openui.lang.stringValue

/*
 * Components an app describes once for every platform and for its agent: a portable definition
 * with a snake_case `name`, a `description`, `parameters` as a JSON Schema object that keeps their
 * order in `x-order`, and the `events` the component reports.
 *
 * { "name": "place_card", "description": "A photo and a short summary of a place.",
 *   "parameters": { "type": "object", "properties": { "title": { "type": "string" } },
 *                   "required": ["title"], "x-order": ["title"] },
 *   "events": [{ "name": "ask_about", "description": "The person asks about the place." }] }
 */

/**
 * The OpenUI name of a portable component: OpenUI Lang calls components by capitalized names, so
 * `place_card` is `PlaceCard`.
 */
internal fun ComponentSchema.Companion.typeName(portableName: String): String = portableName
    .split('_', '-', ' ')
    .filter { it.isNotEmpty() }
    .joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }

/**
 * The schema of a portable component definition. Its parameters' descriptions and its events join
 * its description, which is all of a component the model reads besides its signature.
 *
 * @throws OpenUIPortableComponentException when the definition has no name.
 */
internal fun ComponentSchema.Companion.fromPortableDefinition(definition: OpenUIValue): ComponentSchema {
    val name = definition["name"].stringValue
    if (name.isNullOrEmpty()) throw OpenUIPortableComponentException("A component definition needs a name")
    val parameters = definition["parameters"]
    val notes = buildList {
        definition["description"].stringValue?.takeIf { it.isNotEmpty() }?.let { add(sentence(it)) }
        for ((field, schema) in orderedProperties(parameters)) {
            schema["description"].stringValue?.takeIf { it.isNotEmpty() }?.let { add(sentence("$field: $it")) }
        }
        for (event in definition["events"].arrayValue.orEmpty()) {
            event["description"].stringValue?.takeIf { it.isNotEmpty() }?.let { add(sentence(it)) }
        }
    }
    return ComponentSchema(typeName(name), notes.joinToString(" "), fields(parameters))
}

/**
 * The prop type closest to a JSON Schema type. `type: ["string", "null"]` reads as a string,
 * `anyOf`/`oneOf` as a union, `const` as a literal, and an object with only
 * `additionalProperties` as a record. Anything else it can't describe accepts any value.
 */
internal fun PropType.Companion.fromJsonSchema(schema: OpenUIValue?): PropType {
    val types = schema["type"].arrayValue
    val type = if (types != null) {
        types.mapNotNull { it.stringValue }.filter { it != "null" }.singleOrNull()
    } else {
        schema["type"].stringValue
    }
    val values = schema["enum"].arrayValue
    if (!values.isNullOrEmpty()) {
        val strings = values.mapNotNull { it.stringValue }
        // An enum of other values keeps the type they share, so none of them is rejected.
        if (strings.size == values.size) return enumeration(strings)
    }
    schema["const"].stringValue?.let { return literal(it) }
    val options = (schema["anyOf"] ?: schema["oneOf"]).arrayValue
    if (!options.isNullOrEmpty()) {
        val optionTypes = options.filter { it["type"].stringValue != "null" }.map(::fromJsonSchema)
        return when {
            optionTypes.size == 1 -> optionTypes[0]
            optionTypes.isEmpty() || any in optionTypes -> any
            else -> union(optionTypes)
        }
    }
    return when (type) {
        "string" -> string
        "number", "integer" -> number
        "boolean" -> boolean
        "array" -> array(fromJsonSchema(schema["items"]))
        "object" -> objectType(schema)
        else -> any
    }
}

private fun objectType(schema: OpenUIValue?): PropType {
    if (!schema["properties"].objectValue.isNullOrEmpty()) return PropType.objectOf(fields(schema))
    val values = schema["additionalProperties"]
    return if (values is OpenUIValue.Obj) PropType.record(PropType.fromJsonSchema(values)) else PropType.any
}

/** The fields of a JSON Schema object in positional order, with their `default`s. */
internal fun fields(schema: OpenUIValue?): List<PropField> {
    val required = schema["required"].arrayValue.orEmpty().mapNotNull { it.stringValue }.toSet()
    return orderedProperties(schema).map { (name, property) ->
        val defaultValue = property["default"]
        PropField(
            name = name,
            type = PropType.fromJsonSchema(property),
            isOptional = name !in required || defaultValue != null,
            defaultValue = defaultValue,
        )
    }
}

// A component's line in the prompt stays on one line, so a description does too.
private fun sentence(text: String): String {
    val trimmed = text.split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(" ")
    return if (trimmed.isEmpty() || trimmed.last() in ".!?") trimmed else "$trimmed."
}

// A JSON Schema object's properties in the order of its `x-order`, then as written.
private fun orderedProperties(schema: OpenUIValue?): List<Pair<String, OpenUIValue>> {
    val properties = schema["properties"].objectValue ?: return emptyList()
    val ordered = schema["x-order"].arrayValue.orEmpty()
        .mapNotNull { it.stringValue }
        .filter { it in properties }
        .distinct()
    val rest = properties.keys.filter { it !in ordered }
    return (ordered + rest).map { it to properties.getValue(it) }
}

/**
 * The type with [names] added to every union of content components in it, such as the children
 * of a chat `Card`. A union holds content when it includes `TextContent`.
 */
internal fun PropType.addingContentComponents(names: List<String>): PropType {
    val kind = kind
    return when {
        kind is PropType.Kind.Union && PropType.component("TextContent") in kind.options ->
            PropType.union(kind.options + names.map(PropType::component).filter { it !in kind.options })
        kind is PropType.Kind.ArrayOf -> PropType.array(kind.items.addingContentComponents(names), kind.minItems)
        else -> this
    }
}
