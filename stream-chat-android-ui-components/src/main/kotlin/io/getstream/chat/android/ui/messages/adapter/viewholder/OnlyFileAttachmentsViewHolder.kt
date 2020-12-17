package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageFileAttachmentsBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.ListenerContainer
import io.getstream.chat.android.ui.messages.adapter.MessageListItemPayloadDiff

public class OnlyFileAttachmentsViewHolder(
    parent: ViewGroup,
    private val listenerContainer: ListenerContainer?,
    internal val binding: StreamUiItemMessageFileAttachmentsBinding = StreamUiItemMessageFileAttachmentsBinding.inflate(
        LayoutInflater.from(
            parent.context
        ),
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {

    public override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        binding.fileAttachmentsView.setAttachments(data.message.attachments)
        binding.fileAttachmentsView.setOnLongClickListener {
            listenerContainer?.messageLongClickListener?.onMessageLongClick(data.message)
            true
        }
    }
}
