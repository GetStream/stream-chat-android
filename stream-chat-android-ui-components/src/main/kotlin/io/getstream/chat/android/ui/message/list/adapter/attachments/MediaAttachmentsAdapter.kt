package io.getstream.chat.android.ui.message.list.adapter.attachments

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.adapters.SimpleListAdapter
import io.getstream.chat.android.ui.common.extensions.internal.isMedia
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewHolderFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewHolderFactoryImpl

internal class MediaAttachmentsAdapter(
    private val viewHolderFactory: AttachmentViewHolderFactory,
) : SimpleListAdapter<List<Attachment>, SimpleListAdapter.ViewHolder<List<Attachment>>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<List<Attachment>> {
        return viewHolderFactory.attachmentViewHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return if (itemList.first().isMedia())
            AttachmentViewHolderFactoryImpl.MEDIA
        else
            AttachmentViewHolderFactoryImpl.FILE
    }
}
