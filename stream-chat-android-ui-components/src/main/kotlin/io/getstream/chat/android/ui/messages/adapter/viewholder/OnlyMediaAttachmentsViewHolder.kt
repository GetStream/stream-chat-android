package io.getstream.chat.android.ui.messages.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.getstream.sdk.chat.ImageLoader.load
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.databinding.StreamItemMessageAttachmentsOnlyBinding
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder

public class OnlyMediaAttachmentsViewHolder(
    parent: ViewGroup,
    internal val binding: StreamItemMessageAttachmentsOnlyBinding = StreamItemMessageAttachmentsOnlyBinding.inflate(
        LayoutInflater.from(
            parent.context
        ),
        parent,
        false
    )
) : BaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root) {

    override fun bindData(data: MessageListItem.MessageItem) {
        if (data.message.attachments.firstOrNull()?.type == "image") {
            showImage(data.message.attachments.first())
        }
    }

    private fun showImage(imageAttachment: Attachment) {
        imageAttachment.imageUrl?.let { binding.imageView.load(it) }
    }
}