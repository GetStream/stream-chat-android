package com.getstream.sdk.chat.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.viewholder.attachment.BaseAttachmentViewHolder
import com.getstream.sdk.chat.view.MessageListViewStyle

internal class AttachmentListItemAdapter(
    private val messageListItem: MessageItem,
    private val factory: AttachmentViewHolderFactory,
    private val style: MessageListViewStyle
) : RecyclerView.Adapter<BaseAttachmentViewHolder>() {

    private val attachments: List<AttachmentListItem> =
        messageListItem.message.attachments.map(::AttachmentListItem)

    override fun getItemViewType(position: Int): Int {
        return factory.getAttachmentViewType(attachments[position])
    }

    override fun getItemCount() = attachments.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseAttachmentViewHolder {
        return factory.createAttachmentViewHolder(parent, viewType, style, messageListItem)
    }

    override fun onBindViewHolder(holder: BaseAttachmentViewHolder, position: Int) {
        holder.bind(attachments[position])
    }
}
