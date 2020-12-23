package io.getstream.chat.android.ui.channel.list.adapter

import io.getstream.chat.android.client.models.Channel

public sealed class ChannelListItem {
    public data class ChannelItem(val channel: Channel) : ChannelListItem()
    public object LoadingMoreItem : ChannelListItem()
}
