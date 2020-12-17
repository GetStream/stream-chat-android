package io.getstream.chat.ui.sample.feature.chat.info.shared.files

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.utils.MediaStringUtil
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.utils.UiUtils
import io.getstream.chat.ui.sample.databinding.ChatInfoSharedFileDateDividerBinding
import io.getstream.chat.ui.sample.databinding.ChatInfoSharedFileItemBinding
import io.getstream.chat.ui.sample.feature.chat.info.shared.SharedAttachment
import java.text.DateFormat

abstract class BaseViewHolder<T : SharedAttachment>(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    /**
     * Workaround to allow a downcast of the SharedAttachment to T
     */
    @Suppress("UNCHECKED_CAST")
    internal fun bindListItem(item: SharedAttachment) = bind(item as T)

    protected abstract fun bind(item: T)
}

class ChatInfoSharedFileViewHolder(
    private val binding: ChatInfoSharedFileItemBinding,
    attachmentClickListener: ChatInfoSharedFilesAdapter.AttachmentClickListener?
) : BaseViewHolder<SharedAttachment.AttachmentItem>(binding.root) {

    private lateinit var attachmentItem: SharedAttachment.AttachmentItem

    init {
        binding.fileItemContainer.setOnClickListener { attachmentClickListener?.onClick(attachmentItem) }
    }

    @InternalStreamChatApi
    override fun bind(item: SharedAttachment.AttachmentItem) {
        attachmentItem = item
        binding.fileTypeImageView.setImageResource(UiUtils.getIcon(item.attachment.mimeType))
        binding.fileNameTextView.text = item.attachment.name
        binding.fileSizeTextView.text = MediaStringUtil.convertFileSizeByteCount(item.attachment.fileSize.toLong())
    }
}

class ChatInfoSharedFileDateDividerViewHolder(
    private val binding: ChatInfoSharedFileDateDividerBinding,
    private val dateFormat: DateFormat
) : BaseViewHolder<SharedAttachment.DateDivider>(binding.root) {

    override fun bind(item: SharedAttachment.DateDivider) {
        binding.dateLabel.text = dateFormat.format(item.date)
    }
}
