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

import coil3.intercept.Interceptor
import coil3.request.ImageResult
import coil3.request.SuccessResult
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Image request data for a video preview that should be loaded from the server thumbnail when
 * available, and fall back to a frame extracted from the video itself when the thumbnail is
 * missing or fails to load.
 *
 * Pass an instance of this as the Coil request data (instead of a raw URL) to opt into the
 * fallback handled by [VideoThumbnailFallbackInterceptor].
 *
 * @param thumbnailUrl The server-provided thumbnail URL, already transformed for CDN resizing if enabled.
 * @param videoUrl The URL of the video asset, used to extract a preview frame when [thumbnailUrl] fails.
 */
@InternalStreamChatApi
public data class VideoThumbnailImageData(
    public val thumbnailUrl: String?,
    public val videoUrl: String?,
)

/**
 * A Coil [Interceptor] that resolves a [VideoThumbnailImageData] request by loading the server
 * thumbnail first and, when that is missing or fails, extracting a frame from the video asset.
 *
 * Server-side thumbnail generation is asynchronous, so right after a video is sent the thumbnail
 * URL can return 404 until generation finishes. Without a fallback the preview stays blank until
 * the item is rendered again. This mirrors the iOS behaviour of generating a frame from the video
 * when the thumbnail is not yet available.
 *
 * The interceptor rewrites the request data to a plain URL before proceeding, so the rest of the
 * pipeline (CDN signing, network fetching, frame decoding) is reused for both the thumbnail and
 * the video frame. It must be registered as the outermost interceptor so URL-rewriting
 * interceptors still apply to the fallback video URL.
 */
@InternalStreamChatApi
public class VideoThumbnailFallbackInterceptor : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val data = chain.request.data as? VideoThumbnailImageData ?: return chain.proceed()
        val thumbnailUrl = data.thumbnailUrl
        val videoUrl = data.videoUrl

        if (thumbnailUrl != null) {
            val thumbnailResult = chain
                .withRequest(chain.request.newBuilder().data(thumbnailUrl).build())
                .proceed()
            if (thumbnailResult is SuccessResult || videoUrl == null) {
                return thumbnailResult
            }
        }

        if (videoUrl != null) {
            val videoRequest = chain.request.newBuilder()
                .data(videoUrl)
                .videoFramePreview()
                .build()
            return chain.withRequest(videoRequest).proceed()
        }

        return chain.proceed()
    }
}
