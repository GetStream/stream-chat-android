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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.LruCache
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Loads the images of messages' attachments at the size they show at, and keeps the latest ones
 * in memory so a message scrolled back into view shows its images at once.
 *
 * Images on this device are read through the content resolver; images on the web are
 * downloaded, up to [MaxDownloadBytes].
 */
internal object AttachmentImageLoader {

    private const val MaxDownloadBytes = 20 * 1024 * 1024
    private const val TimeoutMs = 15_000
    private const val MaxCacheBytes = 32 * 1024 * 1024
    private const val CacheShareOfHeap = 8

    private val cache = object : LruCache<String, Bitmap>(
        minOf(MaxCacheBytes.toLong(), Runtime.getRuntime().maxMemory() / CacheShareOfHeap).toInt(),
    ) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    /** The image at [uri] at [targetSizePx], if it is in memory. */
    fun cached(uri: Uri, targetSizePx: Int): Bitmap? = cache.get(key(uri, targetSizePx))

    /** The image at [uri], downsampled to about [targetSizePx], or null when it can't be read. */
    @Suppress("TooGenericExceptionCaught") // Whatever goes wrong, the placeholder stays.
    suspend fun load(context: Context, uri: Uri, targetSizePx: Int): Bitmap? {
        val key = key(uri, targetSizePx)
        cache.get(key)?.let { return it }
        val bitmap = withContext(Dispatchers.IO) {
            try {
                when (uri.scheme?.lowercase()) {
                    "http", "https" -> download(uri, targetSizePx)
                    else -> decodeSampledBitmap(context.contentResolver, uri, targetSizePx)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
        } ?: return null
        cache.put(key, bitmap)
        return bitmap
    }

    private fun key(uri: Uri, targetSizePx: Int) = "$targetSizePx:$uri"

    private fun download(uri: Uri, targetSizePx: Int): Bitmap? {
        val connection = URL(uri.toString()).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TimeoutMs
            connection.readTimeout = TimeoutMs
            if (connection.responseCode !in HttpURLConnection.HTTP_OK until HttpURLConnection.HTTP_MULT_CHOICE) {
                return null
            }
            if (connection.contentLength > MaxDownloadBytes) return null
            val bytes = connection.inputStream.use { it.readAtMost(MaxDownloadBytes) } ?: return null
            return decodeSampledBitmap(bytes, targetSizePx)
        } finally {
            connection.disconnect()
        }
    }
}

/** Reads the whole stream, or returns null once it runs past [limit] bytes. */
internal fun InputStream.readAtMost(limit: Int): ByteArray? {
    val out = ByteArrayOutputStream()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0
    while (true) {
        val read = read(buffer)
        if (read < 0) break
        total += read
        if (total > limit) return null
        out.write(buffer, 0, read)
    }
    return out.toByteArray()
}

/**
 * Decodes an encoded image, downsampled to about [targetSizePx] on its shorter side and turned
 * upright as its EXIF orientation says.
 */
internal fun decodeSampledBitmap(bytes: ByteArray, targetSizePx: Int): Bitmap? {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    if (options.outWidth <= 0 || options.outHeight <= 0) return null
    options.inSampleSize = sampleSize(options.outWidth, options.outHeight, targetSizePx)
    options.inJustDecodeBounds = false
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return null
    val exif = ExifInterface(ByteArrayInputStream(bytes))
    if (exif.rotationDegrees == 0 && !exif.isFlipped) return bitmap
    // A flipped image is mirrored first, then turned.
    val matrix = Matrix().apply {
        if (exif.isFlipped) postScale(-1f, 1f)
        postRotate(exif.rotationDegrees.toFloat())
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

/** The largest power of two to divide an image by that keeps both its sides at least [targetSizePx]. */
internal fun sampleSize(width: Int, height: Int, targetSizePx: Int): Int {
    var sampleSize = 1
    if (targetSizePx <= 0) return sampleSize
    while (width / (sampleSize * 2) >= targetSizePx && height / (sampleSize * 2) >= targetSizePx) {
        sampleSize *= 2
    }
    return sampleSize
}
