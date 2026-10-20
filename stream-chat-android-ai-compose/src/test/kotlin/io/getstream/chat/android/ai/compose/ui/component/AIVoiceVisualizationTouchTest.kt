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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AIVoiceVisualizationTouchTest {

    @get:Rule
    val compose = createComposeRule()

    // The visualization animates for as long as it is shown, so the clock is stepped by hand.
    @Before
    fun stopTheClock() {
        compose.mainClock.autoAdvance = false
    }

    @Test
    fun `a clickable on the wave gets a tap on the line`() {
        var clicks = 0
        compose.setContent {
            AIVoiceWave(AIVoiceAgentState.Listening, Modifier.size(200.dp).testTag("wave").clickable { clicks++ })
        }

        compose.onNodeWithTag("wave").performTouchInput { click(center) }

        assertEquals(1, clicks)
    }

    @Test
    fun `a clickable parent gets a tap beside the line`() {
        var clicks = 0
        compose.setContent {
            Box(Modifier.size(200.dp).testTag("parent").clickable { clicks++ }) {
                AIVoiceWave(AIVoiceAgentState.Listening, Modifier.fillMaxSize())
            }
        }

        compose.onNodeWithTag("parent").performTouchInput { click(Offset(10f, 10f)) }

        assertEquals(1, clicks)
    }
}
