package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.databinding.StreamUiItemMessagePlainTextWithMediaAttachmentsBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.messages.adapter.view.AttachmentClickListener
import io.getstream.chat.android.ui.messages.adapter.view.AttachmentLongClickListener
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator
import io.getstream.chat.android.ui.utils.extensions.hasLink

public class PlainTextWithMediaAttachmentsViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    listenerContainer: MessageListListenerContainer?,
    internal val binding: StreamUiItemMessagePlainTextWithMediaAttachmentsBinding =
        StreamUiItemMessagePlainTextWithMediaAttachmentsBinding.inflate(
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
                mediaAttachmentsGroupView.attachmentClickListener = AttachmentClickListener {
                    listeners.attachmentClickListener.onAttachmentClick(data.message, it)
                }
                reactionsView.setReactionClickListener {
                    listeners.reactionViewClickListener.onReactionViewClick(data.message)
                }
                threadRepliesFootnote.root.setOnClickListener {
                    listeners.threadClickListener.onThreadClick(data.message)
                }

                root.setOnLongClickListener {
                    listeners.messageLongClickListener.onMessageLongClick(data.message)
                    true
                }
                mediaAttachmentsGroupView.attachmentLongClickListener = AttachmentLongClickListener {
                    listeners.messageLongClickListener.onMessageLongClick(data.message)
                }
            }
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        binding.messageText.text = data.message.text
        binding.mediaAttachmentsGroupView.showAttachments(
            data.message
                .attachments
                .filter { attachment -> attachment.hasLink().not() }
        )
    }
}
