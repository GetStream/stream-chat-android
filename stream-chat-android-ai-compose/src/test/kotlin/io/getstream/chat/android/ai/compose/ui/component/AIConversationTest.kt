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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import io.getstream.chat.android.ai.compose.ui.PaparazziTest
import org.junit.Rule
import org.junit.Test

internal class AIConversationTest : PaparazziTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        renderingMode = RenderingMode.SHRINK,
    )

    private data class Message(val id: String, val text: String, val isFromUser: Boolean)

    @Test
    fun `two turns`() {
        val messages = listOf(
            Message("q1", "What's the weather in Amsterdam?", isFromUser = true),
            Message("a1", "Mild and cloudy, around 14°C, with rain likely in the evening.", isFromUser = false),
            Message("q2", "Do I need an umbrella tonight?", isFromUser = true),
            Message("a2", "Yes. Rain is expected from about 7 pm, so bring one.", isFromUser = false),
        )
        snapshot {
            Box(modifier = Modifier.height(320.dp)) {
                AIConversation(
                    messages = messages,
                    key = Message::id,
                    isFromUser = Message::isFromUser,
                    footer = { Text("Sources: weather service", style = MaterialTheme.typography.labelSmall) },
                ) { message ->
                    Bubble(message)
                }
            }
        }
    }

    @Test
    fun `empty`() {
        snapshot {
            Box(modifier = Modifier.height(200.dp)) {
                AIConversation(
                    messages = emptyList<Message>(),
                    key = Message::id,
                    isFromUser = Message::isFromUser,
                    emptyContent = { Text("How can I help?") },
                ) { message ->
                    Bubble(message)
                }
            }
        }
    }

    @Composable
    private fun Bubble(message: Message) {
        if (message.isFromUser) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(message.text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
            }
        } else {
            Text(message.text)
        }
    }
}
