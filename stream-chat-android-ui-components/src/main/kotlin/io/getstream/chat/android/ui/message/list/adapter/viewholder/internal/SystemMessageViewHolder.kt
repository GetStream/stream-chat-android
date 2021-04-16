package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.databinding.StreamUiItemSystemMessageBinding
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff

internal class SystemMessageViewHolder(
    parent: ViewGroup,
    private val style: MessageListItemStyle,
    internal val binding: StreamUiItemSystemMessageBinding = StreamUiItemSystemMessageBinding.inflate(
        parent.inflater,
        parent,
        false
    ),
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        binding.messageTextView.text = data.message.text
        style.textStyleSystemMessage.apply(binding.messageTextView)
    }
}
