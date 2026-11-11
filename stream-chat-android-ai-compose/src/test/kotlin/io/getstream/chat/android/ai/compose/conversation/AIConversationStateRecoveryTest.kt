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

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowLog
import kotlin.time.Duration

// Robolectric, for the log of a failed load.
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class AIConversationStateRecoveryTest {

    private fun TestScope.conversation(
        transport: AIConversationTransport<ChatMessage>,
        replyTimeout: Duration? = AIConversationState.DefaultReplyTimeout,
    ) = AIConversationState(transport, backgroundScope, replyTimeout = replyTimeout)

    private val AIConversationState<ChatMessage>.shown get() = messages.value.ids

    // Errors

    @Test
    fun `retry without an error does nothing`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)

        state.retry()
        runCurrent()

        assertTrue(transport.requests.isEmpty())
    }

    @Test
    fun `retry after a stop failed has nothing to try`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1", "Day one", generating = true)))
        transport.stopError = TransportError()
        val state = conversation(transport)
        state.stop()
        runCurrent()

        state.retry()
        runCurrent()

        assertTrue(transport.requests.isEmpty())
    }

    @Test
    fun `retry after the agent failed in an empty conversation does nothing`() = runTest {
        val transport = FakeTransport()
        val state = conversation(transport)
        transport.update(AIAgentActivity(AIAgentActivity.State.Error))

        state.retry()
        runCurrent()

        assertTrue(transport.requests.isEmpty())
    }

    @Test
    fun `clearError forgets the error and the request that failed`() = runTest {
        val transport = FakeTransport()
        transport.sendError = TransportError()
        val state = conversation(transport)
        state.send("Hi")
        runCurrent()

        state.clearError()
        state.retry()
        runCurrent()

        assertNull(state.error.value)
        assertEquals(AIConversationStatus.Ready, state.status.value)
        assertEquals(1, transport.requests.size)
    }

    // Older messages

    @Test
    fun `loadOlderMessages asks the transport only while there are any`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)

        state.loadOlderMessages()
        assertEquals(0, transport.loads)

        transport.feed?.update(hasOlderMessages = true)
        state.loadOlderMessages()
        assertEquals(1, transport.loads)
    }

    @Test
    fun `loadOlderMessages loads once at a time`() = runTest {
        val transport = FakeTransport()
        transport.holdsLoads = true
        val state = conversation(transport)
        transport.feed?.update(hasOlderMessages = true)

        val first = launch { state.loadOlderMessages() }
        val second = launch { state.loadOlderMessages() }
        runCurrent()
        transport.finishLoads()
        first.join()
        second.join()

        assertEquals(1, transport.loads)

        transport.holdsLoads = false
        state.loadOlderMessages()

        assertEquals(2, transport.loads)
    }

    @Test
    fun `loadOlderMessages when the transport fails shows the error and logs it`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val error = TransportError()
        transport.loadError = error
        val state = conversation(transport)
        transport.feed?.update(hasOlderMessages = true)

        state.loadOlderMessages()

        assertSame(error, state.error.value)
        assertEquals(AIConversationStatus.Error, state.status.value)
        assertTrue(ShadowLog.getLogsForTag("AIConversationState").any { it.throwable === error })
    }

    @Test
    fun `loadOlderMessages that succeeds after failing forgets its error`() = runTest {
        val transport = FakeTransport()
        transport.loadError = TransportError()
        val state = conversation(transport)
        transport.feed?.update(hasOlderMessages = true)
        state.loadOlderMessages()

        transport.loadError = null
        state.loadOlderMessages()

        assertEquals(2, transport.loads)
        assertNull(state.error.value)
        assertEquals(AIConversationStatus.Ready, state.status.value)
    }

    @Test
    fun `loadOlderMessages that succeeds keeps the error of a request`() = runTest {
        val transport = FakeTransport()
        transport.sendError = TransportError()
        val state = conversation(transport)
        state.send("Hi")
        runCurrent()
        transport.feed?.update(hasOlderMessages = true)

        state.loadOlderMessages()

        assertTrue(state.error.value is TransportError)
        assertEquals(AIConversationStatus.Error, state.status.value)
    }

    @Test
    fun `an error loading older messages goes with retry or the next request`() = runTest {
        val transport = FakeTransport()
        transport.loadError = TransportError()
        val state = conversation(transport)
        transport.feed?.update(hasOlderMessages = true)
        state.loadOlderMessages()

        state.retry()
        runCurrent()

        // Nothing to send: the list loads again as the person scrolls.
        assertTrue(transport.requests.isEmpty())
        assertNull(state.error.value)

        state.loadOlderMessages()
        assertTrue(state.error.value is TransportError)
        state.send("Hi")

        assertNull(state.error.value)
        assertEquals(AIConversationStatus.Submitted, state.status.value)
    }

    @Test
    fun `the transport's defaults load nothing and keep ratings on the device`() = runTest {
        val transport = MinimalTransport()
        val state = conversation(transport)

        state.receiveHasOlderMessages(true)
        state.loadOlderMessages()
        state.rate(reply("a1"), AIMessageRating.Negative)
        runCurrent()

        assertEquals(AIMessageRating.Negative, state.ratings.value["a1"])
    }

    // Ratings

    @Test
    fun `rate keeps the rating and tells the transport`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)

        state.rate(reply("a1"), AIMessageRating.Positive)
        assertEquals(AIMessageRating.Positive, state.ratings.value["a1"])
        runCurrent()
        state.rate(reply("a1"), null)
        runCurrent()

        assertNull(state.ratings.value["a1"])
        assertEquals(listOf("a1" to AIMessageRating.Positive, "a1" to null), transport.ratings)
    }

    @Test
    fun `rate the same again does nothing`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)

        state.rate(reply("a1"), AIMessageRating.Positive)
        state.rate(reply("a1"), AIMessageRating.Positive)
        runCurrent()

        assertEquals(1, transport.ratings.size)
    }

    @Test
    fun `rate when the transport fails puts the rating back`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        val state = conversation(transport)
        state.rate(reply("a1"), AIMessageRating.Positive)
        runCurrent()

        transport.failsRating = { true }
        state.rate(reply("a1"), AIMessageRating.Negative)
        runCurrent()

        assertEquals(AIMessageRating.Positive, state.ratings.value["a1"])
    }

    @Test
    fun `rate sends only the latest of the ratings given before one is sent`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        transport.failsRating = { it == AIMessageRating.Positive }
        val state = conversation(transport)

        state.rate(reply("a1"), AIMessageRating.Positive)
        state.rate(reply("a1"), AIMessageRating.Negative)
        runCurrent()

        assertEquals(AIMessageRating.Negative, state.ratings.value["a1"])
        assertEquals(listOf("a1" to AIMessageRating.Negative), transport.ratings)
    }

    @Test
    fun `rate sends each reply's ratings in order`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        transport.holdsRatings = true
        val state = conversation(transport)

        state.rate(reply("a1"), AIMessageRating.Positive)
        runCurrent()
        state.rate(reply("a1"), AIMessageRating.Negative)
        runCurrent()

        // The next one waits.
        assertEquals(listOf<AIMessageRating?>(AIMessageRating.Positive), transport.ratings.map { it.second })

        transport.finishRating(0)
        runCurrent()
        transport.finishRating(1)
        runCurrent()

        assertEquals(listOf(AIMessageRating.Positive, AIMessageRating.Negative), transport.ratings.map { it.second })
        assertEquals(AIMessageRating.Negative, state.ratings.value["a1"])
    }

    @Test
    fun `rate keeps the latest when an earlier rating fails`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1")))
        transport.holdsRatings = true
        val state = conversation(transport)

        state.rate(reply("a1"), AIMessageRating.Positive)
        runCurrent()
        state.rate(reply("a1"), AIMessageRating.Negative)
        transport.finishRating(0, TransportError())
        runCurrent()

        assertEquals(AIMessageRating.Negative, state.ratings.value["a1"])

        transport.finishRating(1, TransportError())
        runCurrent()

        // What the transport has.
        assertNull(state.ratings.value["a1"])
    }

    // Status

    @Test
    fun `status follows the latest reply`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1", "", generating = true)))
        val state = conversation(transport)

        assertEquals(AIConversationStatus.Submitted, state.status.value)

        transport.update(user("u1"), reply("a1", "Day", generating = true))
        assertEquals(AIConversationStatus.Streaming, state.status.value)

        transport.update(user("u1"), reply("a1", "Day one"))
        assertEquals(AIConversationStatus.Ready, state.status.value)
    }

    @Test
    fun `status emits only when it changes`() = runTest {
        val transport = FakeTransport(listOf(user("u1"), reply("a1", "Day", generating = true)))
        val state = conversation(transport)
        val statuses = mutableListOf<AIConversationStatus>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { state.status.toList(statuses) }

        transport.update(user("u1"), reply("a1", "Day one", generating = true))
        transport.update(user("u1"), reply("a1", "Day one, day two", generating = true))
        transport.update(user("u1"), reply("a1", "Day one, day two"))

        assertEquals(listOf(AIConversationStatus.Streaming, AIConversationStatus.Ready), statuses)
    }
}
