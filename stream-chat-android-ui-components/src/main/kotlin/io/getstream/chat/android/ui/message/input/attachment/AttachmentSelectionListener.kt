package io.getstream.chat.android.ui.message.input.attachment

import com.getstream.sdk.chat.model.AttachmentMetaData

public fun interface AttachmentSelectionListener {
    /**
     * Called when attachment picking has been completed.
     *
     * @param attachments The set of selected attachments
     * @param attachmentSource The source that the attachments were obtained from, see [AttachmentSource]
     */
    public fun onAttachmentsSelected(attachments: Set<AttachmentMetaData>, attachmentSource: AttachmentSource)
}

public enum class AttachmentSource {
    MEDIA,
    FILE,
    CAMERA,
}
