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
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.components.audio.PlaybackTimerText
import io.getstream.chat.android.compose.ui.components.audio.StaticWaveformSlider
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.messages.composer.AudioRecordingFloatingIconStyle
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.compose.ui.util.padding
import io.getstream.chat.android.compose.ui.util.size
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.common.utils.openSystemSettings
import kotlinx.coroutines.delay
import kotlin.math.abs

private const val HOLD_TO_RECORD_THRESHOLD = 1000L
private const val HOLD_TO_RECORD_DISMISS_TIMEOUT = 1000L
private const val PERMISSION_RATIONALE_DISMISS_TIMEOUT = 1000L

/**
 * Default implementation of the audio record button.
 *
 * @param state The current recording state.
 * @param recordingActions The actions to perform on the recording.
 * @param holdToRecordThreshold The threshold to hold to record.
 * @param holdToRecordDismissTimeout The timeout to dismiss the hold to record popup.
 * @param permissionRationaleDismissTimeout The timeout to dismiss the permission rationale popup.
 */
@Composable
@OptIn(ExperimentalPermissionsApi::class)
public fun DefaultAudioRecordButton(
    state: RecordingState,
    recordingActions: AudioRecordingActions = AudioRecordingActions.None,
    holdToRecordThreshold: Long = HOLD_TO_RECORD_THRESHOLD,
    holdToRecordDismissTimeout: Long = HOLD_TO_RECORD_DISMISS_TIMEOUT,
    permissionRationaleDismissTimeout: Long = PERMISSION_RATIONALE_DISMISS_TIMEOUT,
) {
    val layoutDirection = LocalLayoutDirection.current
    val recordAudioButtonDescription = stringResource(id = R.string.stream_compose_cd_record_audio_message)

    var micSize by remember { mutableStateOf(IntSize.Zero) }

    var showDurationWarning by remember { mutableStateOf(false) }
    if (showDurationWarning) {
        DefaultHoldToRecordPopup(
            offset = micSize.height,
            dismissTimeoutMs = holdToRecordDismissTimeout,
            onDismissRequest = { showDurationWarning = false },
        )
    }

    var showPermissionRationale by remember { mutableStateOf(false) }
    if (showPermissionRationale) {
        DefaultAudioRecordPermissionRationale(
            dismissTimeoutMs = permissionRationaleDismissTimeout,
            onDismissRequest = { showPermissionRationale = false },
        )
    }

    var showPermissionDenied by remember { mutableStateOf(false) }
    val permissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO) { permissionGranted ->
        showPermissionDenied = !permissionGranted
    }
    if (showPermissionDenied) {
        SimpleDialog(
            title = stringResource(id = R.string.stream_ui_message_composer_permission_audio_record_title),
            message = stringResource(id = R.string.stream_ui_message_composer_permission_audio_record_message),
            onDismiss = { showPermissionDenied = false },
            onPositiveAction = { showPermissionDenied = false },
            showDismissButton = false,
        )
    }

    val style = ChatTheme.messageComposerTheme.audioRecording.recordButton
    val isRecording = state !is RecordingState.Idle
    val interactionSource = remember { MutableInteractionSource() }
    val currentState by rememberUpdatedState(state)

    val density = LocalDensity.current
    val cancelThresholdX = with(density) {
        ChatTheme.messageComposerTheme.audioRecording.slideToCancel.threshold.toPx().toInt()
    }
    val lockThresholdY = with(density) {
        ChatTheme.messageComposerTheme.audioRecording.floatingIcons.lockThreshold.toPx().toInt()
    }
    Box(
        modifier = Modifier
            .run { if (isRecording) size(0.dp) else size(style.size) }
            .padding(style.padding)
            .onSizeChanged { micSize = it }
            .indication(
                interactionSource,
                ripple(
                    bounded = true,
                    radius = with(density) { micSize.height.toDp() / 2 },
                ),
            )
            .semantics { contentDescription = recordAudioButtonDescription }
            .pointerInput(Unit) {
                awaitEachGesture {
                    // Await the first pointer down event
                    val downEvent = awaitFirstDown()
                    downEvent.consume()

                    // Trigger ripple when the gesture starts
                    interactionSource.tryEmit(PressInteraction.Press(downEvent.position))

                    if (permissionState.status.shouldShowRationale) {
                        showPermissionRationale = true
                    } else if (!permissionState.status.isGranted) {
                        permissionState.launchPermissionRequest()
                    } else {
                        val downOffset = downEvent.position
                        val holdStartTime = SystemClock.elapsedRealtime()
                        val startOffset = downOffset.minus(Offset(micSize.width.toFloat(), micSize.height.toFloat()))
                        recordingActions.onStartRecording(Offset.Zero)

                        // Await drag events
                        while (true) {
                            // If the recording is already locked, exit the drag loop
                            if (currentState is RecordingState.Locked) {
                                break
                            }

                            val dragEvent = awaitDragOrCancellation(downEvent.id)
                            if (dragEvent == null || !dragEvent.pressed) {
                                // On release, only perform actions if not already locked.
                                // The currentState can change during awaitDragOrCancellation, so this check is needed.
                                @Suppress("KotlinConstantConditions")
                                if (currentState !is RecordingState.Locked) {
                                    val holdElapsedTime = SystemClock.elapsedRealtime() - holdStartTime
                                    if (holdElapsedTime < holdToRecordThreshold) {
                                        recordingActions.onCancelRecording()
                                        showDurationWarning = true
                                    } else {
                                        recordingActions.onSendRecording()
                                    }
                                }
                                break
                            }
                            dragEvent.consume()
                            val diffOffset = dragEvent.position.minus(startOffset)
                            recordingActions.onHoldRecording(diffOffset)

                            if (diffOffset.x <= -cancelThresholdX) {
                                recordingActions.onCancelRecording()
                                break
                            } else if (diffOffset.y <= -lockThresholdY) {
                                recordingActions.onLockRecording()
                                break
                            }
                        }
                    }

                    // End the ripple when the gesture is complete
                    interactionSource.tryEmit(PressInteraction.Release(PressInteraction.Press(downEvent.position)))
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
            contentDescription = stringResource(id = R.string.stream_compose_record_audio_message),
            tint = style.icon.tint,
        )
    }
}

/**
 * Default implementation of the hold to record popup.
 */
@Composable
internal fun DefaultHoldToRecordPopup(
    offset: Int,
    dismissTimeoutMs: Long = 1000L,
    onDismissRequest: () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(dismissTimeoutMs)
        onDismissRequest()
    }
    Popup(
        onDismissRequest = onDismissRequest,
        offset = IntOffset(0, -offset),
        alignment = BottomCenter,
    ) {
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
internal fun DefaultAudioRecordPermissionRationale(
    dismissTimeoutMs: Long = 1000L,
    onDismissRequest: () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(dismissTimeoutMs)
        onDismissRequest()
    }
    val theme = ChatTheme.messageComposerTheme.audioRecording.permissionRationale
    val offsetY = with(LocalDensity.current) {
        theme.containerBottomOffset.toPx()
    }
    Popup(
        onDismissRequest = onDismissRequest,
        offset = IntOffset(0, -offsetY.toInt()),
        alignment = BottomCenter,
    ) {
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

/**
 * Default implementation of the audio recording content.
 */
@Composable
public fun DefaultMessageComposerRecordingContent(
    messageComposerState: MessageComposerState,
    recordingActions: AudioRecordingActions = AudioRecordingActions.None,
) {
    val recordingState = messageComposerState.recording

    val durationInMs = when (recordingState) {
        is RecordingState.Recording -> recordingState.durationInMs
        is RecordingState.Overview -> recordingState.durationInMs
        else -> 0
    }

    val waveformVisible = when (recordingState) {
        is RecordingState.Locked,
        is RecordingState.Overview,
        -> true
        else -> false
    }

    val waveformData = when (recordingState) {
        is RecordingState.Recording -> recordingState.waveform
        is RecordingState.Overview -> recordingState.waveform
        else -> emptyList()
    }

    val waveformThumbVisible = recordingState is RecordingState.Overview
    val waveformPlaying = recordingState is RecordingState.Overview && recordingState.isPlaying

    val waveformProgress = when (recordingState) {
        is RecordingState.Overview -> recordingState.playingProgress
        is RecordingState.Locked -> 1f // Locked state should color all bars as passed
        else -> 0f
    }

    val slideToCancelVisible = recordingState is RecordingState.Hold

    val holdControlsVisible = recordingState.let { it is RecordingState.Hold || it is RecordingState.Locked }
    val holdControlsLocked = recordingState is RecordingState.Locked
    val holdControlsOffset = when (recordingState) {
        is RecordingState.Hold -> IntOffset(
            x = recordingState.offsetX.toInt().coerceAtMost(maximumValue = 0),
            y = recordingState.offsetY.toInt().coerceAtMost(maximumValue = 0),
        )
        else -> IntOffset.Zero
    }

    val recordingControlsVisible = when (recordingState) {
        is RecordingState.Locked,
        is RecordingState.Overview,
        -> true
        else -> false
    }

    val recordingStopControlVisible = recordingState is RecordingState.Locked

    DefaultMessageComposerRecordingContent(
        durationInMs = durationInMs,
        waveformVisible = waveformVisible,
        waveformData = waveformData,
        waveformPlaying = waveformPlaying,
        waveformProgress = waveformProgress,
        slideToCancelVisible = slideToCancelVisible,
        waveformThumbVisible = waveformThumbVisible,
        holdControlsVisible = holdControlsVisible,
        holdControlsLocked = holdControlsLocked,
        holdControlsOffset = holdControlsOffset,
        recordingControlsVisible = recordingControlsVisible,
        recordingStopControlVisible = recordingStopControlVisible,
        recordingActions = recordingActions,
    )
}

@Composable
private fun DefaultMessageComposerRecordingContent(
    modifier: Modifier = Modifier,
    durationInMs: Int = 0,
    waveformVisible: Boolean = true,
    waveformThumbVisible: Boolean = false,
    waveformData: List<Float>,
    waveformPlaying: Boolean = false,
    waveformProgress: Float = 0f,
    slideToCancelVisible: Boolean = true,
    holdControlsVisible: Boolean = false,
    holdControlsLocked: Boolean = false,
    holdControlsOffset: IntOffset = IntOffset.Zero,
    recordingControlsVisible: Boolean = true,
    recordingStopControlVisible: Boolean = true,
    recordingActions: AudioRecordingActions = AudioRecordingActions.None,
) {
    var contentSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val cancelThresholdX = with(density) {
        ChatTheme.messageComposerTheme.audioRecording.slideToCancel.threshold.toPx().toInt()
    }

    val cancelOffsetX = abs(holdControlsOffset.x.takeIf { it <= 0 } ?: 0).toFloat()
    val slideToCancelProgress = (cancelOffsetX / cancelThresholdX).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .onSizeChanged {
                contentSize = it
            },
    ) {
        RecordingContent(
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

        if (recordingControlsVisible) {
            RecordingControlButtons(
                isStopControlVisible = recordingStopControlVisible,
                onDeleteRecording = recordingActions.onDeleteRecording,
                onStopRecording = recordingActions.onStopRecording,
                onCompleteRecording = recordingActions.onCompleteRecording,
            )
        }

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

@Composable
private fun RecordingContent(
    modifier: Modifier = Modifier,
    durationInMs: Int = 0,
    waveformVisible: Boolean = true,
    waveformThumbVisible: Boolean = false,
    waveformData: List<Float>,
    waveformPlaying: Boolean = false,
    waveformProgress: Float = 0f,
    slideToCancelVisible: Boolean = true,
    slideToCancelProgress: Float = 0f,
    holdControlsOffset: IntOffset = IntOffset.Zero,
    onToggleRecordingPlayback: () -> Unit,
    onSliderDragStart: (Float) -> Unit,
    onSliderDragStop: (Float) -> Unit,
) {
    val playbackTheme = ChatTheme.messageComposerTheme.audioRecording.playback
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(playbackTheme.height),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (waveformThumbVisible) {
            val btnStyle = when (waveformPlaying) {
                true -> playbackTheme.pauseButton
                else -> playbackTheme.playButton
            }
            IconButton(
                onClick = onToggleRecordingPlayback,
                modifier = Modifier
                    .size(btnStyle.size)
                    .padding(btnStyle.padding)
                    .focusable(true),
            ) {
                Icon(
                    painter = btnStyle.icon.painter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(btnStyle.size),
                    tint = btnStyle.icon.tint,
                )
            }
        } else {
            val micStyle = playbackTheme.micIndicator
            Box(
                modifier = Modifier
                    .size(micStyle.size)
                    .padding(micStyle.padding),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = micStyle.icon.painter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(micStyle.size),
                    tint = micStyle.icon.tint,
                )
            }
        }

        var currentProgress by remember { mutableFloatStateOf(waveformProgress) }
        LaunchedEffect(waveformProgress, durationInMs) { currentProgress = waveformProgress }

        PlaybackTimerText(
            progress = currentProgress,
            durationInMs = durationInMs,
            style = playbackTheme.timerTextStyle,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(playbackTheme.height),
            contentAlignment = Alignment.CenterEnd,
        ) {
            if (waveformVisible) {
                StaticWaveformSlider(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterStart)
                        .padding(playbackTheme.waveformSliderPadding),
                    style = playbackTheme.waveformSliderStyle,
                    waveformData = waveformData,
                    visibleBarLimit = 100,
                    adjustBarWidthToLimit = true,
                    isThumbVisible = waveformThumbVisible,
                    progress = currentProgress,
                    onDragStart = { currentProgress = it.also(onSliderDragStart) },
                    onDrag = { currentProgress = it },
                    onDragStop = { currentProgress = it.also(onSliderDragStop) },
                )
            }

            if (slideToCancelVisible) {
                RecordingSlideToCancelIndicator(slideToCancelProgress, holdControlsOffset)
            }
        }
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
    progress: Float = 0f,
    holdControlsOffset: IntOffset,
) {
    val theme = ChatTheme.messageComposerTheme.audioRecording.slideToCancel
    val offsetX = abs(holdControlsOffset.x.takeIf { it <= 0 } ?: 0)
    Row(
        modifier = Modifier.alpha(1 - progress),
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
            modifier = Modifier
                .align(Alignment.CenterVertically),
            style = theme.textStyle,
        )
        Spacer(modifier = Modifier.width(theme.marginEnd))
        Spacer(
            modifier = Modifier.width(
                with(LocalDensity.current) {
                    offsetX.toDp()
                },
            ),
        )
    }
}

@Composable
private fun RecordingControlButtons(
    isStopControlVisible: Boolean,
    onDeleteRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCompleteRecording: (Boolean) -> Unit,
) {
    val sendOnComplete = ChatTheme.messageComposerTheme.audioRecording.sendOnComplete
    val theme = ChatTheme.messageComposerTheme.audioRecording.controls
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(theme.height),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val deleteStyle = theme.deleteButton
        IconButton(
            onClick = onDeleteRecording,
            modifier = Modifier
                .semantics {
                    testTag = "Stream_ComposerDeleteAudioRecordingButton"
                }
                .size(deleteStyle.size)
                .padding(deleteStyle.padding)
                .focusable(true),
        ) {
            Icon(
                painter = deleteStyle.icon.painter,
                contentDescription = null,
                modifier = Modifier.size(deleteStyle.icon.size),
                tint = deleteStyle.icon.tint,
            )
        }

        if (isStopControlVisible) {
            Spacer(modifier = Modifier.weight(1f))
            val stopStyle = theme.stopButton
            IconButton(
                onClick = onStopRecording,
                modifier = Modifier
                    .semantics {
                        testTag = "Stream_ComposerStopAudioRecordingButton"
                    }
                    .size(stopStyle.size)
                    .padding(stopStyle.padding)
                    .focusable(true),
            ) {
                Icon(
                    painter = stopStyle.icon.painter,
                    contentDescription = null,
                    modifier = Modifier.size(stopStyle.icon.size),
                    tint = stopStyle.icon.tint,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        val completeStyle = theme.completeButton
        IconButton(
            onClick = { onCompleteRecording(sendOnComplete) },
            modifier = Modifier
                .semantics {
                    testTag = "Stream_ComposerCompleteAudioRecordingButton"
                }
                .size(completeStyle.size)
                .padding(completeStyle.padding)
                .focusable(true),
        ) {
            Icon(
                painter = completeStyle.icon.painter,
                contentDescription = null,
                modifier = Modifier
                    .size(completeStyle.icon.size),
                tint = completeStyle.icon.tint,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 200)
@Composable
private fun MessageComposerRecordingContentHoldPreview() {
    ChatPreviewTheme {
        MessageComposerRecordingContentHold()
    }
}

@Composable
internal fun MessageComposerRecordingContentHold() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = BottomCenter,
    ) {
        DefaultMessageComposerRecordingContent(
            durationInMs = DurationInMs,
            waveformVisible = false,
            waveformData = WaveformData,
            holdControlsVisible = true,
            recordingControlsVisible = false,
        )
    }
}

@Preview(showBackground = true, heightDp = 200)
@Composable
private fun MessageComposerRecordingContentLockPreview() {
    ChatPreviewTheme {
        MessageComposerRecordingContentLock()
    }
}

@Composable
internal fun MessageComposerRecordingContentLock() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = BottomCenter,
    ) {
        DefaultMessageComposerRecordingContent(
            durationInMs = DurationInMs,
            waveformData = WaveformData,
            waveformProgress = 0.3f,
            slideToCancelVisible = false,
            holdControlsVisible = true,
            holdControlsLocked = true,
        )
    }
}

@Preview(showBackground = true, heightDp = 200)
@Composable
private fun MessageComposerRecordingContentOverviewPreview() {
    ChatPreviewTheme {
        MessageComposerRecordingContentOverview()
    }
}

@Composable
internal fun MessageComposerRecordingContentOverview() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = BottomCenter,
    ) {
        DefaultMessageComposerRecordingContent(
            durationInMs = DurationInMs,
            waveformThumbVisible = true,
            waveformData = WaveformData,
            slideToCancelVisible = false,
            recordingStopControlVisible = false,
        )
    }
}

private const val DurationInMs = 120_000

@Suppress("MagicNumber")
private val WaveformData = (0..10).map {
    listOf(0.5f, 0.8f, 0.3f, 0.6f, 0.4f, 0.7f, 0.2f, 0.9f, 0.1f)
}.flatten()
