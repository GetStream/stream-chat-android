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

package io.getstream.chat.android.ui.common.images.resizing

import io.getstream.chat.android.client.extensions.createResizedStreamCdnImageUrl
import io.getstream.chat.android.models.streamcdn.image.StreamCdnCropImageMode
import io.getstream.chat.android.models.streamcdn.image.StreamCdnResizeImageMode

public fun interface StreamCdnImageResizer {
    /** Returns a (possibly) resized Stream CDN image URL for [imageUrl]. */
    public fun resizeUrl(imageUrl: String): String

    public companion object {
        /** Total-pixel budget matching the iOS SDK default (2MP). */
        public const val DEFAULT_MAX_IMAGE_PIXELS: Long = 2_000_000L
    }
}

/** Caps images to [maxImagePixels] total pixels (default 2MP), preserving aspect ratio; on by presence. */
public class StreamCdnMaxPixelsImageResizer(
    private val maxImagePixels: Long = StreamCdnImageResizer.DEFAULT_MAX_IMAGE_PIXELS,
    private val resizeMode: StreamCdnResizeImageMode? = null,
    private val cropMode: StreamCdnCropImageMode? = null,
) : StreamCdnImageResizer {

    init {
        require(maxImagePixels > 0) { "maxImagePixels must be positive, but was $maxImagePixels" }
    }

    override fun resizeUrl(imageUrl: String): String =
        imageUrl.createResizedStreamCdnImageUrl(maxImagePixels, resizeMode, cropMode)
}

/** Disables resizing — full-resolution originals. Explicit opt-out. */
public val NoOpStreamCdnImageResizer: StreamCdnImageResizer = StreamCdnImageResizer { imageUrl -> imageUrl }
