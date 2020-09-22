package com.getstream.sdk.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import io.getstream.chat.android.client.models.Channel

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
        channel: Channel,
        messageListItem: MessageListItem,
        bubbleHelper: BubbleHelper,
        position: Int
    ) {
        @Suppress("UNCHECKED_CAST")
        bind(channel, messageListItem as T, bubbleHelper, position)
    }

    protected abstract fun bind(
        channel: Channel,
        messageListItem: T,
        bubbleHelper: BubbleHelper,
        position: Int
    )

    protected val context: Context
        get() = itemView.context
}
