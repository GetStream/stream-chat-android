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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.skydoves.landscapist.ImageOptions
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.attachments.preview.handler.AttachmentPreviewHandler
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.messages.attachments.FileAttachmentTheme
import io.getstream.chat.android.compose.ui.util.MimeTypeIconProvider
import io.getstream.chat.android.compose.ui.util.StreamImage
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.compose.util.attachmentDownloadState
import io.getstream.chat.android.compose.util.onDownloadHandleRequest
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.images.resizing.applyStreamCdnImageResizingIfEnabled
import io.getstream.chat.android.ui.common.utils.MediaStringUtil
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl

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
    val (message, isMine, onItemLongClick) = attachmentState
    val previewHandlers = ChatTheme.attachmentPreviewHandlers

    Column(
        modifier = modifier.combinedClickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = {},
            onLongClick = { onItemLongClick(message) },
        ).testTag("Stream_MultipleFileAttachmentsColumn"),
    ) {
        for (attachment in message.attachments) {
            FileAttachmentItem(
                modifier = Modifier
                    .padding(2.dp)
                    .fillMaxWidth()
                    .combinedClickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            onItemClick(previewHandlers, attachment)
                        },
                        onLongClick = { onItemLongClick(message) },
                    ),
                attachment = attachment,
                isMine = isMine,
                showFileSize = showFileSize,
            )
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
    val fileAttachmentTheme: FileAttachmentTheme = when {
        isMine -> ChatTheme.ownFileAttachmentTheme
        else -> ChatTheme.otherFileAttachmentTheme
    }

    Surface(
        modifier = modifier,
        color = fileAttachmentTheme.background,
        shape = fileAttachmentTheme.itemShape,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(vertical = 8.dp, horizontal = 8.dp),
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
            FileAttachmentDownloadIcon(
                attachment = attachment,
                isMine = isMine,
            )
        }
    }
}

/**
 *  Displays information about the attachment such as
 *  the attachment title and its size in bytes.
 *
 *  @param attachment The attachment for which the information is displayed.
 */
@Composable
private fun FileAttachmentDescription(
    attachment: Attachment,
    isMine: Boolean,
    showFileSize: (Attachment) -> Boolean,
) {
    val fileAttachmentTheme: FileAttachmentTheme = when {
        isMine -> ChatTheme.ownFileAttachmentTheme
        else -> ChatTheme.otherFileAttachmentTheme
    }
    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(start = 16.dp, end = 8.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            modifier = Modifier.testTag("Stream_FileAttachmentName"),
            text = attachment.title ?: attachment.name ?: "",
            style = fileAttachmentTheme.fileNameTextStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (showFileSize(attachment)) {
            Text(
                modifier = Modifier.testTag("Stream_FileAttachmentSize"),
                text = MediaStringUtil.convertFileSizeByteCount(attachment.fileSize.toLong()),
                style = fileAttachmentTheme.fileMetadataTextStyle,
            )
        }
    }
}

/**
 * Downloads the given attachment when clicked.
 *
 * @param attachment The attachment to download.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RowScope.FileAttachmentDownloadIcon(attachment: Attachment, isMine: Boolean) {
    val fileAttachmentTheme: FileAttachmentTheme = when {
        isMine -> ChatTheme.ownFileAttachmentTheme
        else -> ChatTheme.otherFileAttachmentTheme
    }
    if (LocalInspectionMode.current) {
        Icon(
            modifier = Modifier
                .align(Alignment.Top)
                .padding(end = 2.dp),
            painter = fileAttachmentTheme.downloadIconStyle.painter,
            contentDescription = stringResource(id = R.string.stream_compose_download),
            tint = fileAttachmentTheme.downloadIconStyle.tint,
        )
        return
    }

    val (writePermissionState, downloadPayload) = attachmentDownloadState()
    val context = LocalContext.current
    val downloadAttachmentUriGenerator = ChatTheme.streamDownloadAttachmentUriGenerator
    val downloadRequestInterceptor = ChatTheme.streamDownloadRequestInterceptor
    Icon(
        modifier = Modifier
            .align(Alignment.Top)
            .padding(end = 2.dp)
            .testTag("Stream_FileAttachmentDownloadButton")
            .clickable(bounded = false) {
                onDownloadHandleRequest(
                    context = context,
                    payload = attachment,
                    permissionState = writePermissionState,
                    downloadPayload = downloadPayload,
                    generateDownloadUri = downloadAttachmentUriGenerator::generateDownloadUri,
                    interceptRequest = downloadRequestInterceptor::intercept,
                )
            },
        painter = fileAttachmentTheme.downloadIconStyle.painter,
        contentDescription = stringResource(id = R.string.stream_compose_download),
        tint = fileAttachmentTheme.downloadIconStyle.tint,
    )
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
    val fileAttachmentTheme: FileAttachmentTheme = when {
        isMine -> ChatTheme.ownFileAttachmentTheme
        else -> ChatTheme.otherFileAttachmentTheme
    }
    val isImage = attachment.isImage()
    val isVideoWithThumbnails = attachment.isVideo() && ChatTheme.videoThumbnailsEnabled

    val data = when {
        isImage ->
            attachment.imagePreviewUrl
                ?.applyStreamCdnImageResizingIfEnabled(ChatTheme.streamCdnImageResizing)
                ?: attachment.upload

        isVideoWithThumbnails ->
            attachment.thumbUrl
                ?.applyStreamCdnImageResizingIfEnabled(ChatTheme.streamCdnImageResizing)
                ?: attachment.upload

        else -> MimeTypeIconProvider.getIconRes(attachment.mimeType)
    }

    val shape = if (isImage || isVideoWithThumbnails) fileAttachmentTheme.imageThumbnail else null

    val imageModifier = Modifier
        .size(height = 40.dp, width = 35.dp)
        .let { baseModifier ->
            if (shape != null) baseModifier.clip(shape) else baseModifier
        }
        .testTag("Stream_FileAttachmentImage")

    StreamImage(
        modifier = imageModifier,
        data = { data },
        imageOptions = ImageOptions(
            contentScale = if (isImage || isVideoWithThumbnails) {
                ContentScale.Crop
            } else {
                ContentScale.Fit
            },
        ),
    )
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

@Preview(showSystemUi = true, showBackground = true)
@Composable
internal fun FileAttachmentContentPreview() {
    val attachment = Attachment(type = AttachmentType.FILE)
    val attachmentState = AttachmentState(Message(attachments = mutableListOf(attachment)))

    ChatPreviewTheme {
        FileAttachmentContent(
            attachmentState = attachmentState,
        )
    }
}
