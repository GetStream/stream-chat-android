package io.getstream.chat.android.ui.message.input.attachment.selected.internal

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.images.loadVideoThumbnail
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.inflater
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.databinding.StreamUiItemSelectedAttachmentMediaBinding
import io.getstream.chat.android.ui.message.input.internal.SIZE_MEGA_20

internal class SelectedMediaAttachmentAdapter(
    var onAttachmentCancelled: (AttachmentMetaData) -> Unit = {},
) : SimpleListAdapter<AttachmentMetaData, SelectedMediaAttachmentAdapter.SelectedMediaAttachmentViewHolder>() {

    internal var attachmentMaxFileSize: Int = SIZE_MEGA_20

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedMediaAttachmentViewHolder {
        return StreamUiItemSelectedAttachmentMediaBinding
            .inflate(parent.inflater, parent, false)
            .let { SelectedMediaAttachmentViewHolder(it, onAttachmentCancelled, attachmentMaxFileSize) }
    }

    class SelectedMediaAttachmentViewHolder(
        private val binding: StreamUiItemSelectedAttachmentMediaBinding,
        private val onAttachmentCancelled: (AttachmentMetaData) -> Unit,
        private val attachmentMaxFileSize: Int
    ) : SimpleListAdapter.ViewHolder<AttachmentMetaData>(binding.root) {

        lateinit var item: AttachmentMetaData

        init {
            binding.btnClose.setOnClickListener { onAttachmentCancelled(item) }
        }

        override fun bind(item: AttachmentMetaData) {
            this.item = item

            bindAttachmentImage(item)
        }

        private fun bindAttachmentImage(attachment: AttachmentMetaData) {
            val cornerRadius =
                context.resources.getDimensionPixelSize(R.dimen.stream_ui_selected_attachment_corner_radius).toFloat()
            binding.ivMedia.shapeAppearanceModel =
                ShapeAppearanceModel.builder().setAllCornerSizes(cornerRadius).build()
            if (attachment.type == ModelType.attach_video) {
                binding.ivMedia.loadVideoThumbnail(
                    uri = attachment.uri,
                    placeholderResId = R.drawable.stream_placeholder,
                )
            } else {
                binding.ivMedia.load(data = attachment.uri)
            }

            if (attachment.size > attachmentMaxFileSize) {
                binding.tvError.isVisible = true
                binding.tvError.text = context.getString(R.string.stream_ui_file_too_big)
            }
        }
    }
}
