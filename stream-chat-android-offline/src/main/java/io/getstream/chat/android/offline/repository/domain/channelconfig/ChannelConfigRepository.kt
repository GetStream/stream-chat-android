package io.getstream.chat.android.offline.repository.domain.channelconfig

import io.getstream.chat.android.offline.model.ChannelConfig
import java.util.Collections

internal interface ChannelConfigRepository {
    /**
     * Caches in memory data from DB.
     */
    suspend fun cacheChannelConfigs()
    fun selectChannelConfig(channelType: String): ChannelConfig?
    suspend fun insertChannelConfigs(configs: Collection<ChannelConfig>)
    suspend fun insertChannelConfig(config: ChannelConfig)
}

/**
 * The channel config repository stores all channel configs in room as well as in memory
 * Call channelConfigRepository.load to load all configs into memory
 */
internal class ChannelConfigRepositoryImpl(private val channelConfigDao: ChannelConfigDao) : ChannelConfigRepository {
    private val channelConfigs: MutableMap<String, ChannelConfig> = Collections.synchronizedMap(mutableMapOf())

    override suspend fun cacheChannelConfigs() {
        channelConfigs += channelConfigDao.selectAll().map(ChannelConfigEntity::toModel)
            .associateBy(ChannelConfig::type)
    }

    override fun selectChannelConfig(channelType: String): ChannelConfig? {
        return channelConfigs[channelType]
    }

    override suspend fun insertChannelConfigs(configs: Collection<ChannelConfig>) {
        // update the local configs
        channelConfigs += configs.associateBy(ChannelConfig::type)

        // insert into room db
        channelConfigDao.insert(configs.map(ChannelConfig::toEntity))
    }

    override suspend fun insertChannelConfig(config: ChannelConfig) {
        channelConfigs += config.type to config
        channelConfigDao.insert(config.toEntity())
    }
}
