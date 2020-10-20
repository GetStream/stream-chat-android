package io.getstream.chat.android.client.sample.repositories

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.sample.ChannelsCache
import io.getstream.chat.android.client.sample.common.Channel

class ChannelsRepositorySync(
    private val client: ChatClient,
    private val cache: ChannelsCache
) {
    fun getChannels(): List<Channel> {

        return null!!

//        val result = client.queryChannels().execute()
//        return if (result.isSuccess()) {
//            val channels = ApiMapper.mapChannels(result.data())
//            cache.storeSync(channels)
//            channels
//        } else {
//            cache.getAllSync()
//        }
    }
}
