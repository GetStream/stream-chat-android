package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.ListenerContainer
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageMediaAttachmentsBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewTypeMapper.isMedia

public class OnlyMediaAttachmentsViewHolder(
    parent: ViewGroup,
    private val listenerContainer: ListenerContainer?,
    internal val binding: StreamUiItemMessageMediaAttachmentsBinding = StreamUiItemMessageMediaAttachmentsBinding.inflate(
        LayoutInflater.from(
            parent.context
        ),
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        if (data.message.attachments.isMedia()) {
            showAttachments(data.message.attachments, data.isMine)
        }

        binding.mediaAttachmentsGroupView.setOnLongClickListener {
            listenerContainer?.messageLongClickListener?.onMessageLongClick(data.message)
            true
        }
    }

    private fun showAttachments(imageAttachments: Collection<Attachment>, isMine: Boolean) {
        constraintView(isMine, binding.mediaAttachmentsGroupView, binding.root)
        binding.mediaAttachmentsGroupView.showAttachments(*imageAttachments.toTypedArray())
    }
}
