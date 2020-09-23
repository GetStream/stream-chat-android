package com.getstream.sdk.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseMessageListItemViewHolder<T : MessageListItem>(
    resId: Int,
    parent: ViewGroup
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(resId, parent, false)
) {

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
