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

package io.getstream.chat.android.compose.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private const val DEFAULT_TRACKER_HEIGHT_DP = 60
private const val DEFAULT_TRACKER_WIDTH = 4

@Deprecated(
    message = "This component is deprecated, not used anywhere, and will be removed in the future. " +
        "Contact the support team for more information.",
    level = DeprecationLevel.WARNING,
)
@Composable
public fun AudioWaveVSeekbar(
    waveBars: List<Float>,
    modifier: Modifier = Modifier,
    colorLeft: Color = Color.Blue,
    colorRight: Color = Color.Gray,
    trackerDraw: DrawScope.(Float, Size) -> Unit = { progressWidth, trackerSize ->
        drawRoundRect(
            color = Color.Red,
            topLeft = Offset(
                java.lang.Float.min(
                    java.lang.Float.max(progressWidth - trackerSize.width / 2, 0F),
                    size.width - trackerSize.width,
                ),
                0F,
            ),
            size = trackerSize,
            cornerRadius = CornerRadius(10F, 10F),
        )
    },
) {
    var progress by remember { mutableStateOf(0F) }
    var isDragging by remember { mutableStateOf(false) }

    var seekWidth: Float? = null
    val barSpacing = 0.4

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = {
                    isDragging = true

                    progress = it.x * 100F / (seekWidth ?: 10F)
                },
                onHorizontalDrag = { change, _ ->
                    progress = change.position.x * 100F / (seekWidth ?: 10F)
                },
                onDragCancel = {
                    isDragging = false
                },
                onDragEnd = {
                    isDragging = false
                },
            )
        },
    ) {
        seekWidth = size.width

        val totalSpaceWidth = size.width * barSpacing
        val totalBarWidth = size.width - totalSpaceWidth

        val barWidth = totalBarWidth / waveBars.size
        val spaceWidth = totalSpaceWidth / waveBars.size
        val progressToWidth = progress / 100F * size.width
        val trackerHeight = DEFAULT_TRACKER_HEIGHT_DP

        val trackerWidth = DEFAULT_TRACKER_WIDTH.dp.toPx()

        repeat(waveBars.size) { i ->
            val barHeight = java.lang.Float.max(size.height * waveBars[i], 3.dp.toPx())
            val left = (barWidth + spaceWidth) * i
            val right = left + barWidth
            val top = (size.height - barHeight) / 2
            val bottom = top + barHeight

            val color = if (progressToWidth > left) colorLeft else colorRight
            val barRect = Rect(left.toFloat(), top, right.toFloat(), bottom)

            drawRoundRect(
                color = color,
                topLeft = barRect.topLeft,
                size = barRect.size,
                cornerRadius = CornerRadius(50F, 50F),
            )
        }

        trackerDraw(this, progressToWidth, Size(trackerWidth.dp.toPx(), size.height))
    }
}

@Preview(showBackground = true)
@Composable
private fun AudioWaveVSeekbarPreview() {
    AudioWaveVSeekbar()
}

@Suppress("MagicNumber")
@Composable
internal fun AudioWaveVSeekbar() {
    AudioWaveVSeekbar(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth(),
        waveBars = (0..10).map {
            listOf(0.5f, 0.8f, 0.3f, 0.6f, 0.4f, 0.7f, 0.2f, 0.9f, 0.1f)
        }.flatten(),
    )
}
