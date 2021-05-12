package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiItemErrorMessageBinding
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff

internal class ErrorMessageViewHolder(
    parent: ViewGroup,
    private val style: MessageListItemStyle,
    internal val binding: StreamUiItemErrorMessageBinding = StreamUiItemErrorMessageBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false
    ),
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        binding.messageTextView.text = data.message.text
        style.textStyleErrorMessage.apply(binding.messageTextView)
    }
}
