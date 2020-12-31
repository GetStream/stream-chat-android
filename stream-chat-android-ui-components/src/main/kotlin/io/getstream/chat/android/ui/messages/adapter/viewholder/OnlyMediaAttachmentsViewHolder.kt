package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageMediaAttachmentsBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewTypeMapper.isMedia
import io.getstream.chat.android.ui.messages.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.Decorator

public class OnlyMediaAttachmentsViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val listenerContainer: MessageListListenerContainer?,
    internal val binding: StreamUiItemMessageMediaAttachmentsBinding = StreamUiItemMessageMediaAttachmentsBinding.inflate(
        parent.inflater,
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        listenerContainer?.let { listeners ->
            binding.run {
                mediaAttachmentsGroupView.listener =
                    { attachment -> listeners.attachmentClickListener.onAttachmentClick(data.message, attachment) }

                mediaAttachmentsGroupView.setOnLongClickListener {
                    listeners.messageLongClickListener.onMessageLongClick(data.message)
                    true
                }

                reactionsView.setReactionClickListener {
                    listeners.reactionViewClickListener.onReactionViewClick(data.message)
                }
            }
        }

        if (data.message.attachments.isMedia()) {
            showAttachments(data.message.attachments)
        }
    }

    private fun showAttachments(imageAttachments: Collection<Attachment>) {
        binding.mediaAttachmentsGroupView.showAttachments(*imageAttachments.toTypedArray())
    }
}
