package io.getstream.chat.android.livedata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.entity.ChannelEntity

@Dao
internal interface ChannelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(channelEntity: ChannelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMany(channelEntities: List<ChannelEntity>)

    @Query(
        "SELECT * FROM stream_chat_channel_state " +
            "WHERE stream_chat_channel_state.syncStatus IN (:syncStatus)"
    )
    suspend fun selectSyncNeeded(syncStatus: SyncStatus = SyncStatus.SYNC_NEEDED): List<ChannelEntity>

    @Query(
        "SELECT * FROM stream_chat_channel_state " +
            "WHERE stream_chat_channel_state.cid IN (:cids)"
    )
    suspend fun select(cids: List<String>): List<ChannelEntity>

    @Query(
        "SELECT * FROM stream_chat_channel_state " +
            "WHERE stream_chat_channel_state.cid IN (:cid)"
    )
    suspend fun select(cid: String?): ChannelEntity?

    @Query("SELECT * FROM stream_chat_channel_State")
    suspend fun selectAll(): List<ChannelEntity>

    @Query("DELETE from stream_chat_channel_state WHERE cid = :cid")
    suspend fun delete(cid: String)
}
