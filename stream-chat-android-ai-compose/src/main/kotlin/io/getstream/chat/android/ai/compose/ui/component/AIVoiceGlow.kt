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

import android.os.Build
import android.os.PowerManager
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.getSystemService
import io.getstream.chat.android.ai.compose.ui.component.internal.VoiceGlowCanvas
import io.getstream.chat.android.ai.compose.ui.component.internal.VoiceGlowShader
import io.getstream.chat.android.ai.compose.ui.component.internal.isDark
import io.getstream.chat.android.ai.compose.ui.component.internal.rememberVoiceVisualization
import io.getstream.chat.android.ai.compose.ui.component.internal.voiceVisualization
import kotlin.math.hypot

/**
 * A voice agent as a soft, airy globe: color masses orbit inside it and swell with speech, and the
 * gaps between them let the background show through. Tapping the globe bounces it; drawn with the
 * shader, it also ripples from the touch and under a hovering mouse or stylus.
 *
 * It has no size of its own and draws in the middle of the space it is given, about half as wide
 * as its shorter side, so give it a square.
 *
 * @param state What the agent is doing, which sets how the glow moves.
 * @param modifier The [Modifier] to be applied to the glow.
 * @param colors One to five colors. Past five are ignored.
 * @param expressivity From 0 to 2: how far and how fast it moves. Colors never change with it.
 * @param audioLevel The level of whoever is talking, from 0 to 1, read once a frame. Feed it at
 * 15 Hz or more for the glow to move with the words; null animates speech on its own.
 * @param rendering How the glow is drawn. See [AIVoiceGlowRendering].
 */
@Composable
public fun AIVoiceGlow(
    state: AIVoiceAgentState,
    modifier: Modifier = Modifier,
    colors: List<Color> = AIVoiceVisualizationDefaults.colors(),
    expressivity: Float = 1f,
    audioLevel: (() -> Float?)? = null,
    rendering: AIVoiceGlowRendering = AIVoiceGlowRendering.Auto,
) {
    val visualization = rememberVoiceVisualization(state, colors, expressivity, audioLevel)
    val dark = if (MaterialTheme.colorScheme.surface.isDark()) 1f else 0f
    val context = LocalContext.current
    val saving = remember(context) { context.getSystemService<PowerManager>()?.isPowerSaveMode == true }
    val shader = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        usesShader(rendering, Build.VERSION.SDK_INT, saving)
    ) {
        remember { VoiceGlowShader() }
    } else {
        null
    }
    val canvas = remember { VoiceGlowCanvas() }
    Canvas(
        modifier
            .voiceVisualization(state, visualization) { x, y -> hypot(x, y) < GlobeReach }
            // Its own layer, so the Canvas glow's soft edge masks only the glow, not what is behind it.
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
    ) {
        visualization.frame.longValue
        if (shader != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            shader.draw(this, visualization.driver, dark)
        } else {
            canvas.draw(this, visualization.driver, dark)
        }
    }
}

/** Whether [rendering] draws with the shader on API level [sdk], [saving] battery or not. */
internal fun usesShader(rendering: AIVoiceGlowRendering, sdk: Int, saving: Boolean): Boolean {
    val canRun = sdk >= Build.VERSION_CODES.TIRAMISU
    return when (rendering) {
        AIVoiceGlowRendering.Auto -> canRun && !saving
        AIVoiceGlowRendering.Shader -> canRun
        AIVoiceGlowRendering.Canvas -> false
    }
}

@Preview(showBackground = true, widthDp = 240, heightDp = 240)
@Composable
private fun AIVoiceGlowPreview() {
    AIVoiceGlow(state = AIVoiceAgentState.Speaking, rendering = AIVoiceGlowRendering.Canvas)
}

/** How far from the centre a tap still lands on the globe, as a share of the shorter side. */
private const val GlobeReach = 0.32f
