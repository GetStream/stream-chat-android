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

import android.Manifest
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.compose.ui.util.padding
import io.getstream.chat.android.compose.ui.util.size
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.common.utils.openSystemSettings
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay

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
    val showControls = recordingState is RecordingState.Locked || recordingState is RecordingState.Overview
    val showFloatingIcons = recordingState is RecordingState.Hold || recordingState is RecordingState.Locked

    // When recording, fill the available width so the recording content (timer, waveform,
    // slide-to-cancel) takes the space freed by the hidden center content in MessageInput.
    // When idle, wrap to the mic button size.
    Column(modifier = modifier.then(if (isRecording) Modifier.fillMaxWidth() else Modifier)) {
        Row(verticalAlignment = Alignment.Bottom) {
            if (isRecording) {
                AudioRecordContent(
                    recordingState = recordingState,
                    recordingActions = recordingActions,
                    modifier = Modifier.weight(1f),
                )
            }

            MicButton(
                isVisible = isMicVisible,
                recordingState = recordingState,
                recordingActions = recordingActions,
            )
        }

        if (showControls) {
            RecordingControlButtons(
                recordingState = recordingState,
                recordingActions = recordingActions,
            )
        }

        if (showFloatingIcons) {
            RecordingFloatingIcons(recordingState)
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
@OptIn(ExperimentalPermissionsApi::class)
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

    val permissionState = rememberAudioRecordPermission()

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

    MicButtonLayout(
        isVisible = isVisible,
        micSize = micSize,
        interactionSource = interactionSource,
        showHint = showHint,
        modifier = modifier,
        onSizeChanged = { micSize = it },
        onDismissHint = { showHint = false },
        onGesture = {
            awaitEachGesture {
                val down = awaitFirstDown()
                down.consume()
                pressOffset = down.position
                isFingerDown = true

                // Gate recording behind RECORD_AUDIO permission
                if (permissionState?.status?.shouldShowRationale == true) {
                    permissionState.showRationale()
                } else if (permissionState?.status?.isGranted == false) {
                    permissionState.launchPermissionRequest()
                } else {
                    handleRecordingGesture(
                        down = down,
                        micSize = micSize,
                        cancelThresholdPx = cancelThresholdPx,
                        lockThresholdPx = lockThresholdPx,
                        currentState = { currentState },
                        recordingActions = recordingActions,
                        onShowHint = { showHint = true },
                    )
                }

                isFingerDown = false
            }
        },
    )
}

@Composable
private fun MicButtonLayout(
    isVisible: Boolean,
    micSize: IntSize,
    interactionSource: MutableInteractionSource,
    showHint: Boolean,
    modifier: Modifier = Modifier,
    onSizeChanged: (IntSize) -> Unit,
    onDismissHint: () -> Unit,
    onGesture: suspend PointerInputScope.() -> Unit,
) {
    val style = ChatTheme.messageComposerTheme.audioRecording.recordButton
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val buttonDescription = stringResource(R.string.stream_compose_cd_record_audio_message)

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .run { if (isVisible) size(style.size) else size(0.dp) }
                .padding(style.padding)
                .onSizeChanged(onSizeChanged)
                .clip(CircleShape)
                .indication(
                    interactionSource,
                    ripple(
                        bounded = true,
                        radius = with(density) { micSize.height.toDp() / 2 },
                    ),
                )
                .semantics { contentDescription = buttonDescription }
                .pointerInput(Unit, onGesture),
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
            HoldToRecordPopup(
                offset = micSize.height,
                onDismissRequest = onDismissHint,
            )
        }
    }
}

private const val HoldToRecordThresholdMs = 1_000L

/**
 * Handles the drag gesture loop after recording has started.
 */
private suspend fun AwaitPointerEventScope.handleRecordingGesture(
    down: PointerInputChange,
    micSize: IntSize,
    cancelThresholdPx: Float,
    lockThresholdPx: Float,
    currentState: () -> RecordingState,
    recordingActions: AudioRecordingActions,
    onShowHint: () -> Unit,
) {
    val holdStartTime = SystemClock.elapsedRealtime()
    val startOffset = down.position.minus(
        Offset(micSize.width.toFloat(), micSize.height.toFloat()),
    )
    recordingActions.onStartRecording(Offset.Zero)

    while (true) {
        if (currentState() is RecordingState.Locked) break

        val dragEvent = awaitDragOrCancellation(down.id)
        if (dragEvent == null || !dragEvent.pressed) {
            if (currentState() !is RecordingState.Locked) {
                val holdDuration = SystemClock.elapsedRealtime() - holdStartTime
                if (holdDuration < HoldToRecordThresholdMs) {
                    recordingActions.onCancelRecording()
                    onShowHint()
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
}

/**
 * Wrapper around Accompanist's [rememberPermissionState] that returns `null` in
 * preview / Paparazzi environments (where there is no Activity context).
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun rememberAudioRecordPermission(): AudioRecordPermission? {
    if (LocalInspectionMode.current) return null

    var showRationale by remember { mutableStateOf(false) }
    if (showRationale) {
        AudioRecordPermissionRationale(
            onDismissRequest = { showRationale = false },
        )
    }

    var showDenied by remember { mutableStateOf(false) }
    val state = rememberPermissionState(Manifest.permission.RECORD_AUDIO) { granted ->
        showDenied = !granted
    }
    if (showDenied) {
        SimpleDialog(
            title = stringResource(id = R.string.stream_ui_message_composer_permission_audio_record_title),
            message = stringResource(id = R.string.stream_ui_message_composer_permission_audio_record_message),
            onDismiss = { showDenied = false },
            onPositiveAction = { showDenied = false },
            showDismissButton = false,
        )
    }

    return remember(state) {
        AudioRecordPermission(
            status = state.status,
            launchPermissionRequest = { state.launchPermissionRequest() },
            showRationale = { showRationale = true },
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private class AudioRecordPermission(
    val status: PermissionStatus,
    val launchPermissionRequest: () -> Unit,
    val showRationale: () -> Unit,
)

/**
 * A popup anchored at [Alignment.BottomCenter] that auto-dismisses after [dismissTimeoutMs].
 */
@Composable
private fun TimedPopup(
    offsetY: Int,
    dismissTimeoutMs: Long = 1000L,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(dismissTimeoutMs)
        onDismissRequest()
    }
    Popup(
        onDismissRequest = onDismissRequest,
        offset = IntOffset(0, -offsetY),
        alignment = Alignment.BottomCenter,
    ) {
        content()
    }
}

@Composable
private fun HoldToRecordPopup(
    offset: Int,
    onDismissRequest: () -> Unit,
) {
    TimedPopup(offsetY = offset, onDismissRequest = onDismissRequest) {
        val theme = ChatTheme.messageComposerTheme.audioRecording.holdToRecord
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(theme.containerPadding),
            elevation = CardDefaults.cardElevation(defaultElevation = theme.containerElevation),
            shape = theme.containerShape,
            colors = CardDefaults.cardColors(containerColor = theme.containerColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(theme.contentHeight)
                    .padding(theme.contentPadding),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    style = theme.textStyle,
                    text = stringResource(id = R.string.stream_compose_message_composer_hold_to_record),
                )
            }
        }
    }
}

@Composable
private fun AudioRecordPermissionRationale(
    onDismissRequest: () -> Unit,
) {
    val theme = ChatTheme.messageComposerTheme.audioRecording.permissionRationale
    val offsetY = with(LocalDensity.current) { theme.containerBottomOffset.toPx().toInt() }
    TimedPopup(offsetY = offsetY, onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(theme.containerPadding),
            elevation = CardDefaults.cardElevation(defaultElevation = theme.containerElevation),
            shape = theme.containerShape,
            colors = CardDefaults.cardColors(containerColor = theme.containerColor),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(theme.contentHeight)
                    .padding(theme.contentPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    style = theme.textStyle,
                    text = stringResource(id = R.string.stream_ui_message_composer_permission_audio_record_message),
                )
                Spacer(modifier = Modifier.width(theme.contentSpace))
                val context = LocalContext.current
                TextButton(
                    modifier = Modifier,
                    onClick = { context.openSystemSettings() },
                ) {
                    Text(
                        style = theme.buttonTextStyle,
                        text = stringResource(id = R.string.stream_ui_message_composer_permissions_setting_button)
                            .uppercase(),
                    )
                }
            }
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
