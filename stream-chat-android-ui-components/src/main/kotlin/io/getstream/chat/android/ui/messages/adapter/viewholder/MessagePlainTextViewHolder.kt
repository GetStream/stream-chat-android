package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.databinding.StreamItemMessagePlainTextBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder

public class MessagePlainTextViewHolder(
    parent: ViewGroup,
    internal val binding: StreamItemMessagePlainTextBinding =
        StreamItemMessagePlainTextBinding.inflate(
            LayoutInflater.from(
                parent.context
            ),
            parent,
            false
        )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {

    override fun bindData(data: MessageListItem.MessageItem) {
        constraintView(data.isMine, binding.messageText, binding.root)
        binding.messageText.text = data.message.text
    }
}
