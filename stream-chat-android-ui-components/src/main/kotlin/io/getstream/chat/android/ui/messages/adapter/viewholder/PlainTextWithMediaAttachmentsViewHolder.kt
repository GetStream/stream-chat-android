package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.databinding.StreamUiItemMessagePlainTextWithMediaAttachmentsBinding
import io.getstream.chat.android.ui.messages.adapter.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.messages.adapter.view.AttachmentClickListener
import io.getstream.chat.android.ui.messages.adapter.view.AttachmentLongClickListener
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.utils.LongClickFriendlyLinkMovementMethod
import io.getstream.chat.android.ui.utils.extensions.hasLink

internal class PlainTextWithMediaAttachmentsViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    listeners: MessageListListenerContainer,
    internal val binding: StreamUiItemMessagePlainTextWithMediaAttachmentsBinding =
        StreamUiItemMessagePlainTextWithMediaAttachmentsBinding.inflate(
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
            mediaAttachmentsGroupView.attachmentClickListener = AttachmentClickListener {
                listeners.attachmentClickListener.onAttachmentClick(data.message, it)
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
            mediaAttachmentsGroupView.attachmentLongClickListener = AttachmentLongClickListener {
                listeners.messageLongClickListener.onMessageLongClick(data.message)
            }
            linkAttachmentView.apply {
                setLinkPreviewClickListener { url ->
                    listeners.linkClickListener.onLinkClick(url)
                }
                setLongClickTarget(root)
            }

            LongClickFriendlyLinkMovementMethod.set(
                textView = messageText,
                longClickTarget = root,
                onLinkClicked = { url -> listeners.linkClickListener.onLinkClick(url) }
            )
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        binding.messageText.text = data.message.text
        binding.mediaAttachmentsGroupView.showAttachments(
            data.message
                .attachments
                .filter { attachment -> attachment.hasLink().not() }
        )
    }
}
