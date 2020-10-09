package io.getstream.chat.android.livedata.controller.helper

import io.getstream.chat.android.client.helpers.AttachmentHelper
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message

internal class MessageHelper(private val attachmentHelper: AttachmentHelper = AttachmentHelper()) {

    fun updateValidAttachmentsUrl(newMessages: List<Message>, oldMessages: Map<String, Message>): List<Message> {
        if (oldMessages.isEmpty()) {
            return newMessages
        }
        return newMessages.map { newMessage -> updateValidAttachmentsUrl(newMessage, oldMessages[newMessage.id])}
    }

    private fun updateValidAttachmentsUrl(newMessage: Message, oldMessage: Message?): Message {
        return if (newMessage.attachments.isEmpty() || oldMessage == null) {
            newMessage
        } else {
            newMessage.copy(attachments = updateValidAttachmentsUrl(newMessage.attachments, oldMessage.attachments).toMutableList())
        }
    }

    private fun updateValidAttachmentsUrl(
        newAttachments: List<Attachment>,
        oldAttachments: List<Attachment>
    ): List<Attachment> {
        return newAttachments.map { newAttachment -> updateValidAttachmentUrl(newAttachment, oldAttachments.firstOrNull { it.partialEquality(newAttachment)}) }
    }

    private fun updateValidAttachmentUrl(newAttachment: Attachment, oldAttachment: Attachment?): Attachment {
        return if (oldAttachment == null || oldAttachment.url.isNullOrEmpty() || oldAttachment.url == newAttachment.url) {
            newAttachment
        } else if (attachmentHelper.hasValidUrl(oldAttachment).not()) {
            newAttachment
        } else {
            newAttachment.updateUrlsFrom(oldAttachment)
        }
    }

    private fun Attachment.partialEquality(other: Attachment): Boolean {
        return authorName == other.authorName && titleLink == other.titleLink && mimeType == other.mimeType &&
                fileSize == other.fileSize && title == other.title && text == other.text && type == other.type &&
                name == other.name && fallback == other.fallback
    }

    private fun Attachment.updateUrlsFrom(other: Attachment): Attachment {
        return copy(url = other.url, thumbUrl = other.thumbUrl, ogUrl = other.ogUrl, assetUrl = other.assetUrl, imageUrl = other.imageUrl)
    }
}