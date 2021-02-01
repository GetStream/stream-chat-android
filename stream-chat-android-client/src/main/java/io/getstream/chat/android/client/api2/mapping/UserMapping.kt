package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.UserDto
import io.getstream.chat.android.client.models.User

internal fun User.toDto(): UserDto =
    UserDto(
        id = id,
        role = role,
        invisible = invisible,
        banned = banned,
    )

internal fun UserDto.toDomain(): User =
    User(
        id = id,
        role = role,
        invisible = invisible,
        banned = banned,
    )
