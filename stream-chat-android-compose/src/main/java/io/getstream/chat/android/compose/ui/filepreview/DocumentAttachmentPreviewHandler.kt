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
        if (attachment.assetUrl.isNullOrEmpty()) return false

        val mimeType = attachment.mimeType
        return mimeType == ModelType.attach_mime_doc ||
            mimeType == ModelType.attach_mime_txt ||
            mimeType == ModelType.attach_mime_pdf ||
            mimeType == ModelType.attach_mime_html ||
            mimeType?.contains(ModelType.attach_mime_vnd) == true
    }

    override fun handleAttachmentPreview(attachment: Attachment) {
        context.startActivity(AttachmentDocumentActivity.getIntent(context, attachment.assetUrl))
    }
}
