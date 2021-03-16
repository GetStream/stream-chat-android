package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.response.BannedUserResponse
import io.getstream.chat.android.client.models.BannedUser

internal fun BannedUserResponse.toDomain(): BannedUser {
    return BannedUser(
        user = user.toDomain(),
        bannedBy = banned_by?.toDomain(),
        channel = channel?.toDomain(),
        createdAt = created_at,
        expires = expires,
        shadow = shadow,
        reason = reason,
    )
}
