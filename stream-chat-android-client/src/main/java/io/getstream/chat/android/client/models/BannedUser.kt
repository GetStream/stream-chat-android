package io.getstream.chat.android.client.models

import java.util.Date

public data class BannedUser(
    val user: User,
    val bannedBy: User?,
    val channel: Channel?,
    val createdAt: Date?,
    val expires: Date?,
    val shadow: Boolean,
    val reason: String?,
)
