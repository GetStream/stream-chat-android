package com.getstream.sdk.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.databinding.StreamItemAttachFileBinding
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.Constant
import com.getstream.sdk.chat.utils.LlcMigrationUtils
import com.getstream.sdk.chat.utils.StringUtility
import com.getstream.sdk.chat.view.common.visible

class FileAttachmentListAdapter(
		private var attachments: List<AttachmentMetaData>,
		private val listener: (attachmentMetaData: AttachmentMetaData) -> Unit
) : RecyclerView.Adapter<FileAttachmentListAdapter.MyViewHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
			MyViewHolder(StreamItemAttachFileBinding.inflate(LayoutInflater.from(parent
					.context), parent, false), listener)

	override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(attachments[position])
	override fun getItemCount(): Int = attachments.size
	fun clear() {
		attachments = listOf()
		notifyDataSetChanged()
	}

	class MyViewHolder(
			private val binding: StreamItemAttachFileBinding,
			private val listener: (attachmentMetaData: AttachmentMetaData) -> Unit
	) : RecyclerView.ViewHolder(binding.root) {
		fun bind(attachment: AttachmentMetaData) {
			binding.ivFileThumb.setImageResource(LlcMigrationUtils.getIcon(attachment.mimeType))
			binding.tvFileTitle.text = attachment.title
			binding.ivLargeFileMark.visible(attachment.file.length() > Constant.MAX_UPLOAD_FILE_SIZE)
			binding.ivSelectMark.visible(attachment.isSelected)
			binding.tvClose.visible(false)
			binding.progressBar.visible(false)
			binding.tvFileSize.text = StringUtility.convertFileSizeByteCount(attachment.file.length())
			itemView.setOnClickListener { listener(attachment) }
			binding.executePendingBindings()
		}
	}
}