package io.getstream.chat.android.ui.attachments.selected

import android.view.LayoutInflater
import android.view.ViewGroup
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.MediaStringUtil
import io.getstream.chat.android.ui.databinding.StreamUiItemSelectedAttachmentFileBinding
import io.getstream.chat.android.ui.utils.SimpleListAdapter
import io.getstream.chat.android.ui.utils.UiUtils

internal class SelectedFileAttachmentAdapter(
    var onAttachmentCancelled: (AttachmentMetaData) -> Unit = {}
) : SimpleListAdapter<AttachmentMetaData, SelectedFileAttachmentAdapter.SelectedFileAttachmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedFileAttachmentViewHolder {
        return StreamUiItemSelectedAttachmentFileBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let { SelectedFileAttachmentViewHolder(it, onAttachmentCancelled) }
    }

    class SelectedFileAttachmentViewHolder(
        private val binding: StreamUiItemSelectedAttachmentFileBinding,
        private val onAttachmentCancelled: (AttachmentMetaData) -> Unit

    ) : SimpleListAdapter.ViewHolder<AttachmentMetaData>(binding.root) {
        override fun bind(item: AttachmentMetaData) {
            binding.ivFileThumb.setImageResource(UiUtils.getIcon(item.mimeType))
            binding.tvFileTitle.text = item.title
            binding.tvFileSize.text = MediaStringUtil.convertFileSizeByteCount(item.size)
            binding.tvClose.setOnClickListener { onAttachmentCancelled(item) }
        }
    }
}
