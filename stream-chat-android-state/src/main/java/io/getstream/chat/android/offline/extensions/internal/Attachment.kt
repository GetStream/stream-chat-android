package io.getstream.chat.android.offline.extensions.internal

import io.getstream.chat.android.client.models.Attachment

/**
 * Uses a regex pattern to extract the file name from the attachment URL.
 *
 * Useful in situations where no attachment name or title has been provided and
 * we need a name in order to download the file to storage.
 */
internal fun Attachment.getAttachmentFallbackName(): String? {
    val url = when (this.type) {
        "image" -> this.imageUrl ?: this.thumbUrl ?: this.assetUrl
        else -> this.assetUrl ?: this.imageUrl ?: this.thumbUrl
    }

    return url?.let { Regex("""[^\/]*(?=\?)""").find(it)?.value }
}