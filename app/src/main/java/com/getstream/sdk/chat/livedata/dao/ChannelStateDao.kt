package com.getstream.sdk.chat.livedata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.getstream.sdk.chat.livedata.entity.ChannelStateEntity

@Dao
interface ChannelStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(channelStateEntity: ChannelStateEntity)

}