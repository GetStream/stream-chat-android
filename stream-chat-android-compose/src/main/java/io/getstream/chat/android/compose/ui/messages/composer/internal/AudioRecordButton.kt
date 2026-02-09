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

import android.os.SystemClock
import androidx.compose.foundation.gestures.awaitDragOrCancellation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.compose.ui.util.padding
import io.getstream.chat.android.compose.ui.util.size
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import kotlinx.coroutines.awaitCancellation
import kotlin.math.abs

private const val HOLD_TO_RECORD_THRESHOLD_MS = 1_000L

/**
 * Unified voice recording component that handles both the mic button gesture lifecycle
 * and the recording content (waveform, controls, floating icons).
 *
 * **Layout behavior:**
 * - **Idle**: Renders just the mic button (wrap content).
 * - **Hold**: Mic button stays visible, recording content (timer, slide-to-cancel) fills the
 *   available width alongside it, floating mic/lock icons appear as Popups.
 * - **Locked / Overview**: Mic button hides, recording content fills the width,
 *   control buttons (delete, stop, complete) appear below.
 *
 * The mic button is **always in the composition tree** to preserve gesture continuity.
 * Its size toggles between `style.size` (visible) and `0.dp` (hidden) — it never leaves
 * the tree during a recording session.
 *
 * @param recordingState The current recording state from the ViewModel.
 * @param recordingActions The actions to control the audio recording.
 * @param modifier Modifier applied to the outer container.
 */
@Composable
internal fun AudioRecordButton(
    recordingState: RecordingState,
    recordingActions: AudioRecordingActions,
    modifier: Modifier = Modifier,
) {
    val isRecording = recordingState !is RecordingState.Idle
    val isMicVisible = recordingState is RecordingState.Idle || recordingState is RecordingState.Hold

    // --- Extract recording state for child composables ---

    val durationInMs = when (recordingState) {
        is RecordingState.Recording -> recordingState.durationInMs
        is RecordingState.Overview -> recordingState.durationInMs
        else -> 0
    }

    val waveformVisible = recordingState is RecordingState.Locked || recordingState is RecordingState.Overview
    val waveformData = when (recordingState) {
        is RecordingState.Recording -> recordingState.waveform
        is RecordingState.Overview -> recordingState.waveform
        else -> emptyList()
    }
    val waveformThumbVisible = recordingState is RecordingState.Overview
    val waveformPlaying = recordingState is RecordingState.Overview && recordingState.isPlaying
    val waveformProgress = when (recordingState) {
        is RecordingState.Overview -> recordingState.playingProgress
        is RecordingState.Locked -> 1f
        else -> 0f
    }

    val slideToCancelVisible = recordingState is RecordingState.Hold

    val holdControlsVisible = recordingState is RecordingState.Hold || recordingState is RecordingState.Locked
    val holdControlsLocked = recordingState is RecordingState.Locked
    val holdControlsOffset = when (recordingState) {
        is RecordingState.Hold -> IntOffset(
            x = recordingState.offsetX.toInt().coerceAtMost(maximumValue = 0),
            y = recordingState.offsetY.toInt().coerceAtMost(maximumValue = 0),
        )
        else -> IntOffset.Zero
    }

    val recordingControlsVisible = recordingState is RecordingState.Locked || recordingState is RecordingState.Overview
    val recordingStopControlVisible = recordingState is RecordingState.Locked

    // --- Slide-to-cancel progress ---

    val density = LocalDensity.current
    val cancelThresholdX = with(density) {
        ChatTheme.messageComposerTheme.audioRecording.slideToCancel.threshold.toPx().toInt()
    }
    val cancelOffsetX = abs(holdControlsOffset.x.takeIf { it <= 0 } ?: 0).toFloat()
    val slideToCancelProgress = (cancelOffsetX / cancelThresholdX).coerceIn(0f, 1f)

    // --- Layout ---
    // When recording, fill the available width so the recording content (timer, waveform,
    // slide-to-cancel) takes the space freed by the hidden center content.
    // The Spacer(weight=1f) in MessageInput absorbs whatever is left (zero when we fillMaxWidth).
    // When idle, wrap to the mic button size.

    Column(modifier = modifier.then(if (isRecording) Modifier.fillMaxWidth() else Modifier)) {
        Row(verticalAlignment = Alignment.Bottom) {
            if (isRecording) {
                RecordingContent(
                    modifier = Modifier.weight(1f),
                    durationInMs = durationInMs,
                    waveformVisible = waveformVisible,
                    waveformThumbVisible = waveformThumbVisible,
                    waveformData = waveformData,
                    waveformPlaying = waveformPlaying,
                    waveformProgress = waveformProgress,
                    slideToCancelVisible = slideToCancelVisible,
                    slideToCancelProgress = slideToCancelProgress,
                    holdControlsOffset = holdControlsOffset,
                    onToggleRecordingPlayback = recordingActions.onToggleRecordingPlayback,
                    onSliderDragStart = recordingActions.onRecordingSliderDragStart,
                    onSliderDragStop = recordingActions.onRecordingSliderDragStop,
                )
            }

            MicButton(
                isVisible = isMicVisible,
                recordingState = recordingState,
                recordingActions = recordingActions,
            )
        }

        if (recordingControlsVisible) {
            RecordingControlButtons(
                isStopControlVisible = recordingStopControlVisible,
                onDeleteRecording = recordingActions.onDeleteRecording,
                onStopRecording = recordingActions.onStopRecording,
                onCompleteRecording = recordingActions.onCompleteRecording,
            )
        }

        // Floating icons (rendered as Popups, positioned relative to this Column)
        if (holdControlsVisible) {
            if (!holdControlsLocked) {
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
                val micOffset = micBaseOffset + holdControlsOffset

                Popup(
                    offset = micOffset,
                    properties = PopupProperties(clippingEnabled = false),
                    alignment = Alignment.CenterEnd,
                ) {
                    RecordingMicIcon()
                }
            }

            val playbackHeight = ChatTheme.messageComposerTheme.audioRecording.playback.height
            val controlsHeight = ChatTheme.messageComposerTheme.audioRecording.controls.height
            val totalContentHeight = playbackHeight + controlsHeight
            val edgeOffset = ChatTheme.messageComposerTheme.audioRecording.floatingIcons.lockEdgeOffset
            val lockOffset = with(density) {
                IntOffset(
                    x = -edgeOffset.x.toPx().toInt(),
                    y = when (holdControlsLocked) {
                        true -> -totalContentHeight.toPx().toInt() - edgeOffset.y.toPx().toInt()
                        else -> -playbackHeight.toPx().toInt() - edgeOffset.y.toPx().toInt() + holdControlsOffset.y
                    },
                )
            }

            Popup(
                offset = lockOffset,
                alignment = Alignment.BottomEnd,
            ) {
                RecordingLockableIcon(locked = holdControlsLocked)
            }
        }
    }
}

/**
 * The mic button handles the full gesture lifecycle:
 * - Press → start recording.
 * - Hold + drag left → slide-to-cancel.
 * - Hold + drag up → lock recording.
 * - Short tap (< threshold) → cancel + show "hold to record" hint.
 * - Long hold + release → send recording.
 *
 * Stays full-size during the hold gesture so the layout doesn't shift and pointer
 * coordinates remain stable. Collapses only after the gesture ends (lock/cancel/send).
 */
@Composable
private fun MicButton(
    isVisible: Boolean,
    recordingState: RecordingState,
    recordingActions: AudioRecordingActions,
    modifier: Modifier = Modifier,
) {
    var showHint by remember { mutableStateOf(false) }
    var isFingerDown by remember { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(Offset.Zero) }
    var micSize by remember { mutableStateOf(IntSize.Zero) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = isFingerDown || showHint
    val currentState by rememberUpdatedState(recordingState)

    val density = LocalDensity.current
    val cancelThresholdPx = with(density) {
        ChatTheme.messageComposerTheme.audioRecording.slideToCancel.threshold.toPx()
    }
    val lockThresholdPx = with(density) {
        ChatTheme.messageComposerTheme.audioRecording.floatingIcons.lockThreshold.toPx()
    }

    // Show ripple while finger is down or hint is visible
    LaunchedEffect(isPressed) {
        if (isPressed) {
            val press = PressInteraction.Press(pressOffset)
            interactionSource.emit(press)
            try {
                awaitCancellation()
            } finally {
                interactionSource.tryEmit(PressInteraction.Release(press))
            }
        }
    }

    val style = ChatTheme.messageComposerTheme.audioRecording.recordButton
    val layoutDirection = LocalLayoutDirection.current
    val buttonDescription = stringResource(R.string.stream_compose_cd_record_audio_message)

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .run { if (isVisible) size(style.size) else size(0.dp) }
                .padding(style.padding)
                .onSizeChanged { micSize = it }
                .clip(CircleShape)
                .indication(
                    interactionSource,
                    ripple(
                        bounded = true,
                        radius = with(density) { micSize.height.toDp() / 2 },
                    ),
                )
                .semantics { contentDescription = buttonDescription }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        pressOffset = down.position
                        isFingerDown = true

                        // TODO: Add RECORD_AUDIO permission check before starting recording
                        val holdStartTime = SystemClock.elapsedRealtime()
                        val startOffset = down.position.minus(
                            Offset(micSize.width.toFloat(), micSize.height.toFloat()),
                        )
                        recordingActions.onStartRecording(Offset.Zero)

                        while (true) {
                            // Break if recording was locked by the ViewModel
                            if (currentState is RecordingState.Locked) break

                            val dragEvent = awaitDragOrCancellation(down.id)
                            if (dragEvent == null || !dragEvent.pressed) {
                                // Finger lifted or cancelled — check hold duration.
                                // The state can change during awaitDragOrCancellation, so re-check.
                                @Suppress("KotlinConstantConditions")
                                if (currentState !is RecordingState.Locked) {
                                    val holdDuration = SystemClock.elapsedRealtime() - holdStartTime
                                    if (holdDuration < HOLD_TO_RECORD_THRESHOLD_MS) {
                                        recordingActions.onCancelRecording()
                                        showHint = true
                                    } else {
                                        recordingActions.onSendRecording()
                                    }
                                }
                                break
                            }

                            dragEvent.consume()
                            val diffOffset = dragEvent.position.minus(startOffset)
                            recordingActions.onHoldRecording(diffOffset)

                            if (diffOffset.x <= -cancelThresholdPx) {
                                recordingActions.onCancelRecording()
                                break
                            }
                            if (diffOffset.y <= -lockThresholdPx) {
                                recordingActions.onLockRecording()
                                break
                            }
                        }

                        isFingerDown = false
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier
                    .mirrorRtl(layoutDirection = layoutDirection)
                    .size(style.icon.size)
                    .testTag("Stream_ComposerRecordAudioButton"),
                painter = style.icon.painter,
                contentDescription = stringResource(R.string.stream_compose_record_audio_message),
            )
        }

        if (showHint) {
            DefaultHoldToRecordPopup(
                offset = micSize.height,
                onDismissRequest = { showHint = false },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AudioRecordButtonIdlePreview() {
    ChatPreviewTheme {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center,
        ) {
            AudioRecordButton(
                recordingState = RecordingState.Idle,
                recordingActions = AudioRecordingActions.None,
            )
        }
    }
}
