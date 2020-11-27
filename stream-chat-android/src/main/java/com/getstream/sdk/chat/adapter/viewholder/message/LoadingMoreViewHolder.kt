package com.getstream.sdk.chat.adapter.viewholder.message

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.adapter.MessageListItemPayloadDiff
import com.getstream.sdk.chat.adapter.inflater
import com.getstream.sdk.chat.databinding.StreamItemLoadingMoreBinding

internal class LoadingMoreViewHolder(
    parent: ViewGroup,
    binding: StreamItemLoadingMoreBinding = StreamItemLoadingMoreBinding.inflate(
        parent.inflater,
        parent,
        false
    )
) : BaseMessageListItemViewHolder<MessageListItem.LoadingMoreIndicatorItem>(binding.root) {

    override fun bind(messageListItem: MessageListItem.LoadingMoreIndicatorItem, diff: MessageListItemPayloadDiff) =
        Unit
}
