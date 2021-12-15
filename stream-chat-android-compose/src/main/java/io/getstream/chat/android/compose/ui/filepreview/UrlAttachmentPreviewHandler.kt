package io.getstream.chat.android.compose.ui.filepreview

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Attachment

/**
 * Shows a preview for an URL in the attachment using the [Intent.ACTION_VIEW] action.
 */
public class UrlAttachmentPreviewHandler(private val context: Context) : AttachmentPreviewHandler {

    override fun canHandle(attachment: Attachment): Boolean {
        return !getAttachmentUrl(attachment).isNullOrEmpty()
    }

    override fun handleAttachmentPreview(attachment: Attachment) {
        val url = getAttachmentUrl(attachment)
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun getAttachmentUrl(attachment: Attachment): String? {
        with(attachment) {
            return when (type) {
                ModelType.attach_image -> {
                    when {
                        titleLink != null -> titleLink
                        ogUrl != null -> ogUrl
                        assetUrl != null -> assetUrl
                        else -> imageUrl
                    }
                }
                else -> url
            }
        }
    }
}
