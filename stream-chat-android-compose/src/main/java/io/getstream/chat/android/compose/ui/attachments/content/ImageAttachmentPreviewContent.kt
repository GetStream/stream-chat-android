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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.compose.LocalAsyncImagePreviewHandler
import io.getstream.chat.android.compose.ui.components.CancelIcon
import io.getstream.chat.android.compose.ui.components.composer.MessageInput
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.AsyncImagePreviewHandler
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl

/**
 * UI for currently selected image attachments, within the [MessageInput].
 *
 * @param attachments Selected attachments.
 * @param onAttachmentRemoved Handler when the user removes an attachment from the list.
 * @param modifier Modifier for styling.
 */
@Composable
public fun ImageAttachmentPreviewContent(
    attachments: List<Attachment>,
    onAttachmentRemoved: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
    ) {
        items(attachments) { attachment ->
            ImageAttachmentPreviewContentItem(
                attachment = attachment,
                onAttachmentRemoved = onAttachmentRemoved,
            )
        }
    }
}

@Composable
private fun ImageAttachmentPreviewContentItem(
    attachment: Attachment,
    onAttachmentRemoved: (Attachment) -> Unit,
) {
    val data = attachment.upload ?: attachment.imagePreviewUrl

    Box(
        modifier = Modifier
            .size(95.dp)
            .clip(RoundedCornerShape(16.dp)),
    ) {
        StreamAsyncImage(
            modifier = Modifier.fillMaxSize(),
            data = data,
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        CancelIcon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp),
            onClick = { onAttachmentRemoved(attachment) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImageAttachmentContentPreview() {
    ChatTheme {
        ImageAttachmentPreviewContent()
    }
}

@Composable
internal fun ImageAttachmentPreviewContent() {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(color = Color.Red.toArgb(), width = 200, height = 150)
    }
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        ImageAttachmentPreviewContentItem(
            attachment = Attachment(imageUrl = "Image"),
            onAttachmentRemoved = {},
        )
    }
}
