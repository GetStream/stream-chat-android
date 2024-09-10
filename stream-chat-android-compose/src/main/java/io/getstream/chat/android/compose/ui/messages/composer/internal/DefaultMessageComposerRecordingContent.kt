package io.getstream.chat.android.compose.ui.messages.composer.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.audio.WaveformSlider
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.extensions.limitTo
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.log.StreamLog
import kotlin.random.Random

@Composable
internal fun DefaultMessageComposerRecordingContent(
    messageComposerState: MessageComposerState,
    onLockRecording: () -> Unit = {},
    onPlaybackClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onStopClick: () -> Unit = {},
    onCompleteClick: () -> Unit = {},
) {

    val recordingState = messageComposerState.recording

    val waveformVisible = when (recordingState) {
        is RecordingState.Locked,
        is RecordingState.Overview -> true
        else -> false
    }

    val waveformData = when (recordingState) {
        is RecordingState.Recording -> recordingState.waveform
        is RecordingState.Overview -> recordingState.waveform
        else -> emptyList()
    }

    val waveformProgress = when (recordingState) {
        is RecordingState.Overview -> recordingState.playingProgress
        is RecordingState.Locked -> 1f // Locked state should color all bars as passed
        else -> 0f
    }

    val slideToCancelVisible = recordingState is RecordingState.Hold

    val slideToCancelProgress = when (recordingState) {
        is RecordingState.Hold -> {
            // TODO implement slide to cancel progress
            0f
        }
        else -> 0f
    }

    val isThumbVisible = recordingState is RecordingState.Overview

    val holdControlsVisible = recordingState.let { it is RecordingState.Hold || it is RecordingState.Locked }
    val holdControlsLocked = recordingState is RecordingState.Locked
    val holdControlsOffset = when (recordingState) {
        is RecordingState.Hold -> IntOffset(recordingState.offsetX.toInt(), recordingState.offsetY.toInt())
        else -> IntOffset.Zero
    }


    val recordingControlsVisible = when (recordingState) {
        is RecordingState.Locked,
        is RecordingState.Overview -> true
        else -> false
    }

    val recordingStopControlVisible = recordingState is RecordingState.Locked

    DefaultMessageComposerRecordingContent(
        waveformVisible = waveformVisible,
        waveformData = waveformData,
        waveformProgress = waveformProgress,
        slideToCancelVisible = slideToCancelVisible,
        slideToCancelProgress = slideToCancelProgress,
        waveformThumbVisible = isThumbVisible,
        holdControlsVisible = holdControlsVisible,
        holdControlsLocked = holdControlsLocked,
        holdControlsOffset = holdControlsOffset,
        recordingControlsVisible = recordingControlsVisible,
        recordingStopControlVisible = recordingStopControlVisible,
        onLockRecording = onLockRecording,
        onPlaybackClick = onPlaybackClick,
        onDeleteClick = onDeleteClick,
        onStopClick = onStopClick,
        onCompleteClick = onCompleteClick
    )
}

@Composable
internal fun DefaultMessageComposerRecordingContent(
    modifier: Modifier = Modifier,
    waveformVisible: Boolean = true,
    waveformThumbVisible: Boolean = false,
    waveformData: List<Float>,
    waveformProgress: Float = 0f,
    slideToCancelVisible: Boolean = true,
    slideToCancelProgress: Float = 0f,
    holdControlsVisible: Boolean = false,
    holdControlsLocked: Boolean = false,
    holdControlsOffset: IntOffset = IntOffset.Zero,
    recordingControlsVisible: Boolean = true,
    recordingStopControlVisible: Boolean = true,
    onLockRecording: () -> Unit = {},
    onPlaybackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStopClick: () -> Unit,
    onCompleteClick: () -> Unit,
) {
    var contentSize by remember { mutableStateOf(IntSize.Zero) }
    Column(
        modifier = modifier
            .onSizeChanged {
                StreamLog.i("RecordingContent") {
                    "Content size is $it"
                }
                contentSize = it
            }
    ) {
        RecordingContent(
            parentSize = contentSize,
            waveformVisible = waveformVisible,
            waveformThumbVisible = waveformThumbVisible,
            waveformData = waveformData,
            waveformProgress = waveformProgress,
            slideToCancelVisible = slideToCancelVisible,
            slideToCancelProgress = slideToCancelProgress,
            holdControlsVisible = holdControlsVisible,
            holdControlsLocked = holdControlsLocked,
            holdControlsOffset = holdControlsOffset,
            recordingControlsVisible = recordingControlsVisible,
            recordingStopControlVisible = recordingStopControlVisible,
            onLockRecording = onLockRecording,
            onPlaybackClick = onPlaybackClick,
            onDeleteClick = onDeleteClick,
            onStopClick = onStopClick,
            onCompleteClick = onCompleteClick
        )

        if (recordingControlsVisible) {
            RecordingControlButtons(
                isStopControlVisible = recordingStopControlVisible,
                onDeleteClick = onDeleteClick,
                onStopClick = onStopClick,
                onCompleteClick = onCompleteClick
            )
        }
    }
}

@Composable
private fun RecordingContent(
    modifier: Modifier = Modifier,
    parentSize: IntSize,
    waveformVisible: Boolean = true,
    waveformThumbVisible: Boolean = false,
    waveformData: List<Float>,
    waveformProgress: Float = 0f,
    slideToCancelVisible: Boolean = true,
    slideToCancelProgress: Float = 0f,
    holdControlsVisible: Boolean = false,
    holdControlsLocked: Boolean = false,
    holdControlsOffset: IntOffset = IntOffset.Zero,
    recordingControlsVisible: Boolean = true,
    recordingStopControlVisible: Boolean = true,
    onLockRecording: () -> Unit = {},
    onPlaybackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStopClick: () -> Unit,
    onCompleteClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painter = painterResource(id = R.drawable.stream_compose_ic_mic),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
                .clickable { onPlaybackClick() }
                .focusable(true)
                .background(Color.Transparent),
            tint = colorResource(id = R.color.stream_compose_accent_red))

        Text(
            text = "00:00",
            modifier = Modifier,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color.Yellow),
            contentAlignment = Alignment.CenterEnd,
        ) {
            if (waveformVisible) {
                WaveformSlider(
                    waveformData = waveformData,
                    modifier = Modifier
                        .height(36.dp)
                        .align(Alignment.CenterStart)
                        .padding(top = 8.dp, bottom = 8.dp, start = 16.dp),
                    visibleBarLimit = 100,
                    adjustBarWidthToLimit = true,
                    isThumbVisible = waveformThumbVisible,
                    progress = waveformProgress,
                )
            }

            if (slideToCancelVisible) {
                RecordingSlideToCancelIndicator(slideToCancelProgress)
            }

            if (holdControlsVisible) {
                val density = LocalDensity.current

                val lockThreshold = with(density) { 96.dp.toPx().toInt() }

                val micBaseOffset = remember {
                    with(density) {
                        // 64 is width of the mic popup icon
                        // 48 is width of the mic icon next to the send button
                        IntOffset(
                            x = ((64 - 48) / 2).dp.toPx().toInt(),
                            y = 0
                        )
                    }
                }
                val micOffset = micBaseOffset + holdControlsOffset

                if (micOffset.y <= -lockThreshold) {
                    StreamLog.i("RecordingContent") { "Mic offset is $micOffset, locking recording" }
                    onLockRecording()
                }

                Popup(
                    offset = micOffset,
                    properties = PopupProperties(clippingEnabled = false),
                    alignment = Alignment.CenterEnd,
                ) {
                    RecordingMicIcon()
                }


                StreamLog.i("RecordingContent") { "parentSize is $parentSize" }


                // 64 is width of the mic popup icon
                val lockOffset = with(density) {
                    // 4 is the offset from the right edge of the screen
                    // 64 is the height of the mic popup icon
                    IntOffset(
                        x = -4.dp.toPx().toInt(),
                        y = when (holdControlsLocked) {
                            true -> -48.dp.toPx().toInt() - 16.dp.toPx().toInt()
                            else -> -48.dp.toPx().toInt() - 16.dp.toPx().toInt() + holdControlsOffset.y
                        }
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
}

@Composable
private fun RecordingMicIcon() {
    Card(
        modifier = Modifier.size(64.dp),
        backgroundColor = colorResource(id = R.color.stream_compose_grey_gainsboro),
        shape = CircleShape,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize() // Ensures the Icon is centered inside the Card
        ) {
            Icon(
                painter = painterResource(id = R.drawable.stream_compose_ic_mic),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp) // Icon stays the correct size
                    .padding(4.dp), // Padding inside the Icon itself, if needed
                tint = colorResource(id = R.color.stream_compose_accent_blue),
            )
        }
    }
}

@Composable
private fun RecordingLockableIcon(
    locked: Boolean,
) {
    if (locked) {
        RecordingLockedIcon()
    } else {
        RecordingLockIcon()
    }
}

@Composable
private fun RecordingLockIcon() {
    Card(
        modifier = Modifier
            .size(width = 48.dp, height = 88.dp),
        shape = RoundedCornerShape(24.dp),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_mic_lock),
            contentDescription = null,
            modifier = Modifier
                .size(width = 48.dp, height = 88.dp),
            tint = Color.Unspecified,
        )
    }
}

@Composable
private fun RecordingLockedIcon() {
    Card(
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_mic_locked),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color.Unspecified,
        )
    }
}

@Composable
private fun RecordingSlideToCancelIndicator(
    progress: Float = 0f,
) {
    var stcWidth by remember { mutableIntStateOf(0) }
    Row(
        modifier = Modifier
            .background(Color.Magenta)
            .alpha(1 - progress)
            .onSizeChanged {
                stcWidth = it.width
            },
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.stream_compose_ic_arrow_left_black),
            tint = colorResource(id = R.color.stream_compose_grey),
            contentDescription = null
        )
        Text(
            text = "Slide to Cancel",
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(bottom = 2.dp),
            color = colorResource(id = R.color.stream_compose_grey)
        )
        Spacer(modifier = Modifier.width(96.dp))
        Spacer(modifier = Modifier.width(with(LocalDensity.current) {
            (stcWidth * progress).toDp()
        }))
    }
}

@Composable
private fun RecordingControlButtons(
    isStopControlVisible: Boolean,
    onDeleteClick: () -> Unit,
    onStopClick: () -> Unit,
    onCompleteClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier
                .size(32.dp)
                .size(32.dp)
                .padding(4.dp)
                .focusable(true),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.stream_compose_ic_delete),
                contentDescription = null,
                modifier = Modifier,
                tint = colorResource(id = R.color.stream_compose_accent_red)
            )
        }

        if (isStopControlVisible) {
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = onStopClick,
                modifier = Modifier
                    .size(32.dp)
                    .padding(4.dp)
                    .focusable(true),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.stream_compose_ic_stop_circle),
                    contentDescription = null,
                    modifier = Modifier,
                    tint = colorResource(id = R.color.stream_compose_accent_red)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = onCompleteClick,
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
                .focusable(true),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.stream_compose_ic_check_circle),
                contentDescription = null,
                modifier = Modifier,
                tint = colorResource(id = R.color.stream_compose_accent_blue)
            )

        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun DefaultMessageComposerRecordingContentPreview() {
    val randomWaveformData = List(150) { Random.nextFloat() }
    ChatPreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            DefaultMessageComposerRecordingContent(
                modifier = Modifier.fillMaxWidth(),
                waveformVisible = true,
                waveformThumbVisible = true,
                waveformData = randomWaveformData,
                waveformProgress = 0.2f,
                slideToCancelVisible = true,
                slideToCancelProgress = 1.0f,
                holdControlsVisible = true,
                holdControlsLocked = false,
                holdControlsOffset = IntOffset(0, 0),
                recordingControlsVisible = true,
                recordingStopControlVisible = true,
                onPlaybackClick = {},
                onDeleteClick = {},
                onStopClick = {},
                onCompleteClick = {})
        }
    }
}
