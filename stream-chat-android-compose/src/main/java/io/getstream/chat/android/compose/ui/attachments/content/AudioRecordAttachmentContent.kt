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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.components.audio.PlaybackTimerText
import io.getstream.chat.android.compose.ui.components.audio.StaticWaveformSlider
import io.getstream.chat.android.compose.ui.components.button.StreamButton
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.components.common.PlaybackSpeedToggle
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageStyling
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.applyIf
import io.getstream.chat.android.compose.ui.util.shouldBeDisplayedAsFullSizeAttachment
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.AudioPlayerViewModelFactory
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Attachment.UploadState
import io.getstream.chat.android.previewdata.PreviewAttachmentData
import io.getstream.chat.android.ui.common.state.messages.list.AudioPlayerState
import io.getstream.chat.android.ui.common.utils.MediaStringUtil

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
    val isUploading = attachment.uploadState is UploadState.InProgress
    val outlineColor = if (isMine) colors.chatBorderOnChatOutgoing else colors.chatBorderOnChatIncoming
    val textColor = MessageStyling.textColor(isMine, colors)

    AudioRecordAttachmentContentItemBase(
        modifier = modifier,
        attachment = attachment,
        playerState = playerState,
        waveformHeight = 36.dp,
        outlineColor = outlineColor,
        textColor = textColor,
        onPlayToggleClick = onPlayToggleClick,
        onThumbDragStart = onThumbDragStart,
        onThumbDragStop = onThumbDragStop,
        tailContent = {
            val speed = playerState.speeds.getOrDefault(attachment.audioHash, 1f)
            PlaybackSpeedToggle(speed = speed, outlineColor = outlineColor, enabled = !isUploading) {
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
    waveformHeight: Dp,
    outlineColor: Color,
    textColor: Color,
    contentPadding: PaddingValues = PaddingValues(
        start = StreamTokens.spacingXs,
        top = StreamTokens.spacingXs,
        end = StreamTokens.spacingSm,
        bottom = StreamTokens.spacingXs,
    ),
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
    val uploadProgress = attachment.uploadState as? UploadState.InProgress

    val currentAttachment by rememberUpdatedState(attachment)
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 64.dp)
            .fillMaxWidth()
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        PlaybackToggleButton(playing = playing, outlineColor = outlineColor, enabled = uploadProgress == null) {
            onPlayToggleClick(currentAttachment)
        }

        if (uploadProgress != null) {
            UploadProgressIndicator(
                uploadState = uploadProgress,
                modifier = Modifier.weight(1f),
            )
        } else {
            var currentProgress by remember { mutableFloatStateOf(trackProgress) }
            LaunchedEffect(attachmentUrl, playing, trackProgress) { currentProgress = trackProgress }

            val timerTextColor = if (playing) ChatTheme.colors.accentPrimary else textColor
            PlaybackTimerText(
                progress = currentProgress,
                durationInMs = currentAttachment.durationInMs,
                color = timerTextColor,
                countdown = true,
            )

            StaticWaveformSlider(
                modifier = Modifier
                    .height(waveformHeight)
                    .weight(1f),
                waveformData = waveform,
                progress = currentProgress,
                isPlaying = playing,
                visibleBarLimit = 20,
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
        }

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
    enabled: Boolean = true,
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
        enabled = enabled,
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

@Composable
private fun UploadProgressIndicator(
    uploadState: UploadState.InProgress,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
    ) {
        LoadingIndicator(
            progress = progressFraction(uploadState),
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "${MediaStringUtil.convertFileSizeByteCount(uploadState.bytesUploaded)} / " +
                MediaStringUtil.convertFileSizeByteCount(uploadState.totalBytes),
            style = ChatTheme.typography.metadataDefault,
            color = ChatTheme.colors.textSecondary,
            maxLines = 1,
        )
    }
}

private fun progressFraction(state: UploadState.InProgress): Float =
    if (state.totalBytes > 0) (state.bytesUploaded / state.totalBytes.toFloat()) else 0f

@Composable
internal fun AudioRecordAttachmentContentItemPlayback() {
    val previewUri = "preview://audio"
    AudioRecordAttachmentContentItem(
        attachment = PreviewAttachmentData.attachmentAudioRecording1.copy(assetUrl = previewUri),
        playerState = AudioPlayerState(
            current = AudioPlayerState.CurrentAudioState(
                isPlaying = true,
                audioUri = previewUri,
                waveform = PreviewAttachmentData.attachmentAudioRecording1.waveformData!!,
            ),
            getRecordingUri = Attachment::assetUrl,
        ),
    )
}

@Composable
internal fun AudioRecordAttachmentContentItemUploading() {
    AudioRecordAttachmentContentItem(
        attachment = PreviewAttachmentData.attachmentAudioRecording1.copy(
            assetUrl = "preview://audio",
            uploadState = UploadState.InProgress(
                bytesUploaded = 2_400_000,
                totalBytes = 4_000_000,
            ),
        ),
        playerState = AudioPlayerState(getRecordingUri = Attachment::assetUrl),
    )
}

@Preview(showBackground = true)
@Composable
internal fun AudioRecordAttachmentContentItemPreview() {
    ChatPreviewTheme {
        AudioRecordAttachmentContentItemPlayback()
    }
}

@Preview(showBackground = true)
@Composable
internal fun AudioRecordAttachmentContentItemUploadingPreview() {
    ChatPreviewTheme {
        AudioRecordAttachmentContentItemUploading()
    }
}
