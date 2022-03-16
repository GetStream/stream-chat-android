package io.getstream.chat.android.offline.internal.utils

import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState

/**
 * Checks the given CID against the CIDs of channels muted for the current user.
 * Returns true for a muted channel, returns false otherwise.
 *
 * @param cid CID of the channel currently being checked.
 */
internal fun isChannelMutedForCurrentUser(cid: String): Boolean =
    GlobalMutableState.getOrCreate().channelMutes.value.any { mutedChannel -> mutedChannel.channel.cid == cid }

/**
 * Generates the channel id based on the member ids if provided [channelId] is empty.
 * Member-based id should only be used for creating distinct channels.
 * Created member-based id might differ from the one created by the server if the channel already exists
 * and was created with different members order.
 *
 * @param channelId The channel id. ie 123.
 * @param memberIds The list of members' ids.
 *
 * @return [channelId] if not blank or new, member-based id.
 */
internal fun generateChannelIdIfNeeded(channelId: String, memberIds: List<String>): String {
    return channelId.ifBlank {
        memberIds.joinToString(prefix = "!members-")
    }
}
