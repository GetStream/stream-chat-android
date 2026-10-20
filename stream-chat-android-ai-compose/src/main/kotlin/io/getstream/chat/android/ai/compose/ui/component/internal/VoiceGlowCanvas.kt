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

package io.getstream.chat.android.ai.compose.ui.component.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import io.getstream.chat.android.ai.compose.ui.component.internal.VoiceVisualizationMath.mix
import io.getstream.chat.android.ai.compose.ui.component.internal.VoiceVisualizationMath.noise
import io.getstream.chat.android.ai.compose.ui.component.internal.VoiceVisualizationMath.saturate
import io.getstream.chat.android.ai.compose.ui.component.internal.VoiceVisualizationMath.smoothstep
import io.getstream.chat.android.ai.compose.ui.component.internal.VoiceVisualizationMath.speechEnv
import io.getstream.chat.android.ai.compose.ui.component.internal.VoiceVisualizationMath.vivid
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * The glow drawn with a handful of gradients: color masses orbiting inside a soft globe, a
 * highlight, a rim and a halo. Cheaper than the shader and drawn on every API level; it moves the
 * same but has none of the shader's noise-warped marbling or ripples.
 */
internal class VoiceGlowCanvas {
    private val base = FloatArray(3)
    private val k0 = FloatArray(3)
    private val colB = FloatArray(3)
    private val colC = FloatArray(3)
    private val colD = FloatArray(3)
    private val colE = FloatArray(3)
    private val halo = FloatArray(3)
    private val rim = FloatArray(3)
    private val tmp = FloatArray(3)
    private val tmp2 = FloatArray(3)
    private var radius = 0f
    private var energy = 0f

    fun draw(scope: DrawScope, d: VoiceVisualizationDriver, dark: Float) = with(scope) {
        val side = min(size.width, size.height)
        val o = center
        val t = d.time
        val react = d.react
        val speak = smoothstep(0.6f, 1f, react)
        val wobble = 0.55f + 0.25f * sin(t * 1.6f)
        val speech = mix(react * mix(wobble, speechEnv(t), speak), d.mic, d.voice)
        val speechSmooth = mix(speech, react * wobble, 0.5f * speak)
        val energy = 0.12f + 0.30f * react + 0.18f * d.flow + 0.28f * d.load + 0.06f * speechSmooth
        val r = 0.24f * side *
            (1f + speechSmooth * 0.20f + d.load * 0.05f * sin(t * 2f) + react * 0.04f * sin(t * 1.6f))
        val gain = d.bright * mix(1f, 0.45f + 0.35f * sin(t * 2f), d.load)
        radius = r
        this@VoiceGlowCanvas.energy = energy
        colors(d, dark)

        scale(d.scale, o) {
            drawCircle(base.color(0.15f * gain), radius = r * 1.06f, center = o)
            val blobAlpha = 0.60f * (0.75f + 0.25f * energy) * gain
            blob(d, 1, colB, blobAlpha * 0.8f)
            blob(d, 0, k0, blobAlpha)
            blob(d, 0, colC, blobAlpha * 0.5f)
            blob(d, 2, colD, blobAlpha * (d.count - 3f).coerceIn(0f, 1f))
            blob(d, 3, colE, blobAlpha * (d.count - 4f).coerceIn(0f, 1f))
            light(r, gain)
        }
        // The soft edge, outside the bounce's scale so it covers the whole area at any scale and
        // whatever spilled past the globe goes too.
        drawRect(
            Brush.radialGradient(
                0f to Color.Black,
                (0.62f / 1.06f) to Color.Black,
                1f to Color.Transparent,
                center = o,
                radius = r * 1.06f * d.scale,
            ),
            blendMode = BlendMode.DstIn,
        )
        scale(d.scale, o) { haloBehind(r, side, react, dark, gain) }
    }

    private fun colors(d: VoiceVisualizationDriver, dark: Float) {
        vivid(d.color(0), dark, tmp)
        tmp.copyInto(k0)
        saturate(k0, 1.7f)
        for (i in 0..2) base[i] = mix(tmp[i], 1f, 0.06f)
        saturate(base, 1.3f)
        for (i in 0..2) halo[i] = mix(mix(tmp[i], 1f, 0.75f), mix(tmp[i], 1f, 0.32f), dark)
        for (i in 0..2) rim[i] = mix(1f, k0[i], 0.6f)
        mixSlot(d, 1, dark, lighter = true, into = colB)
        mixSlot(d, 2, dark, lighter = false, into = colC)
        vivid(d.color(3), dark, colD)
        saturate(colD, 1.7f)
        vivid(d.color(4), dark, colE)
        saturate(colE, 1.7f)
    }

    /** Slot [index] fading in over a lighter or darker first color as the palette grows. */
    private fun mixSlot(d: VoiceVisualizationDriver, index: Int, dark: Float, lighter: Boolean, into: FloatArray) {
        for (i in 0..2) tmp2[i] = (tmp[i] * if (lighter) 1.18f else 0.85f).coerceIn(0f, 1f)
        saturate(tmp2, 1.3f)
        vivid(d.color(index), dark, into)
        saturate(into, 1.7f)
        val present = (d.count - index).coerceIn(0f, 1f)
        for (i in 0..2) into[i] = mix(tmp2[i], into[i], present)
    }

    private fun DrawScope.blob(d: VoiceVisualizationDriver, i: Int, color: FloatArray, alpha: Float) {
        if (alpha <= 0.002f) return
        val r = radius
        val t = d.time
        val angle = when (i) {
            0 -> d.orbSpin * 0.42f + 2f * noise(t * 0.12f, 0f)
            1 -> -d.orbSpin * 0.34f + 2f * noise(t * 0.10f, 5f) + 2f
            2 -> d.orbSpin * 0.30f + 2f * noise(t * 0.11f, 9f) + 1f
            else -> -d.orbSpin * 0.26f + 2f * noise(t * 0.09f, 14f) + 3.5f
        } + d.flowSpin * 0.30f
        val distance = 0.40f - 0.02f * i
        val reach = floatArrayOf(1.15f, 1.30f, 1.20f, 1.25f)[i]
        // A slow breath stands in for the shader's per-pixel warp.
        val blobRadius = reach * r * (1f + 0.12f * energy * noise(t * 0.5f + i, 33f))
        val at = Offset(center.x + distance * r * cos(angle), center.y - distance * r * sin(angle))
        drawCircle(
            Brush.radialGradient(
                0f to color.color(alpha),
                0.09f to color.color(alpha),
                0.55f to color.color(alpha * 0.5f),
                1f to color.color(0f),
                center = at,
                radius = blobRadius,
            ),
            radius = blobRadius,
            center = at,
        )
    }

    private fun DrawScope.light(r: Float, gain: Float) {
        val highlight = Offset(center.x - 0.35f * r, center.y - 0.45f * r)
        drawCircle(
            Brush.radialGradient(
                0f to Color.White.copy(alpha = 0.16f * gain),
                1f to Color.Transparent,
                center = highlight,
                radius = r,
            ),
            radius = r * 1.06f,
            center = center,
        )
        drawCircle(
            Brush.radialGradient(
                0.70f to rim.color(0f),
                0.95f to rim.color(0.10f * gain),
                1f to rim.color(0f),
                center = center,
                radius = r * 1.06f,
            ),
            radius = r * 1.06f,
            center = center,
        )
    }

    private fun DrawScope.haloBehind(r: Float, side: Float, react: Float, dark: Float, gain: Float) {
        val alpha = (mix(0.05f, 0.10f, dark) + 0.05f * react) * gain
        val outer = r * 0.8f + 0.25f * side
        val inner = r * 0.8f / outer
        drawCircle(
            Brush.radialGradient(
                0f to halo.color(alpha),
                inner to halo.color(alpha),
                (inner + 0.09f * side / outer) to halo.color(alpha * exp(-1f)),
                1f to halo.color(0f),
                center = center,
                radius = outer,
            ),
            radius = outer,
            center = center,
            blendMode = BlendMode.DstOver,
        )
    }

    private fun FloatArray.color(alpha: Float) = Color(this[0], this[1], this[2], alpha.coerceIn(0f, 1f))
}
