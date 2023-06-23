package io.getstream.chat.android.client.attachment

import io.getstream.chat.android.models.Message
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result

internal class AttachmentsVerifier {

    private val logger by taggedLogger("Chat:AttachmentVerifier")

    internal fun verifyAttachments(result: Result<Message>): Result<Message> {
        val message = result.getOrNull() ?: return result
        logger.d { "[verifyAttachments] #uploader; uploadedAttachments: ${message.attachments}" }
        val corruptedAttachment = message.attachments.find {
            it.upload != null && it.imageUrl == null && it.assetUrl == null
        }
        if (corruptedAttachment != null) {
            logger.e {
                "[verifyAttachments] #uploader; message(${message.id}) has corrupted attachment: $corruptedAttachment"
            }
            return Result.Failure(
                Error.GenericError("Message(${message.id}) contains corrupted attachment: $corruptedAttachment")
            )
        }
        return result
    }

}