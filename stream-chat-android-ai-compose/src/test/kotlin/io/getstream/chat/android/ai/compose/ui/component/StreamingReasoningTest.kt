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

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import io.getstream.chat.android.ai.compose.parts.AIReasoningPart
import io.getstream.chat.android.ai.compose.ui.PaparazziTest
import org.junit.Rule
import org.junit.Test

internal class StreamingReasoningTest : PaparazziTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        renderingMode = RenderingMode.SHRINK,
    )

    private val reasoning = """
        The user wants the weather where they are, so I need their location first.

        Once I have the **city**, I can look up the forecast and keep the answer short.
    """.trimIndent()

    @Test
    fun thinking() {
        snapshot {
            StreamingReasoning(text = reasoning, isThinking = true, modifier = Modifier.padding(16.dp))
        }
    }

    @Test
    fun folded() {
        snapshot {
            StreamingReasoning(
                part = AIReasoningPart(
                    id = "r1",
                    status = AIReasoningPart.Status.Completed,
                    summary = "Needs the user's location first",
                    preview = reasoning,
                    durationMs = 12_400,
                ),
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    @Test
    fun `opened when done`() {
        snapshot {
            StreamingReasoning(
                text = reasoning,
                isThinking = false,
                durationSeconds = 65.0,
                summary = "Needs the user's location first",
                footnote = "Only the summary and opening are kept with the reply.",
                initiallyExpanded = true,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
