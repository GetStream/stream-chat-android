package com.getstream.sdk.chat.adapter

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem.ThreadSeparatorItem
import com.getstream.sdk.chat.databinding.StreamItemThreadSeparatorBinding

class ThreadSeparatorViewHolder(
    private val binding: StreamItemThreadSeparatorBinding
) : BaseMessageListItemViewHolder<ThreadSeparatorItem>(binding.root) {

    companion object {
        fun binding(parent: ViewGroup): StreamItemThreadSeparatorBinding {
            return StreamItemThreadSeparatorBinding.inflate(parent.inflater, parent, false)
        }
    }

    override fun bind(
        messageListItem: ThreadSeparatorItem,
        position: Int
    ) {
        /* Empty */
    }
}
