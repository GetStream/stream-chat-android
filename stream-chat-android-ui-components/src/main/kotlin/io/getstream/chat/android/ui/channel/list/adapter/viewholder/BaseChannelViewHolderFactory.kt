package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.view.View
import androidx.annotation.LayoutRes

public abstract class BaseChannelViewHolderFactory<out ViewHolderT : BaseChannelListItemViewHolder>
@JvmOverloads constructor(@LayoutRes public open var viewHolderLayout: Int? = null) {
    public abstract fun createChannelViewHolder(itemView: View): ViewHolderT
}
