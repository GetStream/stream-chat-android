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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
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
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.SnackbarPopup
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.compose.ui.util.padding
import io.getstream.chat.android.compose.ui.util.size
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.common.utils.openSystemSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.roundToInt

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
    val showControls = recordingState is RecordingState.Locked || recordingState is RecordingState.Overview
    val showFloatingIcons = recordingState is RecordingState.Hold || recordingState is RecordingState.Locked

    // --- Floating button state (hoisted so isMicVisible keeps the anchor stable) ---
    val isHolding = recordingState is RecordingState.Hold
    var isReturning by remember { mutableStateOf(false) }

    // Detect Hold→Idle transition: spring-back only to Idle; when locking, just disappear.
    val prevHolding = remember { mutableStateOf(false) }
    if (prevHolding.value && !isHolding) {
        isReturning = recordingState is RecordingState.Idle
    }
    prevHolding.value = isHolding

    val floatingActive = isHolding || isReturning

    // Keep mic button sized during spring-back so the Popup anchor stays stable.
    val isMicVisible = recordingState is RecordingState.Idle || isHolding || isReturning

    val holdOffset = if (recordingState is RecordingState.Hold) {
        IntOffset(
            x = recordingState.offsetX.toInt().coerceAtMost(0),
            y = recordingState.offsetY.toInt().coerceAtMost(0),
        )
    } else {
        IntOffset.Zero
    }

    // --- Floating button animation (owned here so isReturning is set directly) ---
    val floatingOffsetX = remember { Animatable(0f) }
    val floatingOffsetY = remember { Animatable(0f) }

    if (isHolding) {
        LaunchedEffect(holdOffset) {
            floatingOffsetX.snapTo(holdOffset.x.toFloat())
            floatingOffsetY.snapTo(holdOffset.y.toFloat())
        }
    }

    LaunchedEffect(isReturning) {
        if (isReturning) {
            try {
                coroutineScope {
                    launch { floatingOffsetX.animateTo(0f, spring()) }
                    launch { floatingOffsetY.animateTo(0f, spring()) }
                }
            } finally {
                isReturning = false
            }
        }
    }

    // When recording, fill the available width so the recording content takes
    // the space freed by the hidden center content in MessageInput.
    // During spring-back (isReturning) the layout reverts to idle immediately;
    // only the floating Popup animates — this avoids height conflicts with
    // MessageInput's own animateContentSize and center-content toggling.
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
                floatingActive = floatingActive,
                floatingOffset = IntOffset(
                    x = floatingOffsetX.value.roundToInt(),
                    y = floatingOffsetY.value.roundToInt(),
                ),
            )
        }

        if (showControls) {
            RecordingControlButtons(
                isStopVisible = recordingState is RecordingState.Locked,
                recordingActions = recordingActions,
            )
        }

        if (showFloatingIcons) {
            FloatingLockIcon(
                isLocked = recordingState is RecordingState.Locked,
                holdControlsOffset = holdOffset,
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun MicButton(
    isVisible: Boolean,
    recordingState: RecordingState,
    recordingActions: AudioRecordingActions,
    floatingActive: Boolean,
    floatingOffset: IntOffset,
    modifier: Modifier = Modifier,
) {
    var isFingerDown by remember { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(Offset.Zero) }

    val permissionState = rememberAudioRecordPermission()
    val hint = rememberRecordingHint()
    val interactionSource = remember { MutableInteractionSource() }
    val currentState by rememberUpdatedState(recordingState)

    val density = LocalDensity.current
    val gestureConfig = RecordingGestureConfig(
        cancelThresholdPx = with(density) {
            ChatTheme.messageComposerTheme.audioRecording.slideToCancel.threshold.toPx()
        },
        lockThresholdPx = with(density) {
            ChatTheme.messageComposerTheme.audioRecording.floatingIcons.lockThreshold.toPx()
        },
    )

    LaunchedEffect(isFingerDown) {
        if (isFingerDown) {
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
    val buttonDescription = stringResource(R.string.stream_compose_cd_record_audio_message)

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .run { if (isVisible) size(style.size) else size(0.dp) }
                .semantics { contentDescription = buttonDescription }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        pressOffset = down.position
                        isFingerDown = true
                        hint.dismiss()

                        if (permissionState.gateRecording()) {
                            handleRecordingGesture(
                                down = down,
                                config = gestureConfig,
                                currentState = { currentState },
                                recordingActions = recordingActions,
                                onShowHint = { hint.show() },
                            )
                        }

                        isFingerDown = false
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (isVisible && !floatingActive) {
                MicButtonVisual(
                    interactionSource = interactionSource,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }

        if (floatingActive) {
            Popup(
                offset = floatingOffset,
                alignment = Alignment.TopStart,
                properties = PopupProperties(clippingEnabled = false),
            ) {
                MicButtonVisual(
                    interactionSource = interactionSource,
                    isPressed = true,
                    modifier = Modifier.size(style.size),
                )
            }
        }

        SnackbarPopup(
            hostState = hint.snackbarHostState,
            snackbar = { AudioRecordingSnackbar(it) },
        )
    }
}

private const val PressedOverlayAlpha = 0.12f

@Composable
private fun MicButtonVisual(
    interactionSource: MutableInteractionSource,
    isPressed: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val style = ChatTheme.messageComposerTheme.audioRecording.recordButton
    val layoutDirection = LocalLayoutDirection.current
    Box(
        modifier = modifier
            .padding(style.padding)
            .clip(CircleShape)
            .indication(
                interactionSource = interactionSource,
                indication = ripple(),
            )
            .then(
                if (isPressed) {
                    Modifier.background(
                        ChatTheme.colors.textPrimary.copy(alpha = PressedOverlayAlpha),
                    )
                } else {
                    Modifier
                },
            ),
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
}

private class RecordingHintState(
    val snackbarHostState: SnackbarHostState,
    private val scope: CoroutineScope,
    private val message: String,
) {
    fun show() {
        scope.launch {
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    fun dismiss() {
        snackbarHostState.currentSnackbarData?.dismiss()
    }
}

@Composable
private fun rememberRecordingHint(): RecordingHintState {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val message = stringResource(R.string.stream_compose_message_composer_hold_to_record)
    return remember(snackbarHostState, scope, message) {
        RecordingHintState(snackbarHostState, scope, message)
    }
}

private class RecordingGestureConfig(
    val cancelThresholdPx: Float,
    val lockThresholdPx: Float,
)

private const val HoldToRecordThresholdMs = 400L
private const val AxisLockThresholdPx = 10f

private enum class DragAxis { Horizontal, Vertical }

/**
 * Handles the full gesture lifecycle:
 * 1. Waits for [HoldToRecordThresholdMs] — if the user releases before that, it's a tap → [onShowHint].
 * 2. Once the threshold is reached, starts recording and tracks drag for cancel / lock / send.
 *
 * Drag is axis-locked: once the first significant movement picks a direction
 * (left → cancel, up → lock), the offset is constrained to that axis.
 */
private suspend fun AwaitPointerEventScope.handleRecordingGesture(
    down: PointerInputChange,
    config: RecordingGestureConfig,
    currentState: () -> RecordingState,
    recordingActions: AudioRecordingActions,
    onShowHint: () -> Unit,
) {
    // Phase 1: Wait for hold threshold. If released early, treat as tap.
    val releasedBeforeThreshold = withTimeoutOrNull(HoldToRecordThresholdMs) {
        while (true) {
            val event = awaitDragOrCancellation(down.id) ?: return@withTimeoutOrNull
            if (!event.pressed) return@withTimeoutOrNull
            event.consume()
        }
    } != null

    if (releasedBeforeThreshold) {
        onShowHint()
        return
    }

    // Phase 2: Threshold reached — start recording and handle drag.
    val startOffset = down.position
    recordingActions.onStartRecording(Offset.Zero)

    var dragAxis: DragAxis? = null

    while (true) {
        if (currentState() is RecordingState.Locked) break

        val dragEvent = awaitDragOrCancellation(down.id)
        if (dragEvent == null || !dragEvent.pressed) {
            if (currentState() !is RecordingState.Locked) {
                recordingActions.onSendRecording()
            }
            break
        }

        dragEvent.consume()
        val rawDiff = dragEvent.position.minus(startOffset)

        // Lock axis on first significant movement.
        if (dragAxis == null) {
            val absX = abs(rawDiff.x)
            val absY = abs(rawDiff.y)
            if (absX > AxisLockThresholdPx || absY > AxisLockThresholdPx) {
                dragAxis = if (absX > absY) DragAxis.Horizontal else DragAxis.Vertical
            }
        }

        val diffOffset = when (dragAxis) {
            DragAxis.Horizontal -> Offset(rawDiff.x, 0f)
            DragAxis.Vertical -> Offset(0f, rawDiff.y)
            null -> Offset.Zero
        }
        recordingActions.onHoldRecording(diffOffset)

        if (diffOffset.x <= -config.cancelThresholdPx) {
            recordingActions.onCancelRecording()
            break
        }
        if (diffOffset.y <= -config.lockThresholdPx) {
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
 * Returns `true` if the recording can proceed (permission granted or not applicable).
 * Otherwise, triggers the appropriate permission request or rationale dialog and returns `false`.
 */
@OptIn(ExperimentalPermissionsApi::class)
private fun AudioRecordPermission?.gateRecording(): Boolean = when {
    this?.status?.shouldShowRationale == true -> {
        showRationale()
        false
    }
    this?.status?.isGranted == false -> {
        launchPermissionRequest()
        false
    }
    else -> true
}

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

private val SnackbarShape = RoundedCornerShape(StreamTokens.radius3xl)

@Composable
private fun AudioRecordingSnackbar(data: SnackbarData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = StreamTokens.spacingMd),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.shadow(4.dp, shape = SnackbarShape),
            shape = SnackbarShape,
            color = ChatTheme.colors.backgroundCoreInverse,
            contentColor = ChatTheme.colors.textOnAccent,
        ) {
            Text(
                modifier = Modifier.padding(
                    horizontal = StreamTokens.spacingMd,
                    vertical = StreamTokens.spacingSm,
                ),
                text = data.visuals.message,
                style = ChatTheme.typography.bodyDefault,
            )
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
