package io.getstream.chat.android.ui.gallery.overview

import io.getstream.chat.android.client.models.User
import java.util.Date

public data class UserMediaAttachment(
    val imageUrl: String,
    val user: User? = null,
    val createdAt: Date? = null,
)
