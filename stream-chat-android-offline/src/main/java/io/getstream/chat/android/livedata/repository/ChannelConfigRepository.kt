package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.livedata.dao.ChannelConfigDao
import io.getstream.chat.android.livedata.entity.ChannelConfigEntity
import io.getstream.chat.android.livedata.model.ChannelConfig
import io.getstream.chat.android.livedata.repository.mapper.toEntity
import io.getstream.chat.android.livedata.repository.mapper.toModel
import java.util.Collections

internal interface ChannelConfigRepository {
    /**
     * Caches in memory data from DB.
     */
    suspend fun cacheData()
    fun clearCache()
    fun select(channelType: String): ChannelConfig?
    suspend fun insert(configs: Collection<ChannelConfig>)
    suspend fun insert(config: ChannelConfig)
}

/**
 * The channel config repository stores all channel configs in room as well as in memory
 * Call channelConfigRepository.load to load all configs into memory
 */
internal class ChannelConfigRepositoryImpl(private val channelConfigDao: ChannelConfigDao) : ChannelConfigRepository {
    private val channelConfigs: MutableMap<String, ChannelConfig> = Collections.synchronizedMap(mutableMapOf())

    override suspend fun cacheData() {
        channelConfigs += channelConfigDao.selectAll().map(ChannelConfigEntity::toModel)
            .associateBy(ChannelConfig::type)
    }

    override fun clearCache() {
        channelConfigs.clear()
    }

    override fun select(channelType: String): ChannelConfig? {
        return channelConfigs[channelType]
    }

    override suspend fun insert(configs: Collection<ChannelConfig>) {
        // update the local configs
        channelConfigs += configs.associateBy(ChannelConfig::type)

        // insert into room db
        channelConfigDao.insert(configs.map(ChannelConfig::toEntity))
    }

    override suspend fun insert(config: ChannelConfig) {
        channelConfigs += config.type to config
        channelConfigDao.insert(config.toEntity())
    }
}
