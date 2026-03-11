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

package io.getstream.chat.android.compose.ui.messages.composer.internal.attachments

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.attachments.content.AudioRecordAttachmentContentItemBase
import io.getstream.chat.android.compose.ui.components.ComposerCancelIcon
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.previewdata.PreviewAttachmentData
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState

@Composable
internal fun MessageComposerAttachmentAudioRecordItem(
    attachment: Attachment,
    playerState: AudioPlayerState,
    modifier: Modifier = Modifier,
    onPlayToggleClick: (Attachment) -> Unit = {},
    onThumbDragStart: (Attachment) -> Unit = {},
    onThumbDragStop: (Attachment, Float) -> Unit = { _, _ -> },
    onAttachmentRemoved: (Attachment) -> Unit = {},
) {
    val currentAttachment by rememberUpdatedState(attachment)
    AudioRecordAttachmentContentItemBase(
        modifier = modifier,
        attachment = attachment,
        outlineColor = ChatTheme.colors.borderUtilitySelected,
        textColor = ChatTheme.colors.chatTextOutgoing,
        playerState = playerState,
        waveformHeight = 36.dp,
        onPlayToggleClick = onPlayToggleClick,
        onThumbDragStart = onThumbDragStart,
        onThumbDragStop = onThumbDragStop,
        tailContent = {
            ComposerCancelIcon(
                modifier = Modifier
                    .padding(4.dp),
                onClick = { onAttachmentRemoved(currentAttachment) },
            )
        },
    )
}

@Preview
@Composable
private fun MessageComposerAttachmentAudioRecordItemPreview() {
    ChatPreviewTheme {
        MessageComposerAttachmentAudioRecordItem()
    }
}

@Composable
internal fun MessageComposerAttachmentAudioRecordItem() {
    MessageComposerAttachmentAudioRecordItem(
        attachment = PreviewAttachmentData.attachmentAudioRecording1,
        playerState = AudioPlayerState(getRecordingUri = Attachment::assetUrl),
    )
}
