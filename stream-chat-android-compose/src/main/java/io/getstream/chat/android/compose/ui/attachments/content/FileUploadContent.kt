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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.attachments.preview.handler.AttachmentPreviewHandler
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Attachment.UploadState.Idle
import io.getstream.chat.android.models.Attachment.UploadState.InProgress
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.utils.MediaStringUtil
import io.getstream.chat.android.uiutils.extension.isUploading

/**
 * Represents the content when files are being uploaded.
 *
 * @param attachmentState The state of this attachment.
 * @param modifier Modifier for styling.
 * @param onItemClick Lambda called when an item gets clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun FileUploadContent(
    attachmentState: AttachmentState,
    modifier: Modifier = Modifier,
    onItemClick: (Attachment, List<AttachmentPreviewHandler>) -> Unit = ::onFileUploadContentItemClick,
) {
    val message = attachmentState.message
    val previewHandlers = ChatTheme.attachmentPreviewHandlers

    Column(modifier = modifier) {
        for (attachment in message.attachments) {
            ChatTheme.componentFactory.FileUploadItem(
                modifier = Modifier
                    .padding(2.dp)
                    .fillMaxWidth()
                    .combinedClickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            onItemClick(attachment, previewHandlers)
                        },
                        onLongClick = { },
                    ),
                attachment = attachment,
            )
        }
    }
}

/**
 * Represents each uploading item, with its upload progress.
 *
 * @param attachment The attachment that's being uploaded.
 * @param modifier Modifier for styling.
 */
@Composable
public fun FileUploadItem(
    attachment: Attachment,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = ChatTheme.colors.appBackground,
        shape = ChatTheme.shapes.attachment,
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
                true,
            )

            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = attachment.title ?: attachment.name ?: "",
                    style = ChatTheme.typography.bodyBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = ChatTheme.colors.textHighEmphasis,
                )

                when (val uploadState = attachment.uploadState) {
                    is Idle -> {
                        ProgressInfo(uploadedBytes = 0L, totalBytes = attachment.upload?.length() ?: 0L)
                    }
                    is InProgress -> {
                        ProgressInfo(uploadedBytes = uploadState.bytesUploaded, totalBytes = uploadState.totalBytes)
                    }
                    else -> {
                        Text(
                            text = MediaStringUtil.convertFileSizeByteCount(attachment.upload?.length() ?: 0L),
                            style = ChatTheme.typography.footnote,
                            color = ChatTheme.colors.textLowEmphasis,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Displays the progress information of a file upload, showing how many
 * [uploadedBytes] are completed out of [totalBytes].
 *
 * @param uploadedBytes The amount of bytes already transferred.
 * @param totalBytes The total size of the file being uploaded.
 */
@Composable
private fun ProgressInfo(uploadedBytes: Long, totalBytes: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LoadingIndicator(modifier = Modifier.size(12.dp))

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = stringResource(
                id = R.string.stream_compose_upload_progress,
                MediaStringUtil.convertFileSizeByteCount(uploadedBytes),
                MediaStringUtil.convertFileSizeByteCount(totalBytes),
            ),
            style = ChatTheme.typography.footnote,
            color = ChatTheme.colors.textLowEmphasis,
        )
    }
}

/**
 * Handles clicks on individual file upload content items.
 *
 * @param attachment The attachment being clicked.
 * @param previewHandlers A list of preview handlers from which a suitable handler
 * will be looked for.
 */
internal fun onFileUploadContentItemClick(
    attachment: Attachment,
    previewHandlers: List<AttachmentPreviewHandler>,
) {
    if (!attachment.isUploading()) {
        previewHandlers
            .firstOrNull { it.canHandle(attachment) }
            ?.handleAttachmentPreview(attachment)
    }
}

@Composable
@Preview(showBackground = true)
private fun FileUploadContentPreview() {
    ChatTheme {
        FileUploadContent()
    }
}

@Composable
internal fun FileUploadContent() {
    val uploadingAttachment = Attachment(
        type = "file",
        name = "test_document.pdf",
        fileSize = 1024 * 1024,
        mimeType = "application/pdf",
        uploadState = Attachment.UploadState.InProgress(
            bytesUploaded = 512 * 1024, // 512KB uploaded
            totalBytes = 1024 * 1024, // 1MB total
        ),
    )
    val attachmentState = AttachmentState(
        message = Message(
            id = "message-id",
            text = "This is a message with an uploading attachment.",
            attachments = mutableListOf(uploadingAttachment),
        ),
    )
    FileUploadContent(attachmentState = attachmentState)
}
