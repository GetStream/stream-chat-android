package com.getstream.sdk.chat.livedata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.getstream.sdk.chat.livedata.entity.ChannelStateEntity

@Dao
interface ChannelStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(channelStateEntity: ChannelStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMany(channelStateEntities: List<ChannelStateEntity>)


    @Query(
        "SELECT * FROM stream_channel " +
                "WHERE stream_chat_channel_state.cid IN (:cids)"
    )
    suspend fun select(cids: List<String>?): List<ChannelStateEntity?>?

    @Query(
        "SELECT * FROM stream_channel " +
                "WHERE stream_chat_channel_state.cid IN (:cid)"
    )
    suspend fun select(cid: String?): ChannelStateEntity?
}