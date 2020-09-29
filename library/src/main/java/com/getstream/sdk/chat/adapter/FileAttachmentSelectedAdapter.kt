package com.getstream.sdk.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.databinding.StreamItemAttachFileBinding
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.LlcMigrationUtils
import com.getstream.sdk.chat.utils.StringUtility
import com.getstream.sdk.chat.view.common.visible

internal class FileAttachmentSelectedAdapter(
    private var attachments: List<AttachmentMetaData>,
    private val localAttach: Boolean
) : BaseAdapter() {
    private var cancelListener: OnAttachmentCancelListener? = null

    constructor(
        attachments: List<AttachmentMetaData>,
        localAttach: Boolean,
        cancelListener: OnAttachmentCancelListener?
    ) : this(attachments, localAttach) {
        this.cancelListener = cancelListener
    }

    override fun getCount() = attachments.size
    override fun getItem(position: Int) = attachments[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.stream_item_attach_file, null)
        val attachment = attachments[position]
        view.tag = attachment
        val binding = StreamItemAttachFileBinding.bind(view)
        configureFileAttach(binding, attachment)
        return view
    }

    // endregion
    // region Configure Attachments
    private fun configureFileAttach(binding: StreamItemAttachFileBinding, attachment: AttachmentMetaData) {
        binding.ivFileThumb.setImageResource(LlcMigrationUtils.getIcon(attachment.mimeType))
        binding.tvFileTitle.text = attachment.title
        binding.ivLargeFileMark.visibility = View.INVISIBLE
        binding.ivSelectMark.visible(false)
        binding.tvClose.visibility = View.INVISIBLE
        binding.progressBar.visible(false)
        binding.tvFileSize.text = StringUtility.convertFileSizeByteCount(attachment.size)
        if (!localAttach) return
        binding.tvClose.visible(true)
        binding.tvClose.setOnClickListener { cancelListener?.onCancel(attachment) }
    }

    fun setAttachments(attachments: List<AttachmentMetaData>) {
        this.attachments = attachments
        notifyDataSetChanged()
    }

    fun clear() {
        attachments = emptyList()
        notifyDataSetChanged()
    }

    interface OnAttachmentCancelListener {
        fun onCancel(attachment: AttachmentMetaData)
    }

    companion object {
        private const val TAG = "AttachmentListAdapter"
    }
}