package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto
import io.getstream.chat.android.client.models.User

internal fun User.toDto(): UpstreamUserDto =
    UpstreamUserDto(
        banned = banned,
        id = id,
        invisible = invisible,
        role = role,
        extraData = extraData,
    )

internal fun DownstreamUserDto.toDomain(): User =
    User(
        banned = banned,
        id = id,
        invisible = invisible,
        role = role,
        extraData = extraData.toMutableMap(),
    )
