package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageFileAttachmentsBinding
import io.getstream.chat.android.ui.messages.adapter.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.messages.adapter.view.AttachmentClickListener
import io.getstream.chat.android.ui.messages.adapter.view.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.messages.adapter.view.AttachmentLongClickListener
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator

internal class OnlyFileAttachmentsViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    listeners: MessageListListenerContainer,
    internal val binding: StreamUiItemMessageFileAttachmentsBinding =
        StreamUiItemMessageFileAttachmentsBinding.inflate(
            parent.inflater,
            parent,
            false
        ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    init {
        binding.run {
            root.setOnClickListener {
                listeners.messageClickListener.onMessageClick(data.message)
            }
            footnote.setOnThreadClickListener {
                listeners.threadClickListener.onThreadClick(data.message)
            }
            reactionsView.setReactionClickListener {
                listeners.reactionViewClickListener.onReactionViewClick(data.message)
            }
            fileAttachmentsView.attachmentClickListener = AttachmentClickListener {
                listeners.attachmentClickListener.onAttachmentClick(data.message, it)
            }
            fileAttachmentsView.attachmentDownloadClickListener = AttachmentDownloadClickListener {
                listeners.attachmentDownloadClickListener.onAttachmentDownloadClick(it)
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

    override fun bindData(data: MessageListItem.MessageItem, isThread: Boolean, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, isThread, diff)

        binding.fileAttachmentsView.setAttachments(data.message.attachments)
    }
}
