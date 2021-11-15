package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import androidx.core.view.setPadding
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.FileAttachmentsView

internal class FileAttachmentViewHolder(
    private val fileAttachmentView: FileAttachmentsView,
    private val attachmentClickListener: AttachmentClickListener,
    private val attachmentLongClickListener: AttachmentLongClickListener,
    private val attachmentDownloadClickListener: AttachmentDownloadClickListener,
) : SimpleListAdapter.ViewHolder<Attachment>(fileAttachmentView) {

    private companion object {
        private val FILE_ATTACHMENT_VIEW_PADDING = 4.dpToPx()
    }

    override fun bind(item: Attachment) {
        fileAttachmentView.apply {
            setPadding(FILE_ATTACHMENT_VIEW_PADDING)
            // attachmentLongClickListener = AttachmentLongClickListener {
            //     this@FileAttachmentViewHolder.onMessageLongClick(message)
            // }
            // attachmentClickListener = AttachmentClickListener {
            //     attachmentClickListener.onAttachmentClick(message, it)
            // }
            // attachmentDownloadClickListener = AttachmentDownloadClickListener {
            //     attachmentDownloadClickListener.onAttachmentDownloadClick(it)
            // }
            setAttachments(listOf(item))
        }
    }
}
