package io.getstream.chat.android.ui.message.list.adapter.attachments

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.extensions.internal.isMedia
import io.getstream.chat.android.ui.message.list.FileAttachmentViewStyle
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener

internal class AttachmentAdapterFactory(
    private val attachmentClickListener: AttachmentClickListener,
    private val attachmentLongClickListener: AttachmentLongClickListener,
    private val attachmentDownloadClickListener: AttachmentDownloadClickListener,
    private val style: FileAttachmentViewStyle,
) {

    fun adapter(attachments: List<Attachment>): FileAttachmentsAdapter {
        return when {
            attachments.isMedia() -> FileAttachmentsAdapter(
                attachmentClickListener,
                attachmentLongClickListener,
                attachmentDownloadClickListener,
                style
            )

            attachments.isNotEmpty() -> FileAttachmentsAdapter(
                attachmentClickListener,
                attachmentLongClickListener,
                attachmentDownloadClickListener,
                style
            )

            else -> error("Unsupported case for attachment adapter factory!")
        }
    }
}
