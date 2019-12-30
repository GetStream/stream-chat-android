package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.requests.ChannelsQuery

class StreamChatClient {

    private val api = Api(RetrofitApiBuilder().build())

    fun setUser(user: ChatUser) {

    }

    fun disconnect() {

    }

    fun queryChannel() {

    }

    fun queryChannels(query: ChannelsQuery = ChannelsQuery()): Call<List<ChatChannel>> {
        return api.queryChannels(query)
    }
}