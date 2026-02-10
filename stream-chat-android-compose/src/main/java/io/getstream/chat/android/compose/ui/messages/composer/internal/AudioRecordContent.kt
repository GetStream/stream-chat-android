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

package io.getstream.chat.android.compose.ui.messages.composer.internal

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.audio.PlaybackTimerText
import io.getstream.chat.android.compose.ui.components.audio.StaticWaveformSlider
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.IconContainerStyle
import io.getstream.chat.android.compose.ui.theme.messages.composer.AudioRecordingFloatingIconStyle
import io.getstream.chat.android.compose.ui.util.padding
import io.getstream.chat.android.compose.ui.util.size
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import kotlin.math.abs

@Composable
internal fun AudioRecordContent(
    recordingState: RecordingState,
    recordingActions: AudioRecordingActions,
    modifier: Modifier = Modifier,
) {
    when (recordingState) {
        is RecordingState.Hold -> HoldRecordContent(recordingState, modifier)
        is RecordingState.Locked -> LockedRecordContent(recordingState, modifier)
        is RecordingState.Overview -> OverviewRecordContent(recordingState, recordingActions, modifier)
        is RecordingState.Complete,
        is RecordingState.Idle,
        -> Unit
    }
}

/** Finger-down state: timer counts up while the slide-to-cancel hint follows the drag. */
@Composable
private fun HoldRecordContent(
    state: RecordingState.Hold,
    modifier: Modifier = Modifier,
) {
    val playbackTheme = ChatTheme.messageComposerTheme.audioRecording.playback
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(playbackTheme.height),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MicIndicatorIcon()

        PlaybackTimerText(
            progress = 1f,
            durationInMs = state.durationInMs,
            color = ChatTheme.colors.textPrimary,
            countdown = false,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(playbackTheme.height),
            contentAlignment = Alignment.CenterEnd,
        ) {
            RecordingSlideToCancelIndicator(state.holdOffset())
        }
    }
}

/** Recording locked (finger released): waveform grows as recording continues. */
@Composable
private fun LockedRecordContent(
    state: RecordingState.Locked,
    modifier: Modifier = Modifier,
) {
    val playbackTheme = ChatTheme.messageComposerTheme.audioRecording.playback
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(playbackTheme.height),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MicIndicatorIcon()

        PlaybackTimerText(
            progress = 1f,
            durationInMs = state.durationInMs,
            color = ChatTheme.colors.textPrimary,
            countdown = false,
        )

        StaticWaveformSlider(
            modifier = Modifier
                .fillMaxSize()
                .padding(playbackTheme.waveformSliderPadding),
            waveformData = state.waveform,
            progress = 1f,
            isPlaying = false,
            visibleBarLimit = 100,
            adjustBarWidthToLimit = true,
            isThumbVisible = false,
            onDragStart = {},
            onDrag = {},
            onDragStop = {},
        )
    }
}

/** Recording stopped: user can scrub the waveform and play back before sending. */
@Composable
private fun OverviewRecordContent(
    state: RecordingState.Overview,
    recordingActions: AudioRecordingActions,
    modifier: Modifier = Modifier,
) {
    var currentProgress by remember { mutableFloatStateOf(state.playingProgress) }
    LaunchedEffect(state.playingProgress, state.durationInMs) {
        currentProgress = state.playingProgress
    }

    val playbackTheme = ChatTheme.messageComposerTheme.audioRecording.playback
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(playbackTheme.height),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val btnStyle = if (state.isPlaying) playbackTheme.pauseButton else playbackTheme.playButton
        IconButton(
            onClick = recordingActions.onToggleRecordingPlayback,
            modifier = Modifier
                .size(btnStyle.size)
                .padding(btnStyle.padding)
                .focusable(true),
        ) {
            Icon(
                painter = btnStyle.icon.painter,
                contentDescription = null,
                modifier = Modifier.size(btnStyle.size),
                tint = btnStyle.icon.tint,
            )
        }

        PlaybackTimerText(
            progress = currentProgress,
            durationInMs = state.durationInMs,
            color = ChatTheme.colors.textPrimary,
            countdown = false,
        )

        StaticWaveformSlider(
            modifier = Modifier
                .fillMaxSize()
                .padding(playbackTheme.waveformSliderPadding),
            waveformData = state.waveform,
            progress = currentProgress,
            isPlaying = state.isPlaying,
            visibleBarLimit = 100,
            adjustBarWidthToLimit = true,
            isThumbVisible = true,
            onDragStart = { currentProgress = it.also(recordingActions.onRecordingSliderDragStart) },
            onDrag = { currentProgress = it },
            onDragStop = { currentProgress = it.also(recordingActions.onRecordingSliderDragStop) },
        )
    }
}

@Composable
private fun MicIndicatorIcon() {
    val micStyle = ChatTheme.messageComposerTheme.audioRecording.playback.micIndicator
    Box(
        modifier = Modifier
            .size(micStyle.size)
            .padding(micStyle.padding),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = micStyle.icon.painter,
            contentDescription = null,
            modifier = Modifier.size(micStyle.size),
            tint = micStyle.icon.tint,
        )
    }
}

/** Extracts the hold offset from a [RecordingState], clamped to non-positive values. */
private fun RecordingState.holdOffset(): IntOffset = when (this) {
    is RecordingState.Hold -> IntOffset(
        x = offsetX.toInt().coerceAtMost(maximumValue = 0),
        y = offsetY.toInt().coerceAtMost(maximumValue = 0),
    )
    else -> IntOffset.Zero
}

/**
 * Renders the floating mic and lock icons as Popups during Hold/Locked states.
 * Positioned relative to the parent layout's trailing edge.
 */
@Composable
internal fun RecordingFloatingIcons(recordingState: RecordingState) {
    val isLocked = recordingState is RecordingState.Locked
    val holdControlsOffset = recordingState.holdOffset()

    val density = LocalDensity.current

    // Floating mic icon (only while holding, not locked)
    if (!isLocked) {
        val micBaseWidth = ChatTheme.messageComposerTheme.audioRecording.recordButton.size.width
        val micFloatingWidth = ChatTheme.messageComposerTheme.audioRecording.floatingIcons.mic.size.width
        val micBaseOffset = remember {
            with(density) {
                IntOffset(
                    x = ((micFloatingWidth - micBaseWidth) / 2).toPx().toInt(),
                    y = 0,
                )
            }
        }
        Popup(
            offset = micBaseOffset + holdControlsOffset,
            properties = PopupProperties(clippingEnabled = false),
            alignment = Alignment.CenterEnd,
        ) {
            RecordingMicIcon()
        }
    }

    // Lock/locked icon
    val playbackHeight = ChatTheme.messageComposerTheme.audioRecording.playback.height
    val controlsHeight = ChatTheme.messageComposerTheme.audioRecording.controls.height
    val totalContentHeight = playbackHeight + controlsHeight
    val edgeOffset = ChatTheme.messageComposerTheme.audioRecording.floatingIcons.lockEdgeOffset
    val lockOffset = with(density) {
        IntOffset(
            x = -edgeOffset.x.toPx().toInt(),
            y = when (isLocked) {
                true -> -totalContentHeight.toPx().toInt() - edgeOffset.y.toPx().toInt()
                else -> -playbackHeight.toPx().toInt() - edgeOffset.y.toPx().toInt() + holdControlsOffset.y
            },
        )
    }
    Popup(
        offset = lockOffset,
        alignment = Alignment.BottomEnd,
    ) {
        RecordingLockableIcon(locked = isLocked)
    }
}

@Composable
private fun RecordingMicIcon() {
    RecordingFloatingIcon(ChatTheme.messageComposerTheme.audioRecording.floatingIcons.mic)
}

@Composable
private fun RecordingLockableIcon(
    locked: Boolean,
) {
    if (locked) {
        RecordingFloatingIcon(ChatTheme.messageComposerTheme.audioRecording.floatingIcons.locked)
    } else {
        RecordingFloatingIcon(ChatTheme.messageComposerTheme.audioRecording.floatingIcons.lock)
    }
}

@Composable
private fun RecordingFloatingIcon(
    style: AudioRecordingFloatingIconStyle,
) {
    Card(
        modifier = Modifier
            .size(style.size)
            .padding(style.padding),
        shape = style.backgroundShape,
        colors = CardDefaults.cardColors(containerColor = style.backgroundColor),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Icon(
                painter = style.icon.painter,
                contentDescription = null,
                modifier = Modifier.size(style.icon.size),
                tint = style.icon.tint,
            )
        }
    }
}

@Composable
private fun RecordingSlideToCancelIndicator(
    holdControlsOffset: IntOffset,
) {
    val theme = ChatTheme.messageComposerTheme.audioRecording.slideToCancel
    val cancelThresholdPx = with(LocalDensity.current) { theme.threshold.toPx() }
    val dragX = abs(holdControlsOffset.x.coerceAtMost(0)).toFloat()
    val progress = (dragX / cancelThresholdPx).coerceIn(0f, 1f)

    Row(
        modifier = Modifier
            .alpha(1f - progress)
            .offset { IntOffset(holdControlsOffset.x.coerceAtMost(0), 0) },
    ) {
        val iconStyle = theme.iconStyle
        Icon(
            modifier = Modifier.size(iconStyle.size),
            painter = iconStyle.painter,
            tint = iconStyle.tint,
            contentDescription = null,
        )

        Text(
            text = stringResource(id = R.string.stream_compose_message_composer_slide_to_cancel),
            modifier = Modifier.align(Alignment.CenterVertically),
            style = theme.textStyle,
        )
        Spacer(modifier = Modifier.width(theme.marginEnd))
    }
}

@Composable
internal fun RecordingControlButtons(
    recordingState: RecordingState,
    recordingActions: AudioRecordingActions,
) {
    val isStopVisible = recordingState is RecordingState.Locked
    val sendOnComplete = ChatTheme.messageComposerTheme.audioRecording.sendOnComplete
    val theme = ChatTheme.messageComposerTheme.audioRecording.controls
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(theme.height),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ControlIconButton(
            style = theme.deleteButton,
            tag = "Stream_ComposerDeleteAudioRecordingButton",
            onClick = recordingActions.onDeleteRecording,
        )
        if (isStopVisible) {
            Spacer(modifier = Modifier.weight(1f))
            ControlIconButton(
                style = theme.stopButton,
                tag = "Stream_ComposerStopAudioRecordingButton",
                onClick = recordingActions.onStopRecording,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        ControlIconButton(
            style = theme.completeButton,
            tag = "Stream_ComposerCompleteAudioRecordingButton",
            onClick = { recordingActions.onCompleteRecording(sendOnComplete) },
        )
    }
}

@Composable
private fun ControlIconButton(
    style: IconContainerStyle,
    tag: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .semantics { testTag = tag }
            .size(style.size)
            .padding(style.padding)
            .focusable(true),
    ) {
        Icon(
            painter = style.icon.painter,
            contentDescription = null,
            modifier = Modifier.size(style.icon.size),
            tint = style.icon.tint,
        )
    }
}

@Preview(showBackground = true, heightDp = 200)
@Composable
private fun AudioRecordContentHoldPreview() {
    ChatPreviewTheme {
        AudioRecordContentHold()
    }
}

@Composable
internal fun AudioRecordContentHold() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = BottomCenter,
    ) {
        AudioRecordButton(
            recordingState = RecordingState.Hold(
                durationInMs = PreviewDurationInMs,
                waveform = PreviewWaveformData,
            ),
            recordingActions = AudioRecordingActions.None,
        )
    }
}

@Preview(showBackground = true, heightDp = 200)
@Composable
private fun AudioRecordContentLockedPreview() {
    ChatPreviewTheme {
        AudioRecordContentLocked()
    }
}

@Composable
internal fun AudioRecordContentLocked() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = BottomCenter,
    ) {
        AudioRecordButton(
            recordingState = RecordingState.Locked(
                durationInMs = PreviewDurationInMs,
                waveform = PreviewWaveformData,
            ),
            recordingActions = AudioRecordingActions.None,
        )
    }
}

@Preview(showBackground = true, heightDp = 200)
@Composable
private fun AudioRecordContentOverviewPreview() {
    ChatPreviewTheme {
        AudioRecordContentOverview()
    }
}

@Composable
internal fun AudioRecordContentOverview() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = BottomCenter,
    ) {
        AudioRecordButton(
            recordingState = RecordingState.Overview(
                durationInMs = PreviewDurationInMs,
                waveform = PreviewWaveformData,
                attachment = Attachment(),
            ),
            recordingActions = AudioRecordingActions.None,
        )
    }
}

private const val PreviewDurationInMs = 120_000

@Suppress("MagicNumber")
private val PreviewWaveformData = (0..10).map {
    listOf(0.5f, 0.8f, 0.3f, 0.6f, 0.4f, 0.7f, 0.2f, 0.9f, 0.1f)
}.flatten()
