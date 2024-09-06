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
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.audio.WaveformSlider
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import kotlin.random.Random

@Composable
internal fun DefaultMessageComposerRecordingContent(
    messageComposerState: MessageComposerState,
    onPlaybackClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onStopClick: () -> Unit = {},
    onCompleteClick: () -> Unit = {},
) {
    when (val state = messageComposerState.recording) {
        is RecordingState.Idle -> Unit
        is RecordingState.Hold -> {
            DefaultMessageComposerRecordingContent(
                waveformData = state.waveform,
                onPlaybackClick = onPlaybackClick,
                onDeleteClick = onDeleteClick,
                onStopClick = onStopClick,
                onCompleteClick = onCompleteClick
            )
        }

        is RecordingState.Overview -> {
            DefaultMessageComposerRecordingContent(
                waveformData = state.waveform,
                waveformProgress = state.playingProgress,
                isThumbVisible = state.isPlaying,
                onPlaybackClick = onPlaybackClick,
                onDeleteClick = onDeleteClick,
                onStopClick = onStopClick,
                onCompleteClick = onCompleteClick
            )
        }

        is RecordingState.Locked -> {
            DefaultMessageComposerRecordingContent(
                waveformData = state.waveform,
                waveformProgress = 0f,
                isThumbVisible = false,
                onPlaybackClick = onPlaybackClick,
                onDeleteClick = onDeleteClick,
                onStopClick = onStopClick,
                onCompleteClick = onCompleteClick
            )
        }

        is RecordingState.Complete -> {}
    }
}

@Composable
internal fun DefaultMessageComposerRecordingContent(
    modifier: Modifier = Modifier,
    isWaveformVisible: Boolean = true,
    waveformData: List<Float>,
    waveformProgress: Float = 0f,
    isSlideToCancelVisible: Boolean = true,
    slideToCancelProgress: Float = 0f,
    isThumbVisible: Boolean = false,
    areControlsVisible: Boolean = true,
    onPlaybackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStopClick: () -> Unit,
    onCompleteClick: () -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (isWaveformVisible) {
                    WaveformSlider(
                        waveformData = waveformData,
                        modifier = Modifier
                            .height(36.dp)
                            .align(Alignment.CenterStart)
                            .padding(top = 8.dp, bottom = 8.dp, start = 16.dp),
                        visibleBarLimit = 100,
                        adjustBarWidthToLimit = true,
                        isThumbVisible = isThumbVisible,
                        progress = waveformProgress,
                    )
                }

                if (isSlideToCancelVisible) {
                    RecordingSlideToCancelIndicator(slideToCancelProgress)
                }
            }
        }

        if (areControlsVisible) {
            RecordingControlButtons(
                onDeleteClick = onDeleteClick,
                onStopClick = onStopClick,
                onCompleteClick = onCompleteClick
            )
        }
    }
}

@Composable
private fun RecordingSlideToCancelControls(
    x: Float,
    y: Float,
    onLock: () -> Unit,
    onCancel: () -> Unit,
) {



    RecordingSlideToCancelIndicator(0f)
}

@Composable
private fun RecordingMicIcon() {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .shadow(2.dp, CircleShape)
            .background(Color.LightGray, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_mic),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
                .focusable(true),
            tint = colorResource(id = R.color.stream_compose_accent_blue),
        )
    }
}

@Composable
private fun RecordingMicIcon2() {
    Card(
        modifier = Modifier
            .size(64.dp),
        elevation = 2.dp,
        backgroundColor = Color.LightGray,
        shape = CircleShape,
    ) {
        /*Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_mic),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp),
            tint = colorResource(id = R.color.stream_compose_accent_blue),
        )*/
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
        RecordingLockIcon()
    } else {
        RecordingLockedIcon()
    }
}

@Composable
private fun RecordingLockIcon() {
    Icon(
        painter = painterResource(id = R.drawable.stream_compose_ic_mic_lock),
        contentDescription = null,
        modifier = Modifier
            .size(width = 54.dp, height = 92.dp)
            .padding(4.dp),
    )
}

@Composable
private fun RecordingLockedIcon() {
    Icon(
        painter = painterResource(id = R.drawable.stream_compose_ic_mic_locked),
        contentDescription = null,
        modifier = Modifier
            .size(52.dp)
            .padding(4.dp),
    )
}

@Preview(showBackground = true)
@Composable
internal fun DefaultRecordingSlideToCancelControlsPreview() {
    ChatPreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Yellow),
                contentAlignment = Alignment.Center,
            ) {
                RecordingSlideToCancelControls(
                    x = 0f,
                    y = 0f,
                    onLock = {},
                    onCancel = {}
                )

                // Popup(
                //     offset = IntOffset(0, 0),
                //     alignment = Alignment.CenterEnd,
                //
                // ) {
                //     Card(
                //         modifier = Modifier
                //             .size(64.dp),
                //         shape = CircleShape,
                //         elevation = 2.dp,
                //     ) {
                //         Icon(
                //             painter = painterResource(id = R.drawable.stream_compose_ic_mic),
                //             contentDescription = null,
                //             modifier = Modifier
                //                 .size(32.dp)
                //                 .padding(4.dp),
                //             tint = colorResource(id = R.color.stream_compose_accent_blue),
                //         )
                //     }
                // }

                Popup(
                    offset = IntOffset(0, 0),
                    alignment = Alignment.CenterEnd,) {
                    RecordingMicIcon2()
                }
            }
        }

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
    onDeleteClick: () -> Unit,
    onStopClick: () -> Unit,
    onCompleteClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
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
                //waveformData = emptyList(),
                modifier = Modifier.fillMaxWidth(),
                isWaveformVisible = false,
                waveformData = randomWaveformData,
                waveformProgress = 0.2f,
                isSlideToCancelVisible = true,
                slideToCancelProgress = 0.0f,
                areControlsVisible = true,
                onPlaybackClick = {},
                onDeleteClick = {},
                onStopClick = {},
                onCompleteClick = {})
        }
    }
}
