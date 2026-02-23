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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.components.ComposerCancelIcon
import io.getstream.chat.android.compose.ui.components.composer.MessageInput
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.extensions.internal.stableKey
import io.getstream.chat.android.compose.ui.util.rememberAutoScrollLazyListState
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.utils.MediaStringUtil

/**
 * UI for currently selected file attachments, within the [MessageInput].
 *
 * @param attachments Selected attachments.
 * @param onAttachmentRemoved Handler when the user removes an attachment from the list.
 * @param modifier Modifier for styling.
 */
@Suppress("LongMethod")
@Composable
public fun FileAttachmentPreviewContent(
    attachments: List<Attachment>,
    onAttachmentRemoved: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        state = rememberAutoScrollLazyListState(attachments.size),
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .testTag("Stream_FileAttachmentPreviewContent"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
        contentPadding = PaddingValues(12.dp),
    ) {
        items(
            items = attachments,
            key = Attachment::stableKey,
        ) { attachment ->
            Surface(
                modifier = Modifier
                    .animateItem()
                    .padding(1.dp),
                color = ChatTheme.colors.appBackground,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ChatTheme.colors.borderCoreDefault),
            ) {
                Row(
                    modifier = Modifier
                        .width(200.dp)
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FileAttachmentImage(
                        attachment = attachment,
                        true,
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            modifier = Modifier.testTag("Stream_FileNameInPreview"),
                            text = attachment.title ?: attachment.name ?: "",
                            style = ChatTheme.typography.bodyBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = ChatTheme.colors.textPrimary,
                        )

                        val fileSize = attachment.fileSize
                            .takeIf { it > 0 }
                            ?.let { MediaStringUtil.convertFileSizeByteCount(it.toLong()) }
                        if (fileSize != null) {
                            Text(
                                modifier = Modifier.testTag("Stream_FileSizeInPreview"),
                                text = fileSize,
                                style = ChatTheme.typography.footnote,
                                color = ChatTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    ComposerCancelIcon(
                        modifier = Modifier.padding(4.dp).testTag("Stream_AttachmentCancelIcon"),
                        onClick = { onAttachmentRemoved(attachment) },
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun FileAttachmentPreviewContentPreview() {
    ChatTheme {
        FileAttachmentPreviewContent()
    }
}

@Composable
internal fun FileAttachmentPreviewContent() {
    val attachment = Attachment(
        type = "file",
        name = "test_document.pdf",
        fileSize = 1024 * 1024,
        mimeType = "application/pdf",
    )
    FileAttachmentPreviewContent(
        attachments = listOf(attachment),
        onAttachmentRemoved = {},
    )
}
