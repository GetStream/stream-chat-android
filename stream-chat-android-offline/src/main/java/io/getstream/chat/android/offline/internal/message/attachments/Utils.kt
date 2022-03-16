package io.getstream.chat.android.offline.internal.message.attachments

import java.util.UUID

internal fun generateUploadId(): String {
    return "upload_id_${UUID.randomUUID()}"
}
