package io.getstream.chat.android.offline.repository.domain.message.attachment

internal interface AttachmentUploadRepository {
    suspend fun insertAttachmentsToUpload(vararg attachments: AttachmentToUploadEntity)
    suspend fun deleteAttachmentsToUpload(vararg attachments: AttachmentToUploadEntity)
    suspend fun updateAttachmentsToUpload(vararg attachments: AttachmentToUploadEntity)
    suspend fun selectAttachmentsToUploadForMessageId(id: Int): List<AttachmentToUploadEntity>
}

internal class AttachmentUploadRepositoryImpl(
    private val attachmentToUploadDao: AttachmentToUploadDao,
) : AttachmentUploadRepository {
    override suspend fun insertAttachmentsToUpload(vararg attachments: AttachmentToUploadEntity) {
        attachmentToUploadDao.insertAttachmentToUpload(*attachments)
    }

    override suspend fun deleteAttachmentsToUpload(vararg attachments: AttachmentToUploadEntity) {
        attachmentToUploadDao.deleteAttachmentsToUpload(*attachments)
    }

    override suspend fun updateAttachmentsToUpload(vararg attachments: AttachmentToUploadEntity) {
        attachmentToUploadDao.updateAttachmentsToUpload(*attachments)
    }

    override suspend fun selectAttachmentsToUploadForMessageId(id: Int): List<AttachmentToUploadEntity> {
        return attachmentToUploadDao.getAttachmentsToUploadForMessage(id)
    }
}
