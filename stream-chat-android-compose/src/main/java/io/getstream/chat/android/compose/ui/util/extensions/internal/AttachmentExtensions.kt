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
import io.getstream.chat.android.ui.common.images.resizing.applyStreamCdnImageResizingIfEnabled
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl

/**
 * This property checks if the attachment is an image or a video with enabled thumbnails.
 * If so, it returns the image preview URL (applied with Stream CDN image resizing if enabled)
 * or the upload [java.io.File] object.
 * Otherwise, it returns null.
 */
@get:Composable
internal val Attachment.imagePreviewData: Any?
    get() = if (isImage() || (isVideo() && ChatTheme.videoThumbnailsEnabled)) {
        imagePreviewUrl
            ?.applyStreamCdnImageResizingIfEnabled(ChatTheme.streamCdnImageResizing)
            ?: upload
    } else {
        null
    }
