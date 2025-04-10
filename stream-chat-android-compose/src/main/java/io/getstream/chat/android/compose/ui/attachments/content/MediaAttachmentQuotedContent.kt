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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isImgur
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.compose.ui.components.ShimmerProgressIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.images.resizing.applyStreamCdnImageResizingIfEnabled
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl

/**
 * Builds an image attachment for a quoted message which is composed from a singe attachment previewing the attached
 * image, link preview or giphy.
 *
 * @param attachment The attachment we wish to show to users.
 * @param modifier Modifier for styling.
 */
@Composable
public fun MediaAttachmentQuotedContent(
    attachment: Attachment,
    modifier: Modifier = Modifier,
) {
    val isImageContent = attachment.isImage() || attachment.isImgur()
    val isVideo = attachment.isVideo()
    val isGiphy = attachment.isGiphy()

    val backgroundColor =
        if (isImageContent || isGiphy) {
            ChatTheme.colors.imageBackgroundMessageList
        } else {
            ChatTheme.colors.videoBackgroundMessageList
        }

    val data =
        when {
            isGiphy -> attachment.imagePreviewUrl
            isImageContent || (isVideo && ChatTheme.videoThumbnailsEnabled) ->
                attachment.imagePreviewUrl?.applyStreamCdnImageResizingIfEnabled(ChatTheme.streamCdnImageResizing)

            else -> null
        }

    Box(
        modifier = modifier
            .padding(
                start = ChatTheme.dimens.quotedMessageAttachmentStartPadding,
                top = ChatTheme.dimens.quotedMessageAttachmentTopPadding,
                bottom = ChatTheme.dimens.quotedMessageAttachmentBottomPadding,
                end = ChatTheme.dimens.quotedMessageAttachmentEndPadding,
            )
            .size(ChatTheme.dimens.quotedMessageAttachmentPreviewSize)
            .clip(ChatTheme.shapes.quotedAttachment),
        contentAlignment = Alignment.Center,
    ) {
        StreamAsyncImage(
            modifier = Modifier
                .fillMaxSize(1f)
                .background(backgroundColor),
            data = data,
            contentScale = ContentScale.Crop,
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
                    contentScale = ContentScale.Crop,
                )
            }
        }

        if (isVideo) {
            PlayButton(
                modifier = Modifier
                    .padding(10.dp)
                    .shadow(6.dp, shape = CircleShape)
                    .background(color = Color.White, shape = CircleShape)
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f),
            )
        }
    }
}
