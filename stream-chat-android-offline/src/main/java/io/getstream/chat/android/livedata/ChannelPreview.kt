package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User

public data class ChannelPreview(
    val channel: Channel,
    val typing: List<User> = listOf(),
)
