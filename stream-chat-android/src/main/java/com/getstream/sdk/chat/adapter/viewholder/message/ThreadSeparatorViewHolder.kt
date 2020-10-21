package com.getstream.sdk.chat.adapter.viewholder.message

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem.ThreadSeparatorItem
import com.getstream.sdk.chat.adapter.inflater
import com.getstream.sdk.chat.databinding.StreamItemThreadSeparatorBinding

internal class ThreadSeparatorViewHolder(
    parent: ViewGroup,
    private val binding: StreamItemThreadSeparatorBinding =
        StreamItemThreadSeparatorBinding.inflate(parent.inflater, parent, false)
) : BaseMessageListItemViewHolder<ThreadSeparatorItem>(binding.root) {

    override fun bind(messageListItem: ThreadSeparatorItem) {
        /* Empty */
    }
}
