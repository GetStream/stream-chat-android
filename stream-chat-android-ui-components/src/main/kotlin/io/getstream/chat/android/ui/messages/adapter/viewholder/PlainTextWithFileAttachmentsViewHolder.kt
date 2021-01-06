package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.databinding.StreamUiItemMessagePlainTextWithFileAttachmentsBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.messages.adapter.view.AttachmentClickListener
import io.getstream.chat.android.ui.messages.adapter.view.AttachmentLongClickListener
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.utils.extensions.hasLink

public class PlainTextWithFileAttachmentsViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    listenerContainer: MessageListListenerContainer?,
    internal val binding: StreamUiItemMessagePlainTextWithFileAttachmentsBinding =
        StreamUiItemMessagePlainTextWithFileAttachmentsBinding.inflate(
            parent.inflater,
            parent,
            false
        )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    init {
        listenerContainer?.let { listeners ->
            binding.run {
                root.setOnClickListener {
                    listeners.messageClickListener.onMessageClick(data.message)
                }
                reactionsView.setReactionClickListener {
                    listeners.reactionViewClickListener.onReactionViewClick(data.message)
                }
                fileAttachmentsView.attachmentClickListener = AttachmentClickListener {
                    listeners.attachmentClickListener.onAttachmentClick(data.message, it)
                }
                threadRepliesFootnote.root.setOnClickListener {
                    listeners.threadClickListener.onThreadClick(data.message)
                }

                root.setOnLongClickListener {
                    listeners.messageLongClickListener.onMessageLongClick(data.message)
                    true
                }
                fileAttachmentsView.attachmentLongClickListener = AttachmentLongClickListener {
                    listeners.messageLongClickListener.onMessageLongClick(data.message)
                }
            }
        }
    }

    public override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        binding.messageText.text = data.message.text
        binding.fileAttachmentsView.setAttachments(
            data.message
                .attachments
                .filter { it.hasLink().not() }
        )
    }
}
