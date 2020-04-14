package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.livedata.dao.ChannelConfigDao
import io.getstream.chat.android.livedata.entity.ChannelConfigEntity

class ChannelConfigRepository(var channelConfigDao: ChannelConfigDao) {
    var channelConfigs: MutableMap<String, Config> = mutableMapOf()

    suspend fun load() {
        val configEntities = channelConfigDao.selectAll()
        for (configEntity in configEntities) {
            channelConfigs[configEntity.channelType] = configEntity.config
        }
    }

    fun select(channelType: String): Config? {
        val config = channelConfigs.getOrElse(channelType) {null}
        return config
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
        val configEntities = mutableListOf<ChannelConfigEntity>()

        for ((channelType, config) in configs) {
            val entity = ChannelConfigEntity(channelType, config)
            configEntities.add(entity)
        }
        insert(configEntities)

    }
}