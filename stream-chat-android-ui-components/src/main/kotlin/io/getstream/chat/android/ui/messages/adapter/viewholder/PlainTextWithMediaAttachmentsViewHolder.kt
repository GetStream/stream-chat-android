package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.databinding.StreamUiItemMessagePlainTextWithMediaAttachmentsBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.ListenerContainer
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.utils.extensions.hasLink

public class PlainTextWithMediaAttachmentsViewHolder(
    parent: ViewGroup,
    currentUser: User,
    private val listenerContainer: ListenerContainer?,
    internal val binding: StreamUiItemMessagePlainTextWithMediaAttachmentsBinding = StreamUiItemMessagePlainTextWithMediaAttachmentsBinding.inflate(
        parent.inflater,
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(currentUser, binding.root) {
    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        binding.messageText.text = data.message.text
        val mediaAttachments = data.message.attachments.filter { attachment -> attachment.hasLink().not() }
        binding.mediaAttachmentsGroupView.showAttachments(*mediaAttachments.toTypedArray())
        binding.backgroundView.setOnLongClickListener {
            listenerContainer?.messageLongClickListener?.onMessageLongClick(data.message)
            true
        }
        binding.reactionsView.setReactionClickListener {
            listenerContainer?.reactionViewClickListener?.onReactionViewClick(data.message)
        }
    }
}
