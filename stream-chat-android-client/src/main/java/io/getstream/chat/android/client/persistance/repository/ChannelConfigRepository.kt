package io.getstream.chat.android.client.persistance.repository

import io.getstream.chat.android.client.models.ChannelConfig

public interface ChannelConfigRepository {
    /**
     * Caches in memory data from DB.
     */
    public suspend fun cacheChannelConfigs()
    public fun selectChannelConfig(channelType: String): ChannelConfig?
    public suspend fun insertChannelConfigs(configs: Collection<ChannelConfig>)
    public suspend fun insertChannelConfig(config: ChannelConfig)
}
