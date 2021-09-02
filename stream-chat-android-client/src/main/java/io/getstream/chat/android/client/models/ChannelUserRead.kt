package io.getstream.chat.android.client.models

import java.util.Date

public data class ChannelUserRead(
    override var user: User,
    var lastRead: Date? = null,
    var unreadMessages: Int = 0
) : UserEntity
