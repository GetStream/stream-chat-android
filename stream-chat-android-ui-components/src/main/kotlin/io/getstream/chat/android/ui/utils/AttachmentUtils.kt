package io.getstream.chat.android.ui.utils

import android.widget.ImageView
import com.getstream.sdk.chat.ImageLoader.ImageTransformation.RoundedCorners
import com.getstream.sdk.chat.ImageLoader.load
import com.getstream.sdk.chat.ImageLoader.loadVideoThumbnail
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise

private val FILE_THUMB_TRANSFORMATION = RoundedCorners(3.dpToPxPrecise())

internal fun ImageView.loadAttachmentThumb(attachment: Attachment) {
    with(attachment) {
        when (type) {
            ModelType.attach_video -> load(thumbUrl, FILE_THUMB_TRANSFORMATION)
            ModelType.attach_image -> load(imageUrl, FILE_THUMB_TRANSFORMATION)
            else -> load(UiUtils.getIcon(mimeType))
        }
    }
}

internal fun ImageView.loadAttachmentThumb(attachment: AttachmentMetaData) {
    with(attachment) {
        when (type) {
            ModelType.attach_video -> loadVideoThumbnail(uri, null, FILE_THUMB_TRANSFORMATION)
            ModelType.attach_image -> load(uri, FILE_THUMB_TRANSFORMATION)
            else -> load(UiUtils.getIcon(mimeType))
        }
    }
}
