package io.getstream.chat.android.ui.attachments.selected

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.ImageLoader.load
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamItemSelectedAttachmentMediaBinding
import top.defaults.drawabletoolbox.DrawableBuilder

internal class SelectedMediaAttachmentAdapter(
    private val onAttachmentCancelled: (AttachmentMetaData) -> Unit
) : RecyclerView.Adapter<SelectedMediaAttachmentAdapter.SelectedMediaAttachmentViewHolder>() {

    private val attachments: MutableList<AttachmentMetaData> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedMediaAttachmentViewHolder {
        return StreamItemSelectedAttachmentMediaBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let { SelectedMediaAttachmentViewHolder(it, onAttachmentCancelled) }
    }

    override fun onBindViewHolder(holder: SelectedMediaAttachmentViewHolder, position: Int) {
        holder.bind(attachments[position])
    }

    override fun getItemCount(): Int = attachments.size

    internal fun setAttachments(attachments: List<AttachmentMetaData>) {
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

    class SelectedMediaAttachmentViewHolder(
        private val binding: StreamItemSelectedAttachmentMediaBinding,
        private val onAttachmentCancelled: (AttachmentMetaData) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private val context = itemView.context

        fun bind(attachment: AttachmentMetaData) {
            bindAttachmentImage(attachment)
            bindClickListener(attachment)
        }

        private fun bindAttachmentImage(attachment: AttachmentMetaData) {
            val cornerRadius = context.resources.getDimensionPixelSize(R.dimen.stream_selected_attachment_corner_radius)
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
