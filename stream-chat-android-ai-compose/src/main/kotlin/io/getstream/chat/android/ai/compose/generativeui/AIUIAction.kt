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
 * What a person did in a reply's generated UI that the agent should hear about, such as tapping a
 * place or submitting a form.
 *
 * Send it as their next message: [label] is its text, what people read in the conversation, and
 * [customData] holds the action for the agent.
 *
 * ```
 * channel.sendMessage(Message(text = action.label, extraData = action.customData))
 * ```
 *
 * @param messageId The reply whose UI it came from.
 * @param format The format of that UI.
 * @param name What happened, such as an event of an app's component (`ask_about`) or OpenUI's
 * `continue_conversation`.
 * @param label What the person did, in their words, such as "Tell me more about St. Naum".
 * @param payload The details in the format's own shape, such as what was chosen and a form's
 * values, as plain Kotlin values.
 */
public data class AIUIAction(
    val messageId: String,
    val format: AIUIFormat,
    val name: String,
    val label: String,
    val payload: Map<String, Any?> = emptyMap(),
) {
    /** The action as the JSON object an agent's backend reads. */
    val json: Map<String, Any?>
        get() = mapOf(
            "v" to 1,
            "message_id" to messageId,
            "format" to format.rawValue,
            "name" to name,
            "payload" to payload,
        )

    /** The action as the custom data of the person's message, under [CUSTOM_DATA_KEY]. */
    val customData: Map<String, Any> get() = mapOf(CUSTOM_DATA_KEY to json)

    public companion object {
        /** The custom field of the person's message that holds the action. */
        public const val CUSTOM_DATA_KEY: String = "ai_ui_action"
    }
}
