package io.getstream.chat.android.common.state

import io.getstream.chat.android.client.models.Attachment

/**
 * Represents the list of validation errors for the current text input and currently selected attachments.
 */
public sealed class ValidationError {
    /**
     *
     */
    public data class MessageLengthExceeded(
        val messageLength: Int,
        val maxMessageLength: Int,
    ) : ValidationError()

    /**
     *
     */
    public data class AttachmentSizeExceeded(
        val attachments: List<Attachment>,
        val maxAttachmentSize: Long,
    ) : ValidationError()

    /**
     *
     */
    public data class AttachmentCountExceeded(
        val attachmentCount: Int,
        val maxAttachmentCount: Int,
    ) : ValidationError()
}