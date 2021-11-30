package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.databinding.StreamUiItemImageAttachmentBinding

internal class MediaAttachmentsViewHolder(
    private val binding: StreamUiItemImageAttachmentBinding,
) : AttachmentViewHolder(binding.root) {

    override fun bind(data: MessageListItem.MessageItem) {
        binding.mediaAttachmentView.showAttachments(data.message.attachments)
    }
}
