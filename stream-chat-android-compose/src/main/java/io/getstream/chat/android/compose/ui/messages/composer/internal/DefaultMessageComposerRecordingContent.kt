package io.getstream.chat.android.compose.ui.messages.composer.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.audio.WaveformSlider
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import kotlin.random.Random

@Composable
internal fun DefaultMessageComposerRecordingContent(
    waveformData: List<Float>,
    progress: Float = 0f,
    isThumbVisible: Boolean = false,
    onPlaybackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStopClick: () -> Unit,
    onCompleteClick: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
    ) {
        // Creating guidelines
        val horizontalGuideline = createGuidelineFromTop(0.5f) // 50% from the top

        val (
            playbackButton, timerText, sliderText, waveformView, deleteButton, stopButton, completeButton
        ) = createRefs()

        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_mic),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
                .clickable { onPlaybackClick() }
                .focusable(true)
                .background(Color.Transparent)
                .constrainAs(playbackButton) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    bottom.linkTo(horizontalGuideline)
                },
            tint = colorResource(id = R.color.stream_compose_accent_red)
        )

        Text(
            text = "00:00", // Timer text, you can make it dynamic
            modifier = Modifier
                .constrainAs(timerText) {
                    top.linkTo(playbackButton.top)
                    bottom.linkTo(playbackButton.bottom)
                    start.linkTo(playbackButton.end)
                }
        )

        WaveformSlider(
            waveformData = waveformData,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp, start = 16.dp)
                .constrainAs(waveformView) {
                    top.linkTo(parent.top)
                    bottom.linkTo(horizontalGuideline)
                    start.linkTo(timerText.end)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                },
            visibleBarLimit = 100,
            adjustBarWidthToLimit = true,
            isThumbVisible = isThumbVisible,
            progress = progress,
        )

        Row(
            modifier = Modifier
                .padding(end = 96.dp)
                .constrainAs(sliderText) {
                    top.linkTo(playbackButton.top)
                    bottom.linkTo(playbackButton.bottom)
                    end.linkTo(parent.end)
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
        }

        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_delete),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
                .clickable { onDeleteClick() }
                .focusable(true)
                .background(Color.Transparent)
                .constrainAs(deleteButton) {
                    top.linkTo(horizontalGuideline)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                },
            tint = colorResource(id = R.color.stream_compose_accent_red)
        )

        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_stop_circle),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
                .clickable { onStopClick() }
                .focusable(true)
                .background(Color.Transparent)
                .constrainAs(stopButton) {
                    top.linkTo(horizontalGuideline)
                    start.linkTo(deleteButton.end)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(completeButton.start)
                },
            tint = colorResource(id = R.color.stream_compose_accent_red)
        )

        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_check_circle),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
                .clickable { onCompleteClick() }
                .focusable(true)
                .background(Color.Transparent)
                .constrainAs(completeButton) {
                    top.linkTo(horizontalGuideline)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                },
            tint = colorResource(id = R.color.stream_compose_accent_blue)
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun DefaultMessageComposerRecordingContentPreview() {
    val randomWaveformData = List(40) { Random.nextFloat() }
    ChatPreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
        ) {
            DefaultMessageComposerRecordingContent(
                //waveformData = emptyList(),
                waveformData = randomWaveformData,
                onPlaybackClick = {},
                onDeleteClick = {},
                onStopClick = {},
                onCompleteClick = {}
            )
        }
    }
}
