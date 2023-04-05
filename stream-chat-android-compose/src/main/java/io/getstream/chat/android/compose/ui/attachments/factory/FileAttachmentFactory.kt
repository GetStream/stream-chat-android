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

package io.getstream.chat.android.compose.ui.attachments.factory

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentContent
import io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentPreviewContent
import io.getstream.chat.android.compose.ui.attachments.content.onFileAttachmentContentItemClicked
import io.getstream.chat.android.compose.ui.attachments.preview.handler.AttachmentPreviewHandler
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.uiutils.extension.isFile

/**
 * An [AttachmentFactory] that validates attachments as files and uses [FileAttachmentContent] to
 * build the UI for the message.
 *
 * @param onContentItemClick Lambda called when an item gets clicked.
 */
@Suppress("FunctionName")
public fun FileAttachmentFactory(
    onContentItemClick: (
        previewHandlers: List<AttachmentPreviewHandler>,
        attachment: Attachment,
    ) -> Unit = ::onFileAttachmentContentItemClicked,
): AttachmentFactory = AttachmentFactory(
    canHandle = { attachments ->
        attachments.any { it.isFile() }
    },
    previewContent = @Composable { modifier, attachments, onAttachmentRemoved ->
        FileAttachmentPreviewContent(
            modifier = modifier,
            attachments = attachments,
            onAttachmentRemoved = onAttachmentRemoved
        )
    },
    content = @Composable { modifier, state ->
        FileAttachmentContent(
            modifier = modifier
                .wrapContentHeight()
                .width(ChatTheme.dimens.attachmentsContentFileWidth),
            attachmentState = state,
            onItemClicked = onContentItemClick,
        )
    },
)
