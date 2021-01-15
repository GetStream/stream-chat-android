package io.getstream.chat.ui.sample.feature.chat.info.shared

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import java.util.Date

sealed class SharedAttachment {

    val id: String
        get() = when (this) {
            is AttachmentItem -> "${message.id}-${attachment.name}"
            is DateDivider -> date.toString()
        }

    data class AttachmentItem(val message: Message, val createdAt: Date, val attachment: Attachment) : SharedAttachment()
    data class DateDivider(val date: Date) : SharedAttachment()
}
