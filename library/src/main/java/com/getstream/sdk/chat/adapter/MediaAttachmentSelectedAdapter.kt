package com.getstream.sdk.chat.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.databinding.StreamItemAttachedMediaBinding
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.StringUtility
import io.getstream.chat.android.client.logger.ChatLogger
import top.defaults.drawabletoolbox.DrawableBuilder

class MediaAttachmentSelectedAdapter(
    private var attachments: List<AttachmentMetaData>
) : RecyclerView.Adapter<MediaAttachmentSelectedAdapter.MyViewHolder>() {
    private var cancelListener: OnAttachmentCancelListener? = null

    constructor(
        attachments: List<AttachmentMetaData>,
        listener: OnAttachmentCancelListener
    ) : this(attachments) {
        cancelListener = listener
    }

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
        holder.bind(attachments[position], cancelListener)
    }

    override fun getItemCount(): Int {
        return attachments.size
    }

    internal fun setAttachments(attachments: List<AttachmentMetaData>) {
        this.attachments = attachments
        notifyDataSetChanged()
    }

    fun removeAttachment(attachment: AttachmentMetaData) {
        val index = attachments.indexOf(attachment)
        attachments = attachments - attachment
        if (index != -1) {
            notifyItemRemoved(index)
        } else {
            notifyDataSetChanged()
        }
    }

    fun addAttachment(attachment: AttachmentMetaData) {
        attachments = attachments + attachment
        notifyItemInserted(attachments.lastIndex)
    }

    fun clear() {
        attachments = emptyList()
        notifyDataSetChanged()
    }

    interface OnAttachmentCancelListener {
        fun onCancel(attachment: AttachmentMetaData)
    }

    inner class MyViewHolder(private val binding: StreamItemAttachedMediaBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(attachment: AttachmentMetaData, cancelListener: OnAttachmentCancelListener?) {
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
            if (attachment.uri != null) {
                Glide.with(itemView.context)
                    .load(attachment.uri)
                    .into(binding.ivMedia)
            } else {
                try {
                    if (attachment.mimeType == ModelType.attach_mime_mov ||
                        attachment.mimeType == ModelType.attach_mime_mp4
                    ) {
                        binding.ivMedia.setImageResource(R.drawable.stream_placeholder)
                    }
                } catch (e: Exception) {
                    ChatLogger.instance.logE(TAG, e)
                }
            }
            if (ModelType.attach_video == attachment.type) {
                binding.tvLength.text = StringUtility.convertVideoLength(attachment.videoLength)
            } else {
                binding.tvLength.text = ""
            }
            binding.btnClose.setOnClickListener { cancelListener?.onCancel(attachment) }
            binding.ivMask.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.INVISIBLE
            binding.executePendingBindings()
        }
    }

    companion object {
        private val TAG = MediaAttachmentSelectedAdapter::class.java.simpleName
    }
}
