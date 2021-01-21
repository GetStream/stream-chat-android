package io.getstream.chat.android.chat.navigation

import android.content.Context
import android.widget.Toast
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.navigation.destinations.AttachmentDestination
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.images.AttachmentGalleryActivity
import io.getstream.chat.android.ui.utils.extensions.getCreatedAtOrThrow
import java.io.Serializable

public class GalleryImageAttachmentDestination(
    message: Message,
    attachment: Attachment,
    context: Context,
    private val attachmentReplyOptionHandler: AttachmentGalleryActivity.AttachmentOptionReplyHandler,
    private val attachmentShowInChatOptionHandler: AttachmentGalleryActivity.AttachmentOptionShowInChatHandler,
) : AttachmentDestination(message, attachment, context), Serializable {
    override fun showImageViewer(message: Message, attachment: Attachment) {
        val attachments =
            message.attachments.filter { it.type == ModelType.attach_image && !it.imageUrl.isNullOrEmpty() }

        if (attachments.isEmpty()) {
            Toast.makeText(context, "Invalid image(s)!", Toast.LENGTH_SHORT).show()
            return
        }

        val createdAt = message.getCreatedAtOrThrow().time
        val attachmentIndex = message.attachments.indexOf(attachment)

        start(
            AttachmentGalleryActivity.createIntent(context,
                createdAt,
                attachmentIndex,
                message,
                attachments,
                attachmentShowInChatOptionHandler,
                attachmentReplyOptionHandler)
        )
    }
}
