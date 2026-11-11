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

import android.net.Uri
import io.getstream.chat.android.ai.compose.generativeui.AIUIAction
import java.util.UUID

/**
 * What the person asks of the agent, which an [AIConversationState] hands to its
 * [AIConversationTransport].
 *
 * @param kind What the person asks for.
 * @param text What the person wrote. For a [AIUIAction], its label. When a reply is written
 * again, the text of the message it answers.
 * @param attachments The files the person picked, on this device, to upload with their message.
 * @param message The message it is about, such as the reply to write again.
 * @param uiAction What the person did in a reply's generated UI, which their message carries for
 * the agent, such as under [AIUIAction.customData]. Null for a message they wrote.
 * @param messageId An id for the person's message. A transport that creates the message with it
 * lets the conversation find its copy by it. A retry of the request keeps it.
 */
public data class AIConversationRequest<out M : AIMessage>(
    val kind: AIConversationRequestKind,
    val text: String,
    val attachments: List<Uri> = emptyList(),
    val message: M? = null,
    val uiAction: AIUIAction? = null,
    val messageId: String = UUID.randomUUID().toString(),
)

/**
 * What the person asks for in an [AIConversationRequest]. It is an open set: compare against the
 * kinds you know, and treat the rest as a new message.
 *
 * @param rawValue The kind's name, such as `message`.
 */
@JvmInline
public value class AIConversationRequestKind(public val rawValue: String) {
    override fun toString(): String = rawValue

    public companion object {
        /** A new message from the person. */
        public val Message: AIConversationRequestKind = AIConversationRequestKind("message")

        /**
         * A new reply in place of the request's `message`, which is a reply, and of those after
         * it. Without `message`, a reply to the latest message, which has none.
         *
         * [AIConversationState] doesn't send it yet: it is kept for asking the agent for a reply
         * again, which a later version adds.
         */
        public val Regenerate: AIConversationRequestKind = AIConversationRequestKind("regenerate")
    }
}
