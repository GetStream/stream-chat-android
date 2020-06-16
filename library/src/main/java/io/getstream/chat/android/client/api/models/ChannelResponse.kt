package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.*
import java.util.*

data class ChannelResponse(
    val channel: Channel,
    val messages: List<Message>? = null,
    var members: List<Member>? = null,
    var watchers: List<Watcher>? = null,
    var read: List<ChannelUserRead>? = null,
    val watcher_count: Int = 0,
    val hidden: Boolean? = null,
    val hide_messages_before: Date? = null
)