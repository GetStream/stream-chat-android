/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ai.assistant

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * The state of the typing indicator.
 *
 * @property name The name of the state.
 * @property value The value of the state.
 */
@Immutable
public sealed class TypingState(
    public val name: String,
    public val value: String,
) {
    /**
     * The state when there is no typing.
     */
    @Immutable
    public data object Nothing : TypingState(name = "Nothing", value = "nothing")

    /**
     * The state when the AI assistant is thinking
     *
     * @property messageId The id of the message that the AI assistant is thinking.
     */
    @Immutable
    public data class Thinking(
        val messageId: String,
    ) : TypingState(
        name = "Thinking",
        value = "AI_STATE_THINKING",
    )

    /**
     * The state when the AI assistant is generating a response.
     *
     * @property messageId The id of the message that the AI assistant is generating.
     */
    @Immutable
    public data class Generating(
        val messageId: String,
    ) : TypingState(name = "Generating", value = "AI_STATE_GENERATING")

    /**
     * The state when the AI assistant has finished generating a response.
     */
    @Immutable
    public data object Clear : TypingState(name = "Clear", value = "AI_STATE_CLEAR")

    public companion object {
        /**
         * Converts a string to a [TypingState].
         */
        @Stable
        public fun String.toTypingState(id: String? = null): TypingState = when (this) {
            "nothing" -> Nothing
            "AI_STATE_THINKING" -> Thinking(id.orEmpty())
            "AI_STATE_GENERATING" -> Generating(id.orEmpty())
            "AI_STATE_CLEAR" -> Clear
            else -> Nothing
        }
    }
}
