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

@file:Suppress("TooManyFunctions") // Composable UI file: main components + private helpers + previews.

package io.getstream.chat.android.compose.ui.messages.composer.internal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.audio.StaticWaveformSlider
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ComponentPadding
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.padding
import io.getstream.chat.android.models.Attachment
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
    val previewMode = LocalInspectionMode.current
    val enterProgress = remember { Animatable(if (previewMode) 0f else HoldContentEnterOffset) }
    LaunchedEffect(Unit) {
        enterProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = HoldContentEnterDurationMs, easing = FastOutSlowInEasing),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                val t = enterProgress.value
                translationX = t * size.width
                alpha = 1f - t
            },
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.stream_compose_ic_mic),
                    contentDescription = null,
                    tint = ChatTheme.colors.errorAccent,
                )
            }

            Text(
                text = ChatTheme.durationFormatter.format(state.durationInMs),
                style = ChatTheme.typography.bodyEmphasis,
                color = ChatTheme.colors.textPrimary,
            )
        }

        RecordingSlideToCancelIndicator(
            modifier = Modifier.align(Alignment.Center),
            offset = IntOffset(
                x = state.offsetX.toInt().coerceAtMost(0),
                y = state.offsetY.toInt().coerceAtMost(0),
            ),
        )
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
        Row(modifier = RecordingBarModifier, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.stream_compose_ic_mic),
                    contentDescription = null,
                    tint = ChatTheme.colors.errorAccent,
                )
            }

            Text(
                text = ChatTheme.durationFormatter.format(state.durationInMs),
                style = ChatTheme.typography.bodyEmphasis,
                color = ChatTheme.colors.textPrimary,
            )

            StaticWaveformSlider(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ComponentPadding(start = StreamTokens.spacingMd, top = 8.dp, bottom = 8.dp)),
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
        Row(modifier = RecordingBarModifier, verticalAlignment = Alignment.CenterVertically) {
            val playbackIcon = if (state.isPlaying) {
                R.drawable.stream_compose_ic_pause
            } else {
                R.drawable.stream_compose_ic_play
            }
            IconButton(onClick = recordingActions.onToggleRecordingPlayback) {
                Icon(
                    painter = painterResource(id = playbackIcon),
                    contentDescription = null,
                    tint = ChatTheme.colors.textPrimary,
                )
            }

            val playbackInMs = (currentProgress * state.durationInMs).toInt()
            Text(
                text = ChatTheme.durationFormatter.format(playbackInMs),
                style = ChatTheme.typography.bodyEmphasis,
                color = ChatTheme.colors.textPrimary,
            )

            StaticWaveformSlider(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ComponentPadding(start = StreamTokens.spacingMd, top = 8.dp, bottom = 8.dp)),
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
    modifier: Modifier = Modifier,
    offset: IntOffset,
) {
    val cancelThresholdPx = with(LocalDensity.current) { SlideToCancelThreshold.toPx() }
    val dragX = abs(offset.x.coerceAtMost(0)).toFloat()
    val progress = (dragX / cancelThresholdPx).coerceIn(0f, 1f)

    val gradientBrush = Brush.linearGradient(
        colors = listOf(ChatTheme.colors.textPrimary, ChatTheme.colors.textTertiary),
    )

    Row(
        modifier = modifier
            .alpha(1f - progress)
            .offset { IntOffset(offset.x.coerceAtMost(0), 0) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
    ) {
        Text(
            text = stringResource(id = R.string.stream_compose_message_composer_slide_to_cancel),
            style = ChatTheme.typography.bodyDefault.copy(brush = gradientBrush),
        )
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_chevron_left),
            contentDescription = null,
            tint = ChatTheme.colors.textTertiary,
        )
    }
}

/** Drag distance at which the recording is cancelled. */
internal val SlideToCancelThreshold = 96.dp

private val RecordingBarModifier = Modifier
    .fillMaxWidth()
    .height(48.dp)
    .padding(ComponentPadding(end = StreamTokens.spacingMd))

private const val HoldContentEnterOffset = 0.3f
private const val HoldContentEnterDurationMs = 200

private const val PreviewDurationInMs = 120_000

@Suppress("MagicNumber")
private val PreviewWaveformData = (0..10).map {
    listOf(0.5f, 0.8f, 0.3f, 0.6f, 0.4f, 0.7f, 0.2f, 0.9f, 0.1f)
}.flatten()

@Preview(showBackground = true)
@Composable
private fun AudioRecordingHoldContentPreview() {
    ChatPreviewTheme {
        AudioRecordingHoldContent()
    }
}

@Composable
internal fun AudioRecordingHoldContent() {
    MessageComposerAudioRecordingHoldContent(
        state = RecordingState.Hold(
            durationInMs = PreviewDurationInMs,
            waveform = PreviewWaveformData,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun AudioRecordingLockedContentPreview() {
    ChatPreviewTheme {
        AudioRecordingLockedContent()
    }
}

@Composable
internal fun AudioRecordingLockedContent() {
    MessageComposerAudioRecordingLockedContent(
        state = RecordingState.Locked(
            durationInMs = PreviewDurationInMs,
            waveform = PreviewWaveformData,
        ),
        recordingActions = AudioRecordingActions.None,
    )
}

@Preview(showBackground = true)
@Composable
private fun AudioRecordingOverviewContentPreview() {
    ChatPreviewTheme {
        AudioRecordingOverviewContent()
    }
}

@Composable
internal fun AudioRecordingOverviewContent() {
    MessageComposerAudioRecordingOverviewContent(
        state = RecordingState.Overview(
            durationInMs = PreviewDurationInMs,
            waveform = PreviewWaveformData,
            attachment = Attachment(),
        ),
        recordingActions = AudioRecordingActions.None,
    )
}
