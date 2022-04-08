package io.getstream.chat.android.client.persistence.repository.inmemory

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.persistence.repository.AttachmentRepository
import kotlinx.coroutines.flow.Flow

internal class AttachmentInMemoryRepository: AttachmentRepository {

    override fun observeAttachmentsForMessage(messageId: String): Flow<List<Attachment>> {
        TODO("Not yet implemented")
    }
}
