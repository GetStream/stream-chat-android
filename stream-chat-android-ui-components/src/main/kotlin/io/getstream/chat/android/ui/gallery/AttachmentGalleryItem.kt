package io.getstream.chat.android.ui.gallery

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.User
import java.util.Date

public data class AttachmentGalleryItem(
    val attachment: Attachment,
    val user: User,
    val createdAt: Date,
    val messageId: String,
    val cid: String,
    val isMine: Boolean,
)
