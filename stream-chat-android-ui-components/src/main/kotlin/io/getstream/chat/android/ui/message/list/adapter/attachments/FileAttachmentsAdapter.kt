package io.getstream.chat.android.ui.message.list.adapter.attachments

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.databinding.StreamUiItemFileAttachmentBinding
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.FileAttachmentsView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.FileAttachmentViewHolder

internal class FileAttachmentsAdapter(
    private val attachmentClickListener: AttachmentClickListener,
    private val attachmentLongClickListener: AttachmentLongClickListener,
    private val attachmentDownloadClickListener: AttachmentDownloadClickListener,
) : SimpleListAdapter<Attachment, SimpleListAdapter.ViewHolder<Attachment>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<Attachment> {
        return StreamUiItemFileAttachmentBinding
            .inflate(parent.streamThemeInflater, parent, false)
            .let {
                FileAttachmentViewHolder(
                    FileAttachmentsView(parent.context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    },
                    attachmentClickListener,
                    attachmentLongClickListener,
                    attachmentDownloadClickListener,
                )
            }
    }
}
