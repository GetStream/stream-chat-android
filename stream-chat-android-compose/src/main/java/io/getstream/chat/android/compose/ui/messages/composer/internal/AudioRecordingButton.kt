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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.SnackbarPopup
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
internal fun AudioRecordingButton(
    recordingState: RecordingState,
    recordingActions: AudioRecordingActions,
    modifier: Modifier = Modifier,
) {
    val isRecording = recordingState !is RecordingState.Idle
    val showFloatingIcons = recordingState is RecordingState.Hold || recordingState is RecordingState.Locked
    val floatingMic = rememberFloatingMicState(recordingState)

    Box(modifier = modifier.then(if (isRecording) Modifier.fillMaxWidth() else Modifier)) {
        Row(verticalAlignment = Alignment.Bottom) {
            if (isRecording) {
                AudioRecordingContent(
                    recordingState = recordingState,
                    recordingActions = recordingActions,
                    modifier = Modifier.weight(1f),
                )
            }

            MicButton(
                isVisible = floatingMic.isVisible,
                recordingState = recordingState,
                recordingActions = recordingActions,
                floatingActive = floatingMic.isFloating,
                floatingOffset = floatingMic.offset,
            )
        }

        if (showFloatingIcons) {
            FloatingLockIcon(
                isLocked = recordingState is RecordingState.Locked,
                dragOffsetY = floatingMic.offset.y,
            )
        }
    }
}

/**
 * Encapsulates the floating mic button animation state:
 * - Tracks hold → idle transitions to trigger spring-back animation.
 * - Keeps the mic button sized during spring-back so the Popup anchor stays stable.
 *
 * @property isFloating `true` when the mic is being dragged or spring-animating back (show as Popup).
 * @property isVisible `true` when the mic button should have layout size (Idle, Hold, or spring-back).
 * @property offset Current animated offset — follows the drag during Hold, spring-animates to zero on release.
 */
private class FloatingMicState(
    val isFloating: Boolean,
    val isVisible: Boolean,
    val offset: IntOffset,
)

@Composable
private fun rememberFloatingMicState(recordingState: RecordingState): FloatingMicState {
    val isHolding = recordingState is RecordingState.Hold
    var isReturning by remember { mutableStateOf(false) }

    // Detect Hold→Idle transition: spring-back only to Idle; when locking, just disappear.
    val prevHolding = remember { mutableStateOf(false) }
    if (prevHolding.value && !isHolding) {
        isReturning = recordingState is RecordingState.Idle
    }
    prevHolding.value = isHolding

    val holdOffset = if (recordingState is RecordingState.Hold) {
        IntOffset(
            x = recordingState.offsetX.toInt().coerceAtMost(0),
            y = recordingState.offsetY.toInt().coerceAtMost(0),
        )
    } else {
        IntOffset.Zero
    }

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

    val isFloating = isHolding || isReturning
    return FloatingMicState(
        isFloating = isFloating,
        isVisible = recordingState is RecordingState.Idle || isFloating,
        offset = IntOffset(
            x = floatingOffsetX.value.roundToInt(),
            y = floatingOffsetY.value.roundToInt(),
        ),
    )
}

/**
 * Animates a pop-in scale from [FloatingIconInitialScale] to `1f` with a bouncy spring.
 *
 * Shared by both the floating mic and the floating lock Popups so they enter consistently.
 * Returns `1f` immediately in inspection mode so Paparazzi snapshots and previews capture the
 * final state without running the animation.
 */
@Composable
private fun rememberEntranceScale(): Float {
    if (LocalInspectionMode.current) return 1f
    val scale = remember { Animatable(FloatingIconInitialScale) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        )
    }
    return scale.value
}

@Composable
private fun MicButton(
    isVisible: Boolean,
    recordingState: RecordingState,
    recordingActions: AudioRecordingActions,
    floatingActive: Boolean,
    floatingOffset: IntOffset,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier) {
        MicButtonGestureArea(
            isVisible = isVisible,
            floatingActive = floatingActive,
            recordingState = recordingState,
            recordingActions = recordingActions,
            interactionSource = interactionSource,
        )

        if (floatingActive) {
            Popup(
                offset = floatingOffset,
                alignment = Alignment.TopStart,
                properties = PopupProperties(clippingEnabled = false),
            ) {
                val entranceScale = rememberEntranceScale()
                MicButtonVisual(
                    interactionSource = interactionSource,
                    isPressed = true,
                    modifier = Modifier
                        .size(MicButtonSize)
                        .graphicsLayer {
                            scaleX = entranceScale
                            scaleY = entranceScale
                        },
                )
            }
        }
    }
}

/** Gesture target for the mic button: handles touch, permission gating, and recording hints. */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun MicButtonGestureArea(
    isVisible: Boolean,
    floatingActive: Boolean,
    recordingState: RecordingState,
    recordingActions: AudioRecordingActions,
    interactionSource: MutableInteractionSource,
) {
    var isFingerDown by remember { mutableStateOf(false) }
    var pressOffset by remember { mutableStateOf(Offset.Zero) }
    val permissionState = rememberAudioRecordingPermission()
    val hint = rememberRecordingHint()
    val currentState by rememberUpdatedState(recordingState)
    val hapticFeedback = LocalHapticFeedback.current

    val density = LocalDensity.current
    val gestureConfig = RecordingGestureConfig(
        cancelThresholdPx = with(density) { SlideToCancelThreshold.toPx() },
        lockThresholdPx = with(density) { LockThreshold.toPx() },
    )

    PressInteractionEffect(isFingerDown, pressOffset, interactionSource)

    val buttonDescription = stringResource(R.string.stream_compose_cd_record_audio_message)

    Box(
        modifier = Modifier
            .run { if (isVisible) size(MicButtonSize) else size(0.dp) }
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
                            recordingActions = recordingActions.withHapticOnStart(hapticFeedback),
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
                modifier = Modifier.size(MicButtonVisualSize),
            )
        }
    }

    SnackbarPopup(
        hostState = hint.snackbarHostState,
        snackbar = { ChatTheme.componentFactory.MessageComposerAudioRecordingHint(it) },
    )

    SnackbarPopup(
        hostState = permissionState.rationaleSnackbarHostState,
        snackbar = { ChatTheme.componentFactory.MessageComposerAudioRecordingPermissionRationale(it) },
    )
}

/** Emits press/release interactions on [interactionSource] while [isFingerDown] is true. */
@Composable
private fun PressInteractionEffect(
    isFingerDown: Boolean,
    pressOffset: Offset,
    interactionSource: MutableInteractionSource,
) {
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
}

private const val PressedOverlayAlpha = 0.10f

@Composable
private fun MicButtonVisual(
    interactionSource: MutableInteractionSource,
    isPressed: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current
    Box(
        modifier = modifier
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
                .testTag("Stream_ComposerRecordAudioButton"),
            painter = painterResource(id = R.drawable.stream_compose_ic_mic),
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

private val SnackbarShape = RoundedCornerShape(StreamTokens.radius3xl)

/** Size of the mic button container / hit area. */
private val MicButtonSize = 48.dp

/** Size of the mic button visual circle (icon + pressed overlay). */
private val MicButtonVisualSize = 32.dp

/** Vertical drag distance required to lock the recording. */
private val LockThreshold = 96.dp

/** Height of the waveform / timer row. */
private val RecordingRowHeight = 48.dp

/** Height of the control-buttons row (delete, stop, complete). */
private val ControlsRowHeight = 48.dp

/** Starting scale for the pop-in entrance animation of floating icons. */
private const val FloatingIconInitialScale = 0.5f

/** Horizontal margin between the lock icon and the content's end edge. */
private val LockIconMarginEnd = 4.dp

/** Vertical margin between the lock icon and the top of the content. */
private val LockIconMarginTop = 16.dp

/** "Hold to record" instructional hint shown as a snackbar. */
@Composable
internal fun MessageComposerAudioRecordingHint(data: SnackbarData) {
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

/** Floating lock icon positioned above the content at the end edge, following drag during Hold. */
@Composable
private fun FloatingLockIcon(
    isLocked: Boolean,
    dragOffsetY: Int,
) {
    val density = LocalDensity.current
    val contentHeight = if (isLocked) {
        RecordingRowHeight + ControlsRowHeight
    } else {
        RecordingRowHeight
    }
    val offset = with(density) {
        IntOffset(
            x = -LockIconMarginEnd.roundToPx(),
            y = -(contentHeight + LockIconMarginTop).roundToPx() + if (isLocked) 0 else dragOffsetY,
        )
    }

    val entranceScale = rememberEntranceScale()

    Popup(offset = offset, alignment = Alignment.BottomEnd) {
        val iconRes = if (isLocked) {
            R.drawable.stream_compose_ic_mic_locked
        } else {
            R.drawable.stream_compose_ic_mic_lock
        }
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.graphicsLayer {
                scaleX = entranceScale
                scaleY = entranceScale
            },
        )
    }
}

/** Control buttons (delete, stop, complete) shown during locked and overview recording states. */
@Composable
internal fun MessageComposerAudioRecordingControlsContent(
    isStopVisible: Boolean,
    recordingActions: AudioRecordingActions,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = recordingActions.onDeleteRecording,
            modifier = Modifier.semantics { testTag = "Stream_ComposerDeleteAudioRecordingButton" },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.stream_compose_ic_delete),
                contentDescription = null,
                tint = ChatTheme.colors.primaryAccent,
            )
        }
        if (isStopVisible) {
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = recordingActions.onStopRecording,
                modifier = Modifier.semantics { testTag = "Stream_ComposerStopAudioRecordingButton" },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.stream_compose_ic_stop_circle),
                    contentDescription = null,
                    tint = ChatTheme.colors.errorAccent,
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = recordingActions.onConfirmRecording,
            modifier = Modifier.semantics { testTag = "Stream_ComposerConfirmAudioRecordingButton" },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.stream_compose_ic_check_circle),
                contentDescription = null,
                tint = ChatTheme.colors.primaryAccent,
            )
        }
    }
}

/**
 * Returns a copy of [AudioRecordingActions] whose [AudioRecordingActions.onStartRecording] also
 * triggers [HapticFeedbackType.LongPress] via [hapticFeedback].
 */
private fun AudioRecordingActions.withHapticOnStart(
    hapticFeedback: HapticFeedback,
): AudioRecordingActions = copy(
    onStartRecording = {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        onStartRecording()
    },
)

private const val PreviewDurationInMs = 120_000

@Suppress("MagicNumber")
private val PreviewWaveformData = (0..10).map {
    listOf(0.5f, 0.8f, 0.3f, 0.6f, 0.4f, 0.7f, 0.2f, 0.9f, 0.1f)
}.flatten()

@Preview(showBackground = true)
@Composable
private fun AudioRecordingButtonIdlePreview() {
    ChatPreviewTheme {
        AudioRecordingButtonIdle()
    }
}

@Composable
internal fun AudioRecordingButtonIdle() {
    AudioRecordingButton(
        recordingState = RecordingState.Idle,
        recordingActions = AudioRecordingActions.None,
    )
}

@Preview(showBackground = true, heightDp = 200)
@Composable
private fun AudioRecordingButtonHoldPreview() {
    ChatPreviewTheme {
        AudioRecordingButtonHold()
    }
}

@Composable
internal fun AudioRecordingButtonHold() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AudioRecordingButton(
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
private fun AudioRecordingButtonLockedPreview() {
    ChatPreviewTheme {
        AudioRecordingButtonLocked()
    }
}

@Composable
internal fun AudioRecordingButtonLocked() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AudioRecordingButton(
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
private fun AudioRecordingButtonOverviewPreview() {
    ChatPreviewTheme {
        AudioRecordingButtonOverview()
    }
}

@Composable
internal fun AudioRecordingButtonOverview() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AudioRecordingButton(
            recordingState = RecordingState.Overview(
                durationInMs = PreviewDurationInMs,
                waveform = PreviewWaveformData,
                attachment = Attachment(),
            ),
            recordingActions = AudioRecordingActions.None,
        )
    }
}
