package io.getstream.chat.android.compose.viewmodel.channels.usecases

import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelMute

internal class CreateChannelItems {
    /**
     * Creates a list of [ChannelItemState] that represents channel items we show in the list of channels.
     *
     * @param channels The channels to show.
     * @param channelMutes The list of channels muted for the current user.
     *
     */
    operator fun invoke(
        channels: List<Channel>,
        channelMutes: List<ChannelMute>,
    ): List<ItemState.ChannelItemState> {
        val mutedChannelIds = channelMutes.map { channelMute -> channelMute.channel.cid }.toSet()
        return channels.map { ItemState.ChannelItemState(it, it.cid in mutedChannelIds) }
    }
}