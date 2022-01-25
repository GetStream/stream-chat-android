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

        // Check if the base of Attachment.mimeType is audio or video
        if (!mimeType.isNullOrEmpty() && (mimeType.contains(ModelType.attach_audio) || mimeType.contains(ModelType.attach_video))) return true

        // If the previous check fails check Attachment.type for file type
        if (type.isNullOrEmpty()) return false
        else if (type == ModelType.attach_audio || type == ModelType.attach_video) return true

        // Fallback in case we receive an incomplete mime type and both previous checks fail
        return if (mimeType == null) {
            false
        } else {
            val supportedMimeSubTypes = buildMimeSubTypeList()
            supportedMimeSubTypes.any { subtype -> subtype in mimeType }
        }
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

    /**
     * Provides a list of MIME subtypes.
     */
    private fun buildMimeSubTypeList() = listOf(
        // mp3
        "mpeg-3", "x-mpeg3", "mp3", "mpeg", "x-mpeg",
        // aac
        "aac",
        // webm
        "webm",
        // wav
        "wav", "x-wav",
        // flac
        "flac", "x-flac",
        // ac3
        "ac3",
        // ogg
        "ogg", "x-ogg",
        // mp4
        "mp4",
        // m4a
        "x-m4a",
        // matroska
        "x-matroska",
        // vorbis
        "vorbis",
        // quicktime
        "quicktime"
    )
}
