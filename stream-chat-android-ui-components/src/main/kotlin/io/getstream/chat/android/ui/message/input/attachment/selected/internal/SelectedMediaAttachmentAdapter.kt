package io.getstream.chat.android.ui.message.input.attachment.selected.internal

import android.view.ViewGroup
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.images.loadVideoThumbnail
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.inflater
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.databinding.StreamUiItemSelectedAttachmentMediaBinding

internal class SelectedMediaAttachmentAdapter(
    var onAttachmentCancelled: (AttachmentMetaData) -> Unit = {},
) : SimpleListAdapter<AttachmentMetaData, SelectedMediaAttachmentAdapter.SelectedMediaAttachmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedMediaAttachmentViewHolder {
        return StreamUiItemSelectedAttachmentMediaBinding
            .inflate(parent.inflater, parent, false)
            .let { SelectedMediaAttachmentViewHolder(it, onAttachmentCancelled) }
    }

    class SelectedMediaAttachmentViewHolder(
        private val binding: StreamUiItemSelectedAttachmentMediaBinding,
        private val onAttachmentCancelled: (AttachmentMetaData) -> Unit,
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
        }
    }
}
