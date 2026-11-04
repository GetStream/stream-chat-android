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

package io.getstream.chat.android.ai.compose.generativeui

import io.getstream.chat.android.ai.compose.parts.stringKeyed

/**
 * A parameter of a component: what the agent fills in when it places the component.
 *
 * @param name The name the agent fills it in by, such as `title`.
 * @param type What it holds.
 * @param description What it is, for the agent.
 * @param isRequired Whether the agent must fill it in.
 */
public data class AIUIParameter(
    val name: String,
    val type: AIUIParameterType,
    val description: String? = null,
    val isRequired: Boolean = true,
) {
    public companion object {
        /**
         * A parameter the agent must fill in.
         *
         * @param name The parameter's name.
         * @param type The type of its value.
         * @param description What it is, for the agent.
         */
        public fun required(name: String, type: AIUIParameterType, description: String? = null): AIUIParameter =
            AIUIParameter(name, type, description, isRequired = true)

        /**
         * A parameter the agent may leave out.
         *
         * @param name The parameter's name.
         * @param type The type of its value.
         * @param description What it is, for the agent.
         */
        public fun optional(name: String, type: AIUIParameterType, description: String? = null): AIUIParameter =
            AIUIParameter(name, type, description, isRequired = false)

        /** The key that keeps the order of an object's fields, which JSON objects don't have. */
        internal const val ORDER_KEY = "x-order"

        /**
         * [parameters] as a JSON Schema object, in their order. Of parameters with the same name,
         * the first one counts.
         */
        internal fun objectSchema(parameters: List<AIUIParameter>): Map<String, Any?> {
            val distinct = parameters.distinctBy { it.name }
            return mapOf(
                "type" to "object",
                "properties" to distinct.associate { parameter ->
                    val description = listOfNotNull(parameter.description?.let { "description" to it })
                    parameter.name to parameter.type.schema + description
                },
                "required" to distinct.filter { it.isRequired }.map { it.name },
                ORDER_KEY to distinct.map { it.name },
            )
        }

        /** The fields of a JSON Schema object, in the order it keeps, then by name. */
        internal fun fromObjectSchema(schema: Map<*, *>?): List<AIUIParameter> {
            val properties = (schema?.get("properties") as? Map<*, *>).orEmpty()
            val required = (schema?.get("required") as? List<*>).orEmpty().filterIsInstance<String>().toSet()
            val ordered = (schema?.get(ORDER_KEY) as? List<*>).orEmpty().filterIsInstance<String>()
                .filter { properties[it] != null }
                .distinct()
            val names = ordered + properties.keys.filterIsInstance<String>().filterNot { it in ordered }.sorted()
            return names.mapNotNull { name ->
                val field = properties[name] as? Map<*, *> ?: return@mapNotNull null
                val fieldSchema = field.stringKeyed()
                AIUIParameter(
                    name = name,
                    type = AIUIParameterType(fieldSchema - "description"),
                    description = fieldSchema["description"] as? String,
                    isRequired = name in required,
                )
            }
        }
    }
}

/**
 * The type of a component's parameter, as JSON Schema. It is an open set: use the members of the
 * companion, or any JSON Schema the formats you render can read.
 *
 * @param schema The type as a JSON Schema object, in plain Kotlin values.
 */
public data class AIUIParameterType(val schema: Map<String, Any?>) {
    public companion object {
        /** Text. */
        public val String: AIUIParameterType = AIUIParameterType(mapOf("type" to "string"))

        /** A number. */
        public val Number: AIUIParameterType = AIUIParameterType(mapOf("type" to "number"))

        /** A whole number. */
        public val Integer: AIUIParameterType = AIUIParameterType(mapOf("type" to "integer"))

        /** True or false. */
        public val Boolean: AIUIParameterType = AIUIParameterType(mapOf("type" to "boolean"))

        /** A day, such as `2026-11-14`. */
        public val Date: AIUIParameterType = AIUIParameterType(mapOf("type" to "string", "format" to "date"))

        /** A moment, such as `2026-11-14T09:30:00Z`. */
        public val DateTime: AIUIParameterType = AIUIParameterType(mapOf("type" to "string", "format" to "date-time"))

        /** A link, such as an image's. */
        public val Url: AIUIParameterType = AIUIParameterType(mapOf("type" to "string", "format" to "uri"))

        /**
         * One of a fixed set of strings.
         *
         * @param values The strings it can be.
         */
        public fun enumeration(values: List<String>): AIUIParameterType =
            AIUIParameterType(mapOf("type" to "string", "enum" to values))

        /**
         * A list of values of one type.
         *
         * @param items The type of the list's values.
         */
        public fun array(items: AIUIParameterType): AIUIParameterType =
            AIUIParameterType(mapOf("type" to "array", "items" to items.schema))

        /**
         * An object with named fields, in order.
         *
         * @param fields The object's fields, in order.
         */
        public fun obj(fields: List<AIUIParameter>): AIUIParameterType =
            AIUIParameterType(AIUIParameter.objectSchema(fields))
    }
}
