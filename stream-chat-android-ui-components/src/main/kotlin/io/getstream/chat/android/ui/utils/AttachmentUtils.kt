package io.getstream.chat.android.ui.utils

import android.widget.ImageView
import com.getstream.sdk.chat.images.StreamImageLoader.ImageTransformation.RoundedCorners
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.images.loadVideoThumbnail
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise

private val FILE_THUMB_TRANSFORMATION = RoundedCorners(3.dpToPxPrecise())

internal fun ImageView.loadAttachmentThumb(attachment: Attachment) {
    with(attachment) {
        when (type) {
            ModelType.attach_video -> load(data = thumbUrl, transformation = FILE_THUMB_TRANSFORMATION)
            ModelType.attach_image -> load(data = imageUrl, transformation = FILE_THUMB_TRANSFORMATION)
            else -> load(data = UiUtils.getIcon(mimeType))
        }
    }
}

internal fun ImageView.loadAttachmentThumb(attachment: AttachmentMetaData) {
    with(attachment) {
        when (type) {
            ModelType.attach_video -> loadVideoThumbnail(
                uri = uri,
                transformation = FILE_THUMB_TRANSFORMATION
            )
            ModelType.attach_image -> load(data = uri, transformation = FILE_THUMB_TRANSFORMATION)
            else -> load(data = UiUtils.getIcon(mimeType))
        }
    }
}
