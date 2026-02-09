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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.audio.audioHash
import io.getstream.chat.android.client.extensions.durationInMs
import io.getstream.chat.android.client.extensions.waveformData
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.components.audio.PlaybackTimerBox
import io.getstream.chat.android.compose.ui.components.audio.StaticWaveformSlider
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ComponentPadding
import io.getstream.chat.android.compose.ui.theme.ComponentSize
import io.getstream.chat.android.compose.ui.theme.IconContainerStyle
import io.getstream.chat.android.compose.ui.theme.IconStyle
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.TextContainerStyle
import io.getstream.chat.android.compose.ui.theme.WaveformSliderLayoutStyle
import io.getstream.chat.android.compose.ui.util.applyIf
import io.getstream.chat.android.compose.ui.util.padding
import io.getstream.chat.android.compose.ui.util.shouldBeDisplayedAsFullSizeAttachment
import io.getstream.chat.android.compose.ui.util.size
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModelFactory
import io.getstream.chat.android.extensions.isInt
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState

/**
 * Represents the audio recording attachment content.
 *
 * @param modifier Modifier for styling.
 * @param attachmentState The state of the attachment.
 * @param viewModelFactory The factory for creating the [AudioPlayerViewModel].
 */
@Composable
public fun AudioRecordAttachmentContent(
    modifier: Modifier = Modifier,
    attachmentState: AttachmentState,
    viewModelFactory: AudioPlayerViewModelFactory,
) {
    val viewModel = viewModel(AudioPlayerViewModel::class.java, factory = viewModelFactory)

    val audioRecordings = attachmentState.message.attachments
        .filter { attachment ->
            val attachmentUrl = attachment.assetUrl ?: attachment.upload?.toUri()?.toString()
            attachment.isAudioRecording() && attachmentUrl != null
        }

    val playerState by viewModel.state.collectAsStateWithLifecycle()

    val shouldBeFullSize = attachmentState.message.shouldBeDisplayedAsFullSizeAttachment()
    Column(
        modifier = modifier.applyIf(!shouldBeFullSize) { padding(MessageStyling.messageSectionPadding) },
    ) {
        audioRecordings.forEach { audioRecording ->
            AudioRecordAttachmentContentItem(
                attachment = audioRecording,
                playerState = playerState,
                isMine = attachmentState.isMine,
                onPlayToggleClick = { attachment ->
                    viewModel.playOrPause(attachment)
                },
                onPlaySpeedClick = { attachment ->
                    viewModel.changeSpeed(attachment)
                },
                onThumbDragStart = { attachment ->
                    viewModel.startSeek(attachment)
                },
                onThumbDragStop = { attachment, progress ->
                    viewModel.seekTo(attachment, progress)
                },
            )
        }
    }

    // Cleanup: Pause any playing tracks in onPause.
    LifecycleEventEffect(event = Lifecycle.Event.ON_PAUSE) {
        // Important: This effect is disposed when the parent composable is disposed. A side effect of this is that if
        // the AudioRecordAttachmentContent is shown in LazyList, and is scrolled away, the effect is disposed and the
        // lifecycle event is not received. Therefore, the audio needs to be paused higher in the hierarchy.
        viewModel.pause()
    }
}

/**
 * Represents the audio recording attachment content item.
 *
 * @param modifier Modifier for styling.
 * @param attachment The attachment to display.
 * @param playerState The state of the audio player.
 * @param isMine If the message is from the current user.
 * @param onPlayToggleClick The callback for when the play button is clicked.
 * @param onPlaySpeedClick The callback for when the speed button is clicked.
 * @param onThumbDragStart The callback for when the thumb gets dragged.
 * @param onThumbDragStop The callback for when the thumb gets released.
 */
@Composable
public fun AudioRecordAttachmentContentItem(
    modifier: Modifier = Modifier,
    attachment: Attachment,
    playerState: AudioPlayerState,
    isMine: Boolean = false,
    onPlayToggleClick: (Attachment) -> Unit = {},
    onPlaySpeedClick: (Attachment) -> Unit = {},
    onThumbDragStart: (Attachment) -> Unit = {},
    onThumbDragStop: (Attachment, Float) -> Unit = { _, _ -> },
) {
    val currentAttachment by rememberUpdatedState(attachment)
    val theme = when (isMine) {
        true -> ChatTheme.ownMessageTheme.audioRecording
        else -> ChatTheme.otherMessageTheme.audioRecording
    }
    AudioRecordAttachmentContentItemBase(
        modifier = modifier.fillMaxWidth(),
        attachment = attachment,
        playerState = playerState,
        size = theme.size,
        padding = theme.padding,
        playbackToggleStyle = { isPlaying -> if (isPlaying) theme.pauseButton else theme.playButton },
        timerStyle = theme.timerStyle,
        waveformSliderStyle = theme.waveformSliderStyle,
        onPlayToggleClick = onPlayToggleClick,
        onThumbDragStart = onThumbDragStart,
        onThumbDragStop = onThumbDragStop,
        tailContent = { isPlaying ->
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(width = theme.tailWidth),
                contentAlignment = Alignment.Center,
            ) {
                if (isPlaying) {
                    val speed = playerState.current.playingSpeed
                    SpeedButton(speed, theme.speedButton) { onPlaySpeedClick(currentAttachment) }
                } else {
                    ContentTypeIcon(theme.contentTypeIcon)
                }
            }
        },
    )
}

@Composable
internal fun AudioRecordAttachmentContentItemBase(
    modifier: Modifier = Modifier,
    attachment: Attachment,
    playerState: AudioPlayerState,
    size: ComponentSize,
    padding: ComponentPadding,
    playbackToggleStyle: (isPlaying: Boolean) -> IconContainerStyle,
    timerStyle: TextContainerStyle,
    waveformSliderStyle: WaveformSliderLayoutStyle,
    onPlayToggleClick: (Attachment) -> Unit = {},
    onThumbDragStart: (Attachment) -> Unit = {},
    onThumbDragStop: (Attachment, Float) -> Unit = { _, _ -> },
    tailContent: @Composable (isPlaying: Boolean) -> Unit = {},
) {
    val attachmentUrl = playerState.getRecordingUri(attachment)
    val isCurrentAttachment = attachmentUrl == playerState.current.audioUri
    val trackProgress = playerState.current.playingProgress.takeIf { isCurrentAttachment }
        ?: attachmentUrl?.let { playerState.seekTo.getOrDefault(attachment.audioHash, 0f) } ?: 0f
    val playing = isCurrentAttachment && playerState.current.isPlaying
    val waveform = when (playing) {
        true -> playerState.current.waveform
        else -> attachment.waveformData
    } ?: emptyList()

    val currentAttachment by rememberUpdatedState(attachment)
    Surface(
        modifier = modifier
            .padding(2.dp),
        color = ChatTheme.colors.appBackground,
        shape = ChatTheme.shapes.attachment,
    ) {
        Row(
            modifier = Modifier
                .size(size)
                .padding(padding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaybackToggleButton(playbackToggleStyle(playing)) { onPlayToggleClick(currentAttachment) }

            var currentProgress by remember { mutableFloatStateOf(trackProgress) }
            LaunchedEffect(attachmentUrl, playing, trackProgress) { currentProgress = trackProgress }

            PlaybackTimerBox(currentProgress, currentAttachment.durationInMs, timerStyle)

            StaticWaveformSlider(
                modifier = Modifier
                    .height(waveformSliderStyle.height)
                    .weight(1f),
                style = waveformSliderStyle.style,
                waveformData = waveform,
                progress = currentProgress,
                onDragStart = {
                    currentProgress = it
                    onThumbDragStart(currentAttachment)
                },
                onDrag = {
                    currentProgress = it
                },
                onDragStop = {
                    currentProgress = it
                    onThumbDragStop(currentAttachment, it)
                },
            )

            tailContent(playing)
        }
    }
}

/**
 * Represents the playback toggle button.
 *
 * @param style The style for the toggle button.
 * @param onClick The callback for when the button is clicked.
 */
@Composable
internal fun PlaybackToggleButton(
    style: IconContainerStyle,
    onClick: () -> Unit = {},
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(style.size)
                .padding(style.padding),
        ) {
            Icon(
                painter = style.icon.painter,
                modifier = Modifier.size(style.icon.size),
                contentDescription = null,
                tint = style.icon.tint,
            )
        }
    }
}

/**
 * Represents the speed button.
 */
@Composable
private fun SpeedButton(
    speed: Float,
    style: TextContainerStyle,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = CircleShape,
        modifier = Modifier
            .size(style.size),
        colors = CardDefaults.cardColors(containerColor = style.backgroundColor),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = when (speed.isInt()) {
                    true -> "x${speed.toInt()}"
                    else -> "x$speed"
                },
                style = style.textStyle,
            )
        }
    }
}

/**
 * Represents the content type icon.
 */
@Composable
private fun ContentTypeIcon(style: IconStyle) {
    Icon(
        modifier = Modifier
            .size(style.size),
        painter = style.painter,
        contentDescription = null,
        tint = style.tint,
    )
}

@Preview(showBackground = true)
@Composable
internal fun AudioRecordAttachmentContentItemPreview() {
    val attachment = Attachment(type = "audio_recording")

    ChatPreviewTheme {
        AudioRecordAttachmentContentItem(
            modifier = Modifier
                .width(250.dp)
                .height(60.dp),
            attachment = attachment,
            playerState = AudioPlayerState(
                current = AudioPlayerState.CurrentAudioState(
                    audioUri = attachment.assetUrl.orEmpty(),
                    isPlaying = true,
                ),
                getRecordingUri = Attachment::assetUrl,
            ),
            onPlayToggleClick = {},
            onPlaySpeedClick = {},
        )
    }
}
