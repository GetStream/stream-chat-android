package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.models.Attachment
import org.apache.commons.validator.routines.UrlValidator

class AttachmentHelper {

    private val urlValidator = UrlValidator()

    fun hasValidUrl(attachment: Attachment): Boolean {
        val url = attachment.url ?: return false
        if (urlValidator.isValid(url).not()) return false
        return true
    }
}
