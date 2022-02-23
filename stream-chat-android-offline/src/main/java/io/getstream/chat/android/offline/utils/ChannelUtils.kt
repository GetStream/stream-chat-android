package io.getstream.chat.android.offline.utils

import io.getstream.chat.android.offline.experimental.global.GlobalMutableState

internal fun isChannelMutedForCurrentUser(cid: String): Boolean =
    GlobalMutableState.getOrCreate().channelMutes.value.any { mutedChannel -> mutedChannel.channel.cid == cid }
