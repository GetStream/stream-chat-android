package io.getstream.chat.android.ui.message.list.adapter.view.internal

import io.getstream.chat.android.client.models.Attachment

public fun interface AttachmentClickListener {
    public fun onAttachmentClick(attachment: Attachment)
}

public fun interface AttachmentLongClickListener {
    public fun onAttachmentLongClick()
}

public fun interface AttachmentDownloadClickListener {
    public fun onAttachmentDownloadClick(attachment: Attachment)
}
