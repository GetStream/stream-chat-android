package io.getstream.chat.android.offline.repository.domain.channel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

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

    @Query("DELETE from stream_chat_channel_state WHERE cid = :cid")
    suspend fun delete(cid: String)

    @Query("UPDATE stream_chat_channel_state SET deletedAt = :deletedAt WHERE cid = :cid")
    suspend fun setDeletedAt(cid: String, deletedAt: Date)

    @Query("UPDATE stream_chat_channel_state SET hidden = :hidden, hideMessagesBefore = :hideMessagesBefore WHERE cid = :cid")
    suspend fun setHidden(cid: String, hidden: Boolean, hideMessagesBefore: Date)

    @Query("UPDATE stream_chat_channel_state SET hidden = :hidden WHERE cid = :cid")
    suspend fun setHidden(cid: String, hidden: Boolean)
}
