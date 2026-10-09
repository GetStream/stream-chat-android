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

package io.getstream.chat.android.ai.ondevice

import kotlinx.coroutines.flow.Flow

/**
 * A language model on the device, for answering when your AI agent can't: the person is offline,
 * or the agent reached its usage limit.
 *
 * [AIOnDeviceModel] is Gemini Nano through ML Kit. Implement this interface to use another model.
 */
public interface AILocalModel {

    /** Whether the model can answer now. */
    public suspend fun isAvailable(): Boolean

    /**
     * Streams the answer to the last of [turns], the person's question.
     *
     * @param instructions How the model should answer: who it is, what it can't do.
     * @param turns The conversation so far, oldest first, ending with the person's question.
     * @return Each emission is the whole answer so far. Cancelling the collection stops the model.
     */
    public fun reply(instructions: String, turns: List<AIConversationTurn>): Flow<String>
}

/**
 * One turn of a conversation, for a local model.
 *
 * @param role Who said it.
 * @param text What they said.
 */
public data class AIConversationTurn(
    val role: Role,
    val text: String,
) {

    /** Who said a turn. */
    public enum class Role {
        /** The person. */
        User,

        /** The assistant. */
        Assistant,
    }

    public companion object {
        private const val BYTES_PER_TOKEN = 3

        /**
         * A turn the person said.
         *
         * @param text What they said.
         */
        public fun user(text: String): AIConversationTurn = AIConversationTurn(Role.User, text)

        /**
         * A turn the assistant said.
         *
         * @param text What it said.
         */
        public fun assistant(text: String): AIConversationTurn = AIConversationTurn(Role.Assistant, text)

        /**
         * The newest turns that fit a budget of [tokens], oldest first, with consecutive turns of one
         * role merged and empty ones left out. The question, the last turn, is always kept. Tokens
         * are estimated at three bytes of UTF-8 each, which overcounts English.
         */
        internal fun fitting(turns: List<AIConversationTurn>, tokens: Int): List<AIConversationTurn> {
            val merged = merged(turns)
            val question = merged.removeLastOrNull() ?: return emptyList()
            val kept = mutableListOf(question)
            var remaining = tokens - estimatedTokens(question)
            for (turn in merged.asReversed()) {
                remaining -= estimatedTokens(turn)
                if (remaining < 0) break
                kept += turn
            }
            return kept.asReversed()
        }

        /** The turns with text, with consecutive turns of one role merged. */
        private fun merged(turns: List<AIConversationTurn>): MutableList<AIConversationTurn> {
            val merged = mutableListOf<AIConversationTurn>()
            for (turn in turns.filter { it.text.isNotBlank() }) {
                val last = merged.lastOrNull()
                if (last?.role == turn.role) {
                    merged[merged.lastIndex] = last.copy(text = last.text + "\n\n" + turn.text)
                } else {
                    merged += turn
                }
            }
            return merged
        }

        private fun estimatedTokens(turn: AIConversationTurn): Int =
            turn.text.toByteArray(Charsets.UTF_8).size / BYTES_PER_TOKEN
    }
}
