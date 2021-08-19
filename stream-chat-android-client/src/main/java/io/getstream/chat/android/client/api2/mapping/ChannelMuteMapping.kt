package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelMuteDto
import io.getstream.chat.android.client.models.ChannelMute

internal fun DownstreamChannelMuteDto.toDomain(): ChannelMute =
    ChannelMute(
        user = user.toDomain(),
        channel = channel.toDomain(),
        createdAt = created_at,
        updatedAt = updated_at,
        expires = expires,
    )
