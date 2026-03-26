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

package io.getstream.chat.android.ui.common.utils.extensions

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.helper.internal.StorageHelper
import io.getstream.chat.android.ui.common.utils.StringUtils

public fun Attachment.getDisplayableName(): String? {
    return StringUtils.removeTimePrefix(title ?: name ?: upload?.name, StorageHelper.TIME_FORMAT)
}

@Deprecated(
    message = "Use the appropriate field for your attachment type: " +
        "imageUrl for image attachments, " +
        "thumbUrl for video thumbnails and link/giphy previews.",
    level = DeprecationLevel.WARNING,
)
public val Attachment.imagePreviewUrl: String?
    get() = thumbUrl ?: imageUrl

/**
 * The image URL to display for link attachment previews.
 *
 * Prefers [Attachment.thumbUrl] over [Attachment.imageUrl].
 */
@InternalStreamChatApi
public val Attachment.linkPreviewImageUrl: String?
    get() = thumbUrl ?: imageUrl

/**
 * The navigation URL for link attachments.
 *
 * Prefers [Attachment.titleLink] over [Attachment.ogUrl].
 */
@InternalStreamChatApi
public val Attachment.linkUrl: String?
    get() = titleLink ?: ogUrl

/**
 * The fallback preview URL for Giphy attachments when [io.getstream.chat.android.ui.common.utils.giphyInfo]
 * is not available.
 *
 * Falls back through [Attachment.thumbUrl], [Attachment.titleLink], and [Attachment.ogUrl].
 */
@InternalStreamChatApi
public val Attachment.giphyFallbackPreviewUrl: String?
    get() = thumbUrl ?: titleLink ?: ogUrl
