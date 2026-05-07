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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.dragPointerInput
import kotlin.random.Random

/**
 * A slider that displays a waveform.
 *
 * @param modifier Modifier for styling.
 * @param waveformData The waveform data to display.
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
    visibleBarLimit: Int = 100,
    adjustBarWidthToLimit: Boolean = false,
    isThumbVisible: Boolean = true,
    onDragStart: (Float) -> Unit = {},
    onDrag: (Float) -> Unit = {},
    onDragStop: (Float) -> Unit = {},
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val currentProgress by rememberUpdatedState(progress)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .progressSemantics(value = progress),
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dragPointerInput(
                    enabled = isThumbVisible,
                    onDragStart = {
                        onDragStart(it.toHorizontalProgress(widthPx, isRtl))
                    },
                    onDrag = {
                        onDrag(it.toHorizontalProgress(widthPx, isRtl))
                    },
                    onDragStop = {
                        onDragStop(it?.toHorizontalProgress(widthPx, isRtl) ?: currentProgress)
                    },
                ),
        ) {
            // Draw the waveform
            WaveformTrack(
                modifier = Modifier.fillMaxSize(),
                waveformData = waveformData,
                visibleBarLimit = visibleBarLimit,
                adjustBarWidthToLimit = adjustBarWidthToLimit,
                progress = progress,
            )

            // Draw the thumb
            if (isThumbVisible) {
                WaveformHandle(
                    isPlaying = isPlaying,
                    progress = progress,
                    parentWidthPx = widthPx,
                )
            }
        }
    }
}

private val handleSize = 14.dp
private val handleBorderSize = 2.dp

@Composable
private fun BoxScope.WaveformHandle(
    isPlaying: Boolean,
    progress: Float,
    parentWidthPx: Float,
) {
    val colors = ChatTheme.colors
    val color = if (isPlaying) colors.chatWaveformBarPlaying else colors.accentNeutral
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
            .background(color, CircleShape),
    )
}

private const val BarSpacingRatio = 0.5f

@Composable
internal fun WaveformTrack(
    modifier: Modifier = Modifier,
    waveformData: List<Float> = emptyList(),
    visibleBarLimit: Int = 100,
    adjustBarWidthToLimit: Boolean = false,
    progress: Float = 0f,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val colors = ChatTheme.colors
    val finalProgress = progress.coerceIn(0f, 1f)
    val progressColor = colors.chatWaveformBarPlaying
    val trackColor = colors.chatWaveformBar

    val totalBars = when (adjustBarWidthToLimit) {
        true -> visibleBarLimit
        else -> minOf(waveformData.size, visibleBarLimit)
    }
    val visibleBars = minOf(visibleBarLimit, waveformData.size)
    var barCornerRadius by remember(totalBars) { mutableStateOf(CornerRadius.Zero) }
    Canvas(
        modifier = modifier.graphicsLayer { scaleX = if (isRtl) -1f else 1f },
    ) {
        if (totalBars <= 0) return@Canvas

        val canvasW = size.width
        val canvasH = size.height
        val spaceWidth = canvasW * BarSpacingRatio
        val barsWidth = canvasW - spaceWidth
        val totalSpaces = totalBars - 1
        val barWidth = barsWidth / totalBars
        val barSpacing = if (totalSpaces > 0) spaceWidth / totalSpaces else 0f

        val thresholdX = canvasW * finalProgress * visibleBars / totalBars
        val halfHeight = canvasH / 2
        if (barCornerRadius.x != barWidth || barCornerRadius.y != barWidth) {
            barCornerRadius = CornerRadius(barWidth, barWidth)
        }

        // Precompute constant values outside the loop
        val startIdx = maxOf(0, waveformData.size - totalBars)
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
                color = if (centerX < thresholdX) progressColor else trackColor,
                topLeft = topLeft,
                cornerRadius = barCornerRadius,
                size = barSize,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 250)
@Composable
private fun StaticWaveformSliderAtStartPreview() {
    ChatPreviewTheme { StaticWaveformSliderAtStart() }
}

@Preview(showBackground = true, widthDp = 250)
@Composable
private fun StaticWaveformSliderMidwayPreview() {
    ChatPreviewTheme { StaticWaveformSliderMidway() }
}

@Preview(showBackground = true, widthDp = 250)
@Composable
private fun StaticWaveformSliderPausedPreview() {
    ChatPreviewTheme { StaticWaveformSliderPaused() }
}

@Preview(showBackground = true, widthDp = 250)
@Composable
private fun StaticWaveformSliderWithoutThumbPreview() {
    ChatPreviewTheme { StaticWaveformSliderWithoutThumb() }
}

@Composable
internal fun StaticWaveformSliderAtStart() = StaticWaveformSliderSample(progress = 0f, isPlaying = true)

@Composable
internal fun StaticWaveformSliderMidway() = StaticWaveformSliderSample(progress = 0.5f, isPlaying = true)

@Composable
internal fun StaticWaveformSliderPaused() = StaticWaveformSliderSample(progress = 0.3f, isPlaying = false)

@Composable
internal fun StaticWaveformSliderWithoutThumb() =
    StaticWaveformSliderSample(progress = 0.7f, isPlaying = true, isThumbVisible = false)

@Composable
private fun StaticWaveformSliderSample(progress: Float, isPlaying: Boolean, isThumbVisible: Boolean = true) {
    val previewWaveform = remember {
        val rand = Random(50)
        List(50) { rand.nextFloat() }
    }

    StaticWaveformSlider(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        waveformData = previewWaveform,
        progress = progress,
        isPlaying = isPlaying,
        isThumbVisible = isThumbVisible,
    )
}

@Preview(showBackground = true, widthDp = 250)
@Composable
private fun FullWaveformTrackPreview() {
    ChatPreviewTheme { FullWaveformTrack() }
}

@Suppress("MagicNumber")
@Composable
internal fun FullWaveformTrack() {
    val waveform = List(100) { (it + 1) / 100f }
    WaveformTrack(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        waveformData = waveform,
        progress = 1f,
        adjustBarWidthToLimit = true,
        visibleBarLimit = 100,
    )
}

private fun Offset.toHorizontalProgress(base: Float, isRtl: Boolean): Float {
    val rawProgress = if (base > 0) (x / base).coerceIn(0f, 1f) else 0f
    return if (isRtl) 1f - rawProgress else rawProgress
}
