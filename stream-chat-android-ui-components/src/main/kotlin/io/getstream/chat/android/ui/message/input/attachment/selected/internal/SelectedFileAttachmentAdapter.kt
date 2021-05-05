package io.getstream.chat.android.ui.message.input.attachment.selected.internal

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.AttachmentConstants
import com.getstream.sdk.chat.utils.MediaStringUtil
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.common.internal.loadAttachmentThumb
import io.getstream.chat.android.ui.databinding.StreamUiItemSelectedAttachmentFileBinding

internal class SelectedFileAttachmentAdapter(
    var onAttachmentCancelled: (AttachmentMetaData) -> Unit = {},
) : SimpleListAdapter<AttachmentMetaData, SelectedFileAttachmentAdapter.SelectedFileAttachmentViewHolder>() {

    internal var attachmentMaxFileSize: Long = AttachmentConstants.MAX_UPLOAD_FILE_SIZE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedFileAttachmentViewHolder {
        return StreamUiItemSelectedAttachmentFileBinding
            .inflate(parent.inflater, parent, false)
            .let { SelectedFileAttachmentViewHolder(it, onAttachmentCancelled, attachmentMaxFileSize) }
    }

    class SelectedFileAttachmentViewHolder(
        private val binding: StreamUiItemSelectedAttachmentFileBinding,
        private val onAttachmentCancelled: (AttachmentMetaData) -> Unit,
        private val attachmentMaxFileSize: Long,
    ) : SimpleListAdapter.ViewHolder<AttachmentMetaData>(binding.root) {
        lateinit var attachment: AttachmentMetaData

        init {
            binding.tvClose.setOnClickListener { onAttachmentCancelled(attachment) }
        }

        override fun bind(item: AttachmentMetaData) {
            this.attachment = item

            binding.apply {
                ivFileThumb.loadAttachmentThumb(attachment)
                tvFileSize.text = MediaStringUtil.convertFileSizeByteCount(attachment.size)
                if (item.size > attachmentMaxFileSize) {
                    tvFileTitle.text = context.getString(R.string.stream_ui_file_too_big)
                    tvFileTitle.setTextColor(ContextCompat.getColor(context, R.color.stream_ui_accent_red))
                } else {
                    tvFileTitle.text = attachment.title
                    tvFileTitle.setTextColor(ContextCompat.getColor(context, R.color.stream_ui_black))
                }
            }
        }
    }
}
