package io.getstream.chat.android.compose.util.extensions

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities

internal fun Channel.isPollEnabled(): Boolean =
    this.config.pollsEnabled && ownCapabilities.contains(ChannelCapabilities.SEND_POLL)