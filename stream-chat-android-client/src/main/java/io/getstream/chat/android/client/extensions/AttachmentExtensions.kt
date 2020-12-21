package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.models.Attachment

internal const val ATTACHMENT_TYPE_IMAGE = "image"
internal const val ATTACHMENT_TYPE_FILE = "file"

internal val Attachment.isImage: Boolean
    get() = mimeType?.startsWith(ATTACHMENT_TYPE_IMAGE) ?: false
