package io.getstream.chat.android.core.poc.app.repositories

import androidx.lifecycle.LiveData
import io.getstream.chat.android.core.poc.app.ChannelsCache
import io.getstream.chat.android.core.poc.app.common.ApiMapper
import io.getstream.chat.android.core.poc.app.common.Channel
import io.getstream.chat.android.core.poc.library.StreamChatClient

class ChannelsRepositoryLive(
    private val client: StreamChatClient,
    private val cache: ChannelsCache
) {

    fun getChannels(): LiveData<List<Channel>> {
        val call = client.queryChannels()
        val live = cache.getAllLive()

        call.enqueue { result ->
            if (result.isSuccess()) {
                cache.storeAsync(ApiMapper.mapChannels(result.data()))
            }
        }

        return live
    }
}