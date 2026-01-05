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

package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.compose.LocalAsyncImagePreviewHandler
import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isImgur
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.AsyncImagePreviewHandler
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.compose.ui.util.extensions.internal.imagePreviewData
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.common.images.resizing.applyStreamCdnImageResizingIfEnabled
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl
import java.io.File

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
            isGiphy ->
                attachment.imagePreviewUrl
            isImageContent ->
                attachment.imagePreviewUrl
                    ?.applyStreamCdnImageResizingIfEnabled(ChatTheme.streamCdnImageResizing)

            else -> attachment.imagePreviewData
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
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

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

@Preview(showBackground = true)
@Composable
private fun MediaAttachmentQuotedContentPreview() {
    ChatTheme {
        MediaAttachmentQuotedContent()
    }
}

@Suppress("LongMethod")
@Composable
internal fun MediaAttachmentQuotedContent() {
    data class PreviewData(
        val type: String,
        val thumbUrl: String?,
        val imageUrl: String?,
        val upload: File?,
    )

    val dataList = listOf(
        PreviewData(
            type = AttachmentType.IMAGE,
            thumbUrl = null,
            imageUrl = "image",
            upload = File("image"),
        ),
        PreviewData(
            type = AttachmentType.IMGUR,
            thumbUrl = null,
            imageUrl = "imgur",
            upload = File("imgur"),
        ),
        PreviewData(
            type = AttachmentType.GIPHY,
            thumbUrl = "giphy",
            imageUrl = null,
            upload = null,
        ),
        PreviewData(
            type = AttachmentType.VIDEO,
            thumbUrl = "video",
            imageUrl = "video",
            upload = File("video"),
        ),
        PreviewData(
            type = AttachmentType.VIDEO,
            thumbUrl = "video",
            imageUrl = "video",
            upload = null,
        ),
    )
    val previewHandler = AsyncImagePreviewHandler { request ->
        ColorImage(
            color = when (request.data) {
                "image" -> Color.Red.toArgb()
                "imgur" -> Color.Green.toArgb()
                "giphy" -> Color.Blue.toArgb()
                "video" -> Color.Yellow.toArgb()
                is File -> Color.Magenta.toArgb()
                else -> Color.LightGray.toArgb()
            },
            width = 200,
            height = 150,
        )
    }
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        Row {
            dataList.forEach { (type, thumbUrl, imageUrl, upload) ->
                MediaAttachmentQuotedContent(
                    attachment = Attachment(
                        type = type,
                        thumbUrl = thumbUrl,
                        imageUrl = imageUrl,
                        upload = upload,
                    ),
                )
            }
        }
    }
}
