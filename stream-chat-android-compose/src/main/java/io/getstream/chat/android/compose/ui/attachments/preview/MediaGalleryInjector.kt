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

package io.getstream.chat.android.compose.ui.attachments.preview

import coil3.ImageLoader
import io.getstream.chat.android.ui.common.images.resizing.StreamCdnImageResizer

/**
 * Injector for non-configuration dependencies of the Media Gallery Activity.
 * Serves a bridge the Activity/Fragment which hosts the `MessageList` and the `MediaGalleryPreviewActivity`,
 * providing values passed in the `ChatTheme` holding the `MessageList` to the `MediaGalleryPreviewActivity`.
 */
internal object MediaGalleryInjector {

    /**
     * The [ImageLoader] instance.
     */
    @Volatile
    var imageLoader: ImageLoader? = null
        internal set

    /**
     * The [StreamCdnImageResizer] instance. Carried across the Activity boundary because a resizer is not
     * serializable and therefore can't travel through the launching Intent like the legacy resizing config.
     */
    @Volatile
    var streamCdnImageResizer: StreamCdnImageResizer? = null
        internal set

    /**
     * Sets the dependencies carried into the [MediaGalleryPreviewActivity].
     *
     * @param imageLoader The [ImageLoader] instance to set.
     * @param streamCdnImageResizer The [StreamCdnImageResizer] instance to set.
     */
    fun install(imageLoader: ImageLoader, streamCdnImageResizer: StreamCdnImageResizer) {
        this.imageLoader = imageLoader
        this.streamCdnImageResizer = streamCdnImageResizer
    }
}
