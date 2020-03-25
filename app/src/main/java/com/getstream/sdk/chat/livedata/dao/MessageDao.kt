package com.getstream.sdk.chat.livedata.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.getstream.sdk.chat.livedata.entity.MessageEntity
import com.getstream.sdk.chat.livedata.entity.ReactionEntity
import com.getstream.sdk.chat.livedata.entity.UserEntity

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMany(messageEntities: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(messageEntity: MessageEntity)

    @Query("SELECT * from stream_chat_message WHERE cid = :cid ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    fun messagesForChannel(cid: String, limit: Int = 100, offset: Int = 0): LiveData<List<MessageEntity>>

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
                "WHERE stream_chat_message.syncStatus IN (-1, 2)"
    )
    suspend fun selectSyncNeeded(): List<MessageEntity>


}