package io.getstream.chat.android.client.api.models

internal data class MuteChannelRequest(
    val channel_cid: String,
    val expiration: Int?,
)
