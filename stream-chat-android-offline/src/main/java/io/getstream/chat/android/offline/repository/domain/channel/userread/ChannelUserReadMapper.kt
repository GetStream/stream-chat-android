package io.getstream.chat.android.offline.repository.domain.channel.userread

import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.User

internal fun ChannelUserRead.toEntity(): ChannelUserReadEntity = ChannelUserReadEntity(getUserId(), lastRead, unreadMessages)

internal suspend fun ChannelUserReadEntity.toModel(getUser: suspend (userId: String) -> User): ChannelUserRead =
    ChannelUserRead(getUser(userId), lastRead, unreadMessages)
