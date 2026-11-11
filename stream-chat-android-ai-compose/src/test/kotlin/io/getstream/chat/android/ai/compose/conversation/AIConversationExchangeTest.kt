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

package io.getstream.chat.android.ai.compose.conversation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class AIConversationExchangeTest {

    private val conversation = listOf(user("u1"), reply("a1"), user("u2"), reply("a2"))

    private fun exchange(
        request: AIConversationRequest<ChatMessage>,
        conversation: List<ChatMessage> = this.conversation,
    ) = AIConversationExchange(id = 1, request = request, conversation = conversation)

    private fun message(text: String = "Hi", messageId: String = "m1") =
        AIConversationRequest<ChatMessage>(kind = AIConversationRequestKind.Message, text = text, messageId = messageId)

    @Test
    fun `a message is on its way and replaces nothing`() {
        val exchange = exchange(message())

        assertEquals("m1", exchange.pending?.id)
        assertEquals("Hi", exchange.pending?.text)
        assertTrue(exchange.replaced.isEmpty())
    }

    @Test
    fun `a kind of its own is on its way like a message`() {
        val exchange = exchange(
            AIConversationRequest(kind = AIConversationRequestKind("ui_action"), text = "Picked Alfama", message = reply("a2")),
        )

        assertEquals("Picked Alfama", exchange.pending?.text)
        assertTrue(exchange.replaced.isEmpty())
    }

    @Test
    fun `regenerating replaces the reply and those after it`() {
        val exchange = exchange(
            AIConversationRequest(kind = AIConversationRequestKind.Regenerate, text = "", message = reply("a1")),
        )

        assertNull(exchange.pending)
        assertEquals(setOf("a1", "u2", "a2"), exchange.replaced.keys)
    }

    @Test
    fun `regenerating a reply not in the conversation replaces nothing`() {
        val exchange = exchange(
            AIConversationRequest(kind = AIConversationRequestKind.Regenerate, text = "", message = reply("gone")),
        )

        assertTrue(exchange.replaced.isEmpty())
    }

    @Test
    fun `the reply starts when a new reply shows something`() {
        val exchange = exchange(message())

        assertFalse(exchange.hasReplyStarted(conversation + user("u3")))
        assertFalse(exchange.hasReplyStarted(conversation + user("u3") + reply("a3", "", generating = true)))
        assertTrue(exchange.hasReplyStarted(conversation + user("u3") + reply("a3", "Sure", generating = true)))
        assertTrue(exchange.hasReplyStarted(conversation + user("u3") + reply("a3", "")))
    }

    @Test
    fun `the reply starts when the one written again is rewritten`() {
        val exchange = exchange(
            AIConversationRequest(kind = AIConversationRequestKind.Regenerate, text = "", message = reply("a2")),
        )

        assertFalse(exchange.hasReplyStarted(conversation))
        assertTrue(exchange.hasReplyStarted(conversation.dropLast(1) + reply("a2", "Another plan", generating = true)))
    }

    @Test
    fun `the reply starts when the one written again arrives finished and started anew`() {
        val exchange = exchange(
            AIConversationRequest(kind = AIConversationRequestKind.Regenerate, text = "", message = reply("a2")),
        )

        assertFalse(exchange.hasReplyStarted(conversation.dropLast(1) + reply("a2", "", generating = true)))
        assertTrue(exchange.hasReplyStarted(conversation.dropLast(1) + reply("a2", "")))
    }

    @Test
    fun `an answer that failed midway is not rewritten until it changes`() {
        val writing = listOf(user("u1"), reply("a1", "Day one", generating = true))
        val exchange = exchange(
            AIConversationRequest(kind = AIConversationRequestKind.Regenerate, text = "", message = writing[1]),
            writing,
        )

        assertNull(exchange.reply(writing))
        assertNull(exchange.reply(listOf(user("u1"), reply("a1", "Day one"))))
        assertNotNull(exchange.reply(listOf(user("u1"), reply("a1", "Day one, day two", generating = true))))
    }

    @Test
    fun `the message arrives with its id or as the person's first new message`() {
        val exchange = exchange(message())

        assertFalse(exchange.hasMessageArrived(conversation))
        assertTrue(exchange.hasMessageArrived(conversation + user("m1")))
        assertTrue(exchange.hasMessageArrived(conversation + user("u3")))
        // Older messages loaded above.
        assertFalse(exchange.hasMessageArrived(listOf(user("u0"), reply("a0")) + conversation))
        assertFalse(exchange.hasMessageArrived(conversation + reply("a3")))
    }

    @Test
    fun `retry sends the request again unless the agent couldn't answer a message it has`() {
        val exchange = exchange(message())
        exchange.state = AIConversationExchange.State.Failed

        // The same message, which the transport can tell.
        assertEquals("m1", exchange.retryRequest(conversation + user("m1")).messageId)
        assertEquals(AIConversationRequestKind.Message, exchange.retryRequest(conversation + user("m1")).kind)

        exchange.state = AIConversationExchange.State.Unanswered

        // Its message never arrived.
        assertEquals(AIConversationRequestKind.Message, exchange.retryRequest(conversation).kind)
        val retried = exchange.retryRequest(conversation + user("m1"))
        assertEquals(AIConversationRequestKind.Regenerate, retried.kind)
        assertEquals("Hi", retried.text)
        // A reply to the latest message.
        assertNull(retried.message)
    }

    @Test
    fun `the person's message shows until its reply and afterwards only for a new message`() {
        val send = exchange(message())
        val regenerate = exchange(
            AIConversationRequest(kind = AIConversationRequestKind.Regenerate, text = "", message = reply("a2")),
        )
        assertNotNull(send.shownPending)

        for (state in listOf(
            AIConversationExchange.State.Stopped,
            AIConversationExchange.State.Failed,
            AIConversationExchange.State.Unanswered,
        )) {
            send.state = state
            regenerate.state = state
            assertNotNull(send.shownPending)
            assertNull(regenerate.shownPending)
            assertNotEquals(state == AIConversationExchange.State.Stopped, send.hasFailed)
        }
    }

    @Test
    fun `an old reply still being written is not the new one`() {
        val writing = listOf(user("u1"), reply("a1", "Day one", generating = true))
        val exchange = exchange(message("Make it three days"), writing)

        assertFalse(exchange.hasReplyStarted(listOf(user("u1"), reply("a1", "Day one, day two", generating = true))))
    }

    @Test
    fun `a request has a message id of its own by default`() {
        val first = AIConversationRequest<ChatMessage>(kind = AIConversationRequestKind.Message, text = "Hi")
        val second = AIConversationRequest<ChatMessage>(kind = AIConversationRequestKind.Message, text = "Hi")

        assertNotEquals(first.messageId, second.messageId)
    }
}
