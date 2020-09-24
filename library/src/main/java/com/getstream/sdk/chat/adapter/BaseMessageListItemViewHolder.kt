package com.getstream.sdk.chat.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseMessageListItemViewHolder<T : MessageListItem>(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    /**
     * Workaround to allow a downcast of the MessageListItem to T
     */
    fun bindListItem(
        messageListItem: MessageListItem,
        position: Int
    ) {
        @Suppress("UNCHECKED_CAST")
        bind(messageListItem as T, position)
    }

    protected abstract fun bind(
        messageListItem: T,
        position: Int
    )

    protected val context: Context
        get() = itemView.context
}
