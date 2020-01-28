package io.getstream.chat.android.core.poc.library.api

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.Channel
import io.getstream.chat.android.core.poc.library.ChannelState


data class QueryChannelsResponse(
    @SerializedName("channels")
    var channelStates: List<ChannelState> = emptyList()
) {

    fun getChannels(): List<Channel> {
        return channelStates.map { state -> state.channel }
    }

}