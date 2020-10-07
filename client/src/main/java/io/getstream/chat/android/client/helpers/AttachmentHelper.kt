package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.models.Attachment

class AttachmentHelper {

    fun hasValidUrl(attachment: Attachment): Boolean {
        if (attachment.url == null) {
            return false
        }
        return false
    }
}
