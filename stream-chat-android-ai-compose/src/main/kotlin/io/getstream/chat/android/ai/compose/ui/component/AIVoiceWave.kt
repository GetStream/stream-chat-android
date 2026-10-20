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

@file:Suppress("MagicNumber")

package io.getstream.chat.android.ai.compose.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.ai.compose.ui.component.internal.VoiceVisualizationDriver
import io.getstream.chat.android.ai.compose.ui.component.internal.VoiceVisualizationMath.mix
import io.getstream.chat.android.ai.compose.ui.component.internal.VoiceVisualizationMath.saturate
import io.getstream.chat.android.ai.compose.ui.component.internal.VoiceVisualizationMath.smoothstep
import io.getstream.chat.android.ai.compose.ui.component.internal.VoiceVisualizationMath.speechEnv
import io.getstream.chat.android.ai.compose.ui.component.internal.VoiceVisualizationMath.vivid
import io.getstream.chat.android.ai.compose.ui.component.internal.isDark
import io.getstream.chat.android.ai.compose.ui.component.internal.rememberVoiceVisualization
import io.getstream.chat.android.ai.compose.ui.component.internal.voiceVisualization
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * A voice agent as a single string, flat at its ends, carrying a wave packet that erupts with
 * speech while its colors flow along it. Tapping the string ripples it from the touch, and so does
 * a hovering mouse or stylus.
 *
 * It has no size of its own and draws in the middle of the space it is given, about 80% as wide as
 * its shorter side, so give it a square or a wide box.
 *
 * @param state What the agent is doing, which sets how the wave moves.
 * @param modifier The [Modifier] to be applied to the wave.
 * @param colors One to five colors, flowing along the string in bands. Past five are ignored.
 * @param expressivity From 0 to 2: how far and how fast it moves. Colors never change with it.
 * @param audioLevel The level of whoever is talking, from 0 to 1, read once a frame. Feed it at
 * 15 Hz or more for the wave to move with the words; null animates speech on its own.
 */
@Composable
public fun AIVoiceWave(
    state: AIVoiceAgentState,
    modifier: Modifier = Modifier,
    colors: List<Color> = AIVoiceVisualizationDefaults.colors(),
    expressivity: Float = 1f,
    audioLevel: (() -> Float?)? = null,
) {
    val visualization = rememberVoiceVisualization(state, colors, expressivity, audioLevel)
    val dark = MaterialTheme.colorScheme.surface.isDark()
    val wave = remember { WaveDrawing() }
    Canvas(
        modifier.voiceVisualization(state, visualization) { x, y -> abs(x) < 0.5f && abs(y) < 0.14f },
    ) {
        visualization.frame.longValue
        wave.draw(this, visualization.driver, if (dark) 1f else 0f)
    }
}

/** What the wave reuses from one frame to the next. */
private class WaveDrawing {
    private val path = Path()
    private val band = Array(5) { FloatArray(3) }
    private val color = FloatArray(3)
    private val stops = MutableList(STOPS) { Color.Transparent }

    fun draw(scope: DrawScope, d: VoiceVisualizationDriver, dark: Float) = with(scope) {
        val m = min(size.width, size.height)
        val t = d.time
        val react = d.react
        val center = d.load * 0.30f * sin(t * 1.3f)
        val freq = 24f + 26f * react + 10f * d.flow
        val speak = smoothstep(0.6f, 1f, react)
        val drive = mix(speak * 0.26f * speechEnv(t), 0.30f * d.mic, d.voice)
        val reach = 0.018f + 0.022f * react + drive + 0.14f * d.flow + 0.10f * d.load
        val tapAlive = d.tapAge < 1.5f
        val hovered = d.hoverAmount > 0.001f

        path.reset()
        for (i in 0..POINTS) {
            val x = START + (END - START) * i / POINTS
            var y = exp(-sq((x - center) / 0.18f)) * reach * sin(x * freq - t * 5f)
            if (tapAlive) {
                y += exp(-sq((x - d.tapX) / 0.12f)) * sin((x - d.tapX) * 40f - d.tapAge * 24f) *
                    exp(-d.tapAge * 2.6f) * 0.05f
            }
            if (hovered) {
                y += exp(-sq((x - d.hoverX) / 0.12f)) * sin((x - d.hoverX) * 40f - t * 7f) * d.hoverAmount * 0.04f
            }
            val px = size.width / 2 + x * m * d.scale
            val py = size.height / 2 - y * m * d.scale
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }

        // Broad color bands drifting along the string, its ends fading out.
        for (i in 0..4) vivid(d.color(i), dark, band[i])
        for (s in 0 until STOPS) stops[s] = stopColor(d, START + (END - START) * s / (STOPS - 1f))
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = stops,
                startX = size.width / 2 + START * m,
                endX = size.width / 2 + END * m,
            ),
            style = Stroke(width = (0.006f * m).coerceAtLeast(1.5f), cap = StrokeCap.Round),
        )
    }

    /** The string's color at [x]: the bands weighed by how near their centres are, faded at the ends. */
    private fun stopColor(d: VoiceVisualizationDriver, x: Float): Color {
        val seg = 1f / d.count.coerceAtLeast(1f)
        val bandWidth = seg * 0.7f
        val gx = (x + 0.5f).coerceIn(0f, 1f)
        var total = 1e-4f
        color.fill(0f)
        for (i in 0..4) {
            val present = if (i == 0) 1f else (d.count - i).coerceIn(0f, 1f)
            val weight = exp(-sq((gx - bandCenter(i, seg, d.time)) / bandWidth)) * present
            for (j in 0..2) color[j] += band[i][j] * weight
            total += weight
        }
        for (j in 0..2) color[j] /= total
        saturate(color, 1.2f)
        saturate(color, d.sat)
        val alpha = smoothstep(0.40f, 0.24f, abs(x)) * d.bright
        return Color(color[0], color[1], color[2], alpha.coerceIn(0f, 1f))
    }

    private fun bandCenter(i: Int, seg: Float, t: Float) = when (i) {
        0 -> 0.5f * seg + 0.12f * sin(t * 0.13f)
        1 -> 1.5f * seg + 0.13f * sin(t * 0.11f + 2.1f)
        2 -> 2.5f * seg + 0.12f * sin(t * 0.17f + 4.2f)
        3 -> 3.5f * seg + 0.12f * sin(t * 0.15f + 1f)
        else -> 4.5f * seg + 0.12f * sin(t * 0.10f + 3.3f)
    }

    private fun sq(x: Float) = x * x

    private companion object {
        const val POINTS = 160
        const val STOPS = 13
        const val START = -0.42f
        const val END = 0.42f
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 160)
@Composable
private fun AIVoiceWavePreview() {
    AIVoiceWave(state = AIVoiceAgentState.Speaking)
}
