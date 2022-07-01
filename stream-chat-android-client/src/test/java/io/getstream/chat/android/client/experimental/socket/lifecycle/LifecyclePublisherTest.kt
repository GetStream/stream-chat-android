/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.experimental.socket.lifecycle

import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.experimental.socket.Event
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
internal class LifecyclePublisherTest {

    private lateinit var lifecyclePublisher1: FakeLifecyclePublisher
    private lateinit var lifecyclePublisher2: FakeLifecyclePublisher
    private lateinit var lifecyclePublisher3: FakeLifecyclePublisher

    private val combinedLifecycleEvents = mutableListOf<Event.Lifecycle>()

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @BeforeEach
    fun setUp() {
        lifecyclePublisher1 = FakeLifecyclePublisher()
        lifecyclePublisher2 = FakeLifecyclePublisher()
        lifecyclePublisher3 = FakeLifecyclePublisher()

        combinedLifecycleEvents.clear()

        listOf(lifecyclePublisher1, lifecyclePublisher2, lifecyclePublisher3).combine()
            .onEach { combinedLifecycleEvents.add(it) }.launchIn(
                testCoroutines.scope
            )
    }

    @Test
    fun `given all started when combining should emit lifecycle started`(): Unit = runTest {
        lifecyclePublisher1.sendLifecycleStartedEvent()
        lifecyclePublisher2.sendLifecycleStartedEvent()
        lifecyclePublisher3.sendLifecycleStartedEvent()

        combinedLifecycleEvents.size `should be equal to` 1
        combinedLifecycleEvents[0] `should be` Event.Lifecycle.Started
    }

    @Test
    fun `given all stopped when combining should emit lifecycle stopped`(): Unit = runTest {
        lifecyclePublisher1.sendLifecycleStoppedEvent()
        lifecyclePublisher2.sendLifecycleStoppedEvent()
        lifecyclePublisher3.sendLifecycleStoppedEvent()

        combinedLifecycleEvents.size `should be equal to` 1
        combinedLifecycleEvents[0] `should be instance of` Event.Lifecycle.Stopped::class
    }

    @Test
    fun `given all started when any publisher start should not emit again`(): Unit = runTest {
        // Given all lifecycle started.
        lifecyclePublisher1.sendLifecycleStartedEvent()
        lifecyclePublisher2.sendLifecycleStartedEvent()
        lifecyclePublisher3.sendLifecycleStartedEvent()

        combinedLifecycleEvents.size `should be equal to` 1
        combinedLifecycleEvents[0] `should be` Event.Lifecycle.Started

        // Sending lifecycle start event again.
        lifecyclePublisher2.sendLifecycleStartedEvent()

        combinedLifecycleEvents.size `should be equal to` 1
        combinedLifecycleEvents[0] `should be` Event.Lifecycle.Started
    }

    @Test
    fun `given all stopped when any publisher stop should not emit again`(): Unit = runTest {

        // Given all lifecycle started.
        lifecyclePublisher1.sendLifecycleStoppedEvent()
        lifecyclePublisher2.sendLifecycleStoppedEvent()
        lifecyclePublisher3.sendLifecycleStoppedEvent()

        combinedLifecycleEvents.size `should be equal to` 1
        combinedLifecycleEvents[0] `should be instance of` Event.Lifecycle.Stopped::class

        // Sending lifecycle start event again.
        lifecyclePublisher2.sendLifecycleStoppedEvent()

        combinedLifecycleEvents.size `should be equal to` 1
        combinedLifecycleEvents[0] `should be instance of` Event.Lifecycle.Stopped::class
    }

    @Test
    fun `given all started when any publisher stop should emit lifecycle stopped`(): Unit = runTest {
        // Given all lifecycle started.
        lifecyclePublisher1.sendLifecycleStartedEvent()
        lifecyclePublisher2.sendLifecycleStartedEvent()
        lifecyclePublisher3.sendLifecycleStartedEvent()

        combinedLifecycleEvents.size `should be equal to` 1
        combinedLifecycleEvents[0] `should be` Event.Lifecycle.Started

        // Sending lifecycle stop event.
        lifecyclePublisher2.sendLifecycleStoppedEvent(Event.Lifecycle.Stopped.WithReason(cause = DisconnectCause.ConnectionReleased))

        combinedLifecycleEvents.size `should be equal to` 2
        combinedLifecycleEvents[0] `should be` Event.Lifecycle.Started
        combinedLifecycleEvents[1] `should be instance of` Event.Lifecycle.Stopped.WithReason::class
    }

    @Test
    fun `given all stopped when not all publisher start should not emit lifecycle start`(): Unit = runTest {
        // Given all lifecycle stopped.
        lifecyclePublisher1.sendLifecycleStoppedEvent()
        lifecyclePublisher2.sendLifecycleStoppedEvent()
        lifecyclePublisher3.sendLifecycleStoppedEvent()

        combinedLifecycleEvents.size `should be equal to` 1
        combinedLifecycleEvents[0] `should be instance of` Event.Lifecycle.Stopped::class

        // Sending lifecycle start event again to publisher2.
        lifecyclePublisher2.sendLifecycleStartedEvent()

        combinedLifecycleEvents.size `should be equal to` 1
        combinedLifecycleEvents[0] `should be instance of` Event.Lifecycle.Stopped::class

        // Sending lifecycle start event again to publisher1.
        lifecyclePublisher1.sendLifecycleStartedEvent()

        combinedLifecycleEvents.size `should be equal to` 1
        combinedLifecycleEvents[0] `should be instance of` Event.Lifecycle.Stopped::class
    }

    @Test
    fun `given lifecycle stopped when all publisher start should emit lifecycle start`(): Unit = runTest {

        // Given all lifecycle stopped.
        lifecyclePublisher1.sendLifecycleStoppedEvent()
        lifecyclePublisher2.sendLifecycleStoppedEvent()
        lifecyclePublisher3.sendLifecycleStoppedEvent()

        combinedLifecycleEvents.size `should be equal to` 1
        combinedLifecycleEvents[0] `should be instance of` Event.Lifecycle.Stopped::class

        // Sending lifecycle start event again to publisher2.
        lifecyclePublisher2.sendLifecycleStartedEvent()

        combinedLifecycleEvents.size `should be equal to` 1
        combinedLifecycleEvents[0] `should be instance of` Event.Lifecycle.Stopped::class

        // Sending lifecycle start event again to publisher1.
        lifecyclePublisher1.sendLifecycleStartedEvent()

        combinedLifecycleEvents.size `should be equal to` 1
        combinedLifecycleEvents[0] `should be instance of` Event.Lifecycle.Stopped::class

        // Sending lifecycle start event again to publisher3.
        lifecyclePublisher3.sendLifecycleStartedEvent()

        combinedLifecycleEvents.size `should be equal to` 2
        combinedLifecycleEvents[0] `should be instance of` Event.Lifecycle.Stopped::class
        combinedLifecycleEvents[1] `should be` Event.Lifecycle.Started
    }

    @Test
    fun `given any stopAndAbort should emit first stopAndAbort event`(): Unit = runTest {
        lifecyclePublisher3.sendLifecycleStoppedEvent(Event.Lifecycle.Stopped.WithReason(cause = DisconnectCause.ConnectionReleased))
        lifecyclePublisher2.sendLifecycleStoppedEvent(Event.Lifecycle.Stopped.AndAborted(cause = DisconnectCause.NetworkNotAvailable))
        lifecyclePublisher1.sendLifecycleStoppedEvent(Event.Lifecycle.Stopped.WithReason(cause = DisconnectCause.Error(null)))

        combinedLifecycleEvents.size `should be equal to` 1
        val latestEvent = combinedLifecycleEvents[0]
        latestEvent `should be instance of` Event.Lifecycle.Stopped.AndAborted::class
        (latestEvent as Event.Lifecycle.Stopped.AndAborted)
        latestEvent.cause `should be instance of` DisconnectCause.NetworkNotAvailable::class
    }

    @Test
    fun `given any stopWithReason and none stopAndAbort should emit first stopWithReason`(): Unit = runTest {
        lifecyclePublisher3.sendLifecycleStoppedEvent(Event.Lifecycle.Stopped.WithReason(cause = DisconnectCause.ConnectionReleased))
        lifecyclePublisher1.sendLifecycleStoppedEvent(Event.Lifecycle.Stopped.WithReason(cause = DisconnectCause.NetworkNotAvailable))
        lifecyclePublisher2.sendLifecycleStoppedEvent(Event.Lifecycle.Stopped.WithReason(cause = DisconnectCause.Error(null)))

        combinedLifecycleEvents.size `should be equal to` 1
        val latestEvent = combinedLifecycleEvents[0]
        latestEvent `should be instance of` Event.Lifecycle.Stopped.WithReason::class
        (latestEvent as Event.Lifecycle.Stopped.WithReason)
        latestEvent.cause `should be instance of` DisconnectCause.ConnectionReleased::class
    }
}
