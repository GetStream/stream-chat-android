package io.getstream.chat.android.compose.ui.filepreview

import android.content.Context
import io.getstream.chat.android.client.models.Attachment

/**
 * Represents a handler that can show a preview for an attachment of certain type.
 */
public interface AttachmentPreviewHandler {

    /**
     * Determines if the current preview handler is responsible for showing preview for the given attachment.
     *
     * @param attachment The attachment we want to show the preview for.
     * @return True if the current handler can provide a preview for this attachment.
     */
    public fun canHandle(attachment: Attachment): Boolean

    /**
     * Shows a preview for this attachment using the current preview handler.
     *
     * @param attachment The attachment we want to show the preview for.
     */
    public fun handleAttachmentPreview(attachment: Attachment)

    public companion object {
        /**
         * Builds the default list of file preview providers.
         *
         * @param context The context to start the preview Activity with.
         * @return The list handlers that can be used to show a preview for an attachment.
         */
        public fun defaultAttachmentHandlers(context: Context): List<AttachmentPreviewHandler> {
            return listOf(
                MediaAttachmentPreviewHandler(context),
                DocumentAttachmentPreviewHandler(context),
                UrlAttachmentPreviewHandler(context)
            )
        }
    }
}
