package io.getstream.chat.android.offline.utils

import io.getstream.chat.android.offline.experimental.global.GlobalMutableState

/**
 * Checks the given CID against the CIDs of channels muted for the current user.
 * Returns true for a muted channel, returns false otherwise.
 *
 * @param cid CID of the channel currently being checked.
 */
internal fun isChannelMutedForCurrentUser(cid: String): Boolean =
    GlobalMutableState.getOrCreate().channelMutes.value.any { mutedChannel -> mutedChannel.channel.cid == cid }
