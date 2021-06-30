package io.getstream.chat.android.client.offline.repository.domain.message.attachment

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface AttachmentDao {
    @Query("SELECT * FROM attachment_inner_entity WHERE messageId == :messageId")
    fun observeAttachmentsForMessage(messageId: String): Flow<List<AttachmentEntity>>
}
