package io.getstream.chat.android.ui.attachments.selected

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.LlcMigrationUtils
import com.getstream.sdk.chat.utils.StringUtility
import io.getstream.chat.android.ui.databinding.StreamItemSelectedAttachmentFileBinding

internal class SelectedFileAttachmentAdapter(
    private val onAttachmentCancelled: (AttachmentMetaData) -> Unit
) : RecyclerView.Adapter<SelectedFileAttachmentAdapter.SelectedFileAttachmentViewHolder>() {

    private val attachments: MutableList<AttachmentMetaData> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedFileAttachmentViewHolder {
        return StreamItemSelectedAttachmentFileBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let { SelectedFileAttachmentViewHolder(it, onAttachmentCancelled) }
    }

    override fun onBindViewHolder(holder: SelectedFileAttachmentViewHolder, position: Int) {
        holder.bind(attachments[position])
    }

    override fun getItemCount(): Int = attachments.size

    fun setAttachments(attachments: List<AttachmentMetaData>) {
        this.attachments.clear()
        this.attachments.addAll(attachments)
        notifyDataSetChanged()
    }

    fun removeAttachment(attachment: AttachmentMetaData) {
        val index = attachments.indexOf(attachment)
        if (index != -1) {
            attachments.remove(attachment)
            notifyItemRemoved(index)
        }
    }

    fun clear() {
        attachments.clear()
        notifyDataSetChanged()
    }

    class SelectedFileAttachmentViewHolder(
        private val binding: StreamItemSelectedAttachmentFileBinding,
        private val onAttachmentCancelled: (AttachmentMetaData) -> Unit

    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(attachment: AttachmentMetaData) {
            binding.ivFileThumb.setImageResource(LlcMigrationUtils.getIcon(attachment.mimeType))
            binding.tvFileTitle.text = attachment.title
            binding.tvFileSize.text = StringUtility.convertFileSizeByteCount(attachment.size)
            binding.tvClose.setOnClickListener { onAttachmentCancelled(attachment) }
        }
    }
}
