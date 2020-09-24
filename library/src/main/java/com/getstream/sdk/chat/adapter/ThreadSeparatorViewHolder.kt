package com.getstream.sdk.chat.adapter

import android.view.ViewGroup
import android.widget.TextView
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.ThreadSeparatorItem

class ThreadSeparatorViewHolder(resId: Int, viewGroup: ViewGroup) :
    BaseMessageListItemViewHolder<ThreadSeparatorItem>(resId, viewGroup) {

    private val tv_text: TextView = itemView.findViewById(R.id.tv_text)

    override fun bind(
        messageListItem: ThreadSeparatorItem,
        position: Int
    ) {
        /* Empty */
    }
}
