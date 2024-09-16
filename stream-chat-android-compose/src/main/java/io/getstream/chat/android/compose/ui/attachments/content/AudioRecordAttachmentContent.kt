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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.extensions.duration
import io.getstream.chat.android.client.extensions.waveformData
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.components.audio.WaveformSlider
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModelFactory
import io.getstream.chat.android.extensions.isInt
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState
import io.getstream.chat.android.ui.common.utils.DurationFormatter

@Deprecated(
    message = "Use AudioRecordAttachmentContent instead",
    replaceWith = ReplaceWith("AudioRecordAttachmentContent"),
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

@Deprecated(
    message = "Use AudioRecordAttachmentContent instead",
    replaceWith = ReplaceWith("AudioRecordAttachmentContent"),
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
        onScrubberDragStart = onScrubberDragStart,
        onScrubberDragStop = onScrubberDragStop,
    )
}

@Composable
public fun AudioRecordAttachmentContent(
    modifier: Modifier = Modifier,
    attachmentState: AttachmentState,
    viewModelFactory: AudioPlayerViewModelFactory,
) {
    val viewModel = viewModel(AudioPlayerViewModel::class.java, factory = viewModelFactory)

    val audioRecordings = attachmentState.message.attachments
        .filter { attachment -> attachment.isAudioRecording() && attachment.assetUrl != null }

    val playerState by viewModel.state.collectAsState()

    Column(modifier = modifier) {
        audioRecordings.forEach { audioRecording ->
            AudioRecordAttachmentContentItem(
                attachment = audioRecording,
                playerState = playerState,
                onPlayToggleClick = { attachment ->
                    viewModel.playOrPause(attachment)
                },
                onPlaySpeedClick = { attachment ->
                    viewModel.changeSpeed(attachment)
                },
                onScrubberDragStart = { attachment ->
                    viewModel.startSeek(attachment)
                },
                onScrubberDragStop = { attachment, progress ->
                    viewModel.seekTo(attachment, progress)
                },
            )
        }
    }
}

/**
 * Represents fallback content for unsupported attachments.
 *
 * @param modifier Modifier for styling.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
public fun AudioRecordAttachmentContentItem(
    modifier: Modifier = Modifier,
    attachment: Attachment,
    playerState: AudioPlayerState?,
    onPlayToggleClick: (Attachment) -> Unit,
    onPlaySpeedClick: (Attachment) -> Unit,
    onScrubberDragStart: (Attachment) -> Unit = {},
    onScrubberDragStop: (Attachment, Float) -> Unit = { _, _ -> },
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
                .height(60.dp)
                .padding(start = 8.dp, end = 0.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(elevation = 1.dp, shape = CircleShape) {
                IconButton(
                    onClick = { onPlayToggleClick(attachment) },
                    modifier = Modifier
                        .width(36.dp)
                        .height(36.dp),
                ) {
                    Icon(
                        painter = painterResource(
                            id = when (playing) {
                                true -> R.drawable.stream_compose_ic_pause
                                else -> R.drawable.stream_compose_ic_play
                            },
                        ),
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }
            }

            Text(
                modifier = Modifier
                    .size(height = 48.dp, width = 48.dp)
                    .wrapContentSize(Alignment.Center),
                style = ChatTheme.typography.body,
                text = playbackText,
                textAlign = TextAlign.Center,
                color = ChatTheme.colors.textHighEmphasis,
            )

            WaveformSlider(
                modifier = Modifier
                    .height(36.dp)
                    .weight(1f),
                waveformData = waveform,
                progress = trackProgress,
                onDragStart = {
                    onScrubberDragStart(attachment)
                },
                onDragStop = { progress ->
                    onScrubberDragStop(attachment, progress)
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(width = 48.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (playing) {
                    Card(
                        onClick = { onPlaySpeedClick(attachment) },
                        elevation = 2.dp,
                        shape = CircleShape,
                    ) {
                        Text(
                            modifier = Modifier
                                .size(height = 36.dp, width = 36.dp)
                                .wrapContentSize(Alignment.Center),
                            text = when (speed.isInt()) {
                                true -> "x${speed.toInt()}"
                                else -> "x$speed"
                            },
                            style = ChatTheme.typography.body,
                            color = ChatTheme.colors.textHighEmphasis,
                        )
                    }
                } else {
                    Icon(
                        modifier = Modifier
                            .size(height = 40.dp, width = 34.dp),
                        painter = painterResource(id = R.drawable.stream_compose_ic_file_aac),
                        contentDescription = null,
                        tint = Color.Unspecified,
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
            playerState = AudioPlayerState(attachment = attachment),
            onPlayToggleClick = {},
            onPlaySpeedClick = {},
        )
    }
}
