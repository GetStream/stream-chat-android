package io.getstream.chat.android.offline.message

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import java.util.regex.Pattern

private val COMMAND_PATTERN = Pattern.compile("^/[a-z]*$")

// TODO: type should be a sealed/class or enum at the client level
internal fun getMessageType(message: Message): String {
    val hasAttachments = message.attachments.isNotEmpty()
    val hasAttachmentsToUpload = message.attachments.any { attachment ->
        attachment.uploadState is Attachment.UploadState.InProgress
    }

    return if (COMMAND_PATTERN.matcher(message.text).find() || (hasAttachments && hasAttachmentsToUpload)) {
        "ephemeral"
    } else {
        "regular"
    }
}
