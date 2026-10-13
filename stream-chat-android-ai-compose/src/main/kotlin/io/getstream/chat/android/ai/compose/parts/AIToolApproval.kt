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
 * What a tool call asks the person it waits for before it runs, such as "Share your location?",
 * and how they answered.
 *
 * The agent's backend writes it on the call's `ai_tool_call` step, which waits with status
 * `awaiting_approval`, and holds the call until the person answers. Allowed, the call goes on (a
 * client tool then awaits the device); declined, it is cancelled and never runs.
 *
 * @param title The question, such as "Share your location?".
 * @param message What allowing it shares or does, such as "Only your city is shared."
 * @param reason The agent's own words for why it wants the call, such as "to check the local
 * weather".
 * @param allowTitle The label of the button that allows the call, or `null` when the agent sent
 * none. The default card then shows "Allow" in the app's language.
 * @param declineTitle The label of the button that declines it, or `null` when the agent sent none.
 * The card then shows "Don't allow" in the app's language.
 * @param decision How the person answered, once they did.
 */
public data class AIToolApproval(
    val title: String,
    val message: String? = null,
    val reason: String? = null,
    val allowTitle: String? = null,
    val declineTitle: String? = null,
    val decision: Decision? = null,
) {

    /**
     * How the person answered. An open set: compare against the decisions you know.
     *
     * @param rawValue The decision as the agent's backend wrote it.
     */
    @JvmInline
    public value class Decision(public val rawValue: String) {
        override fun toString(): String = rawValue

        public companion object {
            /** The person allowed the call. */
            public val Allowed: Decision = Decision("allowed")

            /** The person declined the call, so it never ran. */
            public val Declined: Decision = Decision("declined")
        }
    }

    internal companion object {
        /** Reads the step's `approval`. A question with no title asks nothing. */
        fun from(fields: Fields): AIToolApproval? {
            val title = fields.string("title") ?: return null
            return AIToolApproval(
                title = title,
                message = fields.string("message"),
                reason = fields.string("reason"),
                allowTitle = fields.string("allow_title"),
                declineTitle = fields.string("decline_title"),
                decision = fields.string("decision")?.let(::Decision),
            )
        }
    }
}
