package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.models.Attachment

class AttachmentHelper {

    fun hasValidUrl(attachment: Attachment): Boolean {
        val url = attachment.url ?: return false
        if (URL_PATTERN.matches(url).not()) return false
        return true
    }

    companion object {
        private val URL_PATTERN = Regex("^((https?|ftp|smtp):\\/\\/)?(www.)?[a-z0-9]+\\.[a-z]+(\\/[a-zA-Z0-9#]+\\/?)*\$")
    }
}
