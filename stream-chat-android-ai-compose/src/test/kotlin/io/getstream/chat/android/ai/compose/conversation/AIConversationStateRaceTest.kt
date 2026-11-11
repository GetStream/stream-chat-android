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
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** Failed turns, and requests and stops that overlap. */
@OptIn(ExperimentalCoroutinesApi::class)
internal class AIConversationStateRaceTest {

    private fun TestScope.conversation(
        transport: AIConversationTransport<ChatMessage>,
        replyTimeout: Duration? = AIConversationState.DefaultReplyTimeout,
    ) = AIConversationState(transport, backgroundScope, replyTimeout = replyTimeout)

    private val AIConversationState<ChatMessage>.shown get() = messages.value.ids

    // A model failure: the agent says it couldn't answer, and the turn's reply ends empty.

    @Test
    fun `the agent's error stays when the turn's reply then ends empty`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)
        state.send("Porto?")
        runCurrent()
        transport.update(user("u1"), reply("a1"), user("u2", "Porto?"))

        transport.update(AIAgentActivity(AIAgentActivity.State.Error, text = "The model failed"))
        transport.update(activity = null)
        transport.update(user("u1"), reply("a1"), user("u2", "Porto?"), reply("a2", ""))

        assertEquals(AIAgentError("The model failed"), state.error.value)
        assertEquals(AIConversationStatus.Error, state.status.value)
        // A finished reply with nothing in it is no reply.
        assertEquals(listOf("u1", "a1", "u2"), state.shown)
    }

    @Test
    fun `the agent's error stays when the turn's reply ended empty before it`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)
        state.send("Porto?")
        runCurrent()
        transport.update(user("u1"), reply("a1"), user("u2", "Porto?"), reply("a2", ""))

        transport.update(AIAgentActivity(AIAgentActivity.State.Error, text = "The model failed"))
        transport.update(activity = null)
        transport.update(user("u1"), reply("a1"), user("u2", "Porto?"), reply("a2", ""))

        assertEquals(AIAgentError("The model failed"), state.error.value)
        assertEquals(AIConversationStatus.Error, state.status.value)
        assertEquals(listOf("u1", "a1", "u2"), state.shown)
    }

    @Test
    fun `a reply with something in it after the agent's error still forgets it`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)
        state.send("Porto?")
        runCurrent()
        transport.update(AIAgentActivity(AIAgentActivity.State.Error))
        transport.update(user("u1"), reply("a1"), user("u2", "Porto?"), reply("a2", ""))

        transport.update(user("u1"), reply("a1"), user("u2", "Porto?"), reply("a2", "Porto it is"))

        assertNull(state.error.value)
        assertEquals(AIConversationStatus.Ready, state.status.value)
        assertEquals(listOf("u1", "a1", "u2", "a2"), state.shown)
    }

    // Requests and stops that overlap.

    @Test
    fun `a late reply to a stopped request is not the next request's reply`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)
        state.send("Hi")
        runCurrent()
        state.stop()
        runCurrent()
        transport.holdsSends = true
        state.send("Porto?")
        runCurrent()

        // The stopped request's message and reply arrive after all.
        transport.update(user("u1"), reply("a1"), user("u2", "Hi"), reply("a2", "Hello", generating = true))

        assertEquals(AIConversationStatus.Submitted, state.status.value)
        assertEquals("Porto?", state.pendingMessage.value?.text)

        // The only send held is the second request's.
        transport.finishSend(0, TransportError())
        runCurrent()

        assertTrue(state.error.value is TransportError)
        assertEquals(AIConversationStatus.Error, state.status.value)
    }

    @Test
    fun `the next request's own reply follows a stopped request's late one`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)
        state.send("Hi")
        runCurrent()
        state.stop()
        runCurrent()
        state.send("Porto?")
        runCurrent()

        transport.update(user("u1"), reply("a1"), user("u2", "Hi"), reply("a2", "Hello"))
        assertEquals(AIConversationStatus.Submitted, state.status.value)
        transport.update(
            user("u1"),
            reply("a1"),
            user("u2", "Hi"),
            reply("a2", "Hello"),
            user("u3", "Porto?"),
            reply("a3", "Sure", generating = true),
        )

        assertEquals(AIConversationStatus.Streaming, state.status.value)
        assertNull(state.pendingMessage.value)
    }

    @Test
    fun `the reply timeout waits for the transport to have the request`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        transport.holdsSends = true
        val state = conversation(transport, replyTimeout = 1.seconds)
        state.send("Hi")
        runCurrent()

        transport.update(activity = null)
        advanceTimeBy(1.seconds + 1.milliseconds)
        assertEquals(AIConversationStatus.Submitted, state.status.value)

        transport.finishSend(0, TransportError())
        runCurrent()

        assertTrue(state.error.value is TransportError)
        state.retry()
        runCurrent()
        assertEquals(2, transport.requests.size)
    }

    @Test
    fun `a request stopped while it waits for an earlier stop is not sent`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1", "Day one", generating = true)))
        transport.holdsStops = true
        val state = conversation(transport)
        state.stop()
        state.send("Porto?")
        runCurrent()
        assertEquals(AIConversationStatus.Submitted, state.status.value)

        state.stop()
        runCurrent()
        transport.finishStop(0)
        transport.finishStop(1)
        runCurrent()

        assertTrue(transport.requests.isEmpty())
        assertEquals(AIConversationStatus.Ready, state.status.value)
    }

    @Test
    fun `a send that fails after its request was stopped shows no error`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        transport.holdsSends = true
        val state = conversation(transport)
        state.send("Hi")
        runCurrent()

        state.stop()
        runCurrent()
        transport.finishSend(0, TransportError())
        runCurrent()

        assertNull(state.error.value)
        assertEquals(AIConversationStatus.Ready, state.status.value)
        state.retry()
        runCurrent()
        assertEquals(1, transport.requests.size)
    }

    @Test
    fun `a failed stop lets a reply that arrived meanwhile stream`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)
        state.send("Hi")
        runCurrent()
        transport.holdsStops = true
        transport.stopError = TransportError()

        state.stop()
        runCurrent()
        transport.update(user("u1"), reply("a1"), user("u2", "Hi"), reply("a2", "Hello", generating = true))
        assertEquals(AIConversationStatus.Ready, state.status.value)

        transport.finishStop(0)
        runCurrent()

        // The agent goes on, and the composer offers to stop it again.
        assertEquals(AIConversationStatus.Streaming, state.status.value)
    }
}
