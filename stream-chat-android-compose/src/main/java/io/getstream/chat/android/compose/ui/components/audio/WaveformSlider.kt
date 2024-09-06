package io.getstream.chat.android.compose.ui.components.audio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.log.StreamLog
import kotlin.random.Random

private const val DEFAULT_TRACKER_WIDTH_DP = 7
private const val PRESSED_TRACKER_WIDTH_DP = 10

@Composable
public fun WaveformSlider(
    modifier: Modifier = Modifier,
    waveform: List<Float>,
    futureColor: Color = Color.LightGray,
    passedColor: Color = ChatTheme.colors.primaryAccent,
    visibleBarLimit: Int = 100,
    adjustBarWidthToLimit: Boolean = false,
    barSpacingRatio: Float = 0.2f,
    progress: Float,
    isThumbVisible: Boolean = true,
    onDragStart: () -> Unit = { StreamLog.w("WaveformSeekBar") { "[onDragStart] no args" } },
    onDragStop: (Float) -> Unit = { StreamLog.e("WaveformSeekBar") { "[onDragStop] progress: $it" } },
) {
    /*TODO
    StreamLog.v("WaveformSeekBar") {
        "[onDraw] progress: $progress"
    }*/
    var widthPx by remember { mutableFloatStateOf(0f) }
    var pressed by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(progress) }

    // Sync currentProgress when progress changes from parent
    LaunchedEffect(progress) {
        currentProgress = progress
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        StreamLog.v("WaveformSeekBar") { "[detectHorizontalDragGestures] end" }
                        onDragStop(currentProgress)
                        pressed = false
                    },
                    onDragCancel = {
                        StreamLog.v("WaveformSeekBar") { "[detectHorizontalDragGestures] cancel" }
                        onDragStop(currentProgress)
                        pressed = false
                    }
                ) { change, dragAmount ->
                    change.consume()

                    /*StreamLog.v("WaveformSeekBar") {
                        "[detectHorizontalDragGestures] width: $widthPx, dragAmount: $dragAmount, change: $change"
                    }*/
                    if (widthPx > 0) {
                        currentProgress = (change.position.x / widthPx).coerceIn(0f, 1f)

                        // val center = change.position.x.toDp()
                        // val left = center - (PRESSED_TRACKER_WIDTH_DP.dp / 2)
                        // thumbOffset = left.coerceIn(0.dp, width - DEFAULT_TRACKER_WIDTH_DP.dp)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        StreamLog.v("WaveformSeekBar") {
                            "[detectTapGestures] press: $it"
                        }
                        pressed = true
                        if (widthPx > 0) {
                            currentProgress = (it.x / widthPx).coerceIn(0f, 1f)

                            // val center = it.x.toDp()
                            // val left = center - (PRESSED_TRACKER_WIDTH_DP.dp / 2)
                            // thumbOffset = left.coerceIn(0.dp, width - PRESSED_TRACKER_WIDTH_DP.dp)
                            onDragStart()
                        }
                    },
                ) { offset ->

                    StreamLog.v("WaveformSeekBar") {
                        "[detectTapGestures] tap: $offset"
                    }
                    onDragStop(currentProgress)
                    pressed = false

                }
            }
            .onSizeChanged { size ->
                StreamLog.v("WaveformSeekBar") {
                    "[onSizeChanged] Size changed: $size"
                }
                widthPx = size.width.toFloat()
            }
    ) {
        // Draw the waveform
        WaveformTrack(
            modifier = Modifier.fillMaxSize(),
            waveform = waveform,
            futureColor = futureColor,
            passedColor = passedColor,
            visibleBarLimit = visibleBarLimit,
            adjustBarWidthToLimit = adjustBarWidthToLimit,
            barSpacingRatio = barSpacingRatio,
            progress = currentProgress,
        )

        // Draw the thumb
        if (isThumbVisible) {
            WaveformThumb(
                pressed = pressed,
                progress = currentProgress,
                parentWidthPx = widthPx,
            )
        }
    }
}

@Composable
private fun WaveformThumb(
    modifier: Modifier = Modifier,
    pressed: Boolean = false,
    progress: Float,
    parentWidthPx: Float,
) {

    val thumbWidth = when (pressed) {
        true -> PRESSED_TRACKER_WIDTH_DP
        else -> DEFAULT_TRACKER_WIDTH_DP
    }

    val thumbOffset = when (parentWidthPx > 0) {
        true -> with(LocalDensity.current) {
            val parentWidth = parentWidthPx.toDp()
            val center = parentWidth * progress
            val left = center - (thumbWidth.dp / 2)
            left.coerceIn(0.dp, parentWidth - thumbWidth.dp)
        }

        else -> 0.dp
    }

    Box(
        modifier = modifier
            .offset(thumbOffset)
            .fillMaxHeight()
            .width(thumbWidth.dp)
            .background(Color.White, RoundedCornerShape(5.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(5.dp))
    )
}

@Composable
internal fun WaveformTrack(
    modifier: Modifier = Modifier,
    passedColor: Color = ChatTheme.colors.primaryAccent,
    futureColor: Color = /*Color(0xFF7A7A7A)*/Color.LightGray,
    waveform: List<Float> = emptyList(),
    visibleBarLimit: Int = 100,
    adjustBarWidthToLimit: Boolean = false,
    barSpacingRatio: Float = 0.2f,
    progress: Float = 0f,
) {
    // TODO StreamLog.v("WaveformTrack") { "[onDraw] progress: $progress" }

    // Ensure the spacing ratio is clamped between 0 and 1 (100% spacing would mean no bars)
    val finalSpacingRatio = barSpacingRatio.coerceIn(0f, 1f)
    val finalProgress = progress.coerceIn(0f, 1f)

    val totalBars = when (adjustBarWidthToLimit) {
        true -> visibleBarLimit
        else -> when (waveform.size > visibleBarLimit ) {
            true -> visibleBarLimit
            else -> waveform.size
        }
    }
    val visibleBars = minOf(visibleBarLimit, waveform.size)
    var barCornerRadius by remember(totalBars) { mutableStateOf(CornerRadius.Zero) }
    Canvas(modifier = modifier) {
        val canvasW = size.width
        val canvasH = size.height
        val spaceWidth = canvasW * finalSpacingRatio
        val barsWidth = canvasW - spaceWidth
        val totalSpaces = totalBars - 1
        val barWidth = barsWidth / totalBars
        val barSpacing = spaceWidth / totalSpaces

        val thresholdX = canvasW * finalProgress * visibleBars / visibleBarLimit
        val halfHeight = canvasH / 2
        if (barCornerRadius.x != barWidth || barCornerRadius.y != barWidth) {
            barCornerRadius = CornerRadius(barWidth, barWidth)
        }

        // Precompute constant values outside the loop
        val startIdx = maxOf(0, totalBars - visibleBarLimit)
        for (index in startIdx until waveform.size) {
            val amplitude = waveform[index]
            // Calculate the position and size of each bar
            val barHeight = amplitude * canvasH
            val topLeft = Offset(
                x = index * (barWidth + barSpacing),
                y = halfHeight - barHeight / 2
            )
            val barSize = Size(
                width = barWidth,
                height = barHeight
            )
            val centerX = topLeft.x + barWidth / 2

            // Draw the bar, color based on whether it is before or after the progress threshold
            drawRoundRect(
                color = if (centerX < thresholdX) passedColor else futureColor,
                topLeft = topLeft,
                cornerRadius = barCornerRadius,
                size = barSize
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun WaveformSeekBarPreview() {
    val rand = Random(50)
    val waveform = mutableListOf<Float>()
    for (i in 0..50) {
        waveform.add(rand.nextFloat())
    }


    ChatPreviewTheme {
        Box(
            modifier = Modifier
                .width(250.dp)
                .height(60.dp)
                .background(Color.Cyan),
            contentAlignment = Alignment.Center
        ) {
            WaveformSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                waveform = waveform,
                progress = 0.0f,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun WaveformTrackPreview() {
    val rand = Random(50)
    val waveform = mutableListOf<Float>()
    for (i in 0 until 10) {
        waveform.add(rand.nextFloat())
    }


    ChatPreviewTheme {
        Box(
            modifier = Modifier
                .width(250.dp)
                .height(60.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            WaveformTrack(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                waveform = waveform,
                progress = 0f,
                adjustBarWidthToLimit = true,
                visibleBarLimit = 100,
                barSpacingRatio = 0.2f,
            )
        }
    }
}