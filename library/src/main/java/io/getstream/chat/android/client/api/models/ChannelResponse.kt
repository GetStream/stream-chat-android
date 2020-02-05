package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message

data class ChannelResponse(
    val channel: Channel,
    val messages: List<Message>? = null,
    var members: List<Member>? = null,
    var read: List<ChannelUserRead>? = null,
    val watcher_count: Int = 0
)