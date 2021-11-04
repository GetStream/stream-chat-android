package io.getstream.chat.android.ui.message.input.attachment.selected.internal

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.images.loadVideoThumbnail
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.AttachmentConstants
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.databinding.StreamUiItemSelectedAttachmentMediaBinding

internal class SelectedMediaAttachmentAdapter(
    var onAttachmentCancelled: (AttachmentMetaData) -> Unit = {},
) : SimpleListAdapter<AttachmentMetaData, SelectedMediaAttachmentAdapter.SelectedMediaAttachmentViewHolder>() {

    internal var attachmentMaxFileSize: Long = AttachmentConstants.MAX_UPLOAD_FILE_SIZE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedMediaAttachmentViewHolder {
        return StreamUiItemSelectedAttachmentMediaBinding
            .inflate(parent.streamThemeInflater, parent, false)
            .let { SelectedMediaAttachmentViewHolder(it, onAttachmentCancelled, attachmentMaxFileSize) }
    }

    class SelectedMediaAttachmentViewHolder(
        private val binding: StreamUiItemSelectedAttachmentMediaBinding,
        private val onAttachmentCancelled: (AttachmentMetaData) -> Unit,
        private val attachmentMaxFileSize: Long,
    ) : SimpleListAdapter.ViewHolder<AttachmentMetaData>(binding.root) {

        lateinit var item: AttachmentMetaData

        init {
            binding.btnClose.setOnClickListener { onAttachmentCancelled(item) }

            val cornerRadius = context.getDimension(R.dimen.stream_ui_selected_attachment_corner_radius)
                .toFloat()
            binding.ivMedia.shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setAllCornerSizes(cornerRadius)
                .build()
        }

        override fun bind(item: AttachmentMetaData) {
            this.item = item

            bindAttachmentImage(item)
            bindErrorBadge(item)
        }

        private fun bindAttachmentImage(attachment: AttachmentMetaData) {
            if (attachment.type == ModelType.attach_video) {
                binding.ivMedia.loadVideoThumbnail(
                    uri = attachment.uri,
                    placeholderResId = R.drawable.stream_ui_placeholder,
                )
            } else {
                binding.ivMedia.load(data = attachment.uri)
            }
        }

        private fun bindErrorBadge(attachment: AttachmentMetaData) {
            if (attachment.size > attachmentMaxFileSize) {
                binding.tvError.isVisible = true
                binding.tvError.text = context.getString(R.string.stream_ui_message_input_error_file_size)
            } else {
                binding.tvError.isVisible = false
            }
        }
    }
}
