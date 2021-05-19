package io.getstream.chat.android.offline.repository.domain.message.attachment

import io.getstream.chat.android.client.models.Attachment

internal interface AttachmentUploadRepository {
    suspend fun insertAttachmentsToUpload(messageId: String, attachments: List<Attachment>)
    suspend fun deleteAttachmentsToUpload(messageId: String, attachments: List<Attachment>)
    suspend fun updateAttachmentsToUpload(messageId: String, attachments: List<Attachment>)
    suspend fun selectAttachmentsToUploadForMessageId(id: Int): List<Attachment>
}

internal class AttachmentUploadRepositoryImpl(
    private val attachmentToUploadDao: AttachmentToUploadDao,
) : AttachmentUploadRepository {
    override suspend fun insertAttachmentsToUpload(messageId: String, attachments: List<Attachment>) {
        attachmentToUploadDao.insertAttachmentToUpload(attachments.map { it.toUploadAttachment(messageId) })
    }

    override suspend fun deleteAttachmentsToUpload(messageId: String, attachments: List<Attachment>) {
        attachmentToUploadDao.deleteAttachmentsToUpload(attachments.map { it.toUploadAttachment(messageId) })
    }

    override suspend fun updateAttachmentsToUpload(messageId: String, attachments: List<Attachment>) {
        attachmentToUploadDao.updateAttachmentsToUpload(attachments.map { it.toUploadAttachment(messageId) })
    }

    override suspend fun selectAttachmentsToUploadForMessageId(id: Int): List<Attachment> {
        return attachmentToUploadDao.getAttachmentsToUploadForMessage(id).map(AttachmentToUploadEntity::toModel)
    }
}
