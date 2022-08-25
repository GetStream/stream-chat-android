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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.MaximumNumberOfItemsInAGrid
import io.getstream.chat.android.compose.ui.attachments.content.MediaAttachmentContent
import io.getstream.chat.android.compose.ui.attachments.content.MediaAttachmentPreviewContent
import io.getstream.chat.android.compose.ui.attachments.content.MediaAttachmentPreviewItemSize
import io.getstream.chat.android.compose.ui.attachments.content.PlayButton
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.uiutils.constant.AttachmentType

/**
 * An [AttachmentFactory] that is able to handle Image and Video attachments.
 *
 * @param contentPlayButton Displays a play button above video attachments
 * in the messages list.
 * @param previewContentPlayButton Displays a play button above video attachments
 * in the message input.
 */
@Suppress("FunctionName")
public fun MediaAttachmentFactory(
    contentPlayButton: @Composable () -> Unit = { DefaultContentPlayButton() },
    previewContentPlayButton: @Composable () -> Unit = { DefaultPreviewContentPlayButton() },
): AttachmentFactory =
    AttachmentFactory(
        canHandle = {
            it.none { attachment ->
                attachment.type != AttachmentType.IMAGE && attachment.type != AttachmentType.VIDEO
            }
        },
        previewContent = { modifier, attachments, onAttachmentRemoved ->
            MediaAttachmentPreviewContent(
                attachments = attachments,
                onAttachmentRemoved = onAttachmentRemoved,
                modifier = modifier,
                playButton = previewContentPlayButton
            )
        },
        content = @Composable { modifier, state ->
            MediaAttachmentContent(
                modifier = modifier
                    .width(ChatTheme.dimens.attachmentsContentImageWidth)
                    .wrapContentHeight()
                    .heightIn(max = ChatTheme.dimens.attachmentsContentImageMaxHeight),
                attachmentState = state,
                playButton = { contentPlayButton() }
            )
        }
    )

/**
 * Represents the default play button that is
 * overlaid above video attachment previews inside
 * the messages list.
 */
@Composable
private fun DefaultContentPlayButton() {
    PlayButton(
        modifier = Modifier
            .shadow(10.dp, shape = CircleShape)
            .background(color = Color.White, shape = CircleShape)
            .size(
                width = ChatTheme.dimens.attachmentsContentImageWidth / MaximumNumberOfItemsInAGrid,
                height = ChatTheme.dimens.attachmentsContentImageWidth / MaximumNumberOfItemsInAGrid,
            )
    )
}

/**
 * Represents the default play button that is
 * overlaid above video attachment previews inside
 * the message input.
 */
@Composable
private fun DefaultPreviewContentPlayButton() {
    PlayButton(
        modifier = Modifier
            .shadow(10.dp, shape = CircleShape)
            .background(color = Color.White, shape = CircleShape)
            .size(
                width = (MediaAttachmentPreviewItemSize / 4).dp,
                height = (MediaAttachmentPreviewItemSize / 4).dp
            )
    )
}
