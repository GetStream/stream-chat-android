package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.databinding.StreamUiItemMessagePlainTextWithFileAttachmentsBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.ListenerContainer
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff

public class PlainTextWithFileAttachmentsViewHolder(
    parent: ViewGroup,
    private val listenerContainer: ListenerContainer?,
    internal val binding: StreamUiItemMessagePlainTextWithFileAttachmentsBinding = StreamUiItemMessagePlainTextWithFileAttachmentsBinding.inflate(
        LayoutInflater.from(
            parent.context
        ),
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {

    public override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        binding.messageText.text = data.message.text
        binding.fileAttachmentsView.setAttachments(
            data.message.attachments
        ) { attachment -> listenerContainer?.attachmentClickListener?.onAttachmentClick(data.message, attachment) }
        binding.fileAttachmentsView.setOnLongClickListener {
            listenerContainer?.messageLongClickListener?.onMessageLongClick(data.message)
            true
        }
    }
}
