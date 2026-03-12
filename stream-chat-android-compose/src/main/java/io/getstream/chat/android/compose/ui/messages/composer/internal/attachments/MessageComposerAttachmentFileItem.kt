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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentImage
import io.getstream.chat.android.compose.ui.components.ComposerCancelIcon
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.previewdata.PreviewAttachmentData
import io.getstream.chat.android.ui.common.utils.MediaStringUtil

@Composable
internal fun MessageComposerAttachmentFileItem(
    attachment: Attachment,
    modifier: Modifier = Modifier,
    onAttachmentRemoved: (Attachment) -> Unit = {},
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(StreamTokens.spacing2xs)
                .border(1.dp, ChatTheme.colors.borderCoreDefault, FileItemShape)
                .clip(FileItemShape)
                .background(ChatTheme.colors.backgroundCoreApp)
                .size(width = 260.dp, height = 72.dp)
                .padding(
                    start = StreamTokens.spacingMd,
                    end = StreamTokens.spacingSm,
                    top = StreamTokens.spacingMd,
                    bottom = StreamTokens.spacingMd,
                ),
            horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FileAttachmentImage(attachment = attachment, isMine = true)

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
            ) {
                Text(
                    modifier = Modifier.testTag("Stream_MessageComposerAttachmentFileName"),
                    text = attachment.title ?: attachment.name ?: "",
                    style = ChatTheme.typography.bodyEmphasis,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = ChatTheme.colors.textPrimary,
                )

                val fileSize = remember(attachment.fileSize) {
                    attachment.fileSize.takeIf { it > 0 }?.let {
                        MediaStringUtil.convertFileSizeByteCount(it.toLong())
                    }
                }
                if (fileSize != null) {
                    Text(
                        modifier = Modifier.testTag("Stream_MessageComposerAttachmentFileSize"),
                        text = fileSize,
                        style = ChatTheme.typography.metadataDefault,
                        color = ChatTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        ComposerCancelIcon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .testTag("Stream_MessageComposerAttachmentCancelIcon"),
            onClick = { onAttachmentRemoved(attachment) },
        )
    }
}

private val FileItemShape = RoundedCornerShape(StreamTokens.radiusLg)

@Preview
@Composable
private fun MessageComposerAttachmentFileItemPreview() {
    ChatTheme {
        MessageComposerAttachmentFileItem()
    }
}

@Composable
internal fun MessageComposerAttachmentFileItem() {
    MessageComposerAttachmentFileItem(
        attachment = PreviewAttachmentData.attachmentFile1,
    )
}
