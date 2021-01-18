package io.getstream.chat.android.ui.attachments.media

import android.graphics.Color
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.images.loadVideoThumbnail
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.MediaStringUtil
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiItemAttachmentMediaBinding

internal class MediaAttachmentAdapter(
    private val onAttachmentSelected: (attachmentMetaData: AttachmentMetaData) -> Unit,
) : RecyclerView.Adapter<MediaAttachmentAdapter.MediaAttachmentViewHolder>() {

    private val attachments: MutableList<AttachmentMetaData> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaAttachmentViewHolder {
        return StreamUiItemAttachmentMediaBinding
            .inflate(parent.inflater, parent, false)
            .let { MediaAttachmentViewHolder(it, onAttachmentSelected) }
    }

    override fun onBindViewHolder(holder: MediaAttachmentViewHolder, position: Int) {
        holder.bind(attachments[position])
    }

    override fun getItemCount(): Int = attachments.size

    fun setAttachments(attachments: List<AttachmentMetaData>) {
        this.attachments.clear()
        this.attachments.addAll(attachments)
        notifyDataSetChanged()
    }

    fun selectAttachment(attachment: AttachmentMetaData) = toggleAttachmentSelection(attachment, true)

    fun deselectAttachment(attachment: AttachmentMetaData) = toggleAttachmentSelection(attachment, false)

    fun clearAttachments() {
        attachments.clear()
        notifyDataSetChanged()
    }

    private fun toggleAttachmentSelection(attachment: AttachmentMetaData, isSelected: Boolean) {
        val index = attachments.indexOf(attachment)
        if (index != -1) {
            attachments[index].isSelected = isSelected
            notifyItemChanged(index)
        }
    }

    class MediaAttachmentViewHolder(
        private val binding: StreamUiItemAttachmentMediaBinding,
        private val onAttachmentSelected: (attachmentMetaData: AttachmentMetaData) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        lateinit var attachment: AttachmentMetaData

        init {
            binding.root.setOnClickListener { onAttachmentSelected(attachment) }
        }

        fun bind(attachment: AttachmentMetaData) {
            this.attachment = attachment

            bindMediaImage(attachment)
            bindSelectionMark(attachment)
            bindSelectionOverlay(attachment)
            bindAttachmentType(attachment)
        }

        private fun bindMediaImage(attachment: AttachmentMetaData) {
            if (attachment.type == ModelType.attach_video) {
                binding.mediaThumbnailImageView.loadVideoThumbnail(
                    uri = attachment.uri,
                    placeholderResId = R.drawable.stream_placeholder
                )
                val color = ContextCompat.getColor(itemView.context, R.color.stream_ui_white_smoke)
                binding.mediaThumbnailImageView.setBackgroundColor(color)
            } else {
                binding.mediaThumbnailImageView.load(data = attachment.uri)
                binding.mediaThumbnailImageView.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        private fun bindSelectionMark(attachment: AttachmentMetaData) {
            binding.selectionMarkImageView.isVisible = attachment.isSelected
        }

        private fun bindSelectionOverlay(attachment: AttachmentMetaData) {
            binding.selectionOverlayView.isVisible = attachment.isSelected
        }

        private fun bindAttachmentType(attachment: AttachmentMetaData) {
            if (attachment.type == ModelType.attach_video) {
                binding.videoLengthTextView.isVisible = true
                binding.videoLogoImageView.isVisible = true
                binding.videoLengthTextView.text = MediaStringUtil.convertVideoLength(attachment.videoLength)
            } else {
                binding.videoLengthTextView.isVisible = false
                binding.videoLogoImageView.isVisible = false
                binding.videoLengthTextView.text = ""
            }
        }
    }
}
