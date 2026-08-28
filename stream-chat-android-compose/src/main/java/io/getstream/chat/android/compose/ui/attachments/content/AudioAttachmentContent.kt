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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.audio.audioHash
import io.getstream.chat.android.compose.ui.components.audio.AudioPlayback
import io.getstream.chat.android.compose.ui.components.audio.PlaybackSlider
import io.getstream.chat.android.compose.ui.components.audio.PlaybackTimerText
import io.getstream.chat.android.compose.ui.components.audio.playbackOf
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModelFactory
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Attachment.UploadState
import io.getstream.chat.android.previewdata.PreviewAttachmentData
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState
import io.getstream.chat.android.ui.common.utils.extensions.getDisplayableName

/**
 * A regular audio file attachment rendered with an inline player.
 *
 * Unlike [AudioRecordAttachmentContent], this does not rely on waveform data or on a duration sent along with the
 * attachment: the progress bar and the elapsed time are driven by the player itself.
 *
 * @param attachment The attachment to display.
 * @param isMine If the message is from the current user.
 * @param viewModelFactory The factory for creating the [AudioPlayerViewModel].
 * @param modifier Modifier for styling.
 */
@Composable
internal fun AudioAttachmentItem(
    attachment: Attachment,
    isMine: Boolean,
    viewModelFactory: AudioPlayerViewModelFactory,
    modifier: Modifier = Modifier,
) {
    val viewModel = viewModel(AudioPlayerViewModel::class.java, factory = viewModelFactory)
    val playerState by viewModel.state.collectAsStateWithLifecycle()

    AudioAttachmentContentItem(
        modifier = modifier,
        attachment = attachment,
        playerState = playerState,
        isMine = isMine,
        onPlayToggleClick = viewModel::playOrPause,
        onThumbDragStart = viewModel::startSeek,
        onThumbDragStop = viewModel::seekTo,
    )

    LifecycleEventEffect(event = Lifecycle.Event.ON_PAUSE) {
        // Important: This effect is disposed when the parent composable is disposed. A side effect of this is that if
        // the item is shown in a LazyList and is scrolled away, the effect is disposed and the lifecycle event is not
        // received. Therefore, the audio needs to be paused higher in the hierarchy.
        viewModel.pause()
    }
}

/**
 * Represents a single audio file attachment player: a play/pause toggle, the file name, the elapsed playback time,
 * a seek bar and the file type icon.
 *
 * @param modifier Modifier for styling.
 * @param attachment The attachment to display.
 * @param playerState The state of the audio player.
 * @param isMine If the message is from the current user.
 * @param onPlayToggleClick The callback for when the play button is clicked.
 * @param onThumbDragStart The callback for when the thumb gets dragged.
 * @param onThumbDragStop The callback for when the thumb gets released.
 */
@Composable
public fun AudioAttachmentContentItem(
    modifier: Modifier = Modifier,
    attachment: Attachment,
    playerState: AudioPlayerState,
    isMine: Boolean = false,
    onPlayToggleClick: (Attachment) -> Unit = {},
    onThumbDragStart: (Attachment) -> Unit = {},
    onThumbDragStop: (Attachment, Float) -> Unit = { _, _ -> },
) {
    val colors = ChatTheme.colors
    val outlineColor = if (isMine) colors.chatBorderOnChatOutgoing else colors.chatBorderOnChatIncoming
    val textColor = MessageStyling.textColor(isMine, colors)

    val playback = playerState.playbackOf(attachment)
    val uploadProgress = attachment.uploadState as? UploadState.InProgress

    val currentAttachment by rememberUpdatedState(attachment)
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 64.dp)
            .fillMaxWidth()
            .padding(StreamTokens.spacingXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        PlaybackToggleButton(
            playing = playback.playing,
            outlineColor = outlineColor,
            enabled = uploadProgress == null && playback.hasSource,
        ) {
            onPlayToggleClick(currentAttachment)
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
        ) {
            Text(
                modifier = Modifier.testTag("Stream_AudioAttachmentName"),
                text = attachment.getDisplayableName().orEmpty(),
                style = ChatTheme.typography.captionEmphasis,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (uploadProgress != null) {
                UploadProgressIndicator(uploadState = uploadProgress)
            } else {
                AudioPlaybackProgress(
                    attachment = currentAttachment,
                    playback = playback,
                    textColor = textColor,
                    onThumbDragStart = onThumbDragStart,
                    onThumbDragStop = onThumbDragStop,
                )
            }
        }

        FileAttachmentImage(attachment = attachment, isMine = isMine)
    }
}

/**
 * Holds the dragged progress locally so the thumb follows the finger instead of snapping back to the last value
 * the player reported.
 */
@Composable
private fun AudioPlaybackProgress(
    attachment: Attachment,
    playback: AudioPlayback,
    textColor: Color,
    onThumbDragStart: (Attachment) -> Unit,
    onThumbDragStop: (Attachment, Float) -> Unit,
) {
    val trackProgress = playback.progress
    var currentProgress by remember(attachment.audioHash) { mutableFloatStateOf(trackProgress) }
    LaunchedEffect(trackProgress) {
        if (!playback.isSeeking) currentProgress = trackProgress
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        PlaybackTimerText(
            progress = currentProgress,
            durationInMs = playback.durationInMs,
            color = if (playback.playing) ChatTheme.colors.accentPrimary else textColor,
            countdown = false,
        )

        PlaybackSlider(
            progress = currentProgress,
            isPlaying = playback.playing,
            modifier = Modifier
                .weight(1f)
                .height(SliderHeight)
                .testTag("Stream_AudioAttachmentSlider"),
            enabled = playback.hasSource,
            animationDurationMs = ProgressUpdateIntervalMs,
            onDragStart = {
                currentProgress = it
                onThumbDragStart(attachment)
            },
            onDrag = { currentProgress = it },
            onDragStop = {
                currentProgress = it
                onThumbDragStop(attachment, it)
            },
        )
    }
}

private val SliderHeight = 20.dp

/**
 * Matches the cadence at which the audio player publishes progress updates, so the thumb moves smoothly.
 */
private const val ProgressUpdateIntervalMs = 50

private const val PreviewAudioAssetUrl = "preview://audio"

private val previewAudioAttachment = PreviewAttachmentData.attachmentAudio1.copy(
    assetUrl = PreviewAudioAssetUrl,
)

@Composable
internal fun AudioAttachmentContentItemIdle() {
    AudioAttachmentContentItem(
        attachment = previewAudioAttachment,
        playerState = AudioPlayerState(getRecordingUri = Attachment::assetUrl),
    )
}

@Composable
internal fun AudioAttachmentContentItemPlayback() {
    AudioAttachmentContentItem(
        attachment = previewAudioAttachment,
        playerState = AudioPlayerState(
            current = AudioPlayerState.CurrentAudioState(
                isPlaying = true,
                audioUri = PreviewAudioAssetUrl,
                playingProgress = 0.35f,
                durationInMs = 56_000,
            ),
            getRecordingUri = Attachment::assetUrl,
        ),
    )
}

@Composable
internal fun AudioAttachmentContentItemUploading() {
    AudioAttachmentContentItem(
        attachment = previewAudioAttachment.copy(
            uploadState = UploadState.InProgress(bytesUploaded = 400_000, totalBytes = 1_400_000),
        ),
        playerState = AudioPlayerState(getRecordingUri = Attachment::assetUrl),
    )
}

@Composable
internal fun AudioAttachmentContentMultiple() {
    Column {
        listOf(
            previewAudioAttachment,
            previewAudioAttachment.copy(name = "a-much-longer-audio-file-name-v2.mp3"),
        ).forEach { attachment ->
            AudioAttachmentContentItem(
                attachment = attachment,
                playerState = AudioPlayerState(getRecordingUri = Attachment::assetUrl),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AudioAttachmentContentItemIdlePreview() {
    ChatPreviewTheme {
        AudioAttachmentContentItemIdle()
    }
}

@Preview(showBackground = true)
@Composable
private fun AudioAttachmentContentItemPlaybackPreview() {
    ChatPreviewTheme {
        AudioAttachmentContentItemPlayback()
    }
}

@Preview(showBackground = true)
@Composable
private fun AudioAttachmentContentItemUploadingPreview() {
    ChatPreviewTheme {
        AudioAttachmentContentItemUploading()
    }
}

@Preview(showBackground = true)
@Composable
private fun AudioAttachmentContentMultiplePreview() {
    ChatPreviewTheme {
        AudioAttachmentContentMultiple()
    }
}
