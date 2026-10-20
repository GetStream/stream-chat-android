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

package io.getstream.chat.android.ai.compose.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.compose.ui.component.internal.VoiceVisualizationDriver
import io.getstream.chat.android.ai.compose.ui.component.internal.VoiceVisualizationState
import io.getstream.chat.android.ai.compose.ui.component.internal.rememberVoiceVisualization
import io.getstream.chat.android.ai.compose.ui.component.internal.voiceVisualization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AIVoiceVisualizationFrameTest {

    @get:Rule
    val compose = createComposeRule()

    // The visualization animates for as long as it is shown, so the clock is stepped by hand.
    @Before
    fun stopTheClock() {
        compose.mainClock.autoAdvance = false
    }

    @Test
    fun `the audio level is read once a frame`() {
        var reads = 0
        compose.setContent {
            AIVoiceWave(AIVoiceAgentState.Speaking, audioLevel = {
                reads++
                0.5f
            })
        }
        val before = reads

        repeat(FRAMES) { compose.mainClock.advanceTimeByFrame() }

        // The first frame only starts the clock.
        assertEquals(FRAMES - 1, reads - before)
    }

    @Test
    fun `a level that is not a number counts as no live audio`() {
        lateinit var visualization: VoiceVisualizationState
        compose.setContent {
            visualization = rememberVoiceVisualization(AIVoiceAgentState.Speaking, listOf(Color.Red), 1f) { Float.NaN }
        }

        repeat(FRAMES) { compose.mainClock.advanceTimeByFrame() }

        assertEquals(0f, visualization.driver.voice, 0f)
        assertTrue(visualization.driver.react.isFinite())
    }

    @Test
    fun `no colors fall back to the default palette`() {
        lateinit var visualization: VoiceVisualizationState
        compose.setContent {
            visualization = rememberVoiceVisualization(AIVoiceAgentState.Idle, emptyList(), 1f, null)
        }

        assertEquals(1f, visualization.driver.count, 0f)
    }

    @Test
    fun `a tap on the shape ripples from where it landed`() {
        lateinit var visualization: VoiceVisualizationState
        compose.setContent {
            visualization = rememberVoiceVisualization(AIVoiceAgentState.Listening, listOf(Color.Red), 1f, null)
            Box(Modifier.size(200.dp).testTag("view").voiceVisualization(AIVoiceAgentState.Listening, visualization) { _, y -> y > 0f })
        }

        compose.onNodeWithTag("view").performTouchInput { click(bottomCenter - Offset(0f, 1f)) }
        assertEquals(VoiceVisualizationDriver.NO_TAP, visualization.driver.tapAge)
        compose.onNodeWithTag("view").performTouchInput { click(Offset(width / 2f, height / 4f)) }

        assertEquals(0.25f, visualization.driver.tapY, 0.01f)
    }

    @Test
    fun `a hovering mouse ripples the shape until it moves off`() {
        lateinit var visualization: VoiceVisualizationState
        compose.setContent {
            visualization = rememberVoiceVisualization(AIVoiceAgentState.Listening, listOf(Color.Red), 1f, null)
            Box(Modifier.size(200.dp).testTag("view").voiceVisualization(AIVoiceAgentState.Listening, visualization) { _, _ -> true })
        }

        compose.onNodeWithTag("view").performMouseInput { moveTo(center) }
        repeat(FRAMES) { compose.mainClock.advanceTimeByFrame() }
        val hovering = visualization.driver.hoverAmount
        compose.onNodeWithTag("view").performMouseInput { moveTo(Offset(-20f, -20f)) }
        repeat(FRAMES) { compose.mainClock.advanceTimeByFrame() }

        assertTrue(hovering > 0f)
        assertTrue(visualization.driver.hoverAmount < hovering)
    }

    @Test
    fun `the wave draws a tap and a hover as they play out`() {
        compose.setContent {
            AIVoiceWave(AIVoiceAgentState.Listening, Modifier.size(200.dp).testTag("wave"))
        }

        compose.onNodeWithTag("wave").performTouchInput { click(center) }
        compose.onNodeWithTag("wave").performMouseInput { moveTo(center) }
        repeat(FRAMES) { compose.mainClock.advanceTimeByFrame() }

        compose.onNodeWithTag("wave").assertIsDisplayed()
    }

    private companion object {
        const val FRAMES = 5
    }
}
