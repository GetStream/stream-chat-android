package io.getstream.chat.android.compose.ui.util

import android.content.Context
import com.getstream.sdk.chat.viewmodel.messages.getCreatedAtOrThrow
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.R
import java.util.Date

/**
 * Returns channel's last regular or system message if exists.
 * Deleted and silent messages, as well as messages from shadow-banned users, are not taken into account.
 *
 * @return Last message from the channel or null if it doesn't exist.
 */
public fun Channel.getLastMessage(currentUser: User?): Message? =
    messages.asSequence()
        .filter { it.createdAt != null || it.createdLocallyAt != null }
        .filter { it.deletedAt == null }
        .filter { !it.silent }
        .filter { it.user.id == currentUser?.id || !it.shadowed }
        .filter { it.isRegular() || it.isSystem() }
        .maxByOrNull { it.getCreatedAtOrThrow() }

/**
 * Filters the read status of each person other than the target user.
 *
 * @param userToIgnore The user whose message it is.
 *
 * @return List of [Date] values that represent a read status for each other user in the channel.
 */
public fun Channel.getReadStatuses(userToIgnore: User?): List<Date> {
    return read.filter { it.user.id != userToIgnore?.id }
        .mapNotNull { it.lastRead }
}

/**
 * Checks if the channel is distinct.
 *
 * A distinct channel is a channel created without ID based on members. Internally
 * the server creates a CID which starts with "!members" prefix and is unique for
 * this particular group of users.
 *
 * @return True if the channel is distinct.
 */
public fun Channel.isDistinct(): Boolean = cid.contains("!members")

/**
 * Checks if the channel is a direct conversation between the current user and some
 * other user.
 *
 * A one-to-one chat is basically a corner case of a distinct channel with only 2 members.
 *
 * @param currentUser The currently logged in user.
 * @return True if the channel is a one-to-one conversation.
 */
public fun Channel.isOneToOne(currentUser: User?): Boolean {
    return isDistinct() &&
        members.size == 2 &&
        members.any { it.user.id == currentUser?.id }
}

/**
 * Returns a string describing the member status of the channel: either a member count for a group channel
 * or the last seen text for a direct one-to-one conversation with the current user.
 *
 * @param context The context to load string resources.
 * @param currentUser The currently logged in user.
 * @return The text that represent the member status of the channel.
 */
public fun Channel.getMembersStatusText(context: Context, currentUser: User?): String {
    return when {
        isOneToOne(currentUser) -> members.first { it.user.id != currentUser?.id }
            .user
            .getLastSeenText(context)
        else -> {
            val memberCountString = context.resources.getQuantityString(
                R.plurals.stream_compose_member_count,
                memberCount,
                memberCount
            )

            return if (watcherCount > 0) {
                context.getString(
                    R.string.stream_compose_member_count_online,
                    memberCountString,
                    watcherCount
                )
            } else {
                memberCountString
            }
        }
    }
}
