package io.getstream.chat.android.ui.message.input.attachment.selected.internal

import android.view.ViewGroup
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.MediaStringUtil
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.common.internal.loadAttachmentThumb
import io.getstream.chat.android.ui.databinding.StreamUiItemSelectedAttachmentFileBinding

internal class SelectedFileAttachmentAdapter(
    var onAttachmentCancelled: (AttachmentMetaData) -> Unit = {}
) : SimpleListAdapter<AttachmentMetaData, SelectedFileAttachmentAdapter.SelectedFileAttachmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedFileAttachmentViewHolder {
        return StreamUiItemSelectedAttachmentFileBinding
            .inflate(parent.inflater, parent, false)
            .let { SelectedFileAttachmentViewHolder(it, onAttachmentCancelled) }
    }

    class SelectedFileAttachmentViewHolder(
        private val binding: StreamUiItemSelectedAttachmentFileBinding,
        private val onAttachmentCancelled: (AttachmentMetaData) -> Unit,
    ) : SimpleListAdapter.ViewHolder<AttachmentMetaData>(binding.root) {
        lateinit var attachment: AttachmentMetaData

        init {
            binding.tvClose.setOnClickListener { onAttachmentCancelled(attachment) }
        }

        override fun bind(item: AttachmentMetaData) {
            this.attachment = item

            binding.apply {
                ivFileThumb.loadAttachmentThumb(attachment)
                tvFileTitle.text = attachment.title
                tvFileSize.text = MediaStringUtil.convertFileSizeByteCount(attachment.size)
            }
        }
    }
}
