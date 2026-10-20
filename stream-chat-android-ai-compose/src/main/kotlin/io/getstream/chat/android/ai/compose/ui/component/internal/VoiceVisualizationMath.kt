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

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sin

/** The shader math the Canvas renderers run on the CPU, once per frame. */
internal object VoiceVisualizationMath {

    /** HSV, hue a fraction of a turn, into [out] as RGB. */
    fun hsvToRgb(hue: Float, saturation: Float, value: Float, out: FloatArray) {
        for (i in 0..2) {
            val k = when (i) { 0 -> 1f
                1 -> 2f / 3f
                else -> 1f / 3f }
            val p = abs(fract(hue + k) * 6f - 3f)
            out[i] = value * mix(1f, (p - 1f).coerceIn(0f, 1f), saturation)
        }
    }

    /** A palette color as is; dark mode lifts saturation a touch so it stays vivid on black. */
    fun vivid(color: Hsv, dark: Float, out: FloatArray) =
        hsvToRgb(color.hue, (color.saturation + dark * 0.06f).coerceIn(0f, 1f), color.value, out)

    /** Pushes [c] away from its luminance by [amount]; below 1 desaturates. */
    fun saturate(c: FloatArray, amount: Float) {
        val l = 0.299f * c[0] + 0.587f * c[1] + 0.114f * c[2]
        for (i in 0..2) c[i] = mix(l, c[i], amount).coerceIn(0f, 1f)
    }

    fun mix(a: Float, b: Float, t: Float) = a + (b - a) * t

    fun fract(x: Float) = x - floor(x)

    /** GLSL's smoothstep, evaluated the same with its edges either way round. */
    fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3 - 2 * t)
    }

    /** Smooth 1-D gradient noise from -1 to 1: the slow wobble the CPU needs, where the shader uses simplex. */
    fun noise(x: Float, seed: Float): Float {
        val i = floor(x)
        val f = x - i
        val u = f * f * (3 - 2 * f)
        return mix(gradient(i, seed) * f, gradient(i + 1, seed) * (f - 1), u) * 2f
    }

    private fun gradient(i: Float, seed: Float) = fract(sin(i * 127.1f + seed * 311.7f) * 43758.547f) * 2f - 1f

    /** How much the agent is talking, 0 to 1: a slow phrase gate times a faster syllable wobble. */
    fun speechEnv(t: Float): Float {
        val gate = smoothstep(-0.25f, 0.35f, noise(t * 0.6f, 21f))
        val syllable = 0.55f + 0.45f * noise(t * 3f, 5f)
        return (gate * syllable).coerceIn(0f, 1f)
    }
}
