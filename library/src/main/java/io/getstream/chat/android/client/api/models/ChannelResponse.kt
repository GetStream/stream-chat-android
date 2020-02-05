package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.Channel


data class ChannelResponse(
    val channel: Channel,
    val duration: String = ""
)
