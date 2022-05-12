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

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.ui.util.MimeTypeIconProvider

/**
 * Builds a file attachment quoted message which shows a single file in the attachments list.
 *
 * @param attachment The attachment we wish to show to users.
 */
@Composable
public fun FileAttachmentQuotedContent(
    attachment: Attachment,
    modifier: Modifier = Modifier,
) {
    val isImage = attachment.type == "image"

    val painter = if (isImage) {
        val dataToLoad = attachment.imageUrl ?: attachment.upload

        rememberImagePainter(dataToLoad)
    } else {
        painterResource(id = MimeTypeIconProvider.getIconRes(attachment.mimeType))
    }

    Image(
        modifier = modifier,
        painter = painter,
        contentDescription = null,
        contentScale = if (isImage) ContentScale.Crop else ContentScale.Fit
    )
}
