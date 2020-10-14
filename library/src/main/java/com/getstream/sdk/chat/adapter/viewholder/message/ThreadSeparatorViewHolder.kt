package com.getstream.sdk.chat.adapter.viewholder.message

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.inflater
import com.getstream.sdk.chat.databinding.StreamItemThreadSeparatorBinding
import io.getstream.chat.android.livedata.utils.MessageListItem

class ThreadSeparatorViewHolder(
    parent: ViewGroup,
    private val binding: StreamItemThreadSeparatorBinding =
        StreamItemThreadSeparatorBinding.inflate(parent.inflater, parent, false)
) : BaseMessageListItemViewHolder<MessageListItem.ThreadSeparatorItem>(binding.root) {

    override fun bind(messageListItem: MessageListItem.ThreadSeparatorItem) {
        /* Empty */
    }
}
