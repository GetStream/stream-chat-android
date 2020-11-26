package io.getstream.chat.android.ui.messages.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem

public abstract class BaseMessageItemViewHolder<T : MessageListItem>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    public abstract fun bind(data: T)
}
