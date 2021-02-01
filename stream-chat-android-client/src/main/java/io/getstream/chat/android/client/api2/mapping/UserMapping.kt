package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.UserDto
import io.getstream.chat.android.client.models.User

internal fun User.toDto(): UserDto =
    UserDto(
        banned = banned,
        id = id,
        invisible = invisible,
        role = role,
        extraData = extraData,
    )

internal fun UserDto.toDomain(): User =
    User(
        banned = banned,
        id = id,
        invisible = invisible,
        role = role,
        extraData = extraData.toMutableMap(),
    )
