package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.databinding.StreamUiItemImageAttachmentBinding

internal class MediaAttachmentsViewHolder(
    private val binding: StreamUiItemImageAttachmentBinding,
) : AttachmentViewHolder(binding.root) {

    override fun bind(attachments: List<Attachment>) {
        binding.mediaAttachmentView.showAttachments(attachments)
    }
}
