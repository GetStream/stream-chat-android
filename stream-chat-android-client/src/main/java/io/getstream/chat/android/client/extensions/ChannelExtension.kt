package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi

public fun Channel.isAnonymousChannel(): Boolean = id.isAnonymousChannelId()

/**
 * Checks if [Channel] is muted for [user].
 *
 * @return True if the channel is muted for [user].
 */
public fun Channel.isMutedFor(user: User): Boolean = user.channelMutes.any { mute -> mute.channel.cid == cid }

/**
 * Returns a list of users that are members of the channel excluding the currently
 * logged in user.
 *
 * @param currentUser The currently logged in user.
 * @return The list of users in the channel without the current user.
 */
@InternalStreamChatApi
public fun Channel.getUsersExcludingCurrent(
    currentUser: User? = ChatClient.instance().getCurrentUser(),
): List<User> {
    val users = members.map { it.user }
    val currentUserId = currentUser?.id
    return if (currentUserId != null) {
        users.filterNot { it.id == currentUserId }
    } else {
        users
    }
}
