package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.view.View
import android.view.ViewGroup
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.message.list.adapter.view.internal.QuotedAttachmentView

/**
 * Factory for attachments we support by default.
 */
internal class QuotedAttachmentMessageFactory: QuotedAttachmentFactory {

    /**
     * @param message The quoted message with the attachments we wish to render.
     *
     * @return If the factory can handle the given quoted message attachment or not.
     */
    override fun canHandle(message: Message): Boolean {
        val attachmentType = message.attachments.firstOrNull()?.type ?: return false

       return attachmentType == ModelType.attach_file || attachmentType == ModelType.attach_image
           || attachmentType == ModelType.attach_giphy || attachmentType == ModelType.attach_video
    }

    /**
     * Generates a [QuotedAttachmentView] to render the attachment.
     *
     * @return [QuotedAttachmentView] that will be rendered inside the quoted message.
     */
    override fun generateQuotedAttachmentView(message: Message, parent: ViewGroup): View {
        return QuotedAttachmentView(parent.context).apply {
            showAttachment(message.attachments.first())
        }
    }
}