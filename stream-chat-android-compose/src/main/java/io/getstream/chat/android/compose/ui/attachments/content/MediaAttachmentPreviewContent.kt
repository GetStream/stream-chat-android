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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.compose.LocalAsyncImagePreviewHandler
import io.getstream.chat.android.compose.ui.attachments.factory.DefaultPreviewItemOverlayContent
import io.getstream.chat.android.compose.ui.components.ComposerCancelIcon
import io.getstream.chat.android.compose.ui.components.composer.MessageInput
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.AsyncImagePreviewHandler
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.compose.ui.util.extensions.internal.localPreviewData
import io.getstream.chat.android.compose.ui.util.extensions.internal.stableKey
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType

/**
 * UI for currently selected image and video attachments, within the [MessageInput].
 *
 * @param attachments Selected attachments.
 * @param onAttachmentRemoved Handler when the user removes an attachment from the list.
 * @param modifier Modifier for styling.
 * @param previewItemOverlayContent Represents the content overlaid above individual preview items.
 * By default it is used to display a play button over video previews.
 */
@Composable
public fun MediaAttachmentPreviewContent(
    attachments: List<Attachment>,
    onAttachmentRemoved: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
    previewItemOverlayContent: @Composable (attachmentType: String?) -> Unit = { attachmentType ->
        if (attachmentType == AttachmentType.VIDEO) {
            DefaultPreviewItemOverlayContent()
        }
    },
) {
    LazyRow(
        modifier = modifier
            .clip(ChatTheme.shapes.attachment)
            .testTag("Stream_MediaAttachmentPreviewContent"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs, Alignment.Start),
        contentPadding = PaddingValues(horizontal = StreamTokens.spacingSm),
    ) {
        items(
            items = attachments,
            key = Attachment::stableKey,
        ) { attachment ->
            MediaAttachmentPreviewItem(
                modifier = Modifier.animateItem(),
                mediaAttachment = attachment,
                onAttachmentRemoved = onAttachmentRemoved,
                overlayContent = previewItemOverlayContent,
            )
        }
    }
}

/**
 * A preview of an individual selected image or video attachment.
 *
 * @param mediaAttachment The selected attachment.
 * @param onAttachmentRemoved Handler when the user removes an attachment from the list.
 * @param overlayContent Represents the content overlaid above the item.
 * Usually used to display an icon above video previews.
 */
@Composable
private fun MediaAttachmentPreviewItem(
    mediaAttachment: Attachment,
    onAttachmentRemoved: (Attachment) -> Unit,
    overlayContent: @Composable (attachmentType: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val data = mediaAttachment.localPreviewData

    Box(
        modifier = modifier
            .size(95.dp)
            .clip(ChatTheme.shapes.attachment)
            .testTag("Stream_MediaAttachmentPreviewItem"),
        contentAlignment = Alignment.Center,
    ) {
        StreamAsyncImage(
            modifier = Modifier.fillMaxSize(),
            data = data,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        overlayContent(mediaAttachment.type)

        ComposerCancelIcon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .testTag("Stream_AttachmentCancelIcon"),
            onClick = { onAttachmentRemoved(mediaAttachment) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaAttachmentItemsPreview() {
    ChatTheme {
        MediaAttachmentPreviewItems()
    }
}

@Composable
internal fun MediaAttachmentPreviewItems() {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(color = Color.Green.toArgb(), width = 200, height = 150)
    }
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            MediaAttachmentPreviewItem(
                mediaAttachment = Attachment(imageUrl = "Image"),
                onAttachmentRemoved = {},
                overlayContent = {},
            )
            MediaAttachmentPreviewItem(
                mediaAttachment = Attachment(imageUrl = "Image"),
                onAttachmentRemoved = {},
                overlayContent = { DefaultPreviewItemOverlayContent() },
            )
        }
    }
}
