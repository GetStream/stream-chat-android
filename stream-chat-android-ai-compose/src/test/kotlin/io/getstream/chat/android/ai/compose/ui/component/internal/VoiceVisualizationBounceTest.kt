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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class VoiceVisualizationBounceTest {

    @Test
    fun `the scale follows the keyframes`() {
        assertEquals(1f, VoiceVisualizationBounce.scaleAt(0f), 0f)
        assertEquals(0.965f, VoiceVisualizationBounce.scaleAt(0.15f), 1e-5f)
        assertEquals(0.93f, VoiceVisualizationBounce.scaleAt(0.3f), 1e-5f)
        assertEquals(1.03f, VoiceVisualizationBounce.scaleAt(0.62f), 1e-5f)
        assertEquals(1f, VoiceVisualizationBounce.scaleAt(1f), 1e-5f)
    }

    @Test
    fun `the timing matches the CSS ease-out`() {
        assertEquals(0f, VoiceVisualizationBounce.easeOut(0f), 1e-4f)
        assertEquals(0.684f, VoiceVisualizationBounce.easeOut(0.5f), 2e-3f)
        assertEquals(1f, VoiceVisualizationBounce.easeOut(1f), 1e-4f)
    }

    @Test
    fun `a bounce dips, overshoots and settles`() {
        val bounce = VoiceVisualizationBounce()
        assertEquals(1f, bounce.scale, 0f)

        bounce.start()
        val scales = List(21) { bounce.scale.also { bounce.advance(1f / 60f) } }

        assertEquals(1f, scales.first(), 0f)
        assertEquals(0.93f, scales.min(), 0.01f)
        assertEquals(1.03f, scales.max(), 0.01f)
        assertTrue(scales.indexOf(scales.min()) < scales.indexOf(scales.max()))
        assertEquals(1f, bounce.scale, 0f)
    }
}
