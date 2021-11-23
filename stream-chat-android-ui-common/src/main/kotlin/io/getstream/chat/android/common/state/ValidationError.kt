package io.getstream.chat.android.common.state

import io.getstream.chat.android.client.models.Attachment

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
}
