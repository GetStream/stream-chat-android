package io.getstream.chat.android.ui.message.list.adapter.attachments

import io.getstream.chat.android.ui.message.list.FileAttachmentViewStyle
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener

internal class AttachmentsAdapterFactory(
    private var attachmentClickListener: AttachmentClickListener? = null,
    private var attachmentLongClickListener: AttachmentLongClickListener? = null,
    private var attachmentDownloadClickListener: AttachmentDownloadClickListener? = null,
    private var style: FileAttachmentViewStyle,
) {

    fun getAdapter(): FileAttachmentsAdapter = FileAttachmentsAdapter(
        attachmentClickListener = { attachmentClickListener?.onAttachmentClick(it) },
        attachmentLongClickListener = { attachmentLongClickListener?.onAttachmentLongClick() },
        attachmentDownloadClickListener = { attachmentDownloadClickListener?.onAttachmentDownloadClick(it) },
        // style,
    )
}
