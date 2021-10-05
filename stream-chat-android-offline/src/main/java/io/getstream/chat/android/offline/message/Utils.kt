package io.getstream.chat.android.offline.message

import io.getstream.chat.android.client.models.Message
import java.util.regex.Pattern

private val COMMAND_PATTERN = Pattern.compile("^/[a-z]*$")

// TODO: type should be a sealed/class or enum at the client level
internal fun getMessageType(message: Message): String {
    val hasAttachments = message.attachments.isNotEmpty()
    val hasAttachmentsToUpload = message.hasPendingAttachments()

    return if (COMMAND_PATTERN.matcher(message.text).find() || (hasAttachments && hasAttachmentsToUpload)) {
        Message.TYPE_EPHEMERAL
    } else {
        Message.TYPE_REGULAR
    }
}
