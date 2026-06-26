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

package io.getstream.chat.android.ui.common.images.internal

import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC
import android.os.Build
import coil3.Extras
import coil3.ImageLoader
import coil3.Uri
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.getExtra
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.Options
import coil3.size.pxOrElse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Offset of the extracted frame, kept slightly above zero so the very first frame
 * (often black) is skipped. Matches the iOS preview offset.
 */
private const val VIDEO_FRAME_MICROS = 100_000L

/**
 * Marks a request so [VideoFrameFetcher] extracts a preview frame from the video instead of
 * downloading it through the default network fetcher.
 */
internal val videoFramePreviewKey: Extras.Key<Boolean> = Extras.Key(default = false)

/**
 * Marks this request as a video preview, so the frame is extracted with [MediaMetadataRetriever]
 * range reads rather than downloading the whole file.
 */
internal fun ImageRequest.Builder.videoFramePreview(): ImageRequest.Builder = apply {
    extras[videoFramePreviewKey] = true
}

/**
 * A Coil [Fetcher] that extracts a preview frame from a remote video using [MediaMetadataRetriever].
 *
 * Decoding through `VideoFrameDecoder` requires the whole file to be downloaded first.
 * [MediaMetadataRetriever] instead seeks with HTTP range requests and reads only the bytes needed
 * for the requested frame, and the full video is never written to the image disk cache. Only
 * requests marked with [videoFramePreview] are handled; all others fall through to the default
 * network fetcher.
 *
 * The data and headers reaching this fetcher are already resolved by the upstream interceptors
 * (including CDN signing), so the URL is used as-is.
 */
internal class VideoFrameFetcher(
    private val data: Uri,
    private val options: Options,
) : Fetcher {

    override suspend fun fetch(): FetchResult = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(data.toString(), options.requestHeaders())
            val bitmap = retriever.extractFrame()
                ?: error("Could not extract a preview frame from video: $data")
            ImageFetchResult(
                image = bitmap.asImage(),
                isSampled = false,
                dataSource = DataSource.NETWORK,
            )
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                retriever.close()
            } else {
                @Suppress("DEPRECATION")
                retriever.release()
            }
        }
    }

    private fun Options.requestHeaders(): Map<String, String> =
        httpHeaders.asMap().mapNotNull { (name, values) ->
            values.lastOrNull()?.let { name to it }
        }.toMap()

    private fun MediaMetadataRetriever.extractFrame(): android.graphics.Bitmap? {
        val dstWidth = options.size.width.pxOrElse { 0 }
        val dstHeight = options.size.height.pxOrElse { 0 }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && dstWidth > 0 && dstHeight > 0) {
            getScaledFrameAtTime(VIDEO_FRAME_MICROS, OPTION_CLOSEST_SYNC, dstWidth, dstHeight)
        } else {
            getFrameAtTime(VIDEO_FRAME_MICROS, OPTION_CLOSEST_SYNC)
        }
    }

    class Factory : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (!options.getExtra(videoFramePreviewKey)) return null
            return VideoFrameFetcher(data, options)
        }
    }
}
