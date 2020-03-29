package io.getstream.chat.android.livedata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.getstream.chat.android.livedata.entity.ChannelConfigEntity


@Dao
interface ChannelConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMany(channelConfigEntities: List<ChannelConfigEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(channelConfigEntity: ChannelConfigEntity)

    @Query(
        "SELECT * FROM stream_chat_channel_config "
    )
    // TODO verify we use livedata where needed (like here for instance)
    suspend fun selectAll(): List<ChannelConfigEntity>

}