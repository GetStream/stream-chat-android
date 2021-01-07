package io.getstream.chat.android.ui.utils

import android.net.Uri
import android.widget.ImageView
import com.getstream.sdk.chat.ImageLoader.ImageTransformation.RoundedCorners
import com.getstream.sdk.chat.ImageLoader.load
import com.getstream.sdk.chat.ImageLoader.loadVideoThumbnail
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise

private val THUMB_ROUNDED_CORNERS_TRANSFORMATION = RoundedCorners(3.dpToPxPrecise())

internal fun ImageView.loadAttachmentThumb(attachment: Attachment) {
    loadAttachmentThumb(
        type = attachment.type,
        mimeType = attachment.mimeType,
        imageThumb = attachment.imageUrl,
        videoThumb = attachment.thumbUrl
    )
}

internal fun ImageView.loadAttachmentThumb(attachment: AttachmentMetaData) {
    loadAttachmentThumb(
        type = attachment.type,
        mimeType = attachment.mimeType,
        imageThumb = attachment.uri,
        videoThumb = attachment.uri
    )
}

private fun ImageView.loadAttachmentThumb(
    type: String?,
    mimeType: String?,
    imageThumb: Any?,
    videoThumb: Any?
) {
    when (type) {
        ModelType.attach_video -> {
            if (videoThumb is String) {
                load(videoThumb, THUMB_ROUNDED_CORNERS_TRANSFORMATION)
            } else if (videoThumb is Uri) {
                loadVideoThumbnail(videoThumb, THUMB_ROUNDED_CORNERS_TRANSFORMATION)
            }
        }
        ModelType.attach_image -> load(imageThumb, THUMB_ROUNDED_CORNERS_TRANSFORMATION)
        else -> load(UiUtils.getIcon(mimeType))
    }
}
