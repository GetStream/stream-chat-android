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

/**
 * The UI a reply shows in place of its text, which the agent wrote in a format such as OpenUI.
 *
 * The agent's backend writes it in the reply's `ai_ui` custom field. The reply's text stays
 * readable on its own, for wherever the UI can't show, such as a push notification or an app
 * without a renderer for the format:
 *
 * ```json
 * "ai_ui": { "v": 1, "format": "openui", "status": "streaming", "content": "root = Card([title])" }
 * ```
 *
 * ```
 * override val uiDocument: AIUIDocument? get() = AIUIDocument.from(message.extraData)
 * ```
 *
 * @param format The format it is written in.
 * @param version The format's version, when the agent names one.
 * @param isStreaming Whether the agent is still writing it.
 * @param content The document in its format's own shape: a program's text, or JSON as plain
 * Kotlin values (maps, lists, strings, numbers, booleans and `null`).
 */
public data class AIUIDocument(
    val format: AIUIFormat,
    val version: String? = null,
    val isStreaming: Boolean = false,
    val content: Any?,
) {
    /** The content when it is text, such as an OpenUI program. */
    val text: String? get() = content as? String

    public companion object {
        /** The custom field of a message that holds its document. */
        public const val CUSTOM_DATA_KEY: String = "ai_ui"

        /** The newest version of the `ai_ui` field this SDK reads. */
        public const val SUPPORTED_VERSION: Int = 1

        /**
         * Reads the document a message carries in its custom data, or returns `null` when it
         * carries none this SDK can read.
         *
         * @param customData The message's custom data, such as a Stream Chat message's
         * `extraData`.
         */
        public fun from(customData: Map<String, Any?>): AIUIDocument? {
            val fields = customData[CUSTOM_DATA_KEY] as? Map<*, *> ?: return null
            val format = (fields["format"] as? String)?.takeIf { it.isNotEmpty() } ?: return null
            val content = fields["content"] ?: return null
            // A newer field may mean something else.
            val envelope = fields["v"]
            if (envelope is Number && envelope.toDouble() > SUPPORTED_VERSION) return null
            return AIUIDocument(
                format = AIUIFormat(format),
                version = (fields["version"] as? String)?.takeIf { it.isNotEmpty() },
                isStreaming = fields["status"] == "streaming",
                content = content,
            )
        }
    }
}
