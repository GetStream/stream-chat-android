package io.getstream.chat.android.ui.message.list.adapter.attachments

import android.view.ViewGroup
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewHolderFactory

internal class FileAttachmentsAdapter(
    private val viewHolderFactory: AttachmentViewHolderFactory
) : SimpleListAdapter<AttachmentGroup, SimpleListAdapter.ViewHolder<AttachmentGroup>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<AttachmentGroup> {
        return viewHolderFactory.attachmentViewHolder(parent, viewType)
    }
}
