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

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment

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
 * Builds the [VideoThumbnailImageData] for a video attachment from the given [thumbnailUrl] and the
 * attachment's [Attachment.assetUrl], or returns `null` when neither is available.
 *
 * @param thumbnailUrl The server thumbnail URL, already transformed for CDN resizing if enabled.
 */
@InternalStreamChatApi
public fun Attachment.videoThumbnailImageData(thumbnailUrl: String?): VideoThumbnailImageData? =
    if (thumbnailUrl != null || assetUrl != null) {
        VideoThumbnailImageData(thumbnailUrl = thumbnailUrl, videoUrl = assetUrl)
    } else {
        null
    }
