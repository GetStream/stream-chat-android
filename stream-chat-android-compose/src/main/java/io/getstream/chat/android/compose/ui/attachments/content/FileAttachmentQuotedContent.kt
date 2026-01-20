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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.MimeTypeIconProvider
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.compose.ui.util.extensions.internal.imagePreviewData
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.model.MimeType

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
    val isImage = attachment.isImage()
    val data = attachment.imagePreviewData ?: MimeTypeIconProvider.getIconRes(attachment.mimeType)

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
        contentDescription = null,
    )
}

@Preview(showBackground = true)
@Composable
private fun FileAttachmentQuotedContentPreview() {
    ChatTheme {
        FileAttachmentQuotedContent()
    }
}

@Composable
internal fun FileAttachmentQuotedContent() {
    val mimeTypes = listOf(
        MimeType.MIME_TYPE_PDF,
        MimeType.MIME_TYPE_DOC,
        MimeType.MIME_TYPE_PPT,
        MimeType.MIME_TYPE_XLS,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        mimeTypes.forEach { mimeType ->
            FileAttachmentQuotedContent(
                attachment = Attachment(mimeType = mimeType),
            )
        }
    }
}
