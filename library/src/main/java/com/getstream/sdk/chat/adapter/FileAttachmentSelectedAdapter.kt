package com.getstream.sdk.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.view.isVisible
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.databinding.StreamItemAttachFileBinding
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.LlcMigrationUtils
import com.getstream.sdk.chat.utils.StringUtility

internal class FileAttachmentSelectedAdapter(
    private var attachments: List<AttachmentMetaData>,
    private val localAttach: Boolean,
    var cancelListener: (AttachmentMetaData) -> Unit = { }
) : BaseAdapter() {
    override fun getCount() = attachments.size
    override fun getItem(position: Int) = attachments[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.stream_item_attach_file, parent, false)
        val binding = StreamItemAttachFileBinding.bind(view)
        configureFileAttach(binding, attachments[position])
        return view
    }

    // endregion
    // region Configure Attachments
    private fun configureFileAttach(binding: StreamItemAttachFileBinding, attachment: AttachmentMetaData) {
        binding.ivFileThumb.setImageResource(LlcMigrationUtils.getIcon(attachment.mimeType))
        binding.tvFileTitle.text = attachment.title
        binding.ivLargeFileMark.visibility = View.INVISIBLE
        binding.ivSelectMark.isVisible = false
        binding.tvClose.visibility = View.INVISIBLE
        binding.progressBar.isVisible = false
        binding.tvFileSize.text = StringUtility.convertFileSizeByteCount(attachment.size)
        if (!localAttach) return
        binding.tvClose.isVisible = true
        binding.tvClose.setOnClickListener { cancelListener(attachment) }
    }

    fun setAttachments(attachments: List<AttachmentMetaData>) {
        this.attachments = attachments
        notifyDataSetChanged()
    }

    fun clear() {
        attachments = emptyList()
        notifyDataSetChanged()
    }
}
