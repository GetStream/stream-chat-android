package io.getstream.chat.android.ui.attachments.file

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.LlcMigrationUtils
import com.getstream.sdk.chat.utils.StringUtility
import io.getstream.chat.android.ui.databinding.StreamItemAttachmentFileBinding

internal class FileAttachmentAdapter(
    private val onAttachmentSelected: (AttachmentMetaData) -> Unit,
) : RecyclerView.Adapter<FileAttachmentAdapter.FileAttachmentViewHolder>() {

    private var attachments: List<AttachmentMetaData> = emptyList()

    override fun onBindViewHolder(holder: FileAttachmentViewHolder, position: Int) =
        holder.bind(attachments[position])

    override fun getItemCount(): Int = attachments.size

    fun selectAttachment(attachment: AttachmentMetaData) = toggleSelection(attachment, true)

    fun deselectAttachment(attachment: AttachmentMetaData) = toggleSelection(attachment, false)

    fun setAttachments(attachments: List<AttachmentMetaData>) {
        this.attachments = attachments
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileAttachmentViewHolder {
        return StreamItemAttachmentFileBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let { FileAttachmentViewHolder(it, onAttachmentSelected) }
    }

    private fun toggleSelection(attachment: AttachmentMetaData, isSelected: Boolean) {
        val index = attachments.indexOf(attachment)
        if (index != -1) {
            attachments[index].isSelected = isSelected

            if (isSelected) {
                attachments[index].selectedPosition = attachments.count { it.isSelected }
                notifyItemChanged(index)
            } else {
                val prevSelectedPosition = attachments[index].selectedPosition
                attachments[index].selectedPosition = 0
                attachments.filter { it.selectedPosition > prevSelectedPosition }.forEach {
                    it.selectedPosition = it.selectedPosition - 1
                }

                notifyDataSetChanged()
            }
        }
    }

    class FileAttachmentViewHolder(
        private val binding: StreamItemAttachmentFileBinding,
        private val onAttachmentClick: (AttachmentMetaData) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(attachment: AttachmentMetaData) {
            binding.fileTypeImageView.setImageResource(LlcMigrationUtils.getIcon(attachment.mimeType))
            binding.fileNameTextView.text = attachment.title
            binding.fileSizeTextView.text = StringUtility.convertFileSizeByteCount(attachment.size)
            binding.root.setOnClickListener {
                onAttachmentClick(attachment)
            }
            binding.selectionIndicator.isChecked = attachment.isSelected
            binding.selectionIndicator.text = attachment.selectedPosition.takeIf { it > 0 }?.toString() ?: ""
        }
    }
}
