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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.audio.PlaybackTimerText
import io.getstream.chat.android.compose.ui.components.audio.StaticWaveformSlider
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ComponentPadding
import io.getstream.chat.android.compose.ui.util.padding
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
        is RecordingState.Locked -> ChatTheme.componentFactory.MessageComposerAudioRecordingLockedContent(
            state = recordingState,
            recordingActions = recordingActions,
            modifier = modifier,
        )
        is RecordingState.Overview -> ChatTheme.componentFactory.MessageComposerAudioRecordingOverviewContent(
            state = recordingState,
            recordingActions = recordingActions,
            modifier = modifier,
        )
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_mic),
            contentDescription = null,
            tint = ChatTheme.colors.errorAccent,
        )

        PlaybackTimerText(
            progress = 1f,
            durationInMs = state.durationInMs,
            color = ChatTheme.colors.textPrimary,
            countdown = false,
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
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

/** Recording locked (finger released): waveform grows as recording continues, controls below. */
@Composable
internal fun MessageComposerAudioRecordingLockedContent(
    state: RecordingState.Locked,
    recordingActions: AudioRecordingActions,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.stream_compose_ic_mic),
                contentDescription = null,
                tint = ChatTheme.colors.errorAccent,
            )

            PlaybackTimerText(
                progress = 1f,
                durationInMs = state.durationInMs,
                color = ChatTheme.colors.textPrimary,
                countdown = false,
            )

            StaticWaveformSlider(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ComponentPadding(start = 16.dp, top = 8.dp, bottom = 8.dp)),
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

        ChatTheme.componentFactory.MessageComposerAudioRecordingControlsContent(
            isStopVisible = true,
            recordingActions = recordingActions,
        )
    }
}

/** Recording stopped: user can scrub the waveform and play back before sending, controls below. */
@Composable
internal fun MessageComposerAudioRecordingOverviewContent(
    state: RecordingState.Overview,
    recordingActions: AudioRecordingActions,
    modifier: Modifier = Modifier,
) {
    var currentProgress by remember { mutableFloatStateOf(state.playingProgress) }
    LaunchedEffect(state.playingProgress, state.durationInMs) {
        currentProgress = state.playingProgress
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val playbackIcon = if (state.isPlaying) {
                R.drawable.stream_compose_ic_pause
            } else {
                R.drawable.stream_compose_ic_play
            }
            IconButton(
                onClick = recordingActions.onToggleRecordingPlayback,
            ) {
                Icon(
                    painter = painterResource(id = playbackIcon),
                    contentDescription = null,
                    tint = ChatTheme.colors.primaryAccent,
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
                    .padding(ComponentPadding(start = 16.dp, top = 8.dp, bottom = 8.dp)),
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

        ChatTheme.componentFactory.MessageComposerAudioRecordingControlsContent(
            isStopVisible = false,
            recordingActions = recordingActions,
        )
    }
}

@Composable
private fun RecordingSlideToCancelIndicator(
    holdControlsOffset: IntOffset,
) {
    val cancelThresholdPx = with(LocalDensity.current) { SlideToCancelThreshold.toPx() }
    val dragX = abs(holdControlsOffset.x.coerceAtMost(0)).toFloat()
    val progress = (dragX / cancelThresholdPx).coerceIn(0f, 1f)

    Row(
        modifier = Modifier
            .alpha(1f - progress)
            .offset { IntOffset(holdControlsOffset.x.coerceAtMost(0), 0) },
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_arrow_left_black),
            tint = ChatTheme.colors.textLowEmphasis,
            contentDescription = null,
        )

        Text(
            text = stringResource(id = R.string.stream_compose_message_composer_slide_to_cancel),
            modifier = Modifier.align(Alignment.CenterVertically),
            style = ChatTheme.typography.body.copy(color = ChatTheme.colors.textLowEmphasis),
        )
        Spacer(modifier = Modifier.width(96.dp))
    }
}

/** Drag distance at which the recording is cancelled. */
internal val SlideToCancelThreshold = 96.dp
