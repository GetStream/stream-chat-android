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
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.extensions.duration
import io.getstream.chat.android.client.extensions.waveformData
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.client.utils.message.isMine
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.components.audio.WaveformSlider
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.padding
import io.getstream.chat.android.compose.ui.util.size
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModelFactory
import io.getstream.chat.android.extensions.isInt
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState
import io.getstream.chat.android.ui.common.utils.DurationFormatter

/**
 * Represents the audio recording attachment content.
 */
@Deprecated(
    message = "Use AudioRecordAttachmentContent with 4 parameters instead",
    replaceWith = ReplaceWith(
        expression = "AudioRecordAttachmentContent(/* parameters */, getCurrentUserId = { /* your implementation */ })",
        imports = ["io.getstream.chat.android.compose.ui.attachments.content"],
    ),
)
@Composable
public fun AudioRecordGroupContent(
    modifier: Modifier = Modifier,
    attachmentState: AttachmentState,
    viewModelFactory: AudioPlayerViewModelFactory,
) {
    AudioRecordAttachmentContent(
        modifier = modifier,
        attachmentState = attachmentState,
        viewModelFactory = viewModelFactory,
    )
}

/**
 * Represents the audio recording attachment content item.
 */
@Deprecated(
    message = "Use AudioRecordAttachmentContentItem instead",
    replaceWith = ReplaceWith(
        expression = "AudioRecordAttachmentContentItem(/* parameters */)",
        imports = ["io.getstream.chat.android.compose.ui.attachments.content"],
    ),
)
@Composable
public fun AudioRecordAttachmentContent(
    modifier: Modifier = Modifier,
    attachment: Attachment,
    playerState: AudioPlayerState?,
    onPlayToggleClick: (Attachment) -> Unit,
    onPlaySpeedClick: (Attachment) -> Unit,
    onScrubberDragStart: (Attachment) -> Unit = {},
    onScrubberDragStop: (Attachment, Float) -> Unit = { _, _ -> },
) {
    AudioRecordAttachmentContentItem(
        modifier = modifier,
        attachment = attachment,
        playerState = playerState,
        onPlayToggleClick = onPlayToggleClick,
        onPlaySpeedClick = onPlaySpeedClick,
        onThumbDragStart = onScrubberDragStart,
        onThumbDragStop = onScrubberDragStop,
    )
}

/**
 * Represents the audio recording attachment content.
 *
 * @param modifier Modifier for styling.
 * @param attachmentState The state of the attachment.
 * @param viewModelFactory The factory for creating the [AudioPlayerViewModel].
 * @param getCurrentUserId The function to get the current user ID.
 */
@Composable
public fun AudioRecordAttachmentContent(
    modifier: Modifier = Modifier,
    attachmentState: AttachmentState,
    viewModelFactory: AudioPlayerViewModelFactory,
    getCurrentUserId: () -> String? = { null },
) {
    val viewModel = viewModel(AudioPlayerViewModel::class.java, factory = viewModelFactory)

    val audioRecordings = attachmentState.message.attachments
        .filter { attachment -> attachment.isAudioRecording() && attachment.assetUrl != null }

    val playerState by viewModel.state.collectAsStateWithLifecycle()

    val isMine = attachmentState.message.isMine(getCurrentUserId())
    Column(modifier = modifier) {
        audioRecordings.forEach { audioRecording ->
            AudioRecordAttachmentContentItem(
                attachment = audioRecording,
                playerState = playerState,
                isMine = isMine,
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
@OptIn(ExperimentalMaterialApi::class)
@Composable
public fun AudioRecordAttachmentContentItem(
    modifier: Modifier = Modifier,
    attachment: Attachment,
    playerState: AudioPlayerState?,
    isMine: Boolean = false,
    onPlayToggleClick: (Attachment) -> Unit = {},
    onPlaySpeedClick: (Attachment) -> Unit = {},
    onThumbDragStart: (Attachment) -> Unit = {},
    onThumbDragStop: (Attachment, Float) -> Unit = { _, _ -> },
) {
    val isAttachmentPlaying = playerState?.attachment?.assetUrl == attachment.assetUrl
    val trackProgress = playerState?.playingProgress?.takeIf { isAttachmentPlaying } ?: 0F
    val playing = isAttachmentPlaying && playerState?.isPlaying == true
    val playbackText = when (playing) {
        true -> (playerState?.playbackInMs ?: 0).let(DurationFormatter::formatDurationInMillis)
        else -> (attachment.duration ?: 0f).let(DurationFormatter::formatDurationInSeconds)
    }
    val speed = playerState?.playingSpeed?.takeIf { isAttachmentPlaying } ?: 1F
    val waveform = when (playing) {
        true -> playerState?.waveform ?: emptyList()
        else -> attachment.waveformData ?: emptyList()
    }

    val theme = when (isMine) {
        true -> ChatTheme.ownMessageTheme.audioRecording
        else -> ChatTheme.otherMessageTheme.audioRecording
    }

    Surface(
        modifier = modifier
            .padding(2.dp)
            .fillMaxWidth(),
        color = ChatTheme.colors.appBackground,
        shape = ChatTheme.shapes.attachment,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(theme.height)
                .padding(theme.padding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(
                elevation = 1.dp,
                shape = CircleShape,
            ) {
                val toggleStyle = when (playing) {
                    true -> theme.pauseButton
                    else -> theme.playButton
                }
                IconButton(
                    onClick = { onPlayToggleClick(attachment) },
                    modifier = Modifier
                        .size(toggleStyle.size)
                        .padding(toggleStyle.padding),
                ) {
                    Icon(
                        painter = toggleStyle.icon.painter,
                        modifier = Modifier.size(toggleStyle.icon.size),
                        contentDescription = null,
                        tint = toggleStyle.icon.tint,
                    )
                }
            }

            Text(
                modifier = Modifier
                    .width(theme.timerTextWidth),
                style = theme.timerTextStyle,
                text = playbackText,
                textAlign = TextAlign.Center,
            )

            WaveformSlider(
                modifier = Modifier
                    .height(theme.waveformSliderHeight)
                    .weight(1f),
                style = theme.waveformSliderStyle,
                waveformData = waveform,
                progress = trackProgress,
                onDragStart = {
                    onThumbDragStart(attachment)
                },
                onDragStop = { progress ->
                    onThumbDragStop(attachment, progress)
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(width = theme.tailWidth),
                contentAlignment = Alignment.Center,
            ) {
                if (playing) {
                    Card(
                        onClick = { onPlaySpeedClick(attachment) },
                        elevation = 1.dp,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(theme.speedButton.size),
                        backgroundColor = theme.speedButton.backgroundColor,
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
                                style = theme.speedButton.textStyle,
                            )
                        }
                    }
                } else {
                    Icon(
                        modifier = Modifier
                            .size(theme.contentTypeIcon.size),
                        painter = theme.contentTypeIcon.painter,
                        contentDescription = null,
                        tint = theme.contentTypeIcon.tint,
                    )
                }
            }
        }
    }
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
                attachment = attachment,
                isPlaying = true,
            ),
            onPlayToggleClick = {},
            onPlaySpeedClick = {},
        )
    }
}
