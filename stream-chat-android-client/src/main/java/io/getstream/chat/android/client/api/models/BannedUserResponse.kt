package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.BannedUser
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import java.util.Date

internal data class BannedUserResponse(
    val user: User,
    val bannedBy: User?,
    val channel: Channel?,
    val createdAt: Date?,
    val expires: Date?,
    val shadow: Boolean,
    val reason: String?,
)

internal fun BannedUserResponse.toDomain(): BannedUser {
    return BannedUser(
        user = user,
        bannedBy = bannedBy,
        channel = channel,
        createdAt = createdAt,
        expires = expires,
        shadow = shadow,
        reason = reason,
    )
}
