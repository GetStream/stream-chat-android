/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.attachments.audio

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * A stateful composable that creates a "waveform" visualizer used to display mic input during recording.
 * You should poll the amplitude of the audio input and update the composable's [latestValue] input when each
 * new value is emitted. Null values are ignored.
 *
 * Since the composable is stateful, [restartKey] is used to remember the state. If you want to reset the composable's
 * state simply pass in a new key.
 *
 * @param restartKey Used to reset the state of the composable when a value different to the previous value is passed
 * in.
 * @param newValueKey Should be incremented upon every new [latestValue]. Used so that if the same [latestValue] is
 * posted twice in a valid way, we add a new bar for each emission.
 * @param latestValue Represents the latest value from the audio input. The composable will draw a new bar for each
 * latest value passed in.
 * @param modifier Modifier for styling.
 * @param maxInputValue The maximum amplitude of the input. A [latestValue] equaling [maxInputValue] will result
 * in the bar height equaling the height of the whole composable.
 * @param barMinHeight Minimum bar height, expressed as a percentage of the complete height of the composable.
 * @param barWidth The width of a single bar representing audio input.
 * @param barGap The gap between two bars.
 * @param barCornerRadius The corner radius if the bars.
 * @param barBrush The brush used to outline the bars.
 */
@Composable
public fun RunningWaveForm(
    restartKey: Any,
    newValueKey: Int,
    latestValue: Int?,
    modifier: Modifier = Modifier,
    maxInputValue: Int = 20,
    barMinHeight: Float = 0.1f,
    barWidth: Dp = 8.dp,
    barGap: Dp = 2.dp,
    barCornerRadius: CornerRadius = CornerRadius(barWidth.value / 2.5f, barWidth.value / 2.5f),
    barBrush: Brush = Brush.linearGradient(
        Pair(0f, ChatTheme.colors.primaryAccent),
        Pair(1f, ChatTheme.colors.primaryAccent)
    ),
) {
    val values = remember(restartKey) {
        mutableStateListOf<Int>()
    }

    var canvasWidth by remember { mutableStateOf(0f) }
    var canvasHeight by remember { mutableStateOf(0f) }

    val maxBars by remember(canvasWidth) {
        derivedStateOf { (canvasWidth / (barWidth.value + barGap.value)).toInt() }
    }

    LaunchedEffect(newValueKey) {
        latestValue?.let {
            if (values.count() <= maxBars) {
                values.add(latestValue)
            } else {
                values.removeFirst()
                values.add(latestValue)
            }
            println(values.toList())
        }
    }

    val minBarHeightFloat by remember(canvasHeight, barMinHeight) {
        derivedStateOf { canvasHeight * barMinHeight }
    }

    Canvas(modifier) {
        clipRect {
            canvasWidth = this.size.width
            canvasHeight = this.size.height

            canvasWidth = this.size.width
            canvasHeight = this.size.height

            values.forEachIndexed { index, value ->
                val barHeight = (size.height * (value.toFloat() / maxInputValue))
                    .coerceIn(
                        minimumValue = minBarHeightFloat,
                        maximumValue = this.size.height
                    )

                val xOffset = (barGap.value + barWidth.value) * index.toFloat()
                val yOffset = (this.size.height - barHeight) / 2

                this.drawRoundRect(
                    cornerRadius = barCornerRadius,
                    brush = barBrush,
                    topLeft = Offset(xOffset, yOffset),
                    size = Size(width = barWidth.value, height = barHeight)
                )
            }
        }
    }
}
