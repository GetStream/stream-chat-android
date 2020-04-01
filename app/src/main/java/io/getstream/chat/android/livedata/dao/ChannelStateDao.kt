package io.getstream.chat.android.livedata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.getstream.chat.android.livedata.entity.ChannelStateEntity
import io.getstream.chat.android.livedata.entity.ReactionEntity

@Dao
interface ChannelStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(channelStateEntity: ChannelStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMany(channelStateEntities: List<ChannelStateEntity>)

    @Query(
        "SELECT * FROM stream_chat_channel_state " +
                "WHERE stream_chat_channel_state.syncStatus IN (-1, 2)"
    )
    suspend fun selectSyncNeeded(): List<ChannelStateEntity>

    @Query(
        "SELECT * FROM stream_chat_channel_state " +
                "WHERE stream_chat_channel_state.cid IN (:cids)"
    )
    suspend fun select(cids: List<String>): List<ChannelStateEntity>

    @Query(
        "SELECT * FROM stream_chat_channel_state " +
                "WHERE stream_chat_channel_state.cid IN (:cid)"
    )
    suspend fun select(cid: String?): ChannelStateEntity?
}