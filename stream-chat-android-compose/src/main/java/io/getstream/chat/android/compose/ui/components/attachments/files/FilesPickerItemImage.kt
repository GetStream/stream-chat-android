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

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.ui.theme.StreamTokens
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

    if (isImage) {
        StreamAsyncImage(
            modifier = modifier.clip(RoundedCornerShape(StreamTokens.radiusMd)),
            data = attachment.uri ?: attachment.file,
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )
    } else {
        FileTypeIcon(
            data = MimeTypeIconProvider.getIcon(attachment.mimeType),
            modifier = modifier,
        )
    }
}
