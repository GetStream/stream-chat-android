package io.getstream.chat.android.ui.channel.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelDiff
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelViewHolderFactory
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelViewHolderFactory
import io.getstream.chat.android.ui.utils.extensions.cast
import io.getstream.chat.android.ui.utils.extensions.firstOrDefault

public class ChannelListItemAdapter : BaseChannelListItemAdapter() {

    public var viewHolderFactory: BaseChannelViewHolderFactory<BaseChannelListItemViewHolder> =
        ChannelViewHolderFactory()

    public companion object {
        public val DEFAULT_DIFF: ChannelDiff = ChannelDiff()
    }

    /**
     * Returns the layout for the channel items.
     * Its behavior is such that specifying a layout in the [BaseChannelViewHolderFactory] takes precedence.
     * If the layout is omitted, the layout specified in the style is used.
     * If the style layout is omitted, our default layout resource is used.
     *
     * @return the resolved layout resource
     */
    private fun getChannelItemLayout(): Int =
        viewHolderFactory.viewHolderLayout
            ?: style?.channelPreviewLayout
            ?: R.layout.stream_channel_list_item_view

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseChannelListItemViewHolder =
        LayoutInflater.from(parent.context)
            .inflate(getChannelItemLayout(), parent, false)
            .let { viewHolderFactory.createChannelViewHolder(it) }

    override fun onBindViewHolder(holder: BaseChannelListItemViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.bind(
            getItem(position),
            payloads.firstOrDefault(DEFAULT_DIFF).cast(),
            channelClickListener,
            channelLongClickListener,
            deleteClickListener,
            userClickListener,
            style
        )
    }

    override fun onBindViewHolder(holder: BaseChannelListItemViewHolder, position: Int) {
        holder.bind(
            getItem(position),
            null,
            channelClickListener,
            channelLongClickListener,
            deleteClickListener,
            userClickListener,
            style
        )
    }
}
