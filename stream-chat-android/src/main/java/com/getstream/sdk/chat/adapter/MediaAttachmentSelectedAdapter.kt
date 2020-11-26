package com.getstream.sdk.chat.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.ImageLoader.load
import com.getstream.sdk.chat.ImageLoader.loadVideoThumbnail
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.databinding.StreamItemAttachedMediaBinding
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.StringUtility
import top.defaults.drawabletoolbox.DrawableBuilder

internal class MediaAttachmentSelectedAdapter(
    private var selectedAttachments: List<AttachmentMetaData> = emptyList(),
    var cancelListener: (AttachmentMetaData) -> Unit = { }
) : RecyclerView.Adapter<MediaAttachmentSelectedAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        // create a new view
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding = StreamItemAttachedMediaBinding.inflate(layoutInflater, parent, false)
        return MyViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(selectedAttachments[position], cancelListener)
    }

    override fun getItemCount(): Int {
        return selectedAttachments.size
    }

    internal fun setAttachments(attachments: List<AttachmentMetaData>) {
        this.selectedAttachments = attachments
        notifyDataSetChanged()
    }

    fun removeAttachment(attachment: AttachmentMetaData) {
        val index = selectedAttachments.indexOf(attachment)
        selectedAttachments = selectedAttachments - attachment
        if (index != -1) {
            notifyItemRemoved(index)
        } else {
            notifyDataSetChanged()
        }
    }

    fun addAttachment(attachment: AttachmentMetaData) {
        selectedAttachments = selectedAttachments + attachment
        notifyItemInserted(selectedAttachments.lastIndex)
    }

    fun clear() {
        selectedAttachments = emptyList()
        notifyDataSetChanged()
    }

    inner class MyViewHolder(private val binding: StreamItemAttachedMediaBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(attachment: AttachmentMetaData, cancelListener: (AttachmentMetaData) -> Unit) {
            val cornerRadius =
                itemView.context.resources.getDimensionPixelSize(R.dimen.stream_input_upload_media_radius)
            binding.ivMedia.setShape(
                itemView.context,
                DrawableBuilder()
                    .rectangle()
                    .solidColor(Color.BLACK)
                    .cornerRadii(cornerRadius, cornerRadius, cornerRadius, cornerRadius)
                    .build()
            )

            if (attachment.type == ModelType.attach_video) {
                binding.ivMedia.loadVideoThumbnail(attachment.uri, R.drawable.stream_placeholder)
            } else {
                binding.ivMedia.load(attachment.uri, R.drawable.stream_placeholder)
            }

            if (ModelType.attach_video == attachment.type) {
                binding.tvLength.text = StringUtility.convertVideoLength(attachment.videoLength)
            } else {
                binding.tvLength.text = ""
            }
            binding.btnClose.setOnClickListener { cancelListener(attachment) }
            binding.ivMask.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }
}
