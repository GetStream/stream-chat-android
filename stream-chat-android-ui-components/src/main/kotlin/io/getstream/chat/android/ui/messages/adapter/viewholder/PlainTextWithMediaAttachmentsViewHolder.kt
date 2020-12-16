package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.ListenerContainer
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.databinding.StreamUiMessagePlainTextWithMediaAttachmentsViewBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewTypeMapper.isMedia

public class PlainTextWithMediaAttachmentsViewHolder(
    parent: ViewGroup,
    private val listenerContainer: ListenerContainer?,
    internal val binding: StreamUiMessagePlainTextWithMediaAttachmentsViewBinding = StreamUiMessagePlainTextWithMediaAttachmentsViewBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {
    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        binding.messageText.text = data.message.text
        if (data.message.attachments.isMedia()) {
            binding.mediaAttachmentsGroupView.showAttachments(*data.message.attachments.toTypedArray())
        }
        binding.backgroundView.setOnLongClickListener {
            listenerContainer?.messageLongClickListener?.onMessageLongClick(data.message)
            true
        }
    }
}
