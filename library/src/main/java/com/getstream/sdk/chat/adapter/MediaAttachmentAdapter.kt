package com.getstream.sdk.chat.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.getstream.sdk.chat.databinding.StreamItemSelectPhotoBinding
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.Constant
import com.getstream.sdk.chat.view.common.visible

class MediaAttachmentAdapter(
		private var attachments: List<AttachmentMetaData>,
		private val listener: (attachmentMetaData: AttachmentMetaData) -> Unit
) : RecyclerView.Adapter<MediaAttachmentAdapter.MyViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
			MyViewHolder(StreamItemSelectPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false), listener)

	override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(attachments[position])

	override fun getItemCount(): Int = attachments.size

	fun clear() {
		attachments = listOf()
		notifyDataSetChanged()
	}

	class MyViewHolder(
			private val binding: StreamItemSelectPhotoBinding,
			private val listener: (attachmentMetaData: AttachmentMetaData) -> Unit
	) : RecyclerView.ViewHolder(binding.root) {
		fun bind(attachment: AttachmentMetaData) {
			Glide.with(binding.ivMedia.context)
					.load(Uri.fromFile(attachment.file))
					.into(binding.ivMedia);
			binding.ivSelectMark.visible(attachment.isSelected)
			binding.ivLargeFileMark.visible(attachment.file.length() > Constant.MAX_UPLOAD_FILE_SIZE)
			itemView.setOnClickListener { listener(attachment) }
			binding.executePendingBindings()
		}
	}
}