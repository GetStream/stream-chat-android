package io.getstream.chat.android.compose.ui.messages.composer.internal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.input.pointer.pointerInput
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

@Composable
internal fun WaveformView(
    waveformData: List<Float>,
    modifier: Modifier = Modifier,
    waveformColor: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val spacing = canvasWidth / waveformData.size

        waveformData.forEachIndexed { index, amplitude ->
            val x = index * spacing
            val lineHeight = (amplitude * canvasHeight / 2)
            drawLine(
                color = waveformColor,
                start = Offset(x, canvasHeight / 2 - lineHeight),
                end = Offset(x, canvasHeight / 2 + lineHeight),
                strokeWidth = 4f,
            )
        }
    }
}

@Composable
internal fun WaveformView2(
    waveform: List<Float>,
    modifier: Modifier = Modifier,
    progress: Float = 0f,
    barLimit: Int = 40,
    isSliderVisible: Boolean = true,
    onSliderDragStart: (Float) -> Unit = {},
    onSliderDragStop: (Float) -> Unit = {}
) {
    var currentProgress by remember { mutableFloatStateOf(progress) }
    var isDragging by remember { mutableStateOf(false) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        onSliderDragStart(currentProgress)
                    },
                    onDragEnd = {
                        isDragging = false
                        onSliderDragStop(currentProgress)
                    }
                ) { change, _ ->
                    currentProgress = change.position.x / size.width
                }
            }
    ) {
        val barSpacingRatio = 0.3f
        val barWidthRatio = 1 - barSpacingRatio
        val viewportWidth = size.width
        val viewportHeight = size.height
        val barWidth = viewportWidth / barLimit * barWidthRatio
        val barSpacing = viewportWidth / barLimit * barSpacingRatio

        val paintPassed = Paint().apply {
            color = Color.Blue // Passed bar color
        }
        val paintFuture = Paint().apply {
            color = Color.Gray // Upcoming bar color
        }

        val centerY = viewportHeight / 2f
        val maxBarHeight = viewportHeight

        val visibleBars = waveform.takeLast(barLimit)

        visibleBars.forEachIndexed { index, value ->
            val barHeight = maxOf(maxBarHeight * value, barWidth)
            val top = centerY - barHeight / 2f
            val left = (index * (barWidth + barSpacing)).toFloat()
            val right = left + barWidth
            val bottom = centerY + barHeight / 2f

            val passed = !isSliderVisible || left + barWidth / 2f < currentProgress * size.width
            drawRoundRect(
                color = if (passed) paintPassed.color else paintFuture.color,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f)
            )
        }

        if (isSliderVisible) {
            // Draw the slider
            val sliderX = currentProgress * size.width
            drawRoundRect(
                color = Color.Red, // Slider color
                topLeft = Offset(sliderX - barWidth / 2f, 0f),
                size = androidx.compose.ui.geometry.Size(barWidth, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f)
            )
        }
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


/*
@Preview(showBackground = true)
@Composable
internal fun RandomWaveformPreview() {
    val randomWaveformData = List(100) { Random.nextFloat() }
    WaveformView(
        waveformData = randomWaveformData,
        waveformColor = Color.Blue,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    )
}

@Preview(showBackground = true)
@Composable
internal fun RandomWaveformPreview2() {
    // Generating random waveform data
    val waveformData = remember { List(250) { kotlin.random.Random.nextFloat() } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WaveformView2(
            waveform = waveformData,
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp),
            progress = 0.5f // Example progress
        )
    }
}*/
