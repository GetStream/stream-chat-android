package io.getstream.chat.android.ui.message.list.adapter.attachments

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewHolderFactory

/**
 * Adapter powering the RecyclerView that displays attachment content within
 * message bubbles.
 */
internal class AttachmentsAdapter(
    private val viewHolderFactory: AttachmentViewHolderFactory,
) : ListAdapter<MessageItem, AttachmentViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return viewHolderFactory.getItemViewType(getItem(0))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        return viewHolderFactory.createViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object DiffCallback : DiffUtil.ItemCallback<MessageItem>() {
        override fun areItemsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
            return oldItem.message.id == newItem.message.id
        }

        override fun areContentsTheSame(oldItem: MessageItem, newItem: MessageItem): Boolean {
            return oldItem == newItem
        }
    }
}
