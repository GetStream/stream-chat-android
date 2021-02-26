package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelUserRead
import io.getstream.chat.android.client.models.ChannelUserRead

internal fun DownstreamChannelUserRead.toDomain(): ChannelUserRead =
    ChannelUserRead(
        user = user.toDomain(),
        lastRead = last_read,
        unreadMessages = unread_messages,
    )
