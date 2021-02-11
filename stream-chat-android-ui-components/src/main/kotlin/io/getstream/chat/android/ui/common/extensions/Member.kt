package io.getstream.chat.android.ui.common.extensions

import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.ui.common.extensions.internal.isCurrentUser

public val Member.isOwnerOrAdmin: Boolean
    get() = role == "owner" || role == "admin"

public fun List<Member>?.isCurrentUserOwnerOrAdmin(): Boolean {
    return if (isNullOrEmpty()) {
        false
    } else {
        firstOrNull { it.user.isCurrentUser() }?.isOwnerOrAdmin ?: false
    }
}
