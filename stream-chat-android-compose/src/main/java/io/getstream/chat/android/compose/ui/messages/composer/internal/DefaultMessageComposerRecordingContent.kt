package io.getstream.chat.android.compose.ui.messages.composer.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
private fun RecordingSlideToCancelIndicator(
    progress: Float = 0f,
) {
    var stcWidth by remember { mutableIntStateOf(0) }
    Row(
        modifier = Modifier
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
        Icon(painter = painterResource(id = R.drawable.stream_compose_ic_delete),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
                .clickable { onDeleteClick() }
                .focusable(true)
                .background(Color.Transparent),
            tint = colorResource(id = R.color.stream_compose_accent_red))

        Spacer(modifier = Modifier.weight(1f))

        Icon(painter = painterResource(id = R.drawable.stream_compose_ic_stop_circle),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
                .clickable { onStopClick() }
                .focusable(true)
                .background(Color.Transparent),
            tint = colorResource(id = R.color.stream_compose_accent_red))

        Spacer(modifier = Modifier.weight(1f))

        Icon(painter = painterResource(id = R.drawable.stream_compose_ic_check_circle),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
                .clickable { onCompleteClick() }
                .focusable(true)
                .background(Color.Transparent),
            tint = colorResource(id = R.color.stream_compose_accent_blue))
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
