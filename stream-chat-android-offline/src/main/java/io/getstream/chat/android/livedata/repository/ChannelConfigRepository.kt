package io.getstream.chat.android.livedata.repository

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.livedata.dao.ChannelConfigDao
import io.getstream.chat.android.livedata.entity.ChannelConfigEntity
import io.getstream.chat.android.livedata.model.ChannelConfig
import io.getstream.chat.android.livedata.model.config
import io.getstream.chat.android.livedata.model.type
import java.util.Collections

/**
 * The channel config repository stores all channel configs in room as well as in memory
 * Call channelConfigRepository.load to load all configs into memory
 */
internal class ChannelConfigRepository(private val channelConfigDao: ChannelConfigDao) {
    private val channelConfigs: MutableMap<String, Config> = Collections.synchronizedMap(mutableMapOf())

    internal suspend fun load() {
        channelConfigs += channelConfigDao.selectAll().map { it.channelType to it.config }
    }

    @VisibleForTesting
    internal fun clearCache() {
        channelConfigs.clear()
    }

    internal fun select(channelType: String): ChannelConfig? {
        return channelConfigs[channelType]?.let { ChannelConfig(channelType, it) }
    }

    suspend fun insert(configs: Collection<ChannelConfig>) {
        // update the local configs
        channelConfigs += configs

        // insert into room db
        channelConfigDao.insertMany(configs.map { ChannelConfigEntity(it.type, it.config) })
    }

    suspend fun insert(config: ChannelConfig) {
        channelConfigs += config
        channelConfigDao.insert(ChannelConfigEntity(config.type, config.config))
    }
}
