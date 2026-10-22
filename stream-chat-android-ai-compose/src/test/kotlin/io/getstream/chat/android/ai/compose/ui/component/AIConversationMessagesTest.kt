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
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.compose.conversation.AIMessage
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import io.getstream.chat.android.ai.compose.conversation.AIPendingMessage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AIConversationMessagesTest {

    @get:Rule
    val rule = createComposeRule()

    private data class Message(
        override val id: String,
        override val role: AIMessageRole,
        override val text: String,
        override val isGenerating: Boolean = false,
    ) : AIMessage

    private var messages by mutableStateOf(emptyList<AIMessage>())
    private var pending by mutableStateOf<AIPendingMessage?>(null)

    private fun setContent() {
        rule.setContent {
            Box(modifier = Modifier.size(width = 300.dp, height = 600.dp)) {
                AIConversation(messages = messages, pendingMessage = pending)
            }
        }
    }

    @Test
    fun `a message on its way shows at once, with the agent thinking, until its copy takes over`() {
        messages = listOf(Message("m1", AIMessageRole.User, "Hi"), Message("m2", AIMessageRole.Assistant, "Hello"))
        pending = AIPendingMessage(text = "What is the weather?")
        setContent()

        rule.onNodeWithText("What is the weather?").assertExists()
        rule.onNodeWithText("Thinking", substring = true, useUnmergedTree = true).assertExists()

        messages = messages + Message("m3", AIMessageRole.User, "What is the weather?")
        rule.waitForIdle()

        rule.onAllNodesWithText("What is the weather?").assertCountEquals(1)
        rule.onNodeWithText("Thinking", substring = true, useUnmergedTree = true).assertExists()

        pending = null
        messages = messages + Message("m4", AIMessageRole.Assistant, "Sunny", isGenerating = true)
        rule.waitForIdle()

        rule.onAllNodesWithText("What is the weather?").assertCountEquals(1)
        rule.onNodeWithText("Thinking", substring = true, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `a reply that shows nothing yet is left out while the agent thinks`() {
        messages = listOf(
            Message("m1", AIMessageRole.User, "Hi"),
            Message("m2", AIMessageRole.Assistant, "", isGenerating = true),
        )
        setContent()

        rule.onNodeWithText("Thinking", substring = true, useUnmergedTree = true).assertExists()
    }
}
