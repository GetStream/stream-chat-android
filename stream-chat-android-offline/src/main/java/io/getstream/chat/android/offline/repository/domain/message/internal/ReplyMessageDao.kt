package io.getstream.chat.android.offline.repository.domain.message.internal

import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
internal interface ReplyMessageDao {

    @Query("SELECT * FROM $REPLY_MESSAGE_ENTITY_TABLE_NAME WHERE id = :id")
    @Transaction
    fun selectById(id: String): ReplyMessageEntity?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun insert(replyMessageEntity: ReplyMessageEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun insert(replyMessageEntities: List<ReplyMessageEntity>)

    // Todo: Implement other important methods.

}
