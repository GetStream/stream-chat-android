package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.*

data class ChannelResponse(
    val channel: Channel,
    val messages: List<Message>? = null,
    var members: List<Member>? = null,
    var watchers: List<Watcher>? = null,
    var read: List<ChannelUserRead>? = null,
    val watcher_count: Int = 0
)