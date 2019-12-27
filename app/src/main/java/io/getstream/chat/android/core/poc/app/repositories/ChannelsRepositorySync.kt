package io.getstream.chat.android.core.poc.app.repositories


import io.getstream.chat.android.core.poc.app.ChannelsCache
import io.getstream.chat.android.core.poc.app.common.ApiMapper
import io.getstream.chat.android.core.poc.app.common.Channel
import io.getstream.chat.android.core.poc.library.StreamChatClient

class ChannelsRepositorySync(
    private val client: StreamChatClient,
    private val cache: ChannelsCache
) {
    fun getChannels(): List<Channel> {
        val result = client.queryChannels().execute()
        return if (result.isSuccess()) {
            val channels = ApiMapper.mapChannels(result.data())
            cache.storeSync(channels)
            channels
        } else {
            cache.getAllSync()
        }
    }
}