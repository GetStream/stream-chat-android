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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.getstream.sdk.chat.utils.MediaStringUtil
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.state.messages.attachments.OnFileAttachmentClickState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.MimeTypeIconProvider
import io.getstream.chat.android.compose.ui.util.rememberStreamImagePainter
import io.getstream.chat.android.compose.util.attachmentDownloadState
import io.getstream.chat.android.compose.util.onDownloadHandleRequest

/**
 * Builds a file attachment message which shows a list of files.
 *
 * @param attachmentState - The state of the attachment, holding the root modifier, the message
 * and the onLongItemClick handler.
 * @param modifier Modifier for styling.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun FileAttachmentContent(
    attachmentState: AttachmentState,
    modifier: Modifier = Modifier,
) {
    val (message, onItemLongClick) = attachmentState
    val previewHandlers = ChatTheme.attachmentPreviewHandlers

    Column(
        modifier = modifier.combinedClickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = {},
            onLongClick = { onItemLongClick(message) }
        )
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
                            attachmentState.onAttachmentClick?.let {
                                it(OnFileAttachmentClickState(attachment = attachment))
                            } ?: previewHandlers
                                    .firstOrNull { it.canHandle(attachment) }
                                    ?.handleAttachmentPreview(attachment)
                        },
                        onLongClick = { onItemLongClick(message) },
                    ),
                attachment = attachment
            )
        }
    }
}

/**
 * Represents each file item in the list of file attachments.
 *
 * @param attachment The file attachment to show.
 * @param modifier Modifier for styling.
 */
@Composable
public fun FileAttachmentItem(
    attachment: Attachment,
    modifier: Modifier = Modifier,
) {

    Surface(
        modifier = modifier,
        color = ChatTheme.colors.appBackground,
        shape = ChatTheme.shapes.attachment
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FileAttachmentImage(attachment = attachment)
            FileAttachmentDescription(attachment = attachment)
            FileAttachmentDownloadIcon(attachment = attachment)
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
private fun FileAttachmentDescription(attachment: Attachment) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(start = 16.dp, end = 8.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = attachment.title ?: attachment.name ?: "",
            style = ChatTheme.typography.bodyBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ChatTheme.colors.textHighEmphasis
        )

        Text(
            text = MediaStringUtil.convertFileSizeByteCount(attachment.fileSize.toLong()),
            style = ChatTheme.typography.footnote,
            color = ChatTheme.colors.textLowEmphasis
        )
    }
}

/**
 * Downloads the given attachment when clicked.
 *
 * @param attachment The attachment to download.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun RowScope.FileAttachmentDownloadIcon(attachment: Attachment) {
    val (writePermissionState, downloadPayload) = attachmentDownloadState()
    val context = LocalContext.current

    Icon(
        modifier = Modifier
            .align(Alignment.Top)
            .padding(end = 2.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false)
            ) {
                onDownloadHandleRequest(
                    context = context,
                    payload = attachment,
                    permissionState = writePermissionState,
                    downloadPayload = downloadPayload
                )
            },
        painter = painterResource(id = R.drawable.stream_compose_ic_file_download),
        contentDescription = stringResource(id = R.string.stream_compose_download),
        tint = ChatTheme.colors.textHighEmphasis
    )
}

/**
 * Represents the image that's shown in file attachments. This can be either an image/icon that represents the file type
 * or a thumbnail in case the file type is an image.
 *
 * @param attachment - The attachment we use to show the image.
 */
@Composable
public fun FileAttachmentImage(attachment: Attachment) {
    val isImage = attachment.type == "image"

    val painter = if (isImage) {
        val dataToLoad = attachment.imageUrl ?: attachment.upload

        rememberStreamImagePainter(dataToLoad)
    } else {
        painterResource(id = MimeTypeIconProvider.getIconRes(attachment.mimeType))
    }

    val shape = if (isImage) ChatTheme.shapes.imageThumbnail else null

    val imageModifier = Modifier.size(height = 40.dp, width = 35.dp).let { baseModifier ->
        if (shape != null) baseModifier.clip(shape) else baseModifier
    }

    Image(
        modifier = imageModifier,
        painter = painter,
        contentDescription = null,
        contentScale = if (isImage) ContentScale.Crop else ContentScale.Fit
    )
}
