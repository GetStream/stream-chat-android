/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImagePainter
import io.getstream.chat.android.compose.ui.components.ShimmerProgressIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.MimeTypeIconProvider
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.images.resizing.applyStreamCdnImageResizingIfEnabled
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl

/**
 * Builds a file attachment quoted message which shows a single file in the attachments list.
 *
 * @param attachment The attachment we wish to show to users.
 */
@Composable
public fun FileAttachmentQuotedContent(
    attachment: Attachment,
    modifier: Modifier = Modifier,
) {
    val isImage = attachment.type == "image"

    val data = if (isImage) {
        val dataToLoad =
            attachment.imagePreviewUrl?.applyStreamCdnImageResizingIfEnabled(ChatTheme.streamCdnImageResizing)
                ?: attachment.upload

        dataToLoad
    } else {
        MimeTypeIconProvider.getIconRes(attachment.mimeType)
    }

    val startPadding = ChatTheme.dimens.quotedMessageAttachmentStartPadding
    val verticalPadding = ChatTheme.dimens.quotedMessageAttachmentTopPadding
    val size = ChatTheme.dimens.quotedMessageAttachmentPreviewSize
    val contentScale = if (isImage) ContentScale.Crop else ContentScale.Fit

    StreamAsyncImage(
        modifier = modifier
            .padding(start = startPadding, top = verticalPadding, bottom = verticalPadding)
            .size(size),
        data = data,
        contentScale = contentScale,
    ) { state ->
        if (state !is AsyncImagePainter.State.Success) {
            ShimmerProgressIndicator(
                modifier = Modifier.matchParentSize(),
            )
        } else {
            Image(
                modifier = Modifier.matchParentSize(),
                painter = state.painter,
                contentDescription = null,
                contentScale = contentScale,
            )
        }
    }
}
