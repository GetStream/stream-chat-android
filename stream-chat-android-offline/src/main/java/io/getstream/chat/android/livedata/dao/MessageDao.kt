package io.getstream.chat.android.livedata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.entity.MessageEntity
import java.util.Date

@Dao
internal interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMany(messageEntities: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(messageEntity: MessageEntity)

    @Query("SELECT * from stream_chat_message WHERE cid = :cid AND createdAt > :dateFilter ORDER BY createdAt ASC LIMIT :limit")
    suspend fun messagesForChannelNewerThan(cid: String, limit: Int = 100, dateFilter: Date): List<MessageEntity>

    @Query("SELECT * from stream_chat_message WHERE cid = :cid AND createdAt >= :dateFilter ORDER BY createdAt ASC LIMIT :limit")
    suspend fun messagesForChannelEqualOrNewerThan(cid: String, limit: Int = 100, dateFilter: Date): List<MessageEntity>

    @Query("SELECT * from stream_chat_message WHERE cid = :cid AND createdAt < :dateFilter ORDER BY createdAt DESC LIMIT :limit")
    suspend fun messagesForChannelOlderThan(cid: String, limit: Int = 100, dateFilter: Date): List<MessageEntity>

    @Query("SELECT * from stream_chat_message WHERE cid = :cid AND createdAt <= :dateFilter ORDER BY createdAt DESC LIMIT :limit")
    suspend fun messagesForChannelEqualOrOlderThan(cid: String, limit: Int = 100, dateFilter: Date): List<MessageEntity>

    @Query("SELECT * from stream_chat_message WHERE cid = :cid ORDER BY createdAt DESC LIMIT :limit")
    suspend fun messagesForChannel(cid: String, limit: Int = 100): List<MessageEntity>

    @Transaction
    suspend fun messagesForChannel(cids: List<String>, limit: Int = 100): Map<String, List<MessageEntity>> {
        return cids.map { cid -> cid to messagesForChannel(cid, limit)}.toMap()
    }

    @Query("DELETE from stream_chat_message WHERE cid = :cid AND createdAt < :deleteMessagesBefore")
    suspend fun deleteChannelMessagesBefore(cid: String, deleteMessagesBefore: Date)

    @Query("DELETE from stream_chat_message WHERE cid = :cid AND id = :messageId")
    suspend fun deleteMessage(cid: String, messageId: String)

    @Query(
        "SELECT * FROM stream_chat_message " +
            "WHERE stream_chat_message.id IN (:ids)"
    )
    suspend fun select(ids: List<String>): List<MessageEntity>

    @Query(
        "SELECT * FROM stream_chat_message " +
            "WHERE stream_chat_message.id IN (:id)"
    )
    suspend fun select(id: String?): MessageEntity?

    @Query(
        "SELECT * FROM stream_chat_message " +
            "WHERE stream_chat_message.syncStatus IN (:syncStatus) ORDER BY createdAt ASC"
    )
    suspend fun selectSyncNeeded(syncStatus: SyncStatus = SyncStatus.SYNC_NEEDED): List<MessageEntity>
}
