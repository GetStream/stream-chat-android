package io.getstream.chat.android.client.sample.repositories

import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.sample.ChannelsCache
import io.getstream.chat.android.client.sample.common.Channel

class ChannelsRepositoryLive(
    private val client: ChatClient,
    private val cache: ChannelsCache
) {

    fun getChannels(): LiveData<List<Channel>> {

        return null!!

//        val call = client.queryChannels()
//        val live = cache.getAllLive()
//
//        call.enqueue { result ->
//            if (result.isSuccess()) {
//                cache.storeAsync(ApiMapper.mapChannels(result.data()))
//            }
//        }
//
//        return live
    }
}
