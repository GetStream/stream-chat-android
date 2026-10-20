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

package io.getstream.chat.android.ai.compose.ui.component.internal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import kotlin.math.max
import kotlin.math.min

/** A color as hue (a fraction of a turn), saturation and value, each from 0 to 1. */
internal data class Hsv(val hue: Float, val saturation: Float, val value: Float)

/**
 * Turns colors into the HSV the renderers read, keeping their full hue, saturation and value so
 * muted and pastel brand colors render true.
 */
internal object VoiceVisualizationPalette {
    const val MAXIMUM_COUNT = 5

    fun hsv(colors: List<Color>): List<Hsv> = colors.take(MAXIMUM_COUNT).map(::hsv)

    @Suppress("MagicNumber")
    fun hsv(color: Color): Hsv {
        // In sRGB first, so a Display P3 or extended color reads with its true hue.
        val srgb = color.convert(ColorSpaces.Srgb)
        val r = srgb.red
        val g = srgb.green
        val b = srgb.blue
        val high = max(r, max(g, b))
        val low = min(r, min(g, b))
        val delta = high - low
        val hue = when {
            delta == 0f -> 0f
            high == r -> ((g - b) / delta).mod(6f)
            high == g -> (b - r) / delta + 2f
            else -> (r - g) / delta + 4f
        } / 6f
        return Hsv(
            hue = hue.mod(1f),
            saturation = if (high == 0f) 0f else (delta / high).coerceIn(0f, 1f),
            value = high.coerceIn(0f, 1f),
        )
    }
}
