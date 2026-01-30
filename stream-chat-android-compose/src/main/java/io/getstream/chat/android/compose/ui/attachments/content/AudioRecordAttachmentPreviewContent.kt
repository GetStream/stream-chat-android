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

package io.getstream.chat.android.compose.ui.attachments.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.ui.components.ComposerCancelIcon
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModelFactory
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState

@Composable
public fun AudioRecordAttachmentPreviewContent(
    attachments: List<Attachment>,
    onAttachmentRemoved: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
    viewModelFactory: AudioPlayerViewModelFactory,
) {
    val viewModel = viewModel(AudioPlayerViewModel::class.java, factory = viewModelFactory)

    val playerState by viewModel.state.collectAsStateWithLifecycle()

    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        items(attachments) { audioRecording ->
            AudioRecordAttachmentPreviewContentItem(
                attachment = audioRecording,
                playerState = playerState,
                onPlayToggleClick = { attachment ->
                    viewModel.playOrPause(attachment)
                },
                onThumbDragStart = { attachment ->
                    viewModel.startSeek(attachment)
                },
                onThumbDragStop = { attachment, progress ->
                    viewModel.seekTo(attachment, progress)
                },
                onAttachmentRemoved = {
                    viewModel.reset(it)
                    onAttachmentRemoved(it)
                },
            )
        }
    }

    // Cleanup: Pause any playing tracks in onPause.
    LifecycleEventEffect(event = Lifecycle.Event.ON_PAUSE) {
        viewModel.pause()
    }
}

/**
 * Represents fallback content for unsupported attachments.
 *
 * @param modifier Modifier for styling.
 */
@Composable
public fun AudioRecordAttachmentPreviewContentItem(
    modifier: Modifier = Modifier,
    attachment: Attachment,
    playerState: AudioPlayerState,
    onPlayToggleClick: (Attachment) -> Unit = {},
    onThumbDragStart: (Attachment) -> Unit = {},
    onThumbDragStop: (Attachment, Float) -> Unit = { _, _ -> },
    onAttachmentRemoved: (Attachment) -> Unit = {},
) {
    val currentAttachment by rememberUpdatedState(attachment)
    val theme = ChatTheme.messageComposerTheme.attachmentsPreview.audioRecording
    AudioRecordAttachmentContentItemBase(
        modifier = modifier,
        attachment = attachment,
        outlineColor = ChatTheme.colors.borderCorePrimary,
        playerState = playerState,
        waveformSliderStyle = theme.waveformSliderStyle,
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

@Preview(showBackground = true)
@Composable
internal fun AudioRecordAttachmentPreviewContentItemPreview() {
    val waveformData = (0..100).map { it.toFloat() }
    val attachment = Attachment(
        type = "audio_recording",
        extraData = mutableMapOf(
            "waveform" to waveformData,
            "duration" to 1000,
        ),
    )

    ChatPreviewTheme {
        AudioRecordAttachmentPreviewContentItem(
            modifier = Modifier
                .background(Color.Yellow)
                .width(250.dp)
                .height(60.dp),
            attachment = attachment,
            playerState = AudioPlayerState(
                current = AudioPlayerState.CurrentAudioState(
                    audioUri = attachment.assetUrl.orEmpty(),
                    waveform = waveformData,
                    isPlaying = false,
                ),
                getRecordingUri = Attachment::assetUrl,
            ),
        )
    }
}
