package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.databinding.StreamUiItemFileAttachmentGroupBinding
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener

internal class FileGroupAttachmentViewHolder(
    private val binding: StreamUiItemFileAttachmentGroupBinding,
    attachmentClickListener: AttachmentClickListener,
    attachmentLongClickListener: AttachmentLongClickListener,
    attachmentDownloadClickListener: AttachmentDownloadClickListener,
) : AttachmentViewHolder(binding.root) {

    // init {
    // attachmentLongClickListener = AttachmentLongClickListener {
    //     this@FileAttachmentViewHolder.onMessageLongClick(message)
    // }
    // attachmentClickListener = AttachmentClickListener {
    //     attachmentClickListener.onAttachmentClick(message, it)
    // }
    // attachmentDownloadClickListener = AttachmentDownloadClickListener {
    //     attachmentDownloadClickListener.onAttachmentDownloadClick(it)
    // }    }
    // }

    override fun bind(attachments: List<Attachment>) {
        binding.fileAttachmentsView.setAttachments(attachments)
    }
}
