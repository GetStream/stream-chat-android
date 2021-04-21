package io.getstream.chat.android.ui.common.internal

import android.content.Context
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.getstream.sdk.chat.StreamFileProvider
import com.getstream.sdk.chat.images.StreamImageLoader.ImageTransformation.RoundedCorners
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.images.loadVideoThumbnail
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.extensions.getDisplayableName
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.uploader.ProgressTrackerFactory
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.dpToPxPrecise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine

private val FILE_THUMB_TRANSFORMATION = RoundedCorners(3.dpToPxPrecise())

internal fun ImageView.loadAttachmentThumb(attachment: Attachment) {
    with(attachment) {
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
                        data = StreamFileProvider.getUriForFile(context, attachment.upload!!),
                        transformation = FILE_THUMB_TRANSFORMATION
                    )
                } else {
                    load(data = ChatUI.mimeTypeIconProvider.getIconRes(actualMimeType))
                }
            }
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
            else -> load(data = ChatUI.mimeTypeIconProvider.getIconRes(mimeType))
        }
    }
}

internal object AttachmentUtils {
    internal suspend fun trackFilesSent(context: Context, uploadIdList: List<String>, sentFilesView: TextView) {
        val filesSent = 0
        val totalFiles = uploadIdList.size

        sentFilesView.isVisible = true
        sentFilesView.text = context.getString(R.string.stream_ui_upload_sending, filesSent, totalFiles)

        val completionFlows: List<Flow<Boolean>> = uploadIdList.map { uploadId ->
            ProgressTrackerFactory.getOrCreate(uploadId).isComplete()
        }

        combine(completionFlows) { isCompleteArray ->
            isCompleteArray.count { isComplete -> isComplete }
        }.collect { completedCount ->
            if (completedCount == totalFiles) {
                sentFilesView.text = context.getString(R.string.stream_ui_upload_complete)
            } else {
                sentFilesView.text = context.getString(R.string.stream_ui_upload_sending, completedCount, totalFiles)
            }
        }
    }
}
