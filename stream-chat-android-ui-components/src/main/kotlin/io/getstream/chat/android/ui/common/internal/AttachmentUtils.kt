package io.getstream.chat.android.ui.common.internal

import android.webkit.MimeTypeMap
import android.widget.ImageView
import com.getstream.sdk.chat.StreamFileUtil
import com.getstream.sdk.chat.disposable.Disposable
import com.getstream.sdk.chat.images.StreamImageLoader.ImageTransformation.RoundedCorners
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.images.loadVideoThumbnail
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.getDisplayableName
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.extensions.internal.dpToPxPrecise

private val FILE_THUMB_TRANSFORMATION = RoundedCorners(3.dpToPxPrecise())

internal fun ImageView.loadAttachmentThumb(attachment: Attachment): Disposable {
    return with(attachment) {
        when {
            type == ModelType.attach_video && !thumbUrl.isNullOrBlank() ->
                load(data = thumbUrl, transformation = FILE_THUMB_TRANSFORMATION)
            type == ModelType.attach_image && !imageUrl.isNullOrBlank() ->
                load(data = imageUrl, transformation = FILE_THUMB_TRANSFORMATION)
            else -> {
                // The mime type, or a best guess based on the extension
                val actualMimeType = mimeType ?: MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(attachment.getDisplayableName()?.substringAfterLast('.'))

                // We don't have icons for image types, but we can load the actual image in this case
                if (actualMimeType?.startsWith("image") == true && attachment.upload != null) {
                    load(
                        data = StreamFileUtil.getUriForFile(context, attachment.upload!!),
                        transformation = FILE_THUMB_TRANSFORMATION
                    )
                } else {
                    load(data = ChatUI.mimeTypeIconProvider.getIconRes(actualMimeType))
                }
            }
        }
    }
}

internal fun ImageView.loadAttachmentThumb(attachment: AttachmentMetaData): Disposable {
    return with(attachment) {
        when (type) {
            ModelType.attach_video -> loadVideoThumbnail(
                uri = uri,
                transformation = FILE_THUMB_TRANSFORMATION
            )
            ModelType.attach_image -> load(data = uri, transformation = FILE_THUMB_TRANSFORMATION)
            else -> load(data = ChatUI.mimeTypeIconProvider.getIconRes(mimeType))
        }
    }
}
