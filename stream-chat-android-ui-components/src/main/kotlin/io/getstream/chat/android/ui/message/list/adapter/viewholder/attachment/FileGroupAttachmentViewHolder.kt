package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import androidx.recyclerview.widget.LinearLayoutManager
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.adapters.SimpleListAdapter
import io.getstream.chat.android.ui.databinding.StreamUiItemFileAttachmentGroupBinding
import io.getstream.chat.android.ui.message.list.FileAttachmentViewStyle
import io.getstream.chat.android.ui.message.list.adapter.attachments.FileAttachmentsAdapter
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener

internal class FileGroupAttachmentViewHolder(
    binding: StreamUiItemFileAttachmentGroupBinding,
    attachmentClickListener: AttachmentClickListener,
    attachmentLongClickListener: AttachmentLongClickListener,
    attachmentDownloadClickListener: AttachmentDownloadClickListener,
    style: FileAttachmentViewStyle,
) : SimpleListAdapter.ViewHolder<List<Attachment>>(binding.root) {

    private val adapter = FileAttachmentsAdapter(
        attachmentClickListener,
        attachmentLongClickListener,
        attachmentDownloadClickListener,
        style
    )

    init {
        binding.fileAttachmentView.layoutManager = LinearLayoutManager(context)
        binding.fileAttachmentView.adapter = adapter
    }

    override fun bind(item: List<Attachment>) {
        adapter.setItems(item)
    }
}
