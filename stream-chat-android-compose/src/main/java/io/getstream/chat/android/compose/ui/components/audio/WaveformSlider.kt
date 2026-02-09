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

package io.getstream.chat.android.compose.ui.components.audio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.WaveformSliderStyle
import io.getstream.chat.android.compose.ui.theme.WaveformTrackStyle
import io.getstream.chat.android.compose.ui.util.dragPointerInput
import kotlin.random.Random

/**
 * A slider that displays a waveform.
 *
 * @param modifier Modifier for styling.
 * @param waveformData The waveform data to display.
 * @param style The style for the waveform slider.
 * @param visibleBarLimit The number of bars to display at once.
 * @param adjustBarWidthToLimit Whether to adjust the bar width to fit the visible bar limit.
 * @param progress The current progress of the waveform.
 * @param isThumbVisible Whether to display the thumb.
 * @param onDragStart Callback when the user starts dragging the thumb.
 * @param onDrag Callback when the user drags the thumb.
 * @param onDragStop Callback when the user stops dragging the thumb.
 */
@Composable
public fun StaticWaveformSlider(
    waveformData: List<Float>,
    progress: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    style: WaveformSliderStyle = WaveformSliderStyle.defaultStyle(),
    visibleBarLimit: Int = 100,
    adjustBarWidthToLimit: Boolean = false,
    isThumbVisible: Boolean = true,
    onDragStart: (Float) -> Unit = {},
    onDrag: (Float) -> Unit = {},
    onDragStop: (Float) -> Unit = {},
) {
    val currentProgress by rememberUpdatedState(progress)
    var widthPx by remember { mutableFloatStateOf(0f) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                widthPx = size.width.toFloat()
            }
            .dragPointerInput(
                enabled = isThumbVisible,
                onDragStart = {
                    onDragStart(it.toHorizontalProgress(widthPx))
                },
                onDrag = {
                    onDrag(it.toHorizontalProgress(widthPx))
                },
                onDragStop = {
                    onDragStop(it?.toHorizontalProgress(widthPx) ?: currentProgress)
                },
            ),
    ) {
        // Draw the waveform
        WaveformTrack(
            modifier = Modifier.fillMaxSize(),
            waveformData = waveformData,
            style = style.trackerStyle,
            visibleBarLimit = visibleBarLimit,
            adjustBarWidthToLimit = adjustBarWidthToLimit,
            progress = progress,
        )

        // Draw the thumb
        if (isThumbVisible) {
            WaveformHandle(
                color = if (isPlaying) style.trackerStyle.passedColor else style.trackerStyle.futureColor,
                progress = progress,
                parentWidthPx = widthPx,
            )
        }
    }
}

private val handleSize = 14.dp
private val handleBorderSize = 2.dp

@Composable
private fun BoxScope.WaveformHandle(
    color: Color,
    progress: Float,
    parentWidthPx: Float,
) {
    val thumbOffset = when (parentWidthPx > 0) {
        true -> with(LocalDensity.current) {
            val parentWidth = parentWidthPx.toDp()
            val center = parentWidth * progress
            val left = center - (handleSize / 2)
            left.coerceIn(-handleBorderSize, parentWidth - handleSize)
        }

        else -> 0.dp
    }

    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .offset(x = thumbOffset)
            .size(handleSize)
            .border(handleBorderSize, ChatTheme.colors.borderCoreOnAccent, CircleShape)
            .background(color, CircleShape)
    )
}

@Composable
internal fun WaveformTrack(
    modifier: Modifier = Modifier,
    style: WaveformTrackStyle = WaveformTrackStyle.defaultStyle(),
    waveformData: List<Float> = emptyList(),
    visibleBarLimit: Int = 100,
    adjustBarWidthToLimit: Boolean = false,
    progress: Float = 0f,
) {
    // Ensure the spacing ratio is clamped between 0 and 1 (100% spacing would mean no bars)
    val finalSpacingRatio = style.barSpacingRatio.coerceIn(0f, 1f)
    val finalProgress = progress.coerceIn(0f, 1f)

    val totalBars = when (adjustBarWidthToLimit) {
        true -> visibleBarLimit
        else -> when (waveformData.size > visibleBarLimit) {
            true -> visibleBarLimit
            else -> waveformData.size
        }
    }
    val visibleBars = minOf(visibleBarLimit, waveformData.size)
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
        val startIdx = maxOf(0, waveformData.size - totalBars)
        // StreamLog.v("WaveformTrack") { "[onDraw] startIdx: $startIdx, totalBars: $totalBars, visibleBarLimit: $visibleBarLimit, waveformData.size: ${waveformData.size}" }
        val minBarHeight = 4.dp.toPx()
        for (index in startIdx until waveformData.size) {
            val amplitude = waveformData[index]
            // Calculate the position and size of each bar
            val barHeight = maxOf(amplitude * canvasH, minBarHeight)
            val topLeft = Offset(
                x = (index - startIdx) * (barWidth + barSpacing),
                y = halfHeight - barHeight / 2,
            )
            val barSize = Size(
                width = barWidth,
                height = barHeight,
            )
            val centerX = topLeft.x + barWidth / 2

            // Draw the bar, color based on whether it is before or after the progress threshold
            drawRoundRect(
                color = if (centerX < thresholdX) style.passedColor else style.futureColor,
                topLeft = topLeft,
                cornerRadius = barCornerRadius,
                size = barSize,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun WaveformSeekBarPreview() {
    val rand = Random(50)
    val waveform = List(50) { rand.nextFloat() }

    ChatPreviewTheme {
        Box(
            modifier = Modifier
                .width(250.dp)
                .height(60.dp)
                .background(Color.Cyan),
            contentAlignment = Alignment.Center,
        ) {
            StaticWaveformSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                waveformData = waveform,
                progress = 0.0f,
                isPlaying = true,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun WaveformTrackPreview() {
    val waveform = mutableListOf<Float>()
    val barCount = 100
    for (i in 0 until barCount) {
        waveform.add((i + 1) / barCount.toFloat())
    }

    ChatPreviewTheme {
        Box(
            modifier = Modifier
                .width(250.dp)
                .height(80.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            WaveformTrack(
                modifier = Modifier
                    .background(Color.Red)
                    .fillMaxWidth()
                    .height(60.dp),
                waveformData = waveform,
                progress = 0f,
                adjustBarWidthToLimit = true,
                visibleBarLimit = 100,
            )
        }
    }
}

private fun Offset.toHorizontalProgress(base: Float): Float {
    return if (base > 0) (x / base).coerceIn(0f, 1f) else 0f
}
