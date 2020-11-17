package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.View
import androidx.annotation.LayoutRes
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle

/**
 * The basic blueprint for a channel view holder.
 *
 * @param ViewHolderT represents the type of [BaseChannelListItemViewHolder] expected.
 * @property viewHolderLayout an override point for specifying which layout to use for each view holder
 * @property channelClickListener a listener for channel item clicks
 * @property channelClickListener a listener for long channel item clicks
 * @property channelLongClickListener a listener for user clicks
 * @property style the xml style parsed as an object
 */
public abstract class BaseChannelViewHolderFactory<out ViewHolderT : BaseChannelListItemViewHolder>
@JvmOverloads constructor(
    @LayoutRes public open var viewHolderLayout: Int? = null,
    public open var channelClickListener: ChannelListView.ChannelClickListener? = null,
    public open var channelLongClickListener: ChannelListView.ChannelClickListener? = null,
    public open var userClickListener: ChannelListView.UserClickListener? = null,
    public open var style: ChannelListViewStyle? = null,
) {
    /**
     * Provides all necessary constructor parameters for creating an instance of [BaseChannelListItemViewHolder]
     * Provides default values for click listeners and style, but the values are able to be
     * customized at the time of invocation.
     *
     * @param itemView the inflated view for the [BaseChannelListItemViewHolder]
     * @param channelClickListener invoked when the channel item is clicked
     * @param channelLongClickListener invoked when the channel item is long-clicked
     * @param userClickListener invoked when the avatar is clicked
     * @param style the style specified via xml attributes
     * @return
     */
    public abstract fun createChannelViewHolder(itemView: View): ViewHolderT
}
