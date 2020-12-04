package io.getstream.chat.android.ui.attachments.selected

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import com.getstream.sdk.chat.ImageLoader.load
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamItemSelectedAttachmentMediaBinding
import io.getstream.chat.android.ui.utils.SimpleListAdapter
import top.defaults.drawabletoolbox.DrawableBuilder

internal class SelectedMediaAttachmentAdapter(
    var onAttachmentCancelled: (AttachmentMetaData) -> Unit = {}
) : SimpleListAdapter<AttachmentMetaData, SelectedMediaAttachmentAdapter.SelectedMediaAttachmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedMediaAttachmentViewHolder {
        return StreamItemSelectedAttachmentMediaBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let { SelectedMediaAttachmentViewHolder(it, onAttachmentCancelled) }
    }

    class SelectedMediaAttachmentViewHolder(
        private val binding: StreamItemSelectedAttachmentMediaBinding,
        private val onAttachmentCancelled: (AttachmentMetaData) -> Unit
    ) : SimpleListAdapter.ViewHolder<AttachmentMetaData>(binding.root) {

        override fun bind(attachment: AttachmentMetaData) {
            bindAttachmentImage(attachment)
            bindClickListener(attachment)
        }

        private fun bindAttachmentImage(attachment: AttachmentMetaData) {
            val cornerRadius = context.resources.getDimensionPixelSize(R.dimen.stream_ui_selected_attachment_corner_radius)
            binding.ivMedia.setShape(
                context,
                DrawableBuilder()
                    .rectangle()
                    .solidColor(Color.BLACK)
                    .cornerRadii(cornerRadius, cornerRadius, cornerRadius, cornerRadius)
                    .build()
            )
            if (attachment.type == ModelType.attach_video) {
                binding.ivMedia.load(R.drawable.stream_placeholder)
            } else {
                binding.ivMedia.load(attachment.uri)
            }
        }

        private fun bindClickListener(attachment: AttachmentMetaData) {
            binding.btnClose.setOnClickListener { onAttachmentCancelled(attachment) }
        }
    }
}
