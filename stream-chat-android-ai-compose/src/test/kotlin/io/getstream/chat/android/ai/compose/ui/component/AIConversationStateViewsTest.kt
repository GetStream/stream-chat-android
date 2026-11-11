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

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.compose.conversation.AIAgentActivity
import io.getstream.chat.android.ai.compose.conversation.AIConversationFeed
import io.getstream.chat.android.ai.compose.conversation.AIConversationState
import io.getstream.chat.android.ai.compose.conversation.AIConversationStatus
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.conversation.AIMessageRating
import io.getstream.chat.android.ai.compose.conversation.ChatMessage
import io.getstream.chat.android.ai.compose.conversation.FakeTransport
import io.getstream.chat.android.ai.compose.conversation.TransportError
import io.getstream.chat.android.ai.compose.conversation.rememberAIConversationState
import io.getstream.chat.android.ai.compose.conversation.reply
import io.getstream.chat.android.ai.compose.conversation.user
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AIConversationStateViewsTest {

    @get:Rule
    val rule = createComposeRule()

    private lateinit var state: AIConversationState<ChatMessage>

    private fun show(transport: FakeTransport) {
        rule.setContent {
            state = rememberAIConversationState(transport)
            Column {
                AIConversation(state = state)
                ChatComposer(state = state)
            }
        }
    }

    @Test
    fun `remembered state connects to the transport once and shows its messages`() {
        var connections = 0
        val transport = object : FakeTransport(listOf(user("u1", "Plan Lisbon"), reply("a1", "Day one in Alfama"))) {
            override fun connect(feed: AIConversationFeed<ChatMessage>) {
                connections += 1
                super.connect(feed)
            }
        }
        show(transport)

        rule.onNodeWithText("Plan Lisbon").assertIsDisplayed()
        rule.runOnIdle { assertEquals(1, connections) }
    }

    @Test
    fun `composer sends what the person writes and the conversation shows it at once`() {
        val transport = FakeTransport()
        show(transport)

        rule.onNode(hasSetTextAction()).performTextInput("Plan a weekend in Porto")
        rule.onNode(hasSetTextAction()).performImeAction()

        rule.runOnIdle {
            assertEquals("Plan a weekend in Porto", transport.lastRequest?.text)
            assertEquals("Plan a weekend in Porto", state.pendingMessage.value?.text)
        }
        rule.onNodeWithText("Plan a weekend in Porto").assertIsDisplayed()
    }

    @Test
    fun `conversation shows what the agent says it is doing`() {
        val transport = FakeTransport(listOf(user("u1", "Plan Lisbon")))
        show(transport)

        rule.runOnIdle {
            transport.update(AIAgentActivity(AIAgentActivity.State.CheckingExternalSources, text = "Searching the web"))
        }

        rule.onNodeWithText("Searching the web").assertIsDisplayed()
    }

    @Test
    fun `send of the composer's message carries its attachments`() {
        val transport = FakeTransport()
        show(transport)
        val photo = "content://media/alfama.jpg".toUri()

        rule.runOnIdle { state.send(MessageData(text = "", attachments = setOf(photo))) }

        rule.runOnIdle {
            assertEquals(listOf(photo), transport.lastRequest?.attachments)
            assertEquals(listOf(AIMessageAttachment.image(photo)), state.pendingMessage.value?.attachments)
        }
    }

    @Test
    fun `conversation offers to rate a finished reply and rates it through the state`() {
        val transport = FakeTransport(listOf(user("u1", "Plan Lisbon"), reply("a1", "Day one in Alfama")))
        show(transport)

        rule.onNodeWithContentDescription("Copy").assertIsDisplayed()
        rule.onNodeWithContentDescription("Good response").performClick()

        rule.runOnIdle {
            assertEquals(AIMessageRating.Positive, state.ratings.value["a1"])
            assertEquals(listOf("a1" to AIMessageRating.Positive), transport.ratings)
        }
    }

    @Test
    fun `conversation of messages alone offers no reply actions by default`() {
        rule.setContent {
            AIConversation(messages = listOf(user("u1", "Plan Lisbon"), reply("a1", "Day one in Alfama")))
        }

        rule.onNodeWithText("Day one in Alfama").assertIsDisplayed()
        rule.onNodeWithContentDescription("Good response").assertDoesNotExist()
    }

    @Test
    fun `remembered state made for a new transport disconnects the old one`() {
        val first = FakeTransport()
        val second = FakeTransport()
        var transport by mutableStateOf(first)
        rule.setContent { state = rememberAIConversationState(transport) }
        rule.runOnIdle { assertTrue(first.feed?.isConnected == true) }

        rule.runOnIdle { transport = second }

        rule.runOnIdle {
            assertFalse(first.feed?.isConnected == true)
            assertTrue(second.feed?.isConnected == true)
            assertEquals(1, first.connections)
            assertEquals(1, second.connections)
            first.update(user("u1", "Late"))
            assertEquals(emptyList<ChatMessage>(), state.messages.value)
        }
    }

    @Test
    fun `remembered state that leaves the composition disconnects its transport`() {
        val transport = FakeTransport()
        var shown by mutableStateOf(true)
        rule.setContent { if (shown) state = rememberAIConversationState(transport) }
        rule.runOnIdle { assertTrue(transport.feed?.isConnected == true) }

        rule.runOnIdle { shown = false }

        rule.runOnIdle { assertFalse(transport.feed?.isConnected == true) }
    }

    @Test
    fun `conversation shows the agent thinking while a message is on its way`() {
        val transport = FakeTransport()
        transport.holdsSends = true
        show(transport)

        rule.runOnIdle { state.send("Plan a weekend in Porto") }

        rule.onNodeWithText("Thinking", substring = true, useUnmergedTree = true).assertExists()
    }

    @Test
    fun `conversation stops thinking once the send fails`() {
        val transport = FakeTransport()
        transport.sendError = TransportError()
        show(transport)

        rule.runOnIdle { state.send("Plan a weekend in Porto") }

        rule.runOnIdle { assertEquals(AIConversationStatus.Error, state.status.value) }
        rule.onNodeWithText("Plan a weekend in Porto").assertIsDisplayed()
        rule.onNodeWithText("Thinking", substring = true, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `conversation shows what the agent is doing in place of thinking after a send`() {
        val transport = FakeTransport()
        transport.holdsSends = true
        show(transport)
        rule.runOnIdle { state.send("Plan a weekend in Porto") }

        rule.runOnIdle {
            transport.update(AIAgentActivity(AIAgentActivity.State.CheckingExternalSources, text = "Searching the web"))
        }

        rule.onNodeWithText("Searching the web").assertIsDisplayed()
        rule.onNodeWithText("Thinking", substring = true, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `a file the person sends is on its way as a file, and a picked photo as an image`() {
        val transport = FakeTransport()
        show(transport)
        val plan = "file:///storage/itinerary.pdf".toUri()
        val photo = "content://media/external/images/media/42".toUri()

        rule.runOnIdle { state.send("", attachments = listOf(plan, photo)) }

        rule.runOnIdle {
            val kinds = state.pendingMessage.value?.attachments?.map { it.kind }
            assertEquals(listOf(AIMessageAttachment.Kind.File, AIMessageAttachment.Kind.Image), kinds)
        }
    }
}
