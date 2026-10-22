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

package io.getstream.chat.android.ai.compose.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.ui.component.internal.AttachmentImage
import io.getstream.chat.android.ai.compose.ui.component.internal.FileAttachmentCard

/**
 * One of a message's attachments: an image as a square thumbnail, and any other kind as a file,
 * with its name, type and size, that opens when tapped.
 *
 * ```
 * AIMessageAttachmentItem(attachment = AIMessageAttachment.file(uri, name = "Itinerary.pdf"))
 * ```
 *
 * An image shows the bitmap the app holds, else the one at its URI, on this device or on the web,
 * loaded at the size it shows at. An image with neither shows as a file. A file opens in an app
 * for its type, and a page on the web in the browser.
 *
 * @param attachment What to show.
 * @param modifier The modifier to apply to the attachment.
 * @param imageSize The width and height of an image.
 */
@Composable
public fun AIMessageAttachmentItem(
    attachment: AIMessageAttachment,
    modifier: Modifier = Modifier,
    imageSize: Dp = 180.dp,
) {
    val isImage = attachment.kind == AIMessageAttachment.Kind.Image
    if (isImage && (attachment.bitmap != null || attachment.uri != null)) {
        AttachmentImage(attachment = attachment, size = imageSize, modifier = modifier)
    } else {
        FileAttachmentCard(attachment = attachment, modifier = modifier)
    }
}
