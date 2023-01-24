package io.getstream.chat.android.offline.repository.domain.message.internal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.AttachmentEntity

@Dao
internal interface ReplyMessageDao {

    @Query("SELECT * FROM $REPLY_MESSAGE_ENTITY_TABLE_NAME WHERE id = :id")
    @Transaction
    suspend fun selectById(id: String): ReplyMessageEntity?

    @Transaction
    suspend fun insert(replyMessageEntities: List<ReplyMessageEntity>) {
        insertInnerEntity(replyMessageEntities.map(ReplyMessageEntity::replyMessageInnerEntity))
        insertAttachments(replyMessageEntities.flatMap(ReplyMessageEntity::attachments))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInnerEntity(replyMessageEntities: List<ReplyMessageInnerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(attachmentEntities: List<AttachmentEntity>)

    @Delete
    suspend fun delete(replyMessageInnerEntity: ReplyMessageInnerEntity)

    @Query("DELETE FROM $REPLY_MESSAGE_ENTITY_TABLE_NAME")
    suspend fun deleteAll()

}
