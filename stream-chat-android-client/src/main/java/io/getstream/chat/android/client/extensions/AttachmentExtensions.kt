package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.models.Attachment

internal const val ATTACHMENT_TYPE_IMAGE = "image"
internal const val ATTACHMENT_TYPE_FILE = "file"
private const val EXTRA_UPLOAD_ID: String = "uploadId"

internal val Attachment.isImage: Boolean
    get() = mimeType?.startsWith(ATTACHMENT_TYPE_IMAGE) ?: false

public var Attachment.uploadId: String?
    get() = extraData[EXTRA_UPLOAD_ID] as String?
    set(value) {
        value?.let { extraData[EXTRA_UPLOAD_ID] = it }
    }
