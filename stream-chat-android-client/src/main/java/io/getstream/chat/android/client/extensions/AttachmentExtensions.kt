package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.models.Attachment

internal const val ATTACHMENT_TYPE_IMAGE = "image"
internal const val ATTACHMENT_TYPE_FILE = "file"
private const val EXTRA_UPLOAD_COMPLETE: String = "uploadComplete"

internal val Attachment.isImage: Boolean
    get() = mimeType?.startsWith(ATTACHMENT_TYPE_IMAGE) ?: false

public var Attachment.uploadComplete: Boolean?
    get() = extraData[EXTRA_UPLOAD_COMPLETE] as Boolean?
    set(value) {
        value?.let { extraData[EXTRA_UPLOAD_COMPLETE] = it }
    }
