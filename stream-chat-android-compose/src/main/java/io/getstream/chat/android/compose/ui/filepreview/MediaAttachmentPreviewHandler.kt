package io.getstream.chat.android.compose.ui.filepreview

import android.content.Context
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.ui.mediapreview.MediaPreviewActivity

/**
 * Shows a preview for the audio/video stream in the attachment using Exoplayer library.
 */
public class MediaAttachmentPreviewHandler(private val context: Context) : AttachmentPreviewHandler {

    override fun canHandle(attachment: Attachment): Boolean {
        if (attachment.assetUrl.isNullOrEmpty()) return false

        val type = attachment.type
        val mimeType = attachment.mimeType

        return type == ModelType.attach_audio ||
            type == ModelType.attach_video ||
            mimeType?.contains(MIME_TYPE_VIDEO) == true ||
            mimeType?.contains(MIME_TYPE_AUDIO) == true ||
            // For compatibility with other client SDKs
            mimeType?.contains(MIME_SUBTYPE_MP4) == true ||
            mimeType?.contains(MIME_SUBTYPE_QUICKTIME) == true
    }

    override fun handleAttachmentPreview(attachment: Attachment) {
        val url = requireNotNull(attachment.assetUrl)
        context.startActivity(MediaPreviewActivity.getIntent(context, url))
    }

    private companion object {
        private const val MIME_TYPE_VIDEO = "video"
        private const val MIME_TYPE_AUDIO = "audio"
        private const val MIME_SUBTYPE_MP4 = "mp4"
        private const val MIME_SUBTYPE_QUICKTIME = "quicktime"
    }
}
