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

package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentContent
import io.getstream.chat.android.compose.ui.attachments.content.onFileAttachmentContentItemClick
import io.getstream.chat.android.compose.ui.attachments.preview.handler.AttachmentPreviewHandler
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.uiutils.extension.isAnyFileType

/**
 * An [AttachmentFactory] that validates attachments as files and uses [FileAttachmentContent] to
 * build the UI for the message.
 *
 * @param showFileSize Lambda that determines whether to show the file size in the attachment content.
 * @param onContentItemClick Lambda called when an item gets clicked.
 * @param canHandle Lambda that checks if the factory can handle the given attachments.
 */
public class FileAttachmentFactory(
    showFileSize: (Attachment) -> Boolean = { true },
    onContentItemClick: (
        previewHandlers: List<AttachmentPreviewHandler>,
        attachment: Attachment,
    ) -> Unit = ::onFileAttachmentContentItemClick,
    canHandle: (attachments: List<Attachment>) -> Boolean = { attachments ->
        attachments.any(Attachment::isAnyFileType)
    },
) : AttachmentFactory(
    type = Type.BuiltIn.FILE,
    canHandle = canHandle,
    previewContent = @Composable { modifier, attachments, onAttachmentRemoved ->
        ChatTheme.componentFactory.FileAttachmentPreviewContent(
            modifier = modifier,
            attachments = attachments,
            onAttachmentRemoved = onAttachmentRemoved,
        )
    },
    content = @Composable { modifier, state ->
        ChatTheme.componentFactory.FileAttachmentContent(
            modifier = modifier
                .wrapContentHeight()
                .width(ChatTheme.dimens.attachmentsContentFileWidth),
            attachmentState = state,
            showFileSize = showFileSize,
            onItemClick = onContentItemClick,
        )
    },
)
