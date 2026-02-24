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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.utils.attachment.isAudio
import io.getstream.chat.android.client.utils.attachment.isFile
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.attachments.preview.handler.AttachmentPreviewHandler
import io.getstream.chat.android.compose.ui.components.attachments.files.FileTypeIcon
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.MimeTypeIconProvider
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.compose.ui.util.applyIf
import io.getstream.chat.android.compose.ui.util.extensions.internal.imagePreviewData
import io.getstream.chat.android.compose.ui.util.shouldBeDisplayedAsFullSizeAttachment
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.model.MimeType
import io.getstream.chat.android.ui.common.utils.MediaStringUtil
import io.getstream.chat.android.ui.common.utils.extensions.getDisplayableName

/**
 * Builds a file attachment message which shows a list of files.
 *
 * @param attachmentState - The state of the attachment, holding the root modifier, the message
 * and the onLongItemClick handler.
 * @param modifier Modifier for styling.
 * @param onItemClick Lambda called when an item gets clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun FileAttachmentContent(
    attachmentState: AttachmentState,
    modifier: Modifier = Modifier,
    showFileSize: (Attachment) -> Boolean = { true },
    onItemClick: (
        previewHandlers: List<AttachmentPreviewHandler>,
        attachment: Attachment,
    ) -> Unit = ::onFileAttachmentContentItemClick,
) {
    val message = attachmentState.message
    val previewHandlers = ChatTheme.attachmentPreviewHandlers

    val shouldBeFullSize = message.shouldBeDisplayedAsFullSizeAttachment()

    Column(
        modifier = modifier
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {},
                onLongClick = { attachmentState.onLongItemClick(message) },
            )
            .testTag("Stream_MultipleFileAttachmentsColumn"),
    ) {
        for (attachment in message.attachments) {
            if (attachment.isFile() || attachment.isAudio()) {
                ChatTheme.componentFactory.FileAttachmentItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .applyIf(!shouldBeFullSize) {
                            val color = MessageStyling.attachmentBackgroundColor(attachmentState.isMine)
                            padding(MessageStyling.messageSectionPadding)
                                .background(color, fileAttachmentShape)
                        }
                        .combinedClickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onItemClick(previewHandlers, attachment) },
                            onLongClick = { attachmentState.onLongItemClick(message) },
                        ),
                    attachment = attachment,
                    isMine = attachmentState.isMine,
                    showFileSize = showFileSize,
                )
            }
        }
    }
}

/**
 * Represents each file item in the list of file attachments.
 *
 * @param attachment The file attachment to show.
 * @param isMine Whether the message is sent by the current user or not.
 * @param showFileSize Whether to show the file size or not.
 * @param modifier Modifier for styling.
 */
@Composable
public fun FileAttachmentItem(
    attachment: Attachment,
    isMine: Boolean,
    showFileSize: (Attachment) -> Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.padding(StreamTokens.spacingSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FileAttachmentImage(
            attachment = attachment,
            isMine = isMine,
        )
        FileAttachmentDescription(
            attachment = attachment,
            isMine = isMine,
            showFileSize = showFileSize,
        )
    }
}

private val fileAttachmentShape = RoundedCornerShape(StreamTokens.radiusLg)

/**
 *  Displays information about the attachment such as
 *  the attachment title and its size in bytes.
 *
 *  @param attachment The attachment for which the information is displayed.
 */
@Composable
internal fun RowScope.FileAttachmentDescription(
    attachment: Attachment,
    isMine: Boolean,
    showFileSize: (Attachment) -> Boolean,
) {
    val typography = ChatTheme.typography
    val textColor = when (isMine) {
        true -> ChatTheme.colors.chatTextOutgoing
        false -> ChatTheme.colors.chatTextIncoming
    }

    Column(
        modifier = Modifier
            .weight(1f)
            .padding(start = StreamTokens.spacingSm),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            modifier = Modifier.testTag("Stream_FileAttachmentName"),
            text = attachment.getDisplayableName() ?: "",
            style = typography.captionEmphasis,
            color = textColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        if (showFileSize(attachment)) {
            Text(
                modifier = Modifier
                    .padding(top = StreamTokens.spacing2xs)
                    .testTag("Stream_FileAttachmentSize"),
                text = MediaStringUtil.convertFileSizeByteCount(attachment.fileSize.toLong()),
                style = typography.metadataDefault,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Represents the image that's shown in file attachments. This can be either an image/icon that represents the file type
 * or a thumbnail in case the file type is an image.
 *
 * @param attachment - The attachment we use to show the image.
 */
@Composable
public fun FileAttachmentImage(
    attachment: Attachment,
    isMine: Boolean,
) {
    val baseModifier = Modifier
        .size(height = 40.dp, width = 35.dp)
        .testTag("Stream_FileAttachmentImage")
    val data = attachment.imagePreviewData

    if (data != null) {
        val showThumbnail = attachment.isImage() || attachment.isVideo() && ChatTheme.videoThumbnailsEnabled
        val imageModifier = if (showThumbnail) {
            baseModifier.clip(RoundedCornerShape(StreamTokens.radiusMd))
        } else {
            baseModifier
        }

        StreamAsyncImage(
            modifier = imageModifier,
            data = data,
            contentScale = if (showThumbnail) ContentScale.Crop else ContentScale.Fit,
            contentDescription = null,
        )
    } else {
        FileTypeIcon(
            data = MimeTypeIconProvider.getIcon(attachment.mimeType),
            modifier = baseModifier,
        )
    }
}

/**
 * Handles clicks on individual file attachment content items.
 *
 * @param previewHandlers A list of preview handlers from which a suitable handler
 * will be looked for.
 * @param attachment The attachment being clicked.
 */
internal fun onFileAttachmentContentItemClick(
    previewHandlers: List<AttachmentPreviewHandler>,
    attachment: Attachment,
) {
    previewHandlers.firstOrNull { it.canHandle(attachment) }?.handleAttachmentPreview(attachment)
}

@Preview(showBackground = true)
@Composable
private fun OwnFileAttachmentContentPreview() {
    ChatTheme {
        FileAttachmentContent(isMine = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherFileAttachmentContentPreview() {
    ChatTheme {
        FileAttachmentContent(isMine = false)
    }
}

@Composable
internal fun FileAttachmentContent(isMine: Boolean) {
    val attachments = listOf(Attachment(mimeType = MimeType.MIME_TYPE_PDF, type = AttachmentType.FILE))
    FileAttachmentContent(
        attachmentState = AttachmentState(
            message = Message(attachments = attachments),
            isMine = isMine,
        ),
    )
}
