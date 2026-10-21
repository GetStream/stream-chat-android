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

import io.getstream.chat.android.ai.compose.ui.component.AIVoiceAgentState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class VoiceVisualizationDriverTest {

    private val blue = Hsv(218f / 360f, 1f, 1f)
    private val frame = 1f / 60f

    private fun input(
        state: AIVoiceAgentState,
        colors: List<Hsv> = listOf(blue),
        expressivity: Float = 1f,
        audioLevel: Float? = null,
        reduceMotion: Boolean = false,
    ) = VoiceVisualizationDriver.Input(state, colors, expressivity, audioLevel, reduceMotion)

    private fun VoiceVisualizationDriver.run(seconds: Float) {
        repeat((seconds / frame).toInt()) { advance(frame) }
    }

    @Test
    fun `it starts settled on the state`() {
        val driver = VoiceVisualizationDriver(input(AIVoiceAgentState.Speaking))

        assertEquals(1f, driver.react, 0f)
        assertEquals(0f, driver.load, 0f)
        assertEquals(0f, driver.flow, 0f)
        assertEquals(1f, driver.bright, 0f)
        assertEquals(0f, driver.time, 0f)
        assertEquals(1f, driver.count, 0f)
        assertEquals(1f, driver.scale, 0f)
        assertEquals(VoiceVisualizationDriver.NO_TAP, driver.tapAge, 0f)
        assertEquals(blue, driver.color(0))
        assertEquals(blue, driver.color(4))
    }

    @Test
    fun `changing the state eases toward its motion`() {
        val driver = VoiceVisualizationDriver(input(AIVoiceAgentState.Idle))
        driver.input = input(AIVoiceAgentState.Connecting)

        driver.advance(frame)
        assertEquals(0.08f, driver.load, 1e-4f)
        driver.run(2f)
        assertEquals(1f, driver.load, 1e-3f)
        assertEquals(0f, driver.react, 1e-3f)
    }

    @Test
    fun `easing does not depend on the frame rate`() {
        val at60 = VoiceVisualizationDriver(input(AIVoiceAgentState.Idle))
        val at120 = VoiceVisualizationDriver(input(AIVoiceAgentState.Idle))
        at60.input = input(AIVoiceAgentState.Speaking)
        at120.input = input(AIVoiceAgentState.Speaking)

        repeat(30) { at60.advance(1f / 60f) }
        repeat(60) { at120.advance(1f / 120f) }

        assertEquals(at60.react, at120.react, 1e-4f)
        assertEquals(at60.time, at120.time, 1e-2f)
    }

    @Test
    fun `a long frame is clamped`() {
        val driver = VoiceVisualizationDriver(input(AIVoiceAgentState.Idle))

        driver.advance(5f)

        assertEquals(0.05f * 0.4f, driver.time, 1e-5f)
    }

    @Test
    fun `the flow turns only while thinking`() {
        val driver = VoiceVisualizationDriver(input(AIVoiceAgentState.Speaking))
        driver.run(1f)
        assertEquals(0f, driver.flowSpin, 0f)

        driver.input = input(AIVoiceAgentState.Thinking)
        driver.run(1f)
        assertTrue(driver.flowSpin > 0f)
    }

    @Test
    fun `the colors keep turning in every state`() {
        val driver = VoiceVisualizationDriver(input(AIVoiceAgentState.Idle))

        driver.run(1f)

        assertTrue(driver.orbSpin > 0f)
    }

    @Test
    fun `expressivity scales amplitude and speed`() {
        val lively = VoiceVisualizationDriver(input(AIVoiceAgentState.Speaking, expressivity = 2f))
        val still = VoiceVisualizationDriver(input(AIVoiceAgentState.Speaking, expressivity = 0f))

        assertEquals(1.85f, lively.react, 1e-5f)
        assertEquals(0.15f, still.react, 1e-5f)
        lively.advance(0.05f)
        assertEquals(0.05f * 1.25f * 1.4f, lively.time, 1e-5f)
    }

    @Test
    fun `reduced motion slows time and ignores the pointer`() {
        val driver = VoiceVisualizationDriver(input(AIVoiceAgentState.Idle, reduceMotion = true))

        driver.tap(0.1f, 0.1f)
        driver.hover(0.1f, 0.1f)
        driver.advance(0.05f)

        assertEquals(0.05f * 0.07f * 0.4f, driver.time, 1e-6f)
        assertTrue(driver.tapAge > VoiceVisualizationDriver.NO_TAP)
        assertEquals(1f, driver.scale, 0f)
        assertEquals(0f, driver.hoverAmount, 0f)
    }

    @Test
    fun `a tap ripples from its point and bounces`() {
        val driver = VoiceVisualizationDriver(input(AIVoiceAgentState.Idle))

        driver.tap(0.1f, -0.2f)
        assertEquals(0.1f, driver.tapX, 0f)
        assertEquals(-0.2f, driver.tapY, 0f)
        assertEquals(0f, driver.tapAge, 0f)

        driver.advance(0.05f)
        assertEquals(0.05f, driver.tapAge, 1e-6f)
        assertTrue(driver.scale < 1f)
        driver.run(0.5f)
        assertEquals(1f, driver.scale, 0f)
    }

    @Test
    fun `hovering eases in and out`() {
        val driver = VoiceVisualizationDriver(input(AIVoiceAgentState.Idle))

        driver.hover(0.2f, 0.1f)
        driver.run(1f)
        assertEquals(1f, driver.hoverAmount, 1e-3f)
        assertEquals(0.2f, driver.hoverX, 0f)

        driver.hover(null, null)
        driver.run(1f)
        assertEquals(0f, driver.hoverAmount, 1e-3f)
        assertEquals(0.2f, driver.hoverX, 0f)
    }

    @Test
    fun `live audio lifts the motion to its level`() {
        val driver = VoiceVisualizationDriver(input(AIVoiceAgentState.Idle, audioLevel = 0.8f))

        driver.run(2f)
        assertEquals(0.8f, driver.mic, 1e-3f)
        assertEquals(1f, driver.voice, 1e-3f)
        assertEquals(0.8f, driver.react, 1e-3f)

        driver.input = input(AIVoiceAgentState.Idle)
        driver.run(2f)
        assertEquals(0f, driver.voice, 1e-3f)
        assertEquals(0.12f, driver.react, 1e-3f)
    }

    @Test
    fun `a louder level is caught up with within a word`() {
        val driver = VoiceVisualizationDriver(input(AIVoiceAgentState.Idle, audioLevel = 0f))
        driver.run(1f)

        driver.input = input(AIVoiceAgentState.Idle, audioLevel = 1f)
        driver.run(0.1f)
        assertTrue("mic was ${driver.mic}", driver.mic in 0.5f..0.8f)
        driver.run(0.3f)
        assertTrue("mic was ${driver.mic}", driver.mic > 0.95f)
    }

    @Test
    fun `a quieter level falls away over a pause, not a syllable`() {
        val driver = VoiceVisualizationDriver(input(AIVoiceAgentState.Idle, audioLevel = 1f))
        driver.run(1f)

        driver.input = input(AIVoiceAgentState.Idle, audioLevel = 0f)
        driver.run(0.1f)
        assertTrue("mic was ${driver.mic}", driver.mic > 0.7f)
        driver.run(0.9f)
        assertTrue("mic was ${driver.mic}", driver.mic < 0.1f)
    }

    @Test
    fun `a hue eases the short way round`() {
        val driver = VoiceVisualizationDriver(input(AIVoiceAgentState.Idle, colors = listOf(Hsv(0.95f, 1f, 1f))))
        driver.input = input(AIVoiceAgentState.Idle, colors = listOf(Hsv(0.05f, 1f, 1f)))

        driver.advance(frame)
        assertEquals(0.962f, driver.color(0).hue, 1e-4f)
        driver.run(2f)
        assertEquals(0.05f, driver.color(0).hue, 1e-3f)
    }

    @Test
    fun `empty slots repeat the first color and the count crossfades`() {
        val red = Hsv(0f, 1f, 1f)
        val driver = VoiceVisualizationDriver(input(AIVoiceAgentState.Idle, colors = listOf(blue, red)))
        assertEquals(red, driver.color(1))
        assertEquals(blue, driver.color(2))

        driver.input = input(AIVoiceAgentState.Idle, colors = listOf(blue, red, red))
        driver.advance(frame)
        assertEquals(2.12f, driver.count, 1e-4f)
    }

    @Test
    fun `easing moves the per-frame share at 60 Hz`() {
        assertEquals(0.08f, VoiceVisualizationDriver.ease(0.08f, 1f / 60f), 1e-5f)
        assertEquals(0f, VoiceVisualizationDriver.ease(0.08f, 0f), 0f)
    }
}
