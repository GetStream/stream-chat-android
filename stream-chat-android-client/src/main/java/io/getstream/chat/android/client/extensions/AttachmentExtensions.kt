package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Attachment.Companion.EXTRA_UPLOAD_COMPLETE

internal const val ATTACHMENT_TYPE_IMAGE = "image"
internal const val ATTACHMENT_TYPE_FILE = "file"

internal val Attachment.isImage: Boolean
    get() = mimeType?.startsWith(ATTACHMENT_TYPE_IMAGE) ?: false

public val Attachment.uploadComplete: Boolean
    get() = extraData[EXTRA_UPLOAD_COMPLETE] as Boolean
