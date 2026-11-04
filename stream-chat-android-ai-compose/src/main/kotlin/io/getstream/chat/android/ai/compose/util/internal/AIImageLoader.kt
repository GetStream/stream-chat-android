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

package io.getstream.chat.android.ai.compose.util.internal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runInterruptible
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Locale

/**
 * Loads images from the web at the size they show at, and keeps the latest ones in memory, so what
 * scrolls back into view shows its images at once.
 *
 * The agent writes every link, so an image downloads only from a public host: every address the
 * host resolves to must be public, so not the device itself or the local network, and each
 * redirect, up to [ImageDownloader.MaxRedirects], is checked the same way.
 *
 * Each image downloads up to a number of bytes, and decodes upright, as its EXIF orientation says,
 * to no more than [MaxDecodedPixels]. Callers that ask for an image while it loads wait for the
 * same load, which stops once none of them waits any more, such as when the UI that asked for it
 * leaves the screen.
 */
@InternalStreamAIApi
public object AIImageLoader {

    /** The most bytes an image downloads by default. */
    public const val MaxDownloadBytes: Int = 20 * 1024 * 1024

    /** The most pixels an image decodes to: 2048 x 2048, 16 MB at 4 bytes a pixel. */
    public const val MaxDecodedPixels: Long = 2048L * 2048L

    private const val MaxCacheBytes = 32 * 1024 * 1024
    private const val CacheShareOfHeap = 8

    private val cache = object : LruCache<String, Bitmap>(
        minOf(MaxCacheBytes.toLong(), Runtime.getRuntime().maxMemory() / CacheShareOfHeap).toInt(),
    ) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    private val downloader = ImageDownloader()

    /**
     * Downloads from any host, for links from the app's own messages, such as attachments, which
     * can be on an intranet or a local backend.
     */
    internal val anyHostDownloader = ImageDownloader(publicHostsOnly = false)

    private val loads = SharedLoads<Bitmap?>(CoroutineScope(SupervisorJob() + Dispatchers.IO))

    /**
     * Whether [url] is one this loader downloads: a web link. The agent writes every link, so
     * nothing on the device is read for it.
     *
     * @param url The image's link.
     */
    public fun isLoadable(url: String): Boolean {
        val scheme = url.substringBefore(':', missingDelimiterValue = "").lowercase(Locale.ROOT)
        return scheme == "https" || scheme == "http"
    }

    /**
     * The image at [url] at [targetSizePx], when it is in memory.
     *
     * @param url The image's link.
     * @param targetSizePx The size it shows at, in pixels.
     */
    public fun cached(url: String, targetSizePx: Int): Bitmap? = cache.get(key(url, targetSizePx))

    /**
     * The image at [url], sampled down to about [targetSizePx], or `null` when it can't be loaded,
     * such as when it is larger than [maxBytes].
     *
     * @param url The image's link.
     * @param targetSizePx The size it shows at, in pixels: the image keeps both its sides at least
     * this large, unless it would decode to more than [MaxDecodedPixels].
     * @param maxBytes The most bytes it downloads.
     */
    public suspend fun load(url: String, targetSizePx: Int, maxBytes: Int = MaxDownloadBytes): Bitmap? =
        load(url, targetSizePx, maxBytes, downloader)

    /**
     * The image at [url], downloaded with [downloader], sampled down to about [targetSizePx].
     *
     * @param url The image's link.
     * @param targetSizePx The size it shows at, in pixels.
     * @param maxBytes The most bytes it downloads.
     * @param downloader Downloads it.
     */
    internal suspend fun load(url: String, targetSizePx: Int, maxBytes: Int, downloader: ImageDownloader): Bitmap? {
        if (!isLoadable(url)) return null
        return load(key(url, targetSizePx)) {
            downloader.download(url, maxBytes)?.let { decodeSampledBitmap(it, targetSizePx) }
        }
    }

    /**
     * The image [fetch] reads, kept in memory under [key], and read once at a time.
     *
     * @param key The image and the size it shows at.
     * @param fetch Reads it, blocking, or returns `null` when it can't.
     */
    @Suppress("TooGenericExceptionCaught") // Whatever goes wrong, the placeholder stays.
    internal suspend fun load(key: String, fetch: () -> Bitmap?): Bitmap? {
        cache.get(key)?.let { return it }
        return loads.load(key) {
            cache.get(key) ?: runInterruptible {
                try {
                    fetch()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    null
                }
            }?.also { cache.put(key, it) }
        }
    }

    /**
     * The key an image and the size it shows at are kept under.
     *
     * @param source The image's link or URI.
     * @param targetSizePx The size it shows at, in pixels.
     */
    internal fun key(source: String, targetSizePx: Int) = "$targetSizePx:$source"
}

/**
 * The image at [url], sampled down to about [targetSizePx], once it has loaded with
 * [AIImageLoader]; `null` until then, or when it can't be loaded. Its load stops when this leaves
 * the composition. Previews and snapshots don't load.
 *
 * @param url The image's link.
 * @param targetSizePx The size it shows at, in pixels.
 * @param maxBytes The most bytes it downloads.
 */
@InternalStreamAIApi
@Composable
public fun rememberAIImage(
    url: String,
    targetSizePx: Int,
    maxBytes: Int = AIImageLoader.MaxDownloadBytes,
): ImageBitmap? {
    var loaded by remember(url, targetSizePx) { mutableStateOf(AIImageLoader.cached(url, targetSizePx)) }
    if (loaded == null && !LocalInspectionMode.current) {
        LaunchedEffect(url, targetSizePx, maxBytes) {
            loaded = AIImageLoader.load(url, targetSizePx, maxBytes)
        }
    }
    return loaded?.let { bitmap -> remember(bitmap) { bitmap.asImageBitmap() } }
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
 * Decodes an encoded image, sampled down as [sampleSize] says and turned upright as its EXIF
 * orientation says.
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

/**
 * The power of two to divide an image by: the largest that keeps both its sides at least
 * [targetSizePx], or a larger one while the image would still decode to more than [maxPixels]. A
 * long panorama keeps its short side at the target, so its long side alone could need a bitmap too
 * large to hold or draw.
 */
internal fun sampleSize(
    width: Int,
    height: Int,
    targetSizePx: Int,
    maxPixels: Long = AIImageLoader.MaxDecodedPixels,
): Int {
    var sampleSize = 1
    if (targetSizePx > 0) {
        while (width / (sampleSize * 2) >= targetSizePx && height / (sampleSize * 2) >= targetSizePx) {
            sampleSize *= 2
        }
    }
    // A decoder can round a sampled side up.
    while (ceilDiv(width, sampleSize) * ceilDiv(height, sampleSize) > maxPixels) sampleSize *= 2
    return sampleSize
}

private fun ceilDiv(size: Int, divisor: Int): Long = (size.toLong() + divisor - 1) / divisor
