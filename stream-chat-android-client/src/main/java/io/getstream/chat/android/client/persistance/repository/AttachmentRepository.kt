package io.getstream.chat.android.client.persistance.repository

import io.getstream.chat.android.client.models.Attachment
import kotlinx.coroutines.flow.Flow

public interface AttachmentRepository {
    public fun observeAttachmentsForMessage(messageId: String): Flow<List<Attachment>>
}
