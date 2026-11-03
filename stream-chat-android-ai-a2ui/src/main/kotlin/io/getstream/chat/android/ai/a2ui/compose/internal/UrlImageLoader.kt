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

package io.getstream.chat.android.ai.a2ui.compose.internal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * Loads the images of surfaces from the web at the size they show at, and keeps the latest ones
 * in memory, so a reply scrolled back into view shows its images at once.
 *
 * Only `https` and `http` images load: the agent writes every URL, so nothing on the device is
 * read for it.
 */
internal object UrlImageLoader {

    private const val MAX_DOWNLOAD_BYTES = 20 * 1024 * 1024
    private const val TIMEOUT_MILLIS = 15_000
    private const val MAX_CACHE_BYTES = 16 * 1024 * 1024
    private const val CACHE_SHARE_OF_HEAP = 16

    private val cache = object : LruCache<String, Bitmap>(
        minOf(MAX_CACHE_BYTES.toLong(), Runtime.getRuntime().maxMemory() / CACHE_SHARE_OF_HEAP).toInt(),
    ) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    /** Whether [url] is one this loader fetches. */
    fun isLoadable(url: String): Boolean {
        val scheme = url.substringBefore(':', "").lowercase(Locale.ROOT)
        return scheme == "https" || scheme == "http"
    }

    /** The image at [url] at [targetSizePx], when it is in memory. */
    fun cached(url: String, targetSizePx: Int): Bitmap? = cache.get(key(url, targetSizePx))

    /** The image at [url], downsampled to about [targetSizePx], or `null` when it can't be loaded. */
    suspend fun load(url: String, targetSizePx: Int): Bitmap? {
        if (!isLoadable(url)) return null
        cached(url, targetSizePx)?.let { return it }
        val bitmap = withContext(Dispatchers.IO) {
            try {
                download(url, targetSizePx)
            } catch (error: CancellationException) {
                throw error
            } catch (_: IOException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            } catch (_: SecurityException) {
                null
            }
        } ?: return null
        cache.put(key(url, targetSizePx), bitmap)
        return bitmap
    }

    private fun key(url: String, targetSizePx: Int) = "$targetSizePx:$url"

    private fun download(url: String, targetSizePx: Int): Bitmap? {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS
            val code = connection.responseCode
            val succeeded = code >= HttpURLConnection.HTTP_OK && code < HttpURLConnection.HTTP_MULT_CHOICE
            if (!succeeded) return null
            if (connection.contentLength > MAX_DOWNLOAD_BYTES) return null
            val bytes = connection.inputStream.use { it.readAtMost(MAX_DOWNLOAD_BYTES) } ?: return null
            return decode(bytes, targetSizePx)
        } finally {
            connection.disconnect()
        }
    }

    private fun decode(bytes: ByteArray, targetSizePx: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, targetSizePx)
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    /** The largest power of two to divide an image by that keeps both its sides at least [targetSizePx]. */
    fun sampleSize(width: Int, height: Int, targetSizePx: Int): Int {
        var sampleSize = 1
        if (targetSizePx <= 0) return sampleSize
        while (width / (sampleSize * 2) >= targetSizePx && height / (sampleSize * 2) >= targetSizePx) sampleSize *= 2
        return sampleSize
    }

    private fun InputStream.readAtMost(limit: Int): ByteArray? {
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
}
