package com.getstream.sdk.chat.adapter

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem.ThreadSeparatorItem
import com.getstream.sdk.chat.databinding.StreamItemThreadSeparatorBinding

class ThreadSeparatorViewHolder(
    parent: ViewGroup,
    private val binding: StreamItemThreadSeparatorBinding =
        StreamItemThreadSeparatorBinding.inflate(parent.inflater, parent, false)
) : BaseMessageListItemViewHolder<ThreadSeparatorItem>(binding.root) {

    override fun bind(
        messageListItem: ThreadSeparatorItem,
        position: Int
    ) {
        /* Empty */
    }
}
