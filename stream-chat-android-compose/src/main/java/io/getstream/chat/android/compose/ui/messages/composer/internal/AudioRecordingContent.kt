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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.audio.PlaybackTimerText
import io.getstream.chat.android.compose.ui.components.audio.StaticWaveformSlider
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.padding
import io.getstream.chat.android.compose.ui.util.size
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import kotlin.math.abs

@Composable
internal fun AudioRecordingContent(
    recordingState: RecordingState,
    recordingActions: AudioRecordingActions,
    modifier: Modifier = Modifier,
) {
    when (recordingState) {
        is RecordingState.Hold -> ChatTheme.componentFactory.MessageComposerAudioRecordingHoldContent(
            state = recordingState,
            modifier = modifier,
        )
        is RecordingState.Locked -> LockedRecordContent(recordingState, modifier)
        is RecordingState.Overview -> OverviewRecordContent(recordingState, recordingActions, modifier)
        is RecordingState.Complete,
        is RecordingState.Idle,
        -> Unit
    }
}

/** Finger-down state: timer counts up while the slide-to-cancel hint follows the drag. */
@Composable
internal fun MessageComposerAudioRecordingHoldContent(
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
            RecordingSlideToCancelIndicator(
                holdControlsOffset = IntOffset(
                    x = state.offsetX.toInt().coerceAtMost(0),
                    y = state.offsetY.toInt().coerceAtMost(0),
                ),
            )
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
