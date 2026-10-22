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

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.compose.conversation.AIMessage
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import io.getstream.chat.android.ai.compose.parts.AIMessagePart
import io.getstream.chat.android.ai.compose.parts.AIReasoningPart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ConversationFactorySlotsTest {

    @get:Rule
    val rule = createComposeRule()

    private val question = message("q1", AIMessageRole.User, "Plan a weekend in Lisbon")
    private val reply = message("a1", AIMessageRole.Assistant, "Here's a plan")
    private val itinerary = AIMessageAttachment.file("https://example.com/files/itinerary.pdf".toUri())

    @Test
    fun `the default user message shows the text and its attachments through the factory`() {
        val factory = RecordingAttachmentFactory()
        rule.setContent {
            CompositionLocalProvider(LocalChatAiComponentFactory provides factory) {
                with(LocalChatAiComponentFactory.current) {
                    AIConversationUserMessage(
                        AIConversationUserMessageParams(
                            message = question,
                            text = question.text,
                            attachments = listOf(itinerary),
                            modifier = Modifier.testTag("user"),
                        ),
                    )
                }
            }
        }

        rule.onNodeWithTag("user").assertExists()
        rule.onNodeWithText("Plan a weekend in Lisbon").assertExists()
        rule.onNodeWithText("user: itinerary.pdf").assertExists()
        rule.runOnIdle { assertEquals(setOf(AIMessageRole.User), factory.roles()) }
    }

    @Test
    fun `the default assistant message shows the answer and its attachments`() {
        rule.setContent {
            with(DefaultChatAiComponentFactory) {
                AIConversationAssistantMessage(
                    AIConversationAssistantMessageParams(
                        message = reply,
                        text = reply.text,
                        isGenerating = false,
                        parts = emptyList(),
                        attachments = listOf(itinerary),
                        reasoningText = { null },
                        approver = null,
                        modifier = Modifier.testTag("assistant"),
                    ),
                )
            }
        }

        rule.onNodeWithTag("assistant").assertExists()
        rule.onNodeWithText("Here's a plan").assertExists()
        rule.onNodeWithText("itinerary.pdf").assertExists()
    }

    @Test
    fun `the default attachment shows a file as a card with its name`() {
        rule.setContent {
            with(LocalChatAiComponentFactory.current) {
                AIConversationAttachment(
                    AIConversationAttachmentParams(
                        attachment = AIMessageAttachment.file("https://example.com/x".toUri(), name = "Notes.txt"),
                        role = AIMessageRole.Assistant,
                        modifier = Modifier.testTag("attachment"),
                    ),
                )
            }
        }

        rule.onNodeWithTag("attachment").assertExists()
        rule.onNodeWithText("Notes.txt").assertExists()
    }

    @Test
    fun `the default factory is the one the composition provides without setup`() {
        var current: ChatAiComponentFactory? = null
        rule.setContent { current = LocalChatAiComponentFactory.current }

        rule.runOnIdle { assertSame(DefaultChatAiComponentFactory, current) }
    }

    @Test
    fun `user message params hold what they're given and compare by value`() {
        val params = AIConversationUserMessageParams(message = null, text = "Hi", attachments = listOf(itinerary))

        assertEquals(null, params.message)
        assertEquals("Hi", params.text)
        assertEquals(listOf(itinerary), params.attachments)
        assertEquals(Modifier, params.modifier)
        assertEquals(params, params.copy())
        assertEquals(params.hashCode(), params.copy().hashCode())
        assertNotEquals(params, params.copy(message = question))
        assertEquals(question, params.copy(message = question).component1())
    }

    @Test
    fun `assistant message params hold what they're given and compare by value`() {
        val reasoning: (AIReasoningPart) -> String? = { "thinking" }
        val step = AIReasoningPart(id = "r1", status = AIReasoningPart.Status.Streaming)
        val parts = AIMessagePart.partsFromJson(listOf("ai_reasoning" to """{"id":"r1","status":"streaming"}"""))
        val params = AIConversationAssistantMessageParams(
            message = reply,
            text = reply.text,
            isGenerating = true,
            parts = parts,
            attachments = emptyList(),
            reasoningText = reasoning,
            approver = null,
        )

        assertSame(reply, params.message)
        assertEquals("Here's a plan", params.text)
        assertEquals(true, params.isGenerating)
        assertEquals(parts, params.parts)
        assertEquals(emptyList<AIMessageAttachment>(), params.attachments)
        assertEquals("thinking", params.reasoningText(step))
        assertEquals(null, params.approver)
        assertEquals(Modifier, params.modifier)
        assertEquals(params, params.copy())
        assertNotEquals(params, params.copy(isGenerating = false))
    }

    @Test
    fun `attachment params hold what they're given and compare by value`() {
        val params = AIConversationAttachmentParams(attachment = itinerary, role = AIMessageRole.User)

        assertEquals(itinerary, params.attachment)
        assertEquals(AIMessageRole.User, params.role)
        assertEquals(Modifier, params.modifier)
        assertEquals(params, params.copy())
        assertEquals(params.hashCode(), params.copy().hashCode())
        assertNotEquals(params, params.copy(role = AIMessageRole.Assistant))
        assertEquals(
            "AIConversationAttachmentParams(attachment=$itinerary, role=user, modifier=$Modifier)",
            params.toString(),
        )
    }

    private fun message(id: String, role: AIMessageRole, text: String): AIMessage = object : AIMessage {
        override val id = id
        override val role = role
        override val text = text
    }
}
