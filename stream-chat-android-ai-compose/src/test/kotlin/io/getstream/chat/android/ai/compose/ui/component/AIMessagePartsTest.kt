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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import io.getstream.chat.android.ai.compose.parts.AIMessagePart
import io.getstream.chat.android.ai.compose.parts.AIToolApproval
import io.getstream.chat.android.ai.compose.parts.AIToolCallPart
import io.getstream.chat.android.ai.compose.ui.PaparazziTest
import org.junit.Rule
import org.junit.Test

internal class AIMessagePartsTest : PaparazziTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        renderingMode = RenderingMode.SHRINK,
    )

    private fun call(status: AIToolCallPart.Status, summary: String? = null, durationMs: Int? = null) = AIToolCallPart(
        id = status.rawValue,
        name = "athena_device_location",
        displayTitle = "Checking your location",
        status = status,
        executor = AIToolCallPart.Executor.Client,
        summary = summary,
        durationMs = durationMs,
    )

    @Test
    fun `tool call statuses`() {
        snapshot {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AIToolCall(part = call(AIToolCallPart.Status.Running))
                AIToolCall(part = call(AIToolCallPart.Status.AwaitingApproval))
                AIToolCall(
                    part = call(AIToolCallPart.Status.Cancelled, "Location not shared")
                        .copy(approval = AIToolApproval(title = "Share your location?", decision = AIToolApproval.Decision.Declined)),
                )
                AIToolCall(part = call(AIToolCallPart.Status.AwaitingClient))
                AIToolCall(part = call(AIToolCallPart.Status.Completed, "Shared approximate location", 1_600))
                AIToolCall(part = call(AIToolCallPart.Status.Failed, durationMs = 400))
                AIToolCall(part = call(AIToolCallPart.Status.Cancelled))
                AIToolCall(part = call(AIToolCallPart.Status("paused_for_review")).copy(displayTitle = null))
            }
        }
    }

    @Test
    fun `reply steps`() {
        val parts = AIMessagePart.partsFromJson(
            listOf(
                "ai_reasoning" to """
                    {"id":"r1","status":"completed","summary":"Needs the location first",
                    "preview":"The user wants the weather.","duration_ms":3200}
                """.trimIndent(),
                "ai_tool_call" to """
                    {"id":"t1","name":"athena_device_location","display_title":"Checked your location",
                    "status":"completed","summary":"Shared approximate location","duration_ms":1600}
                """.trimIndent(),
                "ai_citation" to """{"id":"s1","url":"https://getstream.io"}""",
                "ai_tool_call" to """{"id":"t2","name":"web_search","display_title":"Searching the web","status":"running"}""",
            ),
        )
        snapshot {
            AIMessageParts(parts = parts, modifier = Modifier.padding(16.dp))
        }
    }
}
