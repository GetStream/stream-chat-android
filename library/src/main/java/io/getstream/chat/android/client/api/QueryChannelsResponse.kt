package io.getstream.chat.android.client.api

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelState


data class QueryChannelsResponse(
    @SerializedName("channels")
    var channelStates: List<ChannelState> = emptyList()
) {

    fun getChannels(): List<Channel> {
        return channelStates.map { state -> state.channel }
    }

}