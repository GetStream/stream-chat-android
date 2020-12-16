package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.ListenerContainer
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.databinding.StreamUiItemMessageAttachmentsOnlyBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder

public class OnlyMediaAttachmentsViewHolder(
    parent: ViewGroup,
    private val listenerContainer: ListenerContainer?,
    internal val binding: StreamUiItemMessageAttachmentsOnlyBinding = StreamUiItemMessageAttachmentsOnlyBinding.inflate(
        LayoutInflater.from(
            parent.context
        ),
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        if (data.message.attachments.all { it.type == "image" }) {
            showAttachments(data.message.attachments, data.isMine)
        }

        binding.mediaAttachmentsGroupView.setOnLongClickListener {
            listenerContainer?.messageLongClickListener?.onMessageLongClick(data.message)
            true
        }
    }

    private fun showAttachments(imageAttachments: Collection<Attachment>, isMine: Boolean) {
        binding.mediaAttachmentsGroupView.showAttachments(*imageAttachments.toTypedArray())
    }
}
