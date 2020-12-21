package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.models.Attachment

internal val Attachment.isImage: Boolean
    get() = mimeType?.startsWith("image") ?: false
