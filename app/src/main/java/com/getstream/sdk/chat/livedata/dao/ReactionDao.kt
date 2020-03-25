package com.getstream.sdk.chat.livedata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.getstream.sdk.chat.livedata.entity.MessageEntity
import com.getstream.sdk.chat.livedata.entity.ReactionEntity
import com.getstream.sdk.chat.livedata.entity.UserEntity

@Dao
interface ReactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reactionEntity: ReactionEntity)

    @Query(
        "SELECT * FROM stream_chat_reaction " +
                "WHERE stream_chat_reaction.syncStatus IN [-1, 2]"
    )
    suspend fun selectSyncNeeded(): List<ReactionEntity>

}