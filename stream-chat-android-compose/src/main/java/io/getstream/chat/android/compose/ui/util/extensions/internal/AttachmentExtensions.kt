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

package io.getstream.chat.android.compose.ui.util.extensions.internal

import androidx.compose.runtime.Composable
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper.Companion.EXTRA_SOURCE_URI
import io.getstream.chat.android.ui.common.images.resizing.applyStreamCdnImageResizingIfEnabled
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl

/**
 * The content URI stored when the attachment was created from a device picker,
 * before the file is resolved at send time.
 */
internal val Attachment.sourceUri: String?
    get() = extraData[EXTRA_SOURCE_URI] as? String

/**
 * Stable identity for `LazyList` keys.
 *
 * Prefers the immutable [sourceUri], falls back to [Attachment.upload] path,
 * then [hashCode] as a last resort.
 */
internal val Attachment.stableKey: String
    get() = sourceUri ?: upload?.absolutePath ?: hashCode().toString()

/**
 * Best available data source for rendering an unsent attachment preview.
 *
 * Prefers [Attachment.upload] (local file), then [imagePreviewUrl] (CDN URL),
 * then [sourceUri] (content URI from the picker).
 */
internal val Attachment.localPreviewData: Any?
    get() = upload ?: imagePreviewUrl ?: sourceUri

/**
 * Image preview data for a sent or received attachment.
 *
 * Returns the CDN image URL (with Stream resizing applied) or the local [Attachment.upload] file
 * for images and videos (when video thumbnails are enabled). Returns `null` for other types.
 */
@get:Composable
internal val Attachment.imagePreviewData: Any?
    get() = if (isImage() || (isVideo() && ChatTheme.config.messageList.videoThumbnailsEnabled)) {
        imagePreviewUrl
            ?.applyStreamCdnImageResizingIfEnabled(ChatTheme.streamCdnImageResizing)
            ?: upload
    } else {
        null
    }
