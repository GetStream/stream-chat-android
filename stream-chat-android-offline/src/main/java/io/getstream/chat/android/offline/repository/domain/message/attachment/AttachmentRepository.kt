package io.getstream.chat.android.offline.repository.domain.message.attachment

import io.getstream.chat.android.client.models.Attachment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal interface AttachmentRepository {
    fun observeAttachmentsForMessage(messageId: String): Flow<List<Attachment>>
}

internal class AttachmentRepositoryImpl(private val attachmentDao: AttachmentDao) : AttachmentRepository {
    override fun observeAttachmentsForMessage(messageId: String): Flow<List<Attachment>> {
        return attachmentDao.observeAttachmentsForMessage(messageId)
            .distinctUntilChanged()
            .map { attachments -> attachments.map(AttachmentEntity::toModel) }
    }
}
