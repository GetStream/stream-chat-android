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

import io.getstream.chat.android.ai.compose.generativeui.AIUIAction
import io.getstream.chat.android.ai.compose.generativeui.AIUIFormat
import io.getstream.chat.android.ai.compose.ui.component.AIToolApprover
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class AIConversationStateTest {

    private fun TestScope.conversation(
        transport: AIConversationTransport<ChatMessage>,
        replyTimeout: Duration? = AIConversationState.DefaultReplyTimeout,
    ) = AIConversationState(transport, backgroundScope, replyTimeout = replyTimeout)

    private val AIConversationState<ChatMessage>.shown get() = messages.value.ids

    // Following the conversation

    @Test
    fun `connects to the transport and shows its messages`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)

        assertNotNull(transport.feed)
        assertEquals(listOf("u1", "a1"), state.shown)
        assertEquals(AIConversationStatus.Ready, state.status.value)
        assertNull(state.pendingMessage.value)
        assertNull(state.error.value)
        assertFalse(state.hasOlderMessages.value)
    }

    @Test
    fun `keeps the approver it is given`() = runTest {
        val approver = AIToolApprover(userId = "u_1", clientId = "android-1", scope = backgroundScope) { _, _ -> }
        val state = AIConversationState(FakeTransport(), backgroundScope, approver = approver)

        assertSame(approver, state.approver)
    }

    @Test
    fun `the feed updates the messages and whether older ones can load`() = runTest {
        val transport = FakeTransport()
        val state = conversation(transport)

        transport.update(user("u1"), reply("a1"))
        transport.feed?.update(hasOlderMessages = true)

        assertEquals(listOf("u1", "a1"), state.shown)
        assertTrue(state.hasOlderMessages.value)

        transport.feed?.update(hasOlderMessages = false)

        assertFalse(state.hasOlderMessages.value)
    }

    @Test
    fun `the feed goes nowhere once the state's scope is cancelled`() = runTest {
        val transport = FakeTransport()
        val scope = backgroundScope + Job()
        val state = AIConversationState(transport, scope)
        assertTrue(transport.feed?.isConnected == true)

        scope.cancel()
        transport.update(user("u1"))
        transport.update(AIAgentActivity(AIAgentActivity.State.Thinking))
        transport.feed?.update(hasOlderMessages = true)

        assertFalse(transport.feed?.isConnected == true)
        assertEquals(emptyList<String>(), state.shown)
        assertNull(state.activity.value)
        assertFalse(state.hasOlderMessages.value)
    }

    // Sending

    @Test
    fun `send shows the message at once and asks the transport`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)

        state.send("And the restaurants?")
        runCurrent()

        assertEquals(AIConversationStatus.Submitted, state.status.value)
        assertEquals("And the restaurants?", state.pendingMessage.value?.text)
        val request = transport.lastRequest
        assertEquals(1, transport.requests.size)
        assertEquals(AIConversationRequestKind.Message, request?.kind)
        assertEquals("And the restaurants?", request?.text)
        assertNull(request?.message)
        assertNull(request?.uiAction)
        // The transport can give the message the id its copy is found by.
        assertEquals(state.pendingMessage.value?.id, request?.messageId)
        assertEquals(listOf("u1", "a1"), state.shown)
    }

    @Test
    fun `send ignores a message with nothing in it`() = runTest {
        val transport = FakeTransport()
        val state = conversation(transport)

        state.send("  \n ")
        runCurrent()

        assertTrue(transport.requests.isEmpty())
        assertEquals(AIConversationStatus.Ready, state.status.value)
        assertNull(state.pendingMessage.value)
    }

    @Test
    fun `send of a UI action sends its label with the action`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)
        val action = AIUIAction(messageId = "a1", format = AIUIFormat.OpenUI, name = "ask_about", label = "Tell me about Alfama")

        state.send(action)
        runCurrent()

        assertEquals(AIConversationRequestKind.Message, transport.lastRequest?.kind)
        assertEquals("Tell me about Alfama", transport.lastRequest?.text)
        assertEquals(action, transport.lastRequest?.uiAction)
        assertEquals("Tell me about Alfama", state.pendingMessage.value?.text)
        assertEquals(AIConversationStatus.Submitted, state.status.value)
    }

    @Test
    fun `send waits until the reply starts`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)

        state.send("And the restaurants?")
        transport.update(user("u1"), reply("a1"), user("u2", "And the restaurants?"))

        // Kept until its reply starts, so the conversation can match its copy.
        assertNotNull(state.pendingMessage.value)
        assertEquals(AIConversationStatus.Submitted, state.status.value)

        transport.update(user("u1"), reply("a1"), user("u2"), reply("a2", "", generating = true))

        // A reply that shows nothing hasn't started.
        assertNotNull(state.pendingMessage.value)
        assertEquals(AIConversationStatus.Submitted, state.status.value)

        transport.update(user("u1"), reply("a1"), user("u2"), reply("a2", "Try", generating = true))

        assertNull(state.pendingMessage.value)
        assertEquals(AIConversationStatus.Streaming, state.status.value)

        transport.update(user("u1"), reply("a1"), user("u2"), reply("a2", "Try Taberna da Rua"))

        assertEquals(AIConversationStatus.Ready, state.status.value)
        assertEquals(listOf("u1", "a1", "u2", "a2"), state.shown)
    }

    @Test
    fun `send ends when the reply arrives finished`() = runTest {
        val transport = FakeTransport()
        val state = conversation(transport)

        state.send("Hi")
        transport.update(user("u1", "Hi"), reply("a1", "Hello!"))

        assertNull(state.pendingMessage.value)
        assertEquals(AIConversationStatus.Ready, state.status.value)
    }

    @Test
    fun `send ends when a reply finishes with nothing in it`() = runTest {
        val transport = FakeTransport()
        val state = conversation(transport)

        state.send("Hi")
        transport.update(user("u1", "Hi"), reply("a1", ""))

        assertNull(state.pendingMessage.value)
        assertEquals(AIConversationStatus.Ready, state.status.value)
    }

    @Test
    fun `send while a reply is written waits for its own reply`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1", "Day one", generating = true)))
        val state = conversation(transport)
        assertEquals(AIConversationStatus.Streaming, state.status.value)

        state.send("Make it three days")
        transport.update(user("u1"), reply("a1", "Day one, day two", generating = true))

        assertNotNull(state.pendingMessage.value)
        assertEquals(AIConversationStatus.Submitted, state.status.value)

        transport.update(
            user("u1"),
            reply("a1", "Day one, day two"),
            user("u2"),
            reply("a2", "Three days", generating = true),
        )

        assertNull(state.pendingMessage.value)
        assertEquals(AIConversationStatus.Streaming, state.status.value)
    }

    @Test
    fun `send when the transport fails shows the error and retry sends it again`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val error = TransportError()
        transport.sendError = error
        val state = conversation(transport)

        state.send("And the restaurants?")
        runCurrent()

        assertEquals(AIConversationStatus.Error, state.status.value)
        assertSame(error, state.error.value)
        // The message stays, to try again.
        assertEquals("And the restaurants?", state.pendingMessage.value?.text)
        val failed = transport.lastRequest

        transport.sendError = null
        state.retry()
        runCurrent()

        assertEquals(2, transport.requests.size)
        assertEquals(AIConversationRequestKind.Message, transport.lastRequest?.kind)
        assertEquals("And the restaurants?", transport.lastRequest?.text)
        // The transport can tell whether the message arrived the first time.
        assertEquals(failed?.messageId, transport.lastRequest?.messageId)
        assertNull(state.error.value)
        assertEquals(AIConversationStatus.Submitted, state.status.value)
        assertEquals("And the restaurants?", state.pendingMessage.value?.text)
    }

    @Test
    fun `send when cancelled shows no error`() = runTest {
        val transport = FakeTransport()
        transport.sendError = CancellationException("Cancelled")
        val state = conversation(transport)

        state.send("Hi")
        runCurrent()

        assertNull(state.error.value)
        assertEquals(AIConversationStatus.Ready, state.status.value)
        assertNull(state.pendingMessage.value)
    }

    @Test
    fun `the failure of a request that was overtaken is ignored`() = runTest {
        val transport = FakeTransport()
        transport.holdsSends = true
        val state = conversation(transport)

        state.send("First")
        state.send("Second")
        runCurrent()
        transport.finishSend(0, TransportError())
        runCurrent()

        assertNull(state.error.value)
        assertEquals(AIConversationStatus.Submitted, state.status.value)
        assertEquals("Second", state.pendingMessage.value?.text)

        transport.finishSend(1)
        runCurrent()

        // A request that reaches the agent waits for its reply.
        assertEquals(AIConversationStatus.Submitted, state.status.value)
    }

    @Test
    fun `send when the reply started before the transport returned`() = runTest {
        val transport = FakeTransport()
        transport.holdsSends = true
        val state = conversation(transport)

        state.send("Hi")
        runCurrent()
        transport.update(user("u1", "Hi"), reply("a1", "Hello", generating = true))
        transport.finishSend(0)
        runCurrent()

        assertEquals(AIConversationStatus.Streaming, state.status.value)
        assertNull(state.pendingMessage.value)
    }

    @Test
    fun `send ignores a failure after the reply arrived`() = runTest {
        val transport = FakeTransport()
        transport.holdsSends = true
        val state = conversation(transport)

        state.send("Hi")
        runCurrent()
        transport.update(user("u1", "Hi"), reply("a1", "Hello"))
        transport.finishSend(0, TransportError())
        runCurrent()

        // The agent had it.
        assertNull(state.error.value)
        assertEquals(AIConversationStatus.Ready, state.status.value)
    }

    @Test
    fun `send failing while a reply is written shows the error once it is finished`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1", "Day one", generating = true)))
        transport.sendError = TransportError()
        val state = conversation(transport)

        state.send("Make it three days")
        runCurrent()

        assertTrue(state.error.value is TransportError)
        // The agent is still answering.
        assertEquals(AIConversationStatus.Streaming, state.status.value)

        transport.update(user("u1"), reply("a1", "Day one, day two"))

        assertEquals(AIConversationStatus.Error, state.status.value)
    }

    @Test
    fun `a failed request answered after all forgets the error`() = runTest {
        val transport = FakeTransport()
        transport.sendError = TransportError()
        val state = conversation(transport)
        state.send("Hi")
        runCurrent()

        transport.update(user("u1", "Hi"), reply("a1", "Hello", generating = true))

        assertNull(state.error.value)
        assertEquals(AIConversationStatus.Streaming, state.status.value)
    }

    @Test
    fun `send shows the agent's error when the reply doesn't start in time`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport, replyTimeout = 1.seconds)

        state.send("Hi")
        runCurrent()
        advanceTimeBy(999.milliseconds)
        assertEquals(AIConversationStatus.Submitted, state.status.value)
        advanceTimeBy(2.milliseconds)

        assertEquals(AIConversationStatus.Error, state.status.value)
        assertEquals(AIAgentError(), state.error.value)
        assertEquals("Hi", state.pendingMessage.value?.text)

        transport.update(user("u1"), reply("a1"), user("u2", "Hi"), reply("a2", "Hello", generating = true))

        // A late reply.
        assertNull(state.error.value)
        assertEquals(AIConversationStatus.Streaming, state.status.value)
    }

    @Test
    fun `send waits for the reply while the agent says it is working`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport, replyTimeout = 1.seconds)
        state.send("Hi")
        runCurrent()

        transport.update(AIAgentActivity(AIAgentActivity.State.Thinking))
        advanceTimeBy(1.minutes)

        // However long it thinks.
        assertEquals(AIConversationStatus.Submitted, state.status.value)
        assertNull(state.error.value)

        transport.update(activity = null)
        advanceTimeBy(1.seconds + 1.milliseconds)

        // It stopped saying it was working, without answering.
        assertEquals(AIAgentError(), state.error.value)
        assertEquals(AIConversationStatus.Error, state.status.value)
    }

    @Test
    fun `send waits for the reply when the agent says it is working before the transport returns`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        transport.holdsSends = true
        val state = conversation(transport, replyTimeout = 1.seconds)
        state.send("Hi")
        runCurrent()

        transport.update(AIAgentActivity(AIAgentActivity.State.Thinking))
        transport.finishSend(0)
        runCurrent()
        advanceTimeBy(1.minutes)

        assertEquals(AIConversationStatus.Submitted, state.status.value)
    }

    @Test
    fun `send without a timeout waits as long as it takes`() = runTest {
        for (timeout in listOf(null, Duration.INFINITE)) {
            val state = conversation(FakeTransport(), replyTimeout = timeout)

            state.send("Hi")
            runCurrent()
            advanceTimeBy(365.days)

            assertEquals(AIConversationStatus.Submitted, state.status.value)
        }
    }

    @Test
    fun `send forgets the previous error and what the agent said it was doing`() = runTest {
        val transport = FakeTransport()
        transport.sendError = TransportError()
        val state = conversation(transport)
        state.send("Hi")
        runCurrent()
        assertNotNull(state.error.value)
        transport.update(AIAgentActivity(AIAgentActivity.State.CheckingExternalSources))

        transport.sendError = null
        state.send("Hi again")

        assertNull(state.error.value)
        assertNull(state.activity.value)
        assertEquals(AIConversationStatus.Submitted, state.status.value)
    }
}
