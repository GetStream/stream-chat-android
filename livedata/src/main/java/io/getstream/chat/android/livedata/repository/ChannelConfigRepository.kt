package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.livedata.dao.ChannelConfigDao
import io.getstream.chat.android.livedata.entity.ChannelConfigEntity

/**
 * The channel config repository stores all channel configs in room as well as in memory
 * Call channelConfigRepository.load to load all configs into memory
 */
internal class ChannelConfigRepository(var channelConfigDao: ChannelConfigDao) {
    var channelConfigs: MutableMap<String, Config> = mutableMapOf()

    suspend fun load() {
        val configEntities = channelConfigDao.selectAll()
        for (configEntity in configEntities) {
            channelConfigs[configEntity.channelType] = configEntity.config
        }
    }

    fun clearCache() {
        channelConfigs = mutableMapOf()
    }

    fun select(channelType: String): Config? {
        return channelConfigs.getOrElse(channelType) { null }
    }

    suspend fun insert(configEntities: List<ChannelConfigEntity>) {

        // update the local configs
        for (configEntity in configEntities) {
            channelConfigs[configEntity.channelType] = configEntity.config
        }

        // insert into room db
        channelConfigDao.insertMany(configEntities)
    }

    suspend fun insertConfigs(configs: MutableMap<String, Config>) {
        val configEntities = configs.map { ChannelConfigEntity(it.key, it.value) }

        insert(configEntities)
    }
}
