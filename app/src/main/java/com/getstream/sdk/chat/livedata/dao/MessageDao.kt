package com.getstream.sdk.chat.livedata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.getstream.sdk.chat.livedata.entity.MessageEntity
import com.getstream.sdk.chat.livedata.entity.ReactionEntity

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(messageEntity: MessageEntity)

}