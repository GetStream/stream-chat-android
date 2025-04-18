/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.attachments.preview.internal

import coil3.ImageLoader

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
     * Sets the [ImageLoader] instance.
     *
     * @param imageLoader The [ImageLoader] instance to set.
     */
    internal fun install(imageLoader: ImageLoader) {
        this.imageLoader = imageLoader
    }
}
