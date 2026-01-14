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
import io.getstream.chat.android.compose.ui.attachments.content.FileUploadContent
import io.getstream.chat.android.compose.ui.attachments.content.onFileUploadContentItemClick
import io.getstream.chat.android.compose.ui.attachments.preview.handler.AttachmentPreviewHandler
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.utils.extensions.isUploading

/**
 * An [AttachmentFactory] that validates and shows uploading attachments using [FileUploadContent].
 * Has no "preview content", given that this attachment only exists after being sent.
 *
 * @param onContentItemClick Lambda called when an item gets clicked.
 * @param canHandle Lambda that checks if the factory can handle the given attachments.
 */
public class UploadAttachmentFactory(
    onContentItemClick: (Attachment, List<AttachmentPreviewHandler>) -> Unit = ::onFileUploadContentItemClick,
    canHandle: (attachments: List<Attachment>) -> Boolean = { attachments -> attachments.any(Attachment::isUploading) },
) : AttachmentFactory(
    type = Type.BuiltIn.UPLOAD,
    canHandle = canHandle,
    content = @Composable { modifier, state ->
        ChatTheme.componentFactory.FileUploadContent(
            modifier = modifier
                .wrapContentHeight()
                .width(ChatTheme.dimens.attachmentsContentFileUploadWidth),
            attachmentState = state,
            onItemClick = onContentItemClick,
        )
    },
)
