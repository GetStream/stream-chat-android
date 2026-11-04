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

package io.getstream.chat.android.ai.compose.parts

/**
 * The name, description, argument schema and read-only hint of a client tool, as the agent sees
 * them.
 *
 * It reads and writes the same JSON as a Model Context Protocol tool (`name`, `description`,
 * `inputSchema` and the `readOnlyHint` of its `annotations`), so a renderer can put it into the
 * model's instructions as it is, and a tool described for MCP converts with [fromJson]:
 *
 * ```json
 * {
 *   "name": "calendar",
 *   "description": "The person's events on a day.",
 *   "inputSchema": { "type": "object", "properties": { "day": { "type": "string" } }, "required": ["day"] },
 *   "annotations": { "readOnlyHint": true }
 * }
 * ```
 *
 * @property name The unique name of the tool.
 * @property description What the tool does, for the agent, or empty when it says nothing.
 * @property inputSchema The JSON Schema of the tool's arguments, as plain Kotlin values, or `null`
 * when it describes none.
 * @property isReadOnly Whether the tool only reads and changes nothing.
 */
public data class AIClientToolDefinition(
    val name: String,
    val description: String = "",
    val inputSchema: Map<String, Any?>? = null,
    val isReadOnly: Boolean = false,
) {
    /**
     * The definition as its portable JSON, in plain Kotlin values. An empty description and a
     * missing schema are left out.
     */
    public fun toJson(): Map<String, Any?> = buildMap {
        put("name", name)
        if (description.isNotEmpty()) put("description", description)
        inputSchema?.let { put("inputSchema", it) }
        put("annotations", mapOf("readOnlyHint" to isReadOnly))
    }

    /** The definition as its portable JSON text, with sorted keys. */
    public fun toJsonString(): String = Json.write(toJson())

    public companion object {
        /**
         * Reads a definition from its portable JSON, in plain Kotlin values, or returns `null`
         * when it has no name.
         *
         * @param json The definition, as plain Kotlin values.
         */
        public fun fromJson(json: Map<String, Any?>): AIClientToolDefinition? {
            val name = (json["name"] as? String)?.takeIf { it.isNotEmpty() } ?: return null
            val annotations = json["annotations"] as? Map<*, *>
            return AIClientToolDefinition(
                name = name,
                description = json["description"] as? String ?: "",
                inputSchema = (json["inputSchema"] as? Map<*, *>)?.stringKeyed(),
                isReadOnly = annotations?.get("readOnlyHint") == true,
            )
        }

        /**
         * Reads a definition from its portable JSON text, or returns `null` when the text is not
         * a JSON object with a name.
         *
         * @param text The definition, as JSON text.
         */
        public fun fromJsonString(text: String): AIClientToolDefinition? =
            (Json.parseOrNull(text) as? Map<*, *>)?.let { map -> fromJson(map.stringKeyed()) }
    }
}
