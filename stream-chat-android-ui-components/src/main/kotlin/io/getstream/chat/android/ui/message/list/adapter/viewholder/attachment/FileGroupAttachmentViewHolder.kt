package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import androidx.recyclerview.widget.LinearLayoutManager
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.adapters.SimpleListAdapter
import io.getstream.chat.android.ui.databinding.StreamUiItemFileAttachmentGroupBinding
import io.getstream.chat.android.ui.databinding.StreamUiItemImageAttachmentBinding
import io.getstream.chat.android.ui.message.list.FileAttachmentViewStyle
import io.getstream.chat.android.ui.message.list.adapter.attachments.FileAttachmentsAdapter
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener

internal class FileGroupAttachmentViewHolder(
    private val binding: StreamUiItemFileAttachmentGroupBinding,
    private val attachmentClickListener: AttachmentClickListener,
    private val attachmentLongClickListener: AttachmentLongClickListener,
    private val attachmentDownloadClickListener: AttachmentDownloadClickListener,
    private val style: FileAttachmentViewStyle,
): SimpleListAdapter.ViewHolder<List<Attachment>>(binding.root) {

    override fun bind(item: List<Attachment>) {
        val adapter = FileAttachmentsAdapter(
            attachmentClickListener,
            attachmentLongClickListener,
            attachmentDownloadClickListener,
            style
        )

        binding.fileAttachmentView.layoutManager = LinearLayoutManager(context)
        binding.fileAttachmentView.adapter = adapter

        adapter.setItems(item)
    }

    override fun unbind() {
        super.unbind()
    }
}
