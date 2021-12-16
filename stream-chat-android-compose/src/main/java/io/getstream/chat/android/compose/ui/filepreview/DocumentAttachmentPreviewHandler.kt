package io.getstream.chat.android.compose.ui.filepreview

import android.content.Context
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.view.activity.AttachmentDocumentActivity
import io.getstream.chat.android.client.models.Attachment

/**
 * Shows a preview for the document in the attachment using Google Docs.
 */
public class DocumentAttachmentPreviewHandler(private val context: Context) : AttachmentPreviewHandler {

    override fun canHandle(attachment: Attachment): Boolean {
        val assetUrl = attachment.assetUrl
        val mimeType = attachment.mimeType

        if (assetUrl.isNullOrEmpty()) return false
        if (mimeType.isNullOrEmpty()) return false

        val supportedMimeTypes = listOf(
            ModelType.attach_mime_doc,
            ModelType.attach_mime_txt,
            ModelType.attach_mime_pdf,
            ModelType.attach_mime_html,
        )

        return mimeType in supportedMimeTypes ||
            // For compatibility with other client SDKs
            mimeType.contains(ModelType.attach_mime_vnd)
    }

    override fun handleAttachmentPreview(attachment: Attachment) {
        context.startActivity(AttachmentDocumentActivity.getIntent(context, attachment.assetUrl))
    }
}
