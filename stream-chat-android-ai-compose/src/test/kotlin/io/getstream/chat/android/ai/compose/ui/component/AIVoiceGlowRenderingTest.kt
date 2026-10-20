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

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class AIVoiceGlowRenderingTest {

    @Test
    fun `auto draws the shader where it can run unless saving battery`() {
        assertTrue(usesShader(AIVoiceGlowRendering.Auto, sdk = 33, saving = false))
        assertFalse(usesShader(AIVoiceGlowRendering.Auto, sdk = 33, saving = true))
        assertFalse(usesShader(AIVoiceGlowRendering.Auto, sdk = 32, saving = false))
    }

    @Test
    fun `shader draws the shader wherever it can run, saving or not`() {
        assertTrue(usesShader(AIVoiceGlowRendering.Shader, sdk = 33, saving = true))
        assertFalse(usesShader(AIVoiceGlowRendering.Shader, sdk = 32, saving = false))
    }

    @Test
    fun `canvas never draws the shader`() {
        assertFalse(usesShader(AIVoiceGlowRendering.Canvas, sdk = 36, saving = false))
    }
}
