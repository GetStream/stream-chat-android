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

package io.getstream.chat.android.ai.compose.generativeui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import io.getstream.chat.android.ai.compose.conversation.AIMessage
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import io.getstream.chat.android.ai.compose.conversation.AIReplyActions
import io.getstream.chat.android.ai.compose.ui.PaparazziTest
import io.getstream.chat.android.ai.compose.ui.component.AIConversation
import io.getstream.chat.android.ai.compose.ui.component.AIConversationReplyActionsParams
import io.getstream.chat.android.ai.compose.ui.component.AIReplyActionsRow
import io.getstream.chat.android.ai.compose.ui.component.ChatAiComponentFactory
import io.getstream.chat.android.ai.compose.ui.component.CompoundChatAiComponentFactory
import org.junit.Rule
import org.junit.Test

internal class AIUIConversationSnapshotTest : PaparazziTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        renderingMode = RenderingMode.SHRINK,
    )

    private data class Message(
        override val id: String,
        override val role: AIMessageRole,
        override val text: String,
        override val isGenerating: Boolean = false,
        override val uiDocument: AIUIDocument? = null,
    ) : AIMessage

    // Shows a document's lines as a card: the first line its title, the rest its body.
    private object CardRenderer : AIUIRenderer {
        override val format = AIUIFormat("cards")

        override fun makeEngine(host: AIUIHost): AIUIEngine = object : AIUIEngine {
            override fun content(document: AIUIDocument, messageId: String): (@Composable () -> Unit)? {
                val lines = document.text?.lines()?.filter { it.isNotBlank() }.orEmpty()
                if (lines.isEmpty()) return null
                return {
                    AIUIEntrance {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(lines.first(), style = MaterialTheme.typography.titleSmall)
                                lines.drop(1).forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                }
            }
        }
    }

    private val host = AIUIHost(listOf(CardRenderer)) {}

    private fun card(content: String, format: String = "cards") =
        AIUIDocument(format = AIUIFormat(format), content = content)

    // The default has no reply actions; a row shows where they sit under a reply's UI.
    private val factory = object : ChatAiComponentFactory {
        @Composable
        override fun AIConversationReplyActions(params: AIConversationReplyActionsParams) {
            AIReplyActionsRow(text = params.text, actions = AIReplyActions())
        }
    }

    private val question = Message("q1", AIMessageRole.User, "Plan a weekend in Ohrid")

    private fun conversation(vararg replies: Message) {
        snapshot {
            CompoundChatAiComponentFactory(factory = { factory }) {
                Box(modifier = Modifier.height(320.dp)) {
                    AIConversation(messages = listOf(question, *replies), ui = host)
                }
            }
        }
    }

    @Test
    fun `reply showing UI in place of its text`() {
        conversation(
            Message(
                "a1",
                AIMessageRole.Assistant,
                "",
                uiDocument = card("Weekend in Ohrid\nSaturday: old town and the lake\nSunday: boat to St. Naum"),
            ),
        )
    }

    @Test
    fun `reply with text and UI shows the UI`() {
        conversation(
            Message(
                "a1",
                AIMessageRole.Assistant,
                "Here is a plan for your weekend in Ohrid.",
                uiDocument = card("Weekend in Ohrid\nSaturday: old town and the lake\nSunday: boat to St. Naum"),
            ),
        )
    }

    @Test
    fun `reply whose UI no renderer reads shows its text`() {
        conversation(
            Message(
                "a1",
                AIMessageRole.Assistant,
                "Spend Saturday in the old town and by the lake, and take the boat to St. Naum on Sunday.",
                uiDocument = card("Weekend in Ohrid", format = "acme"),
            ),
        )
    }
}
