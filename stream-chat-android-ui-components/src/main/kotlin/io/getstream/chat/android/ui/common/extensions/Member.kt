package io.getstream.chat.android.ui.common.extensions

import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.ui.common.extensions.internal.isCurrentUser

/**
 * @return if the member is the owner or an admin of a channel.
 */
public val Member.isOwnerOrAdmin: Boolean
    get() = channelRole == "channel_moderator"

/**
 * @return if the current user is an owner or an admin of a channel.
 */
public fun List<Member>?.isCurrentUserOwnerOrAdmin(): Boolean {
    return if (isNullOrEmpty()) {
        false
    } else {
        firstOrNull { it.user.isCurrentUser() }?.isOwnerOrAdmin ?: false
    }
}
