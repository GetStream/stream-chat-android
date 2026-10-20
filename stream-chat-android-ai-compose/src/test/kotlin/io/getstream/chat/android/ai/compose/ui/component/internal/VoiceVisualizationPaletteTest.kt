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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

internal class VoiceVisualizationPaletteTest {

    @Test
    fun `a color keeps its full hue, saturation and value`() {
        val hsv = VoiceVisualizationPalette.hsv(Color(red = 0f, green = 95f / 255f, blue = 1f))

        assertEquals(217.6f, hsv.hue * 360f, 0.1f)
        assertEquals(1f, hsv.saturation, 1e-4f)
        assertEquals(1f, hsv.value, 1e-4f)
    }

    // A Compose sRGB color keeps 8 bits a channel, so a color built from fractions comes back within
    // a step of 1/255.
    private val step = 1f / 255f

    @Test
    fun `a muted color keeps its saturation and value`() {
        val hsv = VoiceVisualizationPalette.hsv(Color.hsv(108f, 0.25f, 0.6f))

        assertEquals(0.3f, hsv.hue, 0.005f)
        assertEquals(0.25f, hsv.saturation, 0.01f)
        assertEquals(0.6f, hsv.value, step)
    }

    @Test
    fun `a gray has no saturation`() {
        val hsv = VoiceVisualizationPalette.hsv(Color(0.5f, 0.5f, 0.5f))

        assertEquals(0f, hsv.saturation, 1e-4f)
        assertEquals(0.5f, hsv.value, step)
    }

    @Test
    fun `a wide-gamut color reads as its sRGB equivalent`() {
        val p3 = Color(red = 0.5f, green = 0.25f, blue = 0f, colorSpace = ColorSpaces.DisplayP3)

        val hsv = VoiceVisualizationPalette.hsv(p3)

        assertEquals(VoiceVisualizationPalette.hsv(p3.convert(ColorSpaces.Srgb)), hsv)
        assertNotEquals(30f, hsv.hue * 360f, 0.5f)
    }

    @Test
    fun `only the first five colors are kept`() {
        assertEquals(5, VoiceVisualizationPalette.hsv(List(7) { Color.Red }).size)
    }
}
