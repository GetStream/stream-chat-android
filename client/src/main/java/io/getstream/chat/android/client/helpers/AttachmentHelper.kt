package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.utils.SystemTimeProvider
import org.apache.commons.validator.routines.UrlValidator

class AttachmentHelper(private val systemTimeProvider: SystemTimeProvider = SystemTimeProvider()) {

    private val urlValidator = UrlValidator()

    fun hasValidUrl(attachment: Attachment): Boolean {
        val url = attachment.url ?: return false
        if (urlValidator.isValid(url).not()) return false
        if (url.contains(KEY_WORD_EXPIRES))  {
            if (parseTimeStampOrNull(url) == null) return false
        }
        return true
    }

    private fun parseTimeStampOrNull(url: String): Long? {
        return url.substringAfter(KEY_WORD_EXPIRES).substringBefore(CHAR_AMPERSAND).toLongOrNull()
    }

    companion object {
        private const val CHAR_AMPERSAND = "&"
        private const val KEY_WORD_EXPIRES = "Expires="
    }
}
