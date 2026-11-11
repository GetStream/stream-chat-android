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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
internal class AIConversationStateAgentTest {

    private fun TestScope.conversation(
        transport: AIConversationTransport<ChatMessage>,
        replyTimeout: Duration? = AIConversationState.DefaultReplyTimeout,
    ) = AIConversationState(transport, backgroundScope, replyTimeout = replyTimeout)

    private val AIConversationState<ChatMessage>.shown get() = messages.value.ids

    // Stopping

    @Test
    fun `stop ends the reply and tells the transport`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1", "Day one", generating = true)))
        val state = conversation(transport)
        assertEquals(AIConversationStatus.Streaming, state.status.value)

        state.stop()
        runCurrent()

        assertEquals(1, transport.stops)
        assertEquals(AIConversationStatus.Ready, state.status.value)

        transport.update(user("u1"), reply("a1", "Day one", generating = true))

        // A stopped reply is finished, however long the conversation says otherwise, and what it
        // wrote so far stays.
        assertEquals(AIConversationStatus.Ready, state.status.value)
        assertEquals(listOf("u1", "a1"), state.shown)
    }

    @Test
    fun `stop while waiting keeps the person's message`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)
        state.send("Hi")

        state.stop()
        runCurrent()

        // It is on its way until its copy arrives.
        assertEquals("Hi", state.pendingMessage.value?.text)
        assertEquals(AIConversationStatus.Ready, state.status.value)
        assertEquals(1, transport.stops)
    }

    @Test
    fun `stop while waiting finishes a reply that arrives later`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)
        state.send("Hi")
        state.stop()
        runCurrent()

        transport.update(user("u1"), reply("a1"), user("u2", "Hi"), reply("a2", "", generating = true))

        assertEquals(AIConversationStatus.Ready, state.status.value)
        // It has nothing to show.
        assertEquals(listOf("u1", "a1", "u2"), state.shown)

        transport.update(user("u1"), reply("a1"), user("u2", "Hi"), reply("a2", "Hello", generating = true))

        // The person stopped it.
        assertEquals(AIConversationStatus.Ready, state.status.value)
        assertEquals(listOf("u1", "a1", "u2", "a2"), state.shown)
        assertNull(state.pendingMessage.value)
    }

    @Test
    fun `stop while waiting lets the next request's reply stream`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)
        state.send("Hi")
        state.stop()
        runCurrent()

        state.send("Porto?")
        transport.update(user("u1"), reply("a1"), user("u2", "Hi"), user("u3", "Porto?"), reply("a3", "Sure", generating = true))

        assertEquals(AIConversationStatus.Streaming, state.status.value)
    }

    @Test
    fun `stop while waiting for a reply written again brings the reply back`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1", "Day one", generating = true)))
        val state = conversation(transport)
        transport.update(AIAgentActivity(AIAgentActivity.State.Error))
        state.retry()
        assertEquals(listOf("u1"), state.shown)

        state.stop()

        assertEquals(listOf("u1", "a1"), state.shown)
        assertEquals(AIConversationStatus.Ready, state.status.value)
    }

    @Test
    fun `send after a stop waits for the stop to reach the transport`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1", "Day one", generating = true)))
        transport.holdsStops = true
        val state = conversation(transport)

        state.stop()
        state.send("Something else")
        runCurrent()

        assertEquals(1, transport.stops)
        // The stop would stop it too.
        assertTrue(transport.requests.isEmpty())
        assertEquals(AIConversationStatus.Submitted, state.status.value)

        transport.finishStop(0)
        runCurrent()

        assertEquals("Something else", transport.lastRequest?.text)
    }

    @Test
    fun `stop leaves out a reply that shows nothing`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1", "", generating = true)))
        val state = conversation(transport)
        assertEquals(AIConversationStatus.Submitted, state.status.value)

        state.stop()

        // The conversation would think it is still on its way.
        assertEquals(listOf("u1"), state.shown)
        assertEquals(AIConversationStatus.Ready, state.status.value)
    }

    @Test
    fun `stop when not answering does nothing`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)

        state.stop()
        runCurrent()

        assertEquals(0, transport.stops)
    }

    @Test
    fun `stop ignores what the agent says until it stops saying`() = runTest {
        val transport = FakeTransport(listOf(user("u1")))
        val state = conversation(transport)
        transport.update(AIAgentActivity(AIAgentActivity.State.Thinking))
        assertEquals(AIConversationStatus.Submitted, state.status.value)

        state.stop()
        transport.update(AIAgentActivity(AIAgentActivity.State.Generating))

        assertNull(state.activity.value)
        assertEquals(AIConversationStatus.Ready, state.status.value)

        transport.update(activity = null)
        transport.update(AIAgentActivity(AIAgentActivity.State.Thinking))

        assertEquals(AIAgentActivity(AIAgentActivity.State.Thinking), state.activity.value)
        assertEquals(AIConversationStatus.Submitted, state.status.value)
    }

    @Test
    fun `stop when the transport fails shows the reply still being written`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1", "Day one", generating = true)))
        transport.stopError = TransportError()
        val state = conversation(transport)

        state.stop()
        runCurrent()

        // Nothing to try again, and the composer offers to stop it again.
        assertNull(state.error.value)
        assertEquals(AIConversationStatus.Streaming, state.status.value)

        transport.update(user("u1"), reply("a1", "Day one, day two"))

        assertEquals(AIConversationStatus.Ready, state.status.value)
    }

    @Test
    fun `stop while waiting when the transport fails waits for the reply again`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        transport.stopError = TransportError()
        val state = conversation(transport)
        state.send("Hi")

        state.stop()
        runCurrent()

        assertEquals(AIConversationStatus.Submitted, state.status.value)

        transport.update(user("u1"), reply("a1"), user("u2", "Hi"), reply("a2", "Hello", generating = true))

        assertEquals(AIConversationStatus.Streaming, state.status.value)
    }

    @Test
    fun `a stop that fails after a new request is ignored`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1", "Day one", generating = true)))
        transport.stopError = TransportError()
        val state = conversation(transport)

        state.stop()
        state.send("Something else")
        runCurrent()

        assertNull(state.error.value)
        assertEquals(AIConversationStatus.Submitted, state.status.value)
    }

    // What the agent says it is doing

    @Test
    fun `activity while the person's message waits shows the agent working`() = runTest {
        val transport = FakeTransport(listOf(user("u1")))
        val state = conversation(transport)
        assertEquals(AIConversationStatus.Ready, state.status.value)

        transport.update(AIAgentActivity(AIAgentActivity.State.CheckingExternalSources, text = "Searching the web"))

        assertEquals("Searching the web", state.activity.value?.text)
        assertEquals(AIConversationStatus.Submitted, state.status.value)

        transport.update(activity = null)

        assertNull(state.activity.value)
        assertEquals(AIConversationStatus.Ready, state.status.value)
    }

    @Test
    fun `activity in an empty conversation shows the agent working`() = runTest {
        val transport = FakeTransport()
        val state = conversation(transport)

        transport.update(AIAgentActivity(AIAgentActivity.State.Thinking))

        assertEquals(AIConversationStatus.Submitted, state.status.value)
    }

    @Test
    fun `activity after a finished reply does not mean the agent is answering`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)

        transport.update(AIAgentActivity(AIAgentActivity.State.Generating))

        assertEquals(AIAgentActivity.State.Generating, state.activity.value?.state)
        // An indicator that wasn't cleared yet.
        assertEquals(AIConversationStatus.Ready, state.status.value)
    }

    @Test
    fun `an error activity ends the request with the agent's error`() = runTest {
        val transport = FakeTransport(listOf(user("u1", "Lisbon?"), reply("a1")))
        val state = conversation(transport)
        state.send("Porto?")
        transport.update(user("u1", "Lisbon?"), reply("a1"), user("u2", "Porto?"))

        transport.update(AIAgentActivity(AIAgentActivity.State.Error, text = "Rate limited"))

        // Its copy takes its place.
        assertEquals("Porto?", state.pendingMessage.value?.text)
        assertEquals(AIConversationStatus.Error, state.status.value)
        assertEquals(AIAgentError("Rate limited"), state.error.value)
        assertEquals("Rate limited", state.error.value?.message)

        transport.update(activity = null)

        // It stays until the next request.
        assertEquals(AIConversationStatus.Error, state.status.value)

        state.retry()
        runCurrent()

        assertEquals(AIConversationRequestKind.Regenerate, transport.lastRequest?.kind)
        assertEquals("Porto?", transport.lastRequest?.text)
        // The message has no reply to replace.
        assertNull(transport.lastRequest?.message)
        assertEquals(AIConversationStatus.Submitted, state.status.value)
        assertNull(state.error.value)
    }

    @Test
    fun `an error activity while a reply is written finishes it`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1", "Day one", generating = true)))
        val state = conversation(transport)

        transport.update(AIAgentActivity(AIAgentActivity.State.Error))

        assertEquals(AIConversationStatus.Error, state.status.value)
        assertEquals(listOf("u1", "a1"), state.shown)

        state.retry()
        runCurrent()

        assertEquals(AIConversationRequestKind.Regenerate, transport.lastRequest?.kind)
        assertEquals("a1", transport.lastRequest?.message?.id)
        assertEquals("Plan a weekend in Lisbon", transport.lastRequest?.text)
    }

    @Test
    fun `an error activity ignores the failure of the request it ended`() = runTest {
        val transport = FakeTransport()
        transport.holdsSends = true
        val state = conversation(transport)
        state.send("Hi")
        runCurrent()

        transport.update(AIAgentActivity(AIAgentActivity.State.Error, text = "Rate limited"))
        transport.finishSend(0, TransportError())
        runCurrent()

        assertEquals(AIAgentError("Rate limited"), state.error.value)
    }

    @Test
    fun `an error activity before the person's message arrives lets retry send it again`() = runTest {
        val transport = FakeTransport(listOf(user("u1", "Lisbon?"), reply("a1")))
        val state = conversation(transport)
        state.send("Porto?")
        runCurrent()
        val sent = transport.lastRequest

        transport.update(AIAgentActivity(AIAgentActivity.State.Error, text = "Couldn't read it"))
        transport.update(activity = null)

        assertEquals("Porto?", state.pendingMessage.value?.text)
        assertEquals(listOf("u1", "a1"), state.shown)

        state.retry()
        runCurrent()

        // Not a1 written again.
        assertEquals(AIConversationRequestKind.Message, transport.lastRequest?.kind)
        assertEquals(sent?.messageId, transport.lastRequest?.messageId)
        assertEquals("Porto?", transport.lastRequest?.text)
    }

    @Test
    fun `an error activity before the person's message arrives lets retry ask for its reply once it is there`() =
        runTest {
            val transport = FakeTransport(listOf(user("u1", "Lisbon?"), reply("a1")))
            val state = conversation(transport)
            state.send("Porto?")
            runCurrent()

            transport.update(AIAgentActivity(AIAgentActivity.State.Error))
            transport.update(user("u1", "Lisbon?"), reply("a1"), user("u2", "Porto?"))
            state.retry()
            runCurrent()

            assertEquals(AIConversationRequestKind.Regenerate, transport.lastRequest?.kind)
            assertEquals("Porto?", transport.lastRequest?.text)
            assertNull(transport.lastRequest?.message)
        }

    @Test
    fun `an error activity is forgotten once a new reply starts`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)
        state.send("Porto?")
        transport.update(user("u1"), reply("a1"), user("u2", "Porto?"))
        transport.update(AIAgentActivity(AIAgentActivity.State.Error))
        transport.update(activity = null)
        assertEquals(AIConversationStatus.Error, state.status.value)

        transport.update(user("u1"), reply("a1"), user("u2", "Porto?"), reply("a2", "Porto it is", generating = true))

        // The agent answered after all.
        assertNull(state.error.value)
        assertEquals(AIConversationStatus.Streaming, state.status.value)

        transport.update(user("u1"), reply("a1"), user("u2", "Porto?"), reply("a2", "Porto it is"))

        assertEquals(AIConversationStatus.Ready, state.status.value)
        assertNull(state.pendingMessage.value)
    }

    @Test
    fun `an error activity while a reply is written is forgotten once the agent writes on`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1", "Day one", generating = true)))
        val state = conversation(transport)
        transport.update(AIAgentActivity(AIAgentActivity.State.Error))
        transport.update(activity = null)

        transport.update(user("u1"), reply("a1", "Day one"))

        // The same reply, finished.
        assertEquals(AIConversationStatus.Error, state.status.value)

        transport.update(user("u1"), reply("a1"), user("u2"), reply("a2", "Day two", generating = true))

        assertNull(state.error.value)
        assertEquals(AIConversationStatus.Streaming, state.status.value)
    }
}
