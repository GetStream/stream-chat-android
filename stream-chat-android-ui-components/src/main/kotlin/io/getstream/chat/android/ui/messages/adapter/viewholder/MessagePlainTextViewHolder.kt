package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.databinding.StreamUiItemMessagePlainTextBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.MessageListListenerContainer

public class MessagePlainTextViewHolder(
    parent: ViewGroup,
    currentUser: User,
    private val listenerContainer: MessageListListenerContainer?,
    internal val binding: StreamUiItemMessagePlainTextBinding =
        StreamUiItemMessagePlainTextBinding.inflate(
            parent.inflater,
            parent,
            false
        )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(currentUser, binding.root) {

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        binding.messageText.text = data.message.text
        binding.messageContainer.setOnLongClickListener {
            listenerContainer?.messageLongClickListener?.onMessageLongClick(data.message)
            true
        }
        binding.reactionsView.setReactionClickListener {
            listenerContainer?.reactionViewClickListener?.onReactionViewClick(data.message)
        }
    }
}
