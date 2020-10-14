package com.getstream.sdk.chat.adapter.viewholder.message

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.livedata.utils.MessageListItem

abstract class BaseMessageListItemViewHolder<T : MessageListItem>(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    /**
     * Workaround to allow a downcast of the MessageListItem to T
     */
    @Suppress("UNCHECKED_CAST")
    fun bindListItem(messageListItem: MessageListItem) = bind(messageListItem as T)

    protected abstract fun bind(messageListItem: T)

    protected val context: Context
        get() = itemView.context
}
