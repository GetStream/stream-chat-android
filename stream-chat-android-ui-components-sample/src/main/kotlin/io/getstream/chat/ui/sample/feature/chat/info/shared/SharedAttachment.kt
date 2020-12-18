package io.getstream.chat.ui.sample.feature.chat.info.shared

import io.getstream.chat.android.client.models.Attachment
import java.util.Date

sealed class SharedAttachment {

    val id: String?
        get() = when (this) {
            is AttachmentItem -> attachment.name
            is DateDivider -> date.toString()
        }

    data class AttachmentItem(val attachment: Attachment) : SharedAttachment()
    data class DateDivider(val date: Date) : SharedAttachment()
}
