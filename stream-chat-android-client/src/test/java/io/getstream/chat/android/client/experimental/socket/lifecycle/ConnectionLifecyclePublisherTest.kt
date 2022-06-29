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
import io.getstream.chat.android.client.experimental.socket.Timed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be instance of`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
internal class ConnectionLifecyclePublisherTest {

    private val connectionLifecyclePublisher = ConnectionLifecyclePublisher()
    private val lifecycleEvents = connectionLifecyclePublisher.lifecycleEvents

    @Test
    fun `when onConnect should emit lifecycle started`(): Unit = runTest {
        connectionLifecyclePublisher.onConnect(mock())

        val emittedEvent = lifecycleEvents.first()

        emittedEvent `should be instance of` Timed::class
        emittedEvent.value `should be` Event.Lifecycle.Started
    }

    @Test
    fun `when onDisconnect should emit lifecycle stopped with graceful reason`(): Unit = runTest {
        connectionLifecyclePublisher.onDisconnect(null)

        val emittedEvent = lifecycleEvents.first()

        emittedEvent `should be instance of` Timed::class
        emittedEvent.value `should be instance of` Event.Lifecycle.Stopped.WithReason::class
    }

    @Test
    fun `when onDisconnect with cause should emit lifecycle stopped with that cause and graceful reason`(): Unit = runTest {
        connectionLifecyclePublisher.onDisconnect(DisconnectCause.NetworkNotAvailable)

        val emittedEvent = lifecycleEvents.first()

        emittedEvent `should be instance of` Timed::class
        emittedEvent.value `should be instance of` Event.Lifecycle.Stopped.WithReason::class
        emittedEvent.value as Event.Lifecycle.Stopped.WithReason
        emittedEvent.value.cause `should be` DisconnectCause.NetworkNotAvailable
    }

    @Test
    fun `when onDisconnect with unrecoverable cause should reset connection conf`(): Unit = runTest {
        connectionLifecyclePublisher.onDisconnect(DisconnectCause.UnrecoverableError(null))

        connectionLifecyclePublisher.connectionConf `should be` null
    }
}
