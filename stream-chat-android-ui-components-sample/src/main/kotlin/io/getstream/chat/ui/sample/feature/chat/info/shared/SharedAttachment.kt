package io.getstream.chat.ui.sample.feature.chat.info.shared

import io.getstream.chat.android.client.models.AttachmentWithDate
import java.util.Date

sealed class SharedAttachment {

    val id: String?
        get() = when (this) {
            is AttachmentItem -> attachmentWithDate.attachment.name
            is DateDivider -> date.toString()
        }

    data class AttachmentItem(val attachmentWithDate: AttachmentWithDate) : SharedAttachment()
    data class DateDivider(val date: Date) : SharedAttachment()
}
