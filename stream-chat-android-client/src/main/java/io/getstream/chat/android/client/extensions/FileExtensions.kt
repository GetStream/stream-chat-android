package io.getstream.chat.android.client.extensions

import android.webkit.MimeTypeMap
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import java.io.File

internal fun File.getMimeType(): String =
    MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"

internal fun File.getMediaType(): MediaType = getMimeType().toMediaType()
