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

/**
 * Strategy for resizing Stream CDN hosted image URLs before they are loaded, to avoid downloading
 * full-resolution originals. It is applied to image attachment URLs by the Compose and XML UI kits and
 * configured via `ChatTheme(streamCdnImageResizer = …)` / `ChatUI.streamCdnImageResizer`.
 *
 * Resizing is on by default at a 2MP cap ([StreamCdnMaxPixelsImageResizer]). Provide
 * [NoOpStreamCdnImageResizer] to opt out, or implement this interface to apply a custom resizing strategy.
 * Only Stream CDN hosted URLs that carry the original dimensions are affected; any other URL is returned
 * unchanged.
 */
public fun interface StreamCdnImageResizer {
    /** Returns a (possibly) resized Stream CDN image URL for [imageUrl]. */
    public fun resizeUrl(imageUrl: String): String

    public companion object {
        /** The default total-pixel budget (2MP). */
        public const val DEFAULT_MAX_IMAGE_PIXELS: Long = 2_000_000L
    }
}

/**
 * Caps images to [maxImagePixels] total pixels (default 2MP), preserving aspect ratio and never upscaling.
 *
 * @param maxImagePixels The total-pixel budget (width × height). Must be positive.
 * @param resizeMode The Stream CDN resize mode, or null for the CDN default.
 * @param cropMode The Stream CDN crop mode, or null for the CDN default.
 * @param cdnHost An optional custom Stream CDN host to resize in addition to the default Stream CDN hosts,
 * for integrations serving Stream images from a proxied or custom domain. Blank values are treated as absent.
 */
public class StreamCdnMaxPixelsImageResizer(
    private val maxImagePixels: Long = StreamCdnImageResizer.DEFAULT_MAX_IMAGE_PIXELS,
    private val resizeMode: StreamCdnResizeImageMode? = null,
    private val cropMode: StreamCdnCropImageMode? = null,
    private val cdnHost: String? = null,
) : StreamCdnImageResizer {

    init {
        require(maxImagePixels > 0) { "maxImagePixels must be positive, but was $maxImagePixels" }
    }

    override fun resizeUrl(imageUrl: String): String =
        imageUrl.createResizedStreamCdnImageUrl(maxImagePixels, resizeMode, cropMode, cdnHost)
}

/** Disables resizing — full-resolution originals. Explicit opt-out. */
public val NoOpStreamCdnImageResizer: StreamCdnImageResizer = StreamCdnImageResizer { imageUrl -> imageUrl }
