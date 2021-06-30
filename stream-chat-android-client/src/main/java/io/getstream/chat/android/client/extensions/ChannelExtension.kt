package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.EXTRA_DATA_MUTED
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User

public fun Channel.isAnonymousChannel(): Boolean = id.isAnonymousChannelId()

public var Channel.isMuted: Boolean
    get() = extraData[EXTRA_DATA_MUTED] as Boolean? ?: false
    set(value) {
        extraData[EXTRA_DATA_MUTED] = value
    }

public val Channel.lastMessage: Message?
    get() = messages.lastOrNull()

public fun Channel.users(): List<User> {
    return members.map(Member::user) +
        read.map(ChannelUserRead::user) +
        createdBy +
        messages.flatMap { it.users() }
}
