package io.getstream.chat.android.offline.channel

import io.getstream.chat.android.client.helpers.AttachmentHelper
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message

internal class MessageHelper(private val attachmentHelper: AttachmentHelper = AttachmentHelper()) {

    fun updateValidAttachmentsUrl(newMessages: List<Message>, oldMessages: Map<String, Message>): List<Message> {
        if (oldMessages.isEmpty()) {
            return newMessages
        }
        return newMessages.map { newMessage -> updateValidAttachmentsUrl(newMessage, oldMessages[newMessage.id]) }
    }

    private fun updateValidAttachmentsUrl(newMessage: Message, oldMessage: Message?): Message {
        return if (newMessage.attachments.isEmpty() || oldMessage == null) {
            newMessage
        } else {
            newMessage.copy(
                attachments = updateValidAttachmentsUrl(
                    newMessage.attachments,
                    oldMessage.attachments
                ).toMutableList()
            )
        }
    }

    private fun updateValidAttachmentsUrl(
        newAttachments: List<Attachment>,
        oldAttachments: List<Attachment>
    ): List<Attachment> {
        return newAttachments.map { newAttachment ->
            updateValidAttachmentUrl(
                newAttachment,
                oldAttachments.firstOrNull { it.partialEquality(newAttachment) }
            )
        }
    }

    private fun updateValidAttachmentUrl(newAttachment: Attachment, oldAttachment: Attachment?): Attachment {
        return when {
            oldAttachment == null -> newAttachment
            oldAttachment.imageUrl.isNullOrEmpty() -> newAttachment
            oldAttachment.imageUrl == newAttachment.imageUrl -> newAttachment
            attachmentHelper.hasValidImageUrl(oldAttachment).not() -> newAttachment
            else -> newAttachment.copy(imageUrl = oldAttachment.imageUrl)
        }
    }

    private fun Attachment.partialEquality(other: Attachment): Boolean {
        return authorName == other.authorName && titleLink == other.titleLink && mimeType == other.mimeType &&
            fileSize == other.fileSize && title == other.title && text == other.text && type == other.type &&
            name == other.name && fallback == other.fallback
    }
}
