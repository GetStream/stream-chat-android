package io.getstream.chat.android.offline.repository.domain.message.attachment

internal interface AttachmentUploadRepository {
    suspend fun insertAttachmentsToUpload(vararg attachments: AttachmentToUploadEntity)
    suspend fun deleteAttachmentsToUpload(vararg attachments: AttachmentToUploadEntity)
    suspend fun updateAttachmentsToUpload(vararg attachments: AttachmentToUploadEntity)
    suspend fun selectAttachmentsToUploadForMessageId(id: Int): List<AttachmentToUploadEntity>
}

internal class AttachmentUploadRepositoryImpl(
    private val attachmentToUploadEntityDao: AttachmentToUploadEntityDao,
) : AttachmentUploadRepository {
    override suspend fun insertAttachmentsToUpload(vararg attachments: AttachmentToUploadEntity) {
        attachmentToUploadEntityDao.insertAttachmentToUpload(*attachments)
    }

    override suspend fun deleteAttachmentsToUpload(vararg attachments: AttachmentToUploadEntity) {
        attachmentToUploadEntityDao.deleteAttachmentsToUpload(*attachments)
    }

    override suspend fun updateAttachmentsToUpload(vararg attachments: AttachmentToUploadEntity) {
        attachmentToUploadEntityDao.updateAttachmentsToUpload(*attachments)
    }

    override suspend fun selectAttachmentsToUploadForMessageId(id: Int): List<AttachmentToUploadEntity> {
        return attachmentToUploadEntityDao.getAttachmentsToUploadForMessage(id)
    }
}
