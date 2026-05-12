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

package io.getstream.chat.android.compose.ui.messages.composer.internal.attachments

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.compose.LocalAsyncImagePreviewHandler
import io.getstream.chat.android.client.extensions.duration
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.ComposerCancelIcon
import io.getstream.chat.android.compose.ui.components.common.VideoBadge
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.AsyncImagePreviewHandler
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.compose.ui.util.extensions.internal.localPreviewData
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.previewdata.PreviewAttachmentData

@Composable
internal fun MessageComposerAttachmentMediaItem(
    attachment: Attachment,
    modifier: Modifier = Modifier,
    onAttachmentRemoved: (Attachment) -> Unit = {},
) {
    val data = attachment.localPreviewData

    Box(
        modifier = modifier.testTag("Stream_MessageComposerAttachmentMediaItem"),
    ) {
        Box(
            modifier = Modifier
                .padding(StreamTokens.spacing2xs)
                .size(72.dp)
                .clip(MediaItemImageShape),
        ) {
            StreamAsyncImage(
                modifier = Modifier.fillMaxSize(),
                data = data,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )

            if (attachment.type == AttachmentType.VIDEO) {
                VideoBadge(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(StreamTokens.spacing2xs),
                    durationInSeconds = attachment.duration?.toLong() ?: 0,
                    compact = true,
                )
            }
        }

        ComposerCancelIcon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .testTag("Stream_MessageComposerAttachmentCancelIcon"),
            onClick = { onAttachmentRemoved(attachment) },
            contentDescription = mediaAttachmentRemoveDescription(attachment),
        )
    }
}

@Composable
private fun mediaAttachmentRemoveDescription(attachment: Attachment): String {
    val displayName = attachment.title ?: attachment.name
    val isVideo = attachment.type == AttachmentType.VIDEO
    return when {
        isVideo && !displayName.isNullOrBlank() ->
            stringResource(R.string.stream_compose_remove_attachment_video_named, displayName)

        isVideo ->
            stringResource(R.string.stream_compose_remove_attachment_video)

        !displayName.isNullOrBlank() ->
            stringResource(R.string.stream_compose_remove_attachment_photo_named, displayName)

        else ->
            stringResource(R.string.stream_compose_remove_attachment_photo)
    }
}

private val MediaItemImageShape = RoundedCornerShape(StreamTokens.radiusLg)

@Preview
@Composable
private fun MessageComposerAttachmentImageItemPreview() {
    ChatTheme {
        MessageComposerAttachmentImageItem()
    }
}

@Composable
internal fun MessageComposerAttachmentImageItem() {
    MessageComposerAttachmentItem(attachment = PreviewAttachmentData.attachmentImage1)
}

@Preview
@Composable
private fun MessageComposerAttachmentImageItemUnnamedPreview() {
    ChatTheme {
        MessageComposerAttachmentImageItemUnnamed()
    }
}

@Composable
internal fun MessageComposerAttachmentImageItemUnnamed() {
    MessageComposerAttachmentItem(
        attachment = PreviewAttachmentData.attachmentImage1.copy(name = null),
    )
}

@Preview
@Composable
private fun MessageComposerAttachmentVideoItemPreview() {
    ChatTheme {
        MessageComposerAttachmentVideoItem()
    }
}

@Composable
internal fun MessageComposerAttachmentVideoItem() {
    MessageComposerAttachmentItem(attachment = PreviewAttachmentData.attachmentVideo1)
}

@Preview
@Composable
private fun MessageComposerAttachmentVideoItemUnnamedPreview() {
    ChatTheme {
        MessageComposerAttachmentVideoItemUnnamed()
    }
}

@Composable
internal fun MessageComposerAttachmentVideoItemUnnamed() {
    MessageComposerAttachmentItem(
        attachment = PreviewAttachmentData.attachmentVideo1.copy(name = null),
    )
}

@Composable
private fun MessageComposerAttachmentItem(attachment: Attachment) {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(color = Color.Green.toArgb(), width = 200, height = 150)
    }
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        MessageComposerAttachmentMediaItem(
            attachment = attachment,
        )
    }
}
