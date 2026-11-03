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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.compose.conversation.AIMessage
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import io.getstream.chat.android.ai.compose.ui.component.AIConversation
import io.getstream.chat.android.ai.compose.ui.component.AIConversationReplyActionsParams
import io.getstream.chat.android.ai.compose.ui.component.ChatAiComponentFactory
import io.getstream.chat.android.ai.compose.ui.component.CompoundChatAiComponentFactory
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AIUIConversationTest {

    @get:Rule
    val rule = createComposeRule()

    private data class Message(
        override val id: String,
        override val role: AIMessageRole,
        override val text: String,
        override val isGenerating: Boolean = false,
        override val uiDocument: AIUIDocument? = null,
    ) : AIMessage

    // Shows a document's text as the reply's UI, or nothing when the text is empty.
    private class CardRenderer(private val placed: List<String> = emptyList()) : AIUIRenderer {
        override val format = AIUIFormat("cards")

        override fun makeEngine(host: AIUIHost): AIUIEngine = object : AIUIEngine {
            override fun content(document: AIUIDocument, messageId: String): (@Composable () -> Unit)? {
                val text = document.text?.takeIf { it.isNotEmpty() } ?: return null
                return {
                    Column {
                        AIUIEntrance { Text("card: $text") }
                        placed.forEach { name ->
                            val component = host.component(name) ?: return@forEach
                            val uriHandler = LocalUriHandler.current
                            val context = AIUIComponentContext(messageId, document.isStreaming, host, uriHandler) {
                                    event, label, payload ->
                                host.send(AIUIAction(messageId, format, event, label, payload + ("component" to name)))
                            }
                            AIUIEntrance { component.Content(mapOf("title" to text), context) }
                        }
                    }
                }
            }
        }
    }

    private fun ui(document: String) = AIUIDocument(format = AIUIFormat("cards"), content = document)

    private var messages by mutableStateOf(emptyList<AIMessage>())

    private val factory = object : ChatAiComponentFactory {
        @Composable
        override fun AIConversationReplyActions(params: AIConversationReplyActionsParams) {
            Text("actions for ${params.message.id}")
        }
    }

    private fun setContent(host: AIUIHost?, uriHandler: UriHandler? = null) {
        rule.setContent {
            val content = @Composable {
                CompoundChatAiComponentFactory(factory = { factory }) {
                    Box(modifier = Modifier.size(width = 300.dp, height = 600.dp)) {
                        AIConversation(messages = messages, ui = host)
                    }
                }
            }
            if (uriHandler != null) {
                CompositionLocalProvider(LocalUriHandler provides uriHandler) { content() }
            } else {
                content()
            }
        }
    }

    @Test
    fun `a reply shows its UI in place of its text, with its actions once finished`() {
        messages = listOf(
            Message("q1", AIMessageRole.User, "Plan a weekend"),
            Message("a1", AIMessageRole.Assistant, "A weekend in Ohrid", uiDocument = ui("Ohrid")),
        )
        setContent(AIUIHost(listOf(CardRenderer())) {})
        rule.mainClock.advanceTimeBy(2_000)

        rule.onNodeWithText("card: Ohrid").assertExists()
        rule.onNodeWithText("A weekend in Ohrid").assertDoesNotExist()
        rule.onNodeWithText("actions for a1").assertExists()
    }

    @Test
    fun `a reply shows its text without a host`() {
        messages = listOf(
            Message("q1", AIMessageRole.User, "And Struga?"),
            Message("a1", AIMessageRole.Assistant, "Struga is close", uiDocument = ui("Struga")),
        )
        setContent(host = null)
        rule.mainClock.advanceTimeBy(2_000)

        rule.onNodeWithText("Struga is close").assertExists()
        rule.onNodeWithText("card: Struga").assertDoesNotExist()
    }

    @Test
    fun `a reply shows its text when its UI shows nothing, or no renderer reads it`() {
        messages = listOf(
            Message("q1", AIMessageRole.User, "Plan a weekend"),
            Message("a1", AIMessageRole.Assistant, "A weekend in Ohrid", uiDocument = ui("")),
            Message(
                "a2",
                AIMessageRole.Assistant,
                "Or Struga",
                uiDocument = AIUIDocument(AIUIFormat("acme"), content = "Struga"),
            ),
        )
        setContent(AIUIHost(listOf(CardRenderer())) {})
        rule.mainClock.advanceTimeBy(2_000)

        rule.onNodeWithText("A weekend in Ohrid").assertExists()
        rule.onNodeWithText("Or Struga").assertExists()
        rule.onNodeWithText("card: Struga").assertDoesNotExist()
    }

    @Test
    fun `a reply that has only UI so far is listed, and has no actions while it is written`() {
        messages = listOf(
            Message("q1", AIMessageRole.User, "Plan a weekend"),
            Message(
                "a1",
                AIMessageRole.Assistant,
                "",
                uiDocument = AIUIDocument(AIUIFormat("cards"), isStreaming = true, content = "Ohrid"),
            ),
        )
        setContent(AIUIHost(listOf(CardRenderer())) {})
        rule.mainClock.advanceTimeBy(2_000)

        rule.onNodeWithText("card: Ohrid").assertExists()
        rule.onNodeWithText("Thinking", substring = true, useUnmergedTree = true).assertDoesNotExist()
        rule.onNodeWithText("actions for a1").assertDoesNotExist()

        messages = listOf(messages[0], Message("a1", AIMessageRole.Assistant, "", uiDocument = ui("Ohrid")))
        rule.mainClock.advanceTimeBy(2_000)

        rule.onNodeWithText("actions for a1").assertExists()
    }

    @Test
    fun `an app's component tells the agent what the person did, and opens only links the host lets`() {
        val sent = mutableListOf<AIUIAction>()
        val opened = mutableListOf<String>()
        val placeCard = AIUIComponent(AIUIComponentDefinition("place_card", "A place.")) { arguments, context ->
            val title = arguments["title"] as String
            Column {
                Text(
                    "ask about $title",
                    modifier = Modifier.clickable { context.emit("ask_about", "Tell me more about $title") },
                )
                Text("open http", modifier = Modifier.clickable { context.openUrl("http://example.com") })
                Text("open https", modifier = Modifier.clickable { context.openUrl("https://example.com") })
            }
        }
        val host = AIUIHost(listOf(CardRenderer(placed = listOf("place_card"))), components = listOf(placeCard)) {
            sent += it
        }
        messages = listOf(
            Message("q1", AIMessageRole.User, "Plan a weekend"),
            Message("a1", AIMessageRole.Assistant, "A weekend in Ohrid", uiDocument = ui("Ohrid")),
        )
        setContent(
            host,
            uriHandler = object : UriHandler {
                override fun openUri(uri: String) {
                    opened += uri
                }
            },
        )
        rule.mainClock.advanceTimeBy(2_000)

        rule.onNodeWithText("ask about Ohrid").performClick()
        rule.onNodeWithText("open http").performClick()
        rule.onNodeWithText("open https").performClick()

        assertEquals(
            listOf(
                AIUIAction(
                    messageId = "a1",
                    format = AIUIFormat("cards"),
                    name = "ask_about",
                    label = "Tell me more about Ohrid",
                    payload = mapOf("component" to "place_card"),
                ),
            ),
            sent,
        )
        assertEquals(listOf("https://example.com"), opened)
    }

    @Test
    fun `a reply's UI shows on its own outside the conversation`() {
        val host = AIUIHost(listOf(CardRenderer())) {}
        rule.setContent {
            AIUIView(host = host, document = ui("Ohrid"), messageId = "a1")
        }

        rule.onNodeWithText("card: Ohrid").assertExists()
    }
}
