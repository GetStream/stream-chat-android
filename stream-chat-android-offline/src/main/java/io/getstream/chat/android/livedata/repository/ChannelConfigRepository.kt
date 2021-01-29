package io.getstream.chat.android.livedata.repository

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.livedata.dao.ChannelConfigDao
import io.getstream.chat.android.livedata.entity.ChannelConfigEntity
import io.getstream.chat.android.livedata.model.ChannelConfig
import io.getstream.chat.android.livedata.repository.mapper.toEntity
import io.getstream.chat.android.livedata.repository.mapper.toModel
import java.util.Collections

/**
 * The channel config repository stores all channel configs in room as well as in memory
 * Call channelConfigRepository.load to load all configs into memory
 */
internal class ChannelConfigRepository(private val channelConfigDao: ChannelConfigDao) {
    private val channelConfigs: MutableMap<String, ChannelConfig> = Collections.synchronizedMap(mutableMapOf())

    internal suspend fun load() {
        channelConfigs += channelConfigDao.selectAll().map(ChannelConfigEntity::toModel)
            .associateBy(ChannelConfig::type)
    }

    @VisibleForTesting
    internal fun clearCache() {
        channelConfigs.clear()
    }

    internal fun select(channelType: String): ChannelConfig? {
        return channelConfigs[channelType]
    }

    suspend fun insert(configs: Collection<ChannelConfig>) {
        // update the local configs
        channelConfigs += configs.associateBy(ChannelConfig::type)

        // insert into room db
        channelConfigDao.insert(configs.map(ChannelConfig::toEntity))
    }

    suspend fun insert(config: ChannelConfig) {
        channelConfigs += config.type to config
        channelConfigDao.insert(config.toEntity())
    }
}
