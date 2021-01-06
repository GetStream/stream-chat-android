package io.getstream.chat.android.ui.attachments.selected

import android.view.ViewGroup
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.MediaStringUtil
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.ui.databinding.StreamUiItemSelectedAttachmentFileBinding
import io.getstream.chat.android.ui.utils.SimpleListAdapter
import io.getstream.chat.android.ui.utils.UiUtils

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

        lateinit var item: AttachmentMetaData

        init {
            binding.tvClose.setOnClickListener { onAttachmentCancelled(item) }
        }

        override fun bind(item: AttachmentMetaData) {
            this.item = item

            binding.apply {
                ivFileThumb.setImageResource(UiUtils.getIcon(item.mimeType))
                tvFileTitle.text = item.title
                tvFileSize.text = MediaStringUtil.convertFileSizeByteCount(item.size)
            }
        }
    }
}
