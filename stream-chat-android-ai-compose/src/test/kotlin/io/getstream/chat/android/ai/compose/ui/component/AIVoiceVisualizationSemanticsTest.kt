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

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AIVoiceVisualizationSemanticsTest {

    @get:Rule
    val compose = createComposeRule()

    // The visualization animates for as long as it is shown, so the clock is stepped by hand.
    @Before
    fun stopTheClock() {
        compose.mainClock.autoAdvance = false
    }

    @Test
    fun `the wave says it is the voice agent and what it is doing`() {
        compose.setContent { AIVoiceWave(AIVoiceAgentState.Listening) }

        compose.onNodeWithContentDescription("Voice agent")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Listening"))
    }

    @Test
    fun `idle reads as ready`() {
        compose.setContent { AIVoiceWave(AIVoiceAgentState.Idle) }

        compose.onNodeWithContentDescription("Voice agent")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Ready"))
    }

    @Test
    fun `the glow says it is the voice agent and what it is doing`() {
        compose.setContent { AIVoiceGlow(AIVoiceAgentState.Speaking, rendering = AIVoiceGlowRendering.Canvas) }

        compose.onNodeWithContentDescription("Voice agent")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Speaking"))
    }
}
