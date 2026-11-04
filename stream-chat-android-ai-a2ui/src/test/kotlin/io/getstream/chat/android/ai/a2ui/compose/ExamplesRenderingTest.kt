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

package io.getstream.chat.android.ai.a2ui.compose

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.getstream.chat.android.ai.a2ui.core.Fixtures
import io.getstream.chat.android.ai.a2ui.core.RendererMessage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

/** Every example published with the A2UI 1.0 basic catalog draws, with entrances, and reports nothing. */
@RunWith(ParameterizedRobolectricTestRunner::class)
internal class ExamplesRenderingTest(private val file: String) {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `example draws`() {
        val surfaces = SurfaceRule(rule).load("examples/$file")
        surfaces.show(entrances = true)

        surfaces.processor.surfaces.keys.forEach { rule.onNodeWithTag("a2ui:$it").assertExists() }
        assertEquals(emptyList<RendererMessage>(), surfaces.sent.filterIsInstance<RendererMessage.Error>())
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun examples(): List<Array<Any>> =
            Fixtures.text("examples/index.txt").lines().filter { it.endsWith(".json") }.map { arrayOf<Any>(it) }
    }
}
