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

package io.getstream.chat.android.ui.common.state.messages.composer

import io.getstream.chat.android.models.Attachment

/**
 * Represents a validation error for the user input.
 */
public sealed class ValidationError {
    /**
     * Represents a validation error that happens when the message length in the message input
     * exceed the maximum allowed message length.
     *
     * @param messageLength The current message length in the message input.
     * @param maxMessageLength The maximum allowed message length that we exceeded.
     */
    public data class MessageLengthExceeded(
        val messageLength: Int,
        val maxMessageLength: Int,
    ) : ValidationError()

    /**
     * Represents a validation error that happens when one or several attachments are too big
     * to be handled by the server.
     *
     * @param attachments The list of attachments that are bigger than the server can handle.
     * @param maxAttachmentSize The maximum allowed attachment file size in bytes.
     */
    public data class AttachmentSizeExceeded(
        val attachments: List<Attachment>,
        val maxAttachmentSize: Long,
    ) : ValidationError()

    /**
     * Represents a validation error that happens when the number of selected attachments is too
     * big to be sent in a single message.
     *
     * @param attachmentCount The number of selected attachments.
     * @param maxAttachmentCount The maximum allowed number of attachments in a single message.
     */
    public data class AttachmentCountExceeded(
        val attachmentCount: Int,
        val maxAttachmentCount: Int,
    ) : ValidationError()

    /**
     * Triggered if the input contains a link and the user is not allowed to send them.
     */
    public object ContainsLinksWhenNotAllowed : ValidationError() {
        override fun toString(): String = "ContainsLinksWhenNotAllowed"
    }
}
