package io.getstream.chat.android.client.models

import java.util.Date

public data class Member(
    override var user: User,
    var role: String? = null,
    var createdAt: Date? = null,
    var updatedAt: Date? = null,
    var isInvited: Boolean? = null,
    var inviteAcceptedAt: Date? = null,
    var inviteRejectedAt: Date? = null,
    var shadowBanned: Boolean = false,
) : UserEntity
