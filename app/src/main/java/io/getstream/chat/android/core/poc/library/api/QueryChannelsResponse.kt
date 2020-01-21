package io.getstream.chat.android.core.poc.library.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.Channel
import io.getstream.chat.android.core.poc.library.ChannelState


class QueryChannelsResponse {

    @SerializedName("channels")
    
    var channelStates = emptyList<ChannelState>()

    fun getChannels(): List<Channel> {
        return channelStates.map { state -> state.channel }
    }

}