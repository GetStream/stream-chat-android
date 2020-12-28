package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageMediaAttachmentsBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.ListenerContainer
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewTypeMapper.isMedia

public class OnlyMediaAttachmentsViewHolder(
    parent: ViewGroup,
    currentUser: User,
    private val listenerContainer: ListenerContainer?,
    internal val binding: StreamUiItemMessageMediaAttachmentsBinding = StreamUiItemMessageMediaAttachmentsBinding.inflate(
        parent.inflater,
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(currentUser, binding.root) {

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
