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
        val assetUrl = attachment.assetUrl
        val mimeType = attachment.mimeType
        val type = attachment.type

        if (assetUrl.isNullOrEmpty()) return false
        if (mimeType.isNullOrEmpty()) return false
        if (type.isNullOrEmpty()) return false

        val supportedMimeTypes = listOf(
            MIME_TYPE_VIDEO,
            MIME_TYPE_AUDIO,
            // For compatibility with other client SDKs
            MIME_SUBTYPE_MP4,
            MIME_SUBTYPE_QUICKTIME
        )

        return supportedMimeTypes.any { mimeType.contains(it) } ||
            type == ModelType.attach_audio ||
            type == ModelType.attach_video
    }

    override fun handleAttachmentPreview(attachment: Attachment) {
        context.startActivity(
            MediaPreviewActivity.getIntent(
                context = context,
                url = requireNotNull(attachment.assetUrl),
                title = attachment.title ?: attachment.name
            )
        )
    }

    private companion object {
        private const val MIME_TYPE_VIDEO = "video"
        private const val MIME_TYPE_AUDIO = "audio"
        private const val MIME_SUBTYPE_MP4 = "mp4"
        private const val MIME_SUBTYPE_QUICKTIME = "quicktime"
    }
}
