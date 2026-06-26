/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.compose.ui.util

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.annotation.WorkerThread
import androidx.exifinterface.media.ExifInterface
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.models.Attachment
import java.io.File

/**
 * Resolves the original dimensions of locally picked image and video attachments by decoding the
 * cached file, so that [Attachment.originalWidth] and [Attachment.originalHeight] are populated
 * before upload.
 *
 * This is the only place that ever sees the file bytes for attachments uploaded through a custom
 * CDN, so it is the only place dimensions can be backfilled client-side.
 *
 * IMPORTANT: decoding reads the file from disk and must be called off the main thread.
 */
internal object LocalAttachmentDimensionsResolver {

    /**
     * Returns [attachment] with [Attachment.originalWidth]/[Attachment.originalHeight] decoded from
     * [file]. Unchanged if dimensions are already set, [file] is `null`, or decoding fails.
     */
    @WorkerThread
    fun resolveDimensions(attachment: Attachment, file: File?): Attachment {
        if (file == null) return attachment
        if (attachment.originalWidth != null || attachment.originalHeight != null) return attachment

        val (width, height) = resolveLocalDimensions(file, attachment)
        return if (width != null || height != null) {
            attachment.copy(originalWidth = width, originalHeight = height)
        } else {
            attachment
        }
    }

    @Suppress("MagicNumber")
    private fun resolveLocalDimensions(file: File, attachment: Attachment): Pair<Int?, Int?> = when {
        attachment.isImage() -> {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)
            val w = options.outWidth.takeIf { it > 0 }
            val h = options.outHeight.takeIf { it > 0 }
            if (w != null && h != null && hasSwappedExifDimensions(file)) h to w else w to h
        }

        attachment.isVideo() -> {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(file.absolutePath)
                val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    ?.toIntOrNull()?.takeIf { it > 0 }
                val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    ?.toIntOrNull()?.takeIf { it > 0 }
                val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    ?.toIntOrNull() ?: 0
                if (rotation == 90 || rotation == 270) h to w else w to h
            } catch (_: Exception) {
                null to null
            } finally {
                retriever.release()
            }
        }

        else -> null to null
    }

    private fun hasSwappedExifDimensions(file: File): Boolean = try {
        val orientation = ExifInterface(file.absolutePath)
            .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
            orientation == ExifInterface.ORIENTATION_ROTATE_270 ||
            orientation == ExifInterface.ORIENTATION_TRANSPOSE ||
            orientation == ExifInterface.ORIENTATION_TRANSVERSE
    } catch (_: Exception) {
        false
    }
}
