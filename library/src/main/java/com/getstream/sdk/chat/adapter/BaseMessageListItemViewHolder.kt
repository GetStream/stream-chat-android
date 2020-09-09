package com.getstream.sdk.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import com.getstream.sdk.chat.view.MessageListViewStyle
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
        context: Context,
        channel: Channel,
        messageListItem: MessageListItem,
        style: MessageListViewStyle,
        bubbleHelper: BubbleHelper,
        factory: MessageViewHolderFactory,
        position: Int
    ) {
        @Suppress("UNCHECKED_CAST")
        bind(context, channel, messageListItem as T, style, bubbleHelper, factory, position)
    }

    protected abstract fun bind(
        context: Context,
        channel: Channel,
        messageListItem: T,
        style: MessageListViewStyle,
        bubbleHelper: BubbleHelper,
        factory: MessageViewHolderFactory,
        position: Int
    )
}
