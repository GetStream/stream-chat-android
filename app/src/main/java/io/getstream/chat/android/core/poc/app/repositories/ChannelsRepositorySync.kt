package io.getstream.chat.android.core.poc.app.repositories


import io.getstream.chat.android.core.poc.app.ChannelsCache
import io.getstream.chat.android.core.poc.app.common.Channel
import io.getstream.chat.android.core.poc.library.ChatClient
import io.getstream.chat.android.core.poc.library.ChatClientImpl

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