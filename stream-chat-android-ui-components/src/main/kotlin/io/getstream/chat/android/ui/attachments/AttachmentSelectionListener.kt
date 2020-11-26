package io.getstream.chat.android.ui.attachments

import com.getstream.sdk.chat.model.AttachmentMetaData

internal interface AttachmentSelectionListener {
    fun onAttachmentsSelected(attachments: Set<AttachmentMetaData>, attachmentSource: AttachmentSource)
}

internal enum class AttachmentSource {
    MEDIA,
    FILE,
    CAMERA
}
