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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.components.audio.PlaybackTimerText
import io.getstream.chat.android.compose.ui.components.audio.StaticWaveformSlider
import io.getstream.chat.android.compose.ui.components.button.StreamButton
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.theme.WaveformSliderLayoutStyle
import io.getstream.chat.android.compose.ui.theme.WaveformSliderStyle
import io.getstream.chat.android.compose.ui.util.applyIf
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.compose.ui.util.shouldBeDisplayedAsFullSizeAttachment
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModelFactory
import io.getstream.chat.android.extensions.isInt
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState
import kotlin.random.Random

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
        verticalArrangement = Arrangement.spacedBy(MessageStyling.sectionsDistance),
    ) {
        audioRecordings.forEach { audioRecording ->
            AudioRecordAttachmentContentItem(
                modifier = Modifier.applyIf(!shouldBeFullSize) {
                    background(
                        MessageStyling.attachmentBackgroundColor(attachmentState.isMine),
                        RoundedCornerShape(StreamTokens.radiusLg),
                    )
                },
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
    val colors = ChatTheme.colors
    val outlineColor = if (isMine) colors.chatBorderOnChatOutgoing else colors.chatBorderOnChatIncoming

    AudioRecordAttachmentContentItemBase(
        modifier = modifier,
        attachment = attachment,
        playerState = playerState,
        waveformSliderStyle = WaveformSliderLayoutStyle(
            height = 36.dp,
            style = WaveformSliderStyle.defaultStyle(colors = colors),
        ),
        outlineColor = outlineColor,
        onPlayToggleClick = onPlayToggleClick,
        onThumbDragStart = onThumbDragStart,
        onThumbDragStop = onThumbDragStop,
        tailContent = {
            val speed = playerState.speeds.getOrDefault(attachment.audioHash, 1f)
            SpeedButton(speed = speed, outlineColor = outlineColor) {
                onPlaySpeedClick(currentAttachment)
            }
        },
    )
}

@Composable
@Suppress("LongMethod")
internal fun AudioRecordAttachmentContentItemBase(
    modifier: Modifier = Modifier,
    attachment: Attachment,
    playerState: AudioPlayerState,
    waveformSliderStyle: WaveformSliderLayoutStyle,
    outlineColor: Color,
    onPlayToggleClick: (Attachment) -> Unit = {},
    onThumbDragStart: (Attachment) -> Unit = {},
    onThumbDragStop: (Attachment, Float) -> Unit = { _, _ -> },
    tailContent: @Composable () -> Unit = {},
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
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 64.dp)
            .fillMaxWidth()
            .padding(
                top = StreamTokens.spacingXs,
                bottom = StreamTokens.spacingXs,
                start = StreamTokens.spacingXs,
                end = StreamTokens.spacingSm,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        PlaybackToggleButton(playing = playing, outlineColor = outlineColor) {
            onPlayToggleClick(currentAttachment)
        }

        var currentProgress by remember { mutableFloatStateOf(trackProgress) }
        LaunchedEffect(attachmentUrl, playing, trackProgress) { currentProgress = trackProgress }

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

        val timerTextColor =
            if (playing) {
                ChatTheme.colors.accentPrimary
            } else {
                ChatTheme.colors.chatTextIncoming
            }
        PlaybackTimerText(
            progress = currentProgress,
            durationInMs = currentAttachment.durationInMs,
            color = timerTextColor,
            countdown = true,
        )

        tailContent()
    }
}

/**
 * Represents the playback toggle button.
 *
 * @param onClick The callback for when the button is clicked.
 */
@Composable
internal fun PlaybackToggleButton(
    playing: Boolean,
    outlineColor: Color,
    onClick: () -> Unit = {},
) {
    val icon =
        if (playing) {
            painterResource(id = R.drawable.stream_compose_ic_pause)
        } else {
            painterResource(id = R.drawable.stream_compose_ic_play)
        }

    StreamButton(
        onClick = onClick,
        style = StreamButtonStyleDefaults.secondaryOutline.copy(borderColor = outlineColor),
        modifier = Modifier.padding(StreamTokens.spacing2xs),
    ) {
        Icon(
            painter = icon,
            modifier = Modifier.size(20.dp),
            contentDescription = null,
        )
    }
}

private val speedButtonShape = RoundedCornerShape(StreamTokens.radiusLg)

/**
 * Represents the speed button.
 */
@Composable
private fun SpeedButton(
    speed: Float,
    outlineColor: Color,
    onClick: () -> Unit,
) {
    Text(
        text = when (speed.isInt()) {
            true -> "x${speed.toInt()}"
            else -> "x$speed"
        },
        style = ChatTheme.typography.metadataEmphasis,
        modifier = Modifier
            .border(1.dp, outlineColor, speedButtonShape)
            .clip(speedButtonShape)
            .clickable(onClick = onClick)
            .padding(horizontal = StreamTokens.spacingXs, vertical = StreamTokens.spacing2xs),
    )
}

@Preview(showBackground = true)
@Composable
internal fun AudioRecordAttachmentContentItemPreview() {
    ChatPreviewTheme {
        val rand = Random(1)
        val previewUri = "preview://audio"

        AudioRecordAttachmentContentItem(
            attachment = Attachment(type = "audio_recording", assetUrl = previewUri),
            playerState = AudioPlayerState(
                current = AudioPlayerState.CurrentAudioState(
                    isPlaying = true,
                    audioUri = previewUri,
                    waveform = List(size = 100) { rand.nextFloat() },
                ),
                getRecordingUri = Attachment::assetUrl,
            ),
            onPlayToggleClick = {},
            onPlaySpeedClick = {},
        )
    }
}
