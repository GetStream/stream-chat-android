package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.databinding.StreamUiItemMessagePlainTextBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator

public class MessagePlainTextViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    listenerContainer: MessageListListenerContainer?,
    internal val binding: StreamUiItemMessagePlainTextBinding =
        StreamUiItemMessagePlainTextBinding.inflate(
            parent.inflater,
            parent,
            false
        )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    init {
        listenerContainer?.let { listeners ->
            binding.run {
                reactionsView.setReactionClickListener {
                    listeners.reactionViewClickListener.onReactionViewClick(data.message)
                }
                messageContainer.setOnLongClickListener {
                    listeners.messageLongClickListener.onMessageLongClick(data.message)
                    true
                }
            }
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        binding.messageText.text = data.message.text
    }
}
