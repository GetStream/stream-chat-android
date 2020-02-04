package io.getstream.chat.android.client.rest

import io.getstream.chat.android.client.Channel


data class ChannelResponse(
    val channel: Channel,
    val duration: String = ""
)
