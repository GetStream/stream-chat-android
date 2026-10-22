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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.parts.AIMessagePart
import io.getstream.chat.android.ai.compose.ui.PaparazziTest
import org.junit.Rule
import org.junit.Test

internal class AIMessageViewsTest : PaparazziTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        renderingMode = RenderingMode.SHRINK,
    )

    private val itinerary = AIMessageAttachment.file(
        uri = "https://example.com/files/lisbon-itinerary.pdf".toUri(),
        size = 2_400_000,
    )

    @Test
    fun `user message`() {
        val photo = AIMessageAttachment.image(sunset(), name = "Sunset.jpg")
        snapshot {
            AIUserMessage(
                text = "Plan a weekend in Lisbon around this photo and the itinerary.",
                attachments = listOf(photo, itinerary),
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    @Test
    fun `assistant message`() {
        val parts = AIMessagePart.partsFromJson(
            listOf(
                "ai_reasoning" to """
                    {"id":"r1","status":"completed","summary":"Read the itinerary first",
                    "preview":"The user wants a weekend plan.","duration_ms":3200}
                """.trimIndent(),
                "ai_tool_call" to """
                    {"id":"t1","name":"read_file","display_title":"Read the itinerary",
                    "status":"completed","summary":"2 days, 6 stops","duration_ms":800}
                """.trimIndent(),
            ),
        )
        snapshot {
            AIAssistantMessage(
                text = "Here's a **two-day plan** that keeps the sunset at Miradouro da Senhora do Monte.",
                parts = parts,
                attachments = listOf(itinerary),
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    // A small gradient that stands in for a photo.
    private fun sunset(): ImageBitmap {
        val image = ImageBitmap(width = 120, height = 120)
        val paint = Paint().apply {
            Brush.verticalGradient(listOf(Color(0xFFFFB347), Color(0xFFE0457B), Color(0xFF3B2C6B)))
                .applyTo(Size(120f, 120f), this, alpha = 1f)
        }
        Canvas(image).drawRect(Offset.Zero.x, Offset.Zero.y, 120f, 120f, paint)
        return image
    }
}
