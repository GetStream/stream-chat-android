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

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import io.getstream.chat.android.ai.compose.util.internal.sampleSize

/**
 * Decodes a bitmap from a URI, downsampling it to approximately the target size, within a total
 * pixel cap, and applying the correct EXIF orientation.
 * This prevents loading huge images into memory when only a thumbnail is needed.
 *
 * @param contentResolver The content resolver to use for opening the URI
 * @param uri The URI of the image to decode
 * @param targetSize The target size in pixels for the decoded bitmap
 * @return The decoded and correctly oriented bitmap, or null if decoding failed
 */
internal fun decodeSampledBitmap(
    contentResolver: ContentResolver,
    uri: Uri,
    targetSize: Int,
): Bitmap? {
    // First, decode just the bounds to get the image dimensions
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    contentResolver.openInputStream(uri)?.use { inputStream ->
        BitmapFactory.decodeStream(inputStream, null, options)
    }

    if (options.outWidth <= 0 || options.outHeight <= 0) return null
    options.inSampleSize = sampleSize(options.outWidth, options.outHeight, targetSize)
    options.inJustDecodeBounds = false

    // Decode the bitmap with the calculated sample size
    val bitmap = contentResolver.openInputStream(uri)?.use { inputStream ->
        BitmapFactory.decodeStream(inputStream, null, options)
    } ?: return null

    // Read EXIF orientation and rotate if needed
    val orientation = contentResolver.openInputStream(uri)?.use { inputStream ->
        ExifInterface(inputStream).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
    } ?: ExifInterface.ORIENTATION_NORMAL

    return rotateBitmap(bitmap, orientation)
}

/**
 * Rotates a bitmap according to the EXIF orientation value.
 *
 * @param bitmap The bitmap to rotate
 * @param orientation The EXIF orientation value
 * @return The rotated bitmap, or the original if no rotation was needed
 */
@Suppress("MagicNumber")
private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.postRotate(90f)
            matrix.postScale(-1f, 1f)
        }

        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.postRotate(270f)
            matrix.postScale(-1f, 1f)
        }

        else -> return bitmap // No rotation needed
    }

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
        if (it != bitmap) {
            bitmap.recycle()
        }
    }
}
