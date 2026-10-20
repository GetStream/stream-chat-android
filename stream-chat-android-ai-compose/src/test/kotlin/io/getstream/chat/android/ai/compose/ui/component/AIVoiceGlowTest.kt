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

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import io.getstream.chat.android.ai.compose.ui.PaparazziTest
import org.junit.Rule
import org.junit.Test

internal class AIVoiceGlowTest : PaparazziTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        renderingMode = RenderingMode.SHRINK,
    )

    @Test
    fun speaking() {
        snapshot { glow(AIVoiceAgentState.Speaking) }
    }

    @Test
    fun thinking() {
        snapshot { glow(AIVoiceAgentState.Thinking) }
    }

    @Test
    fun connecting() {
        snapshot { glow(AIVoiceAgentState.Connecting) }
    }

    @Test
    fun shaderSpeaking() {
        snapshot { glow(AIVoiceAgentState.Speaking, AIVoiceGlowRendering.Shader) }
    }

    @Test
    fun shaderThinking() {
        snapshot { glow(AIVoiceAgentState.Thinking, AIVoiceGlowRendering.Shader) }
    }

    @Test
    fun shaderConnecting() {
        snapshot { glow(AIVoiceAgentState.Connecting, AIVoiceGlowRendering.Shader) }
    }

    @Test
    fun palette() {
        snapshot { glow(AIVoiceAgentState.Speaking, colors = Palette) }
    }

    @Test
    fun shaderPalette() {
        snapshot { glow(AIVoiceAgentState.Speaking, AIVoiceGlowRendering.Shader, Palette) }
    }

    @Composable
    private fun glow(
        state: AIVoiceAgentState,
        rendering: AIVoiceGlowRendering = AIVoiceGlowRendering.Canvas,
        colors: List<Color> = AIVoiceVisualizationDefaults.colors(),
    ) = AIVoiceGlow(state, Modifier.size(240.dp), colors = colors, rendering = rendering)
}
