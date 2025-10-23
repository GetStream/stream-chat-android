package io.getstream.chat.android.offline.repository.domain.receipts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface MessageReceiptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(receipts: List<MessageReceiptEntity>)

    @Query("SELECT * FROM stream_chat_message_receipt WHERE type = :type ORDER BY createdAt ASC")
    suspend fun selectAllByType(type: String): List<MessageReceiptEntity>

    @Query("DELETE FROM stream_chat_message_receipt WHERE messageId IN (:messageIds)")
    suspend fun deleteByMessageIds(messageIds: List<String>)
}
