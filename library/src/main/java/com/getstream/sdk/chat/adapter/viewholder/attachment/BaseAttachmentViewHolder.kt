package com.getstream.sdk.chat.adapter.viewholder.attachment

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.AttachmentListItem
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.view.MessageListView.AttachmentClickListener
import com.getstream.sdk.chat.view.MessageListView.MessageLongClickListener
import io.getstream.chat.android.client.models.Message

abstract class BaseAttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(
        messageListItem: MessageItem,
        message: Message,
        attachmentListItem: AttachmentListItem,
        clickListener: AttachmentClickListener?,
        longClickListener: MessageLongClickListener?
    )

    protected val context: Context
        get() = itemView.context
}
