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

package io.getstream.chat.android.ai.compose.conversation

import io.getstream.chat.android.ai.compose.parts.AIMessagePart
import io.getstream.chat.android.ai.compose.parts.AIReasoningPart

/**
 * A message in a conversation with an AI agent, as `AIConversation` shows it.
 *
 * Implement it on your own message type, such as one wrapping a Stream Chat message, rather than
 * converting: the conversation hands your message back in its views' params, so cast it there to
 * read what else it holds.
 */
public interface AIMessage {
    /** The message's stable identity. */
    public val id: String

    /** Who wrote it. */
    public val role: AIMessageRole

    /** What it says. A reply's text is Markdown. */
    public val text: String

    /** Whether the agent is still writing it. */
    public val isGenerating: Boolean get() = false

    /** The steps the agent took for it, in order: reasoning, tool calls, and kinds of its own. */
    public val parts: List<AIMessagePart> get() = emptyList()

    /** What it carries besides its text, such as images and files. */
    public val attachments: List<AIMessageAttachment> get() = emptyList()

    /**
     * The whole of a reasoning step's thinking when the app has it, such as from live updates.
     * Without it, the step shows its own excerpt. It is read each time the conversation updates,
     * so look it up rather than work it out.
     */
    public fun reasoningText(step: AIReasoningPart): String? = null
}

/**
 * Who wrote a message. It is an open set: compare against the roles you know.
 *
 * @param rawValue The role's name, such as `user`.
 */
@JvmInline
public value class AIMessageRole(public val rawValue: String) {
    override fun toString(): String = rawValue

    public companion object {
        /** The person using the app. Each of their messages starts a turn. */
        public val User: AIMessageRole = AIMessageRole("user")

        /** The agent. */
        public val Assistant: AIMessageRole = AIMessageRole("assistant")
    }
}
