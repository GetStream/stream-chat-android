package io.getstream.chat.android.ui.message.input.attachment.selected.internal

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.core.ExperimentalStreamChatApi

@ExperimentalStreamChatApi
internal class SelectedCustomAttachmentAdapter(
    var onAttachmentCancelled: (Attachment) -> Unit = {},
) : RecyclerView.Adapter<BaseSelectedCustomAttachmentViewHolder>() {
    private val attachments: MutableList<Attachment> = mutableListOf()
    lateinit var viewHolderFactory: SelectedCustomAttachmentViewHolderFactory

    internal fun setAttachments(attachments: List<Attachment>) {
        this.attachments.apply {
            clear()
            addAll(attachments)
            notifyDataSetChanged()
        }
    }

    fun clear() {
        attachments.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseSelectedCustomAttachmentViewHolder {
        return viewHolderFactory.createAttachmentViewHolder(attachments, parent)
    }

    override fun onBindViewHolder(holder: BaseSelectedCustomAttachmentViewHolder, position: Int) {
        val attachment = attachments.get(position)
        holder.bind(attachment, onAttachmentCancelled)
    }

    override fun getItemCount(): Int = attachments.size
    fun removeItem(attachment: Attachment) {
        val position = attachments.indexOf(attachment)
        attachments -= attachment
        notifyItemRemoved(position)
    }
}

@ExperimentalStreamChatApi
public abstract class SelectedCustomAttachmentViewHolderFactory {
    public abstract fun createAttachmentViewHolder(attachments: List<Attachment>, parent: ViewGroup): BaseSelectedCustomAttachmentViewHolder
}

@ExperimentalStreamChatApi
public abstract class BaseSelectedCustomAttachmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    public abstract fun bind(attachment: Attachment, onAttachmentCancelled: (Attachment) -> Unit)
}
