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
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * A convenience method which applies Stream CDN image resizing if it's enabled and applicable.
 * It doesn't hold any special value other than minimizing repetition.
 *
 * @param streamCdnImageResizing Holds value information about the resizing mode including if it is enabled or not.
 *
 * @return The URL to the resized image if resizing was applicable, otherwise returns the URL to the original image.
 */
// Bridges the deprecated StreamCdnImageResizing for back-compat until it is removed.
@Suppress("DEPRECATION")
@InternalStreamChatApi
public fun String.applyStreamCdnImageResizingIfEnabled(streamCdnImageResizing: StreamCdnImageResizing): String =
    if (streamCdnImageResizing.imageResizingEnabled) {
        this.createResizedStreamCdnImageUrl(
            resizedWidthPercentage = streamCdnImageResizing.resizedWidthPercentage,
            resizedHeightPercentage = streamCdnImageResizing.resizedHeightPercentage,
            resizeMode = streamCdnImageResizing.resizeMode,
            cropMode = streamCdnImageResizing.cropMode,
        )
    } else {
        this
    }

/**
 * Applies image resizing following the resolver precedence: if the legacy [streamCdnImageResizing] is explicitly
 * enabled its exact percentage behavior is honored, otherwise the active [streamCdnImageResizer] is applied
 * (defaulting to a 2MP cap). Disabling resizing is done by setting the resizer to [NoOpStreamCdnImageResizer].
 *
 * @param streamCdnImageResizing The legacy resizing config. Only honored when
 * [StreamCdnImageResizing.imageResizingEnabled] is true.
 * @param streamCdnImageResizer The active resizer, applied when the legacy config is not enabled.
 *
 * @return The URL to the resized image if resizing was applicable, otherwise returns the URL to the original image.
 */
// Bridges the deprecated StreamCdnImageResizing for back-compat until it is removed.
@Suppress("DEPRECATION")
@InternalStreamChatApi
public fun String.applyStreamCdnImageResizing(
    streamCdnImageResizing: StreamCdnImageResizing,
    streamCdnImageResizer: StreamCdnImageResizer,
): String =
    if (streamCdnImageResizing.imageResizingEnabled) {
        this.createResizedStreamCdnImageUrl(
            resizedWidthPercentage = streamCdnImageResizing.resizedWidthPercentage,
            resizedHeightPercentage = streamCdnImageResizing.resizedHeightPercentage,
            resizeMode = streamCdnImageResizing.resizeMode,
            cropMode = streamCdnImageResizing.cropMode,
        )
    } else {
        streamCdnImageResizer.resizeUrl(this)
    }
