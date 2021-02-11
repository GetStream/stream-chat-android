package io.getstream.chat.android.ui.message.input.attachment.file.internal

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.MediaStringUtil
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.common.internal.loadAttachmentThumb
import io.getstream.chat.android.ui.databinding.StreamUiItemAttachmentFileBinding

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
        return StreamUiItemAttachmentFileBinding
            .inflate(parent.inflater, parent, false)
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
        private val binding: StreamUiItemAttachmentFileBinding,
        private val onAttachmentClick: (AttachmentMetaData) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        lateinit var attachment: AttachmentMetaData

        init {
            binding.root.setOnClickListener {
                onAttachmentClick(attachment)
            }
        }

        fun bind(attachment: AttachmentMetaData) {
            this.attachment = attachment

            binding.apply {
                fileTypeImageView.loadAttachmentThumb(attachment)
                fileNameTextView.text = attachment.title
                fileSizeTextView.text = MediaStringUtil.convertFileSizeByteCount(attachment.size)
                selectionIndicator.isChecked = attachment.isSelected
                selectionIndicator.text = attachment.selectedPosition.takeIf { it > 0 }?.toString() ?: ""
            }
        }
    }
}
