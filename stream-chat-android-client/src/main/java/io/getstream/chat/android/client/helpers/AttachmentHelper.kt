package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.utils.SystemTimeProvider
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

public class AttachmentHelper(private val systemTimeProvider: SystemTimeProvider = SystemTimeProvider()) {

    public fun hasValidImageUrl(attachment: Attachment): Boolean {
        val url = attachment.imageUrl?.toHttpUrlOrNull() ?: return false
        if (url.queryParameterNames.contains(QUERY_KEY_NAME_EXPIRES).not()) {
            return true
        }
        val timestamp = url.queryParameter(QUERY_KEY_NAME_EXPIRES)?.toLongOrNull() ?: return false
        return timestamp > systemTimeProvider.provideCurrentTimeInSeconds()
    }

    public fun hasStreamImageUrl(attachment: Attachment): Boolean {
        return attachment.imageUrl?.toHttpUrlOrNull()?.host?.contains(STREAM_CDN_HOST_PART, ignoreCase = true) ?: false
    }

    private companion object {
        private const val QUERY_KEY_NAME_EXPIRES = "Expires"
        private const val STREAM_CDN_HOST_PART = "stream-chat"
    }
}
