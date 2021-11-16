package io.getstream.chat.android.compose.ui.util

import com.getstream.sdk.chat.viewmodel.messages.getCreatedAtOrThrow
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.viewmodel.channel.ChannelListViewModel
import java.util.Date

private const val EXTRA_CHANNEL_MUTED: String = "isMuted"

/**
 * Allows storing additional information if the channel is muted for the current user.
 *
 * @see [ChannelListViewModel.enrichMutedChannels]
 */
public var Channel.isMuted: Boolean
    get() = extraData[EXTRA_CHANNEL_MUTED] as Boolean? ?: false
    set(value) {
        extraData[EXTRA_CHANNEL_MUTED] = value
    }

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
