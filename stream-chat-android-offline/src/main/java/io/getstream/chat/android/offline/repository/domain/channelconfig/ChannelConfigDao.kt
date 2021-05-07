package io.getstream.chat.android.offline.repository.domain.channelconfig

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
internal abstract class ChannelConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    open suspend fun insert(channelConfigEntities: List<ChannelConfigEntity>) {
        insertConfigs(channelConfigEntities.map(ChannelConfigEntity::channelConfigInnerEntity))
        insertCommands(channelConfigEntities.flatMap(ChannelConfigEntity::commands))
    }

    @Transaction
    open suspend fun insert(channelConfigEntity: ChannelConfigEntity) {
        insertConfig(channelConfigEntity.channelConfigInnerEntity)
        insertCommands(channelConfigEntity.commands)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertConfig(channelConfigInnerEntity: ChannelConfigInnerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertConfigs(channelConfigInnerEntities: List<ChannelConfigInnerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertCommands(commands: List<CommandInnerEntity>)

    @Transaction
    @Query("SELECT * FROM stream_chat_channel_config LIMIT 100")
    abstract suspend fun selectAll(): List<ChannelConfigEntity>
}
