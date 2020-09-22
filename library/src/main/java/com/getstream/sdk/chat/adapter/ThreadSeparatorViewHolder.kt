package com.getstream.sdk.chat.adapter

import android.view.ViewGroup
import android.widget.TextView
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.ThreadSeparatorItem
import com.getstream.sdk.chat.view.MessageListView.BubbleHelper
import io.getstream.chat.android.client.models.Channel

class ThreadSeparatorViewHolder(resId: Int, viewGroup: ViewGroup) :
    BaseMessageListItemViewHolder<ThreadSeparatorItem>(resId, viewGroup) {

    private val tv_text: TextView = itemView.findViewById(R.id.tv_text)

    override fun bind(
        channel: Channel,
        messageListItem: ThreadSeparatorItem,
        bubbleHelper: BubbleHelper,
        position: Int
    ) {
        /* Empty */
    }
}
