package io.getstream.chat.android.client.models

import java.util.Date

public data class ChannelMute(
    val user: User,
    val channel: Channel,
    val createdAt: Date,
    var updatedAt: Date,
    val expires: Date?,
)
