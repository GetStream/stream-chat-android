package io.getstream.chat.android.ui.messages.adapter.view

import io.getstream.chat.android.client.models.Attachment

internal fun interface AttachmentClickListener {
    fun onAttachmentClick(attachment: Attachment)
}

internal fun interface AttachmentLongClickListener {
    fun onAttachmentLongClick()
}

internal fun interface AttachmentDownloadClickListener {
    fun onAttachmentDownloadClick(attachment: Attachment)
}
