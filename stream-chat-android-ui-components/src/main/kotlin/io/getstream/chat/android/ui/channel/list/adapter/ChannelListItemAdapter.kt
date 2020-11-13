package io.getstream.chat.android.ui.channel.list.adapter

import android.view.ViewGroup
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelItemDiff
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelViewHolderFactory
import io.getstream.chat.android.ui.utils.extensions.cast
import io.getstream.chat.android.ui.utils.extensions.firstOrDefault

public class ChannelListItemAdapter : BaseChannelListItemAdapter() {

    public companion object {
        public val DEFAULT_DIFF: ChannelItemDiff = ChannelItemDiff()
    }

    override var userClickListener: ChannelListView.UserClickListener? = null

    override var channelClickListener: ChannelListView.ChannelClickListener? = null

    override var channelLongClickListener: ChannelListView.ChannelClickListener? = null

    public var viewHolderFactory: BaseChannelViewHolderFactory<out BaseChannelListItemViewHolder>? = null

    public override var style: ChannelListViewStyle? = null

    private fun getChannelItemLayout(): Int =
        style?.channelPreviewLayout ?: R.layout.stream_channel_list_item_view

    override fun getItemCount(): Int = channels.count()

    override fun getItemViewType(position: Int): Int =
        viewHolderFactory?.getItemViewType(channels[position])
            ?: throw IllegalStateException("Please provide a view holder factory")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseChannelListItemViewHolder =
        viewHolderFactory?.createChannelViewHolder(getChannelItemLayout(), parent, viewType)?.also { vh ->
            vh.style = style
            vh.userClickListener = userClickListener
            vh.channelLongClickListener = channelLongClickListener
            vh.channelClickListener = channelClickListener
        } ?: throw IllegalStateException("Please provide a view holder factory")

    override fun onBindViewHolder(holder: BaseChannelListItemViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.bind(channels[position], position, payloads.firstOrDefault(DEFAULT_DIFF).cast())
    }

    override fun onBindViewHolder(holder: BaseChannelListItemViewHolder, position: Int) {
        holder.bind(channels[position], position)
    }
}
