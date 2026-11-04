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

import io.getstream.chat.android.ai.compose.parts.Json
import io.getstream.chat.android.ai.compose.parts.stringKeyed

/**
 * A component's description, the same on every platform and for the agent: its name, what it
 * shows, the parameters the agent fills in and the events it reports.
 *
 * It reads and writes JSON, so one description can be shared by every platform an app runs on
 * and by the agent's backend. Its parameters are a JSON Schema object, as a tool's are,
 * which keeps their order in `x-order`:
 *
 * ```json
 * {
 *   "name": "place_card",
 *   "description": "A photo and a short summary of a place.",
 *   "parameters": {
 *     "type": "object",
 *     "properties": { "title": { "type": "string", "description": "The place's Wikipedia title." } },
 *     "required": ["title"],
 *     "x-order": ["title"]
 *   },
 *   "events": [{ "name": "ask_about", "description": "The person asks about the place." }]
 * }
 * ```
 *
 * @param name The name the agent places it by, such as `place_card`.
 * @param description What it shows and when to use it, for the agent.
 * @param parameters What the agent fills in, in order: a format such as OpenUI Lang passes
 * arguments by position.
 * @param events What it reports when the person uses it.
 */
public data class AIUIComponentDefinition(
    val name: String,
    val description: String,
    val parameters: List<AIUIParameter> = emptyList(),
    val events: List<Event> = emptyList(),
) {
    /**
     * Something a component reports when the person uses it, such as tapping a place.
     *
     * @param name The name it is reported by, such as `ask_about`.
     * @param description When it happens, for the agent.
     * @param parameters What it carries, in order.
     */
    public data class Event(
        val name: String,
        val description: String,
        val parameters: List<AIUIParameter> = emptyList(),
    )

    /** The parameters as a JSON Schema object, the way a tool's are described. */
    val parametersSchema: Map<String, Any?> get() = AIUIParameter.objectSchema(parameters)

    /** The definition as its portable JSON, in plain Kotlin values. */
    public fun toJson(): Map<String, Any?> = mapOf(
        "name" to name,
        "description" to description,
        "parameters" to parametersSchema,
        "events" to events.map { event ->
            buildMap {
                put("name", event.name)
                put("description", event.description)
                if (event.parameters.isNotEmpty()) put("parameters", AIUIParameter.objectSchema(event.parameters))
            }
        },
    )

    /** The definition as its portable JSON text, with sorted keys. */
    public fun toJsonString(): String = Json.write(toJson())

    public companion object {
        /**
         * Reads a definition from its portable JSON, in plain Kotlin values, or returns `null`
         * when it has no name.
         *
         * @param json The definition, as plain Kotlin values.
         */
        public fun fromJson(json: Map<String, Any?>): AIUIComponentDefinition? {
            val name = (json["name"] as? String)?.takeIf { it.isNotEmpty() } ?: return null
            val events = (json["events"] as? List<*>).orEmpty().mapNotNull { item ->
                val event = item as? Map<*, *> ?: return@mapNotNull null
                val eventName = (event["name"] as? String)?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                Event(
                    name = eventName,
                    description = event["description"] as? String ?: "",
                    parameters = AIUIParameter.fromObjectSchema(event["parameters"] as? Map<*, *>),
                )
            }
            return AIUIComponentDefinition(
                name = name,
                description = json["description"] as? String ?: "",
                parameters = AIUIParameter.fromObjectSchema(json["parameters"] as? Map<*, *>),
                events = events,
            )
        }

        /**
         * Reads a definition from its portable JSON text, or returns `null` when the text is not
         * a JSON object with a name.
         *
         * @param text The definition, as JSON text.
         */
        public fun fromJsonString(text: String): AIUIComponentDefinition? =
            (Json.parseOrNull(text) as? Map<*, *>)?.let { map -> fromJson(map.stringKeyed()) }
    }
}
