package io.getstream.chat.android.offline.repository.domain.message.internal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface ReplyMessageDao {

    @Query("SELECT * FROM $REPLY_MESSAGE_ENTITY_TABLE_NAME WHERE id = :id")
    suspend fun selectById(id: String): ReplyMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(replyMessageEntity: ReplyMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(replyMessageEntities: List<ReplyMessageEntity>)

    @Delete
    suspend fun delete(replyMessageEntity: ReplyMessageEntity)

    @Query("DELETE FROM $REPLY_MESSAGE_ENTITY_TABLE_NAME")
    suspend fun deleteAll()

}
