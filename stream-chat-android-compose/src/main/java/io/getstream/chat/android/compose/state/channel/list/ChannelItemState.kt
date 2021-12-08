package io.getstream.chat.android.compose.state.channel.list

import io.getstream.chat.android.client.models.Channel

/**
 * Represents each channel item we show in the list of channels.
 *
 * @param channel The channel to show.
 * @param isMuted If the channel is muted for the current user.
 */
public data class ChannelItemState(
    val channel: Channel,
    val isMuted: Boolean,
)
