package io.getstream.chat.android.ui.channel.list.adapter.viewholder.internal

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater

internal class ChannelListLoadingMoreViewHolder(
    parent: ViewGroup,
    style: ChannelListViewStyle,
) : BaseChannelListItemViewHolder(parent.streamThemeInflater.inflate(style.loadingMoreView, parent, false)) {

    override fun bind(channel: Channel, diff: ChannelListPayloadDiff): Unit = Unit
}
