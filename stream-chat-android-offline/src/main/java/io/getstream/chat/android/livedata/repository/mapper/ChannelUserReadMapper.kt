package io.getstream.chat.android.livedata.repository.mapper

import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.entity.ChannelUserReadEntity

internal fun ChannelUserRead.toEntity(): ChannelUserReadEntity = ChannelUserReadEntity(getUserId(), lastRead)

internal suspend fun ChannelUserReadEntity.toModel(getUser: suspend (userId: String) -> User): ChannelUserRead =
    ChannelUserRead(getUser(userId), lastRead)
