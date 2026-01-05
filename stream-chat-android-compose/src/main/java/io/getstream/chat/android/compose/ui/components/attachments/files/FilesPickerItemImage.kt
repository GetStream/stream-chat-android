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

package io.getstream.chat.android.compose.ui.components.attachments.files

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.MimeTypeIconProvider
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage

/**
 * Represents the image that's shown in file picker items. This can be either an image/icon that represents the file
 * type or a thumbnail in case the file type is an image.
 *
 * @param fileItem - The item we use to show the image.
 */
@Composable
public fun FilesPickerItemImage(
    fileItem: AttachmentPickerItemState,
    modifier: Modifier = Modifier,
) {
    val attachment = fileItem.attachmentMetaData
    val isImage = fileItem.attachmentMetaData.type == "image"

    val data = if (isImage) {
        attachment.uri ?: attachment.file
    } else {
        MimeTypeIconProvider.getIconRes(attachment.mimeType)
    }

    val shape = if (isImage) ChatTheme.shapes.imageThumbnail else null

    val imageModifier = modifier.let { baseModifier ->
        if (shape != null) baseModifier.clip(shape) else baseModifier
    }

    val contentScale = if (isImage) {
        ContentScale.Crop
    } else {
        ContentScale.Fit
    }

    StreamAsyncImage(
        modifier = imageModifier,
        data = data,
        contentScale = contentScale,
        contentDescription = null,
    )
}
