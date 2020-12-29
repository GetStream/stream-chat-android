package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.databinding.StreamUiItemMessagePlainTextWithFileAttachmentsBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.utils.extensions.hasLink

public class PlainTextWithFileAttachmentsViewHolder(
    parent: ViewGroup,
    currentUser: User,
    private val listenerContainer: MessageListListenerContainer?,
    internal val binding: StreamUiItemMessagePlainTextWithFileAttachmentsBinding = StreamUiItemMessagePlainTextWithFileAttachmentsBinding.inflate(
        parent.inflater,
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(currentUser, binding.root) {

    public override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        binding.messageText.text = data.message.text
        binding.fileAttachmentsView.setAttachments(
            data.message.attachments.filter { attachment -> attachment.hasLink().not() }
        ) { attachment -> listenerContainer?.attachmentClickListener?.onAttachmentClick(data.message, attachment) }
        binding.fileAttachmentsView.setOnLongClickListener {
            listenerContainer?.messageLongClickListener?.onMessageLongClick(data.message)
            true
        }
        binding.reactionsView.setReactionClickListener {
            listenerContainer?.reactionViewClickListener?.onReactionViewClick(data.message)
        }
    }
}
