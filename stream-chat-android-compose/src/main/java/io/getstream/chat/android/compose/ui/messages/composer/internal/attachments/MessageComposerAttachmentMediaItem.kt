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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.compose.LocalAsyncImagePreviewHandler
import io.getstream.chat.android.compose.ui.components.ComposerCancelIcon
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.AsyncImagePreviewHandler
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.compose.ui.util.extensions.internal.localPreviewData
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.previewdata.PreviewAttachmentData

@Composable
internal fun MessageComposerAttachmentMediaItem(
    attachment: Attachment,
    modifier: Modifier = Modifier,
    onAttachmentRemoved: (Attachment) -> Unit = {},
    overlayContent: @Composable (attachmentType: String?) -> Unit = {},
) {
    val data = attachment.localPreviewData

    Box(
        modifier = modifier
            .size(95.dp)
            .clip(RoundedCornerShape(StreamTokens.radiusLg))
            .testTag("Stream_MediaAttachmentPreviewItem"),
        contentAlignment = Alignment.Center,
    ) {
        StreamAsyncImage(
            modifier = Modifier.fillMaxSize(),
            data = data,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        overlayContent(attachment.type)

        ComposerCancelIcon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .testTag("Stream_AttachmentCancelIcon"),
            onClick = { onAttachmentRemoved(attachment) },
        )
    }
}

@Preview
@Composable
private fun MessageComposerAttachmentMediaItemPreview() {
    ChatTheme {
        MessageComposerAttachmentMediaItem()
    }
}

@Composable
internal fun MessageComposerAttachmentMediaItem() {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(color = Color.Green.toArgb(), width = 200, height = 150)
    }
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        MessageComposerAttachmentMediaItem(
            attachment = PreviewAttachmentData.attachmentImage1,
        )
    }
}
