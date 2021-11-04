package io.getstream.chat.android.ui.message.list.adapter.attachments

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.extensions.internal.isMedia
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewHolderFactory

internal class AttachmentsAdapter(
    private val viewHolderFactory: AttachmentViewHolderFactory,
) : ListAdapter<List<Attachment>, AttachmentViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(0).isMedia())
            AttachmentViewHolderFactory.MEDIA
        else
            AttachmentViewHolderFactory.FILE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        return viewHolderFactory.createAttachmentViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object DiffCallback  : DiffUtil.ItemCallback<List<Attachment>>() {

        override fun areItemsTheSame(oldItem: List<Attachment>, newItem: List<Attachment>): Boolean {
            return oldItem == newItem
        }
        override fun areContentsTheSame(oldItem: List<Attachment>, newItem: List<Attachment>): Boolean {
            return oldItem == newItem
        }
    }
}
