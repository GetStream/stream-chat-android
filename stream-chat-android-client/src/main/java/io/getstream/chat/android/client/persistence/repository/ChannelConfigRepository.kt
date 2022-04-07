package io.getstream.chat.android.client.persistence.repository

import io.getstream.chat.android.client.channel.internal.ChannelConfig

public interface ChannelConfigRepository {
    /**
     * Caches in memory data from DB.
     */
    public suspend fun cacheChannelConfigs()
    public fun selectChannelConfig(channelType: String): ChannelConfig?
    public suspend fun insertChannelConfigs(configs: Collection<ChannelConfig>)
    public suspend fun insertChannelConfig(config: ChannelConfig)
}
