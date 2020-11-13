package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import io.getstream.chat.android.client.models.Channel

public interface BaseChannelViewHolderFactory<ViewHolderT : BaseChannelListItemViewHolder> {
    public companion object {
        public const val DEFAULT_CHANNEL_TYPE: Int = 0
    }

    public fun getItemViewType(channel: Channel): Int = DEFAULT_CHANNEL_TYPE

    public fun createChannelViewHolder(
        @LayoutRes layout: Int,
        parent: ViewGroup,
        viewType: Int
    ): ViewHolderT
}
