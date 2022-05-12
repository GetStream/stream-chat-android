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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Builds an image attachment for a quoted message which is composed from a singe attachment previewing the attached
 * image, link preview or giphy.
 *
 * @param attachment The attachment we wish to show to users.
 * @param modifier Modifier for styling.
 */
@Composable
public fun ImageAttachmentQuotedContent(
    attachment: Attachment,
    modifier: Modifier = Modifier,
) {
    val imagePainter = rememberImagePainter(attachment.imagePreviewUrl)

    val startPadding = ChatTheme.dimens.quotedMessageAttachmentStartPadding
    val verticalPadding = ChatTheme.dimens.quotedMessageAttachmentVerticalPadding
    val size = ChatTheme.dimens.quotedMessageAttachmentPreviewSize

    Image(
        modifier = modifier
            .padding(start = startPadding, top = verticalPadding, bottom = verticalPadding)
            .size(size)
            .clip(ChatTheme.shapes.quotedAttachment),
        painter = imagePainter,
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}
