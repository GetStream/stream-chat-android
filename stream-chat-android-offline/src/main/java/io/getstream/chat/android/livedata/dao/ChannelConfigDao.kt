package io.getstream.chat.android.livedata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.getstream.chat.android.livedata.entity.ChannelConfigEntity
import io.getstream.chat.android.livedata.entity.ChannelConfigInnerEntity
import io.getstream.chat.android.livedata.entity.CommandInnerEntity

@Dao
internal interface ChannelConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    suspend fun insertMany(channelConfigEntities: List<ChannelConfigEntity>) {
        channelConfigEntities.map(ChannelConfigEntity::channelConfigInnerEntity).also { insertConfigs(it) }
        channelConfigEntities.flatMap(ChannelConfigEntity::commands).also { insertCommands(it) }
    }

    @Transaction
    suspend fun insert(channelConfigEntity: ChannelConfigEntity) {
        insert(channelConfigEntity.channelConfigInnerEntity)
        insertCommands(channelConfigEntity.commands)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(channelConfigInnerEntity: ChannelConfigInnerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigs(channelConfigInnerEntities: List<ChannelConfigInnerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommands(commands: List<CommandInnerEntity>)

    @Transaction
    @Query("SELECT * FROM stream_chat_channel_config LIMIT 100")
    suspend fun selectAll(): List<ChannelConfigEntity>
}
