package io.getstream.chat.android.client.persistence.repository.inmemory

import io.getstream.chat.android.client.channel.internal.ChannelConfig
import io.getstream.chat.android.client.persistence.repository.ChannelConfigRepository

internal class ChannelConfigInMemoryRepository: ChannelConfigRepository {

    override suspend fun cacheChannelConfigs() {
        TODO("Not yet implemented")
    }

    override fun selectChannelConfig(channelType: String): ChannelConfig? {
        TODO("Not yet implemented")
    }

    override suspend fun insertChannelConfigs(configs: Collection<ChannelConfig>) {
        TODO("Not yet implemented")
    }

    override suspend fun insertChannelConfig(config: ChannelConfig) {
        TODO("Not yet implemented")
    }
}
