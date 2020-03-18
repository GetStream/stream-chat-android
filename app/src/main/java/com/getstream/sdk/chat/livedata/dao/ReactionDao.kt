package com.getstream.sdk.chat.livedata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.getstream.sdk.chat.livedata.entity.ReactionEntity
import com.getstream.sdk.chat.livedata.entity.UserEntity

@Dao
interface ReactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reactionEntity: ReactionEntity)

}