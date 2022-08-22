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

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.content.MediaAttachmentContent
import io.getstream.chat.android.compose.ui.attachments.content.PlayButton
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.uiutils.constant.AttachmentType

/**
 * An [AttachmentFactory] that is able to handle Image and Video attachments.
 *
 * @param playButton Displays a play button above video attachments.
 */
@Suppress("FunctionName")
public fun MediaAttachmentFactory(
    playButton: @Composable () -> Unit = { PlayButton() },
): AttachmentFactory =
    AttachmentFactory(
        canHandle = {
            it.none { attachment -> attachment.type != AttachmentType.IMAGE && attachment.type != AttachmentType.VIDEO }
        },
        previewContent = { modifier, attachments, onAttachmentRemoved -> },
        content = @Composable { modifier, state ->
            MediaAttachmentContent(
                modifier = modifier
                    .width(ChatTheme.dimens.attachmentsContentImageWidth)
                    .wrapContentHeight()
                    .heightIn(max = ChatTheme.dimens.attachmentsContentImageMaxHeight),
                attachmentState = state,
                playButton = { playButton() }
            )
        }
    )
