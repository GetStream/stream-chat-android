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

package io.getstream.chat.android.compose.ui.attachments.preview

import io.getstream.chat.android.models.Attachment

/**
 * Interface for generating URIs for media gallery assets.
 *
 * Provides methods to generate URIs for the asset, image, and thumbnail of a given attachment.
 */
public interface MediaGalleryAssetUriGenerator {

    /**
     * Generates a URI for the main asset of the given [attachment].
     *
     * @param attachment The attachment for which to generate the asset URI.
     */
    public suspend fun generateAssetUri(attachment: Attachment): String?

    /**
     * Generates a URI for the image of the given [attachment].
     *
     * @param attachment The attachment for which to generate the image URI.
     */
    public suspend fun generateImageUri(attachment: Attachment): String?

    /**
     * Generates a URI for the thumbnail of the given [attachment].
     *
     * @param attachment The attachment for which to generate the thumbnail URI.
     */
    public suspend fun generateThumbUri(attachment: Attachment): String?
}

/**
 * Default implementation of [MediaGalleryAssetUriGenerator] that retrieves URIs directly from the [Attachment]
 * properties.
 */
public class DefaultMediaGalleryAssetUriGenerator : MediaGalleryAssetUriGenerator {

    /**
     * Returns the asset URL from the [attachment].
     */
    override suspend fun generateAssetUri(attachment: Attachment): String? = attachment.assetUrl

    /**
     * Returns the image URL from the [attachment].
     */
    override suspend fun generateImageUri(attachment: Attachment): String? = attachment.imageUrl

    /**
     * Returns the thumbnail URL from the [attachment].
     */
    override suspend fun generateThumbUri(attachment: Attachment): String? = attachment.thumbUrl
}
