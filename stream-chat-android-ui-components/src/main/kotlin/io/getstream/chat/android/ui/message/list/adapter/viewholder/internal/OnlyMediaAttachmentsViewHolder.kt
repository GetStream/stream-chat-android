package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageMediaAttachmentsBinding
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.internal.MessageListItemViewTypeMapper.isMedia
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.MediaAttachmentsGroupView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator

internal class OnlyMediaAttachmentsViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val listeners: MessageListListenerContainer,
    private val attachmentViewFactory: AttachmentViewFactory,
    internal val binding: StreamUiItemMessageMediaAttachmentsBinding =
        StreamUiItemMessageMediaAttachmentsBinding.inflate(
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
            reactionsView.setReactionClickListener {
                listeners.reactionViewClickListener.onReactionViewClick(data.message)
            }
            footnote.setOnThreadClickListener {
                listeners.threadClickListener.onThreadClick(data.message)
            }

            root.setOnLongClickListener {
                listeners.messageLongClickListener.onMessageLongClick(data.message)
                true
            }
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        if (data.message.attachments.isMedia()) {
            val attachmentView = attachmentViewFactory.createAttachmentsView(data.message.attachments, context)
            binding.attachmentsContainer.removeAllViews()
            binding.attachmentsContainer.addView(attachmentView)
            (attachmentView as? MediaAttachmentsGroupView)?.also { mediaAttachmentsGroupView ->
                mediaAttachmentsGroupView.attachmentLongClickListener = AttachmentLongClickListener {
                    listeners.messageLongClickListener.onMessageLongClick(data.message)
                }
                mediaAttachmentsGroupView.attachmentClickListener = AttachmentClickListener {
                    listeners.attachmentClickListener.onAttachmentClick(data.message, it)
                }
                mediaAttachmentsGroupView.showAttachments(data.message.attachments)
                mediaAttachmentsGroupView.setupBackground(data)
            }
        }
    }
}
