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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import java.util.concurrent.atomic.AtomicLong

internal class FakeLifecyclePublisher : LifecyclePublisher {
    private val _lifecycleEvents = MutableStateFlow<Timed<Event.Lifecycle>?>(null)

    override val lifecycleEvents: Flow<Timed<Event.Lifecycle>>
        get() = _lifecycleEvents.filterNotNull()

    override suspend fun observe() {
        // no-op
    }

    override suspend fun dispose() {
        // no-op
    }

    fun sendLifecycleStartedEvent() {
        _lifecycleEvents.tryEmit(Timed(Event.Lifecycle.Started, fakeNextMillisecond()))
    }

    fun sendLifecycleStoppedEvent(event: Event.Lifecycle.Stopped? = null) {
        _lifecycleEvents.tryEmit(Timed(event ?: randomLifecycleStoppedEvent(), fakeNextMillisecond()))
    }

    private fun randomLifecycleStoppedEvent(): Event.Lifecycle.Stopped {
        val randomCause = listOf(
            DisconnectCause.ConnectionReleased,
            DisconnectCause.Error(null),
            DisconnectCause.NetworkNotAvailable,
            DisconnectCause.UnrecoverableError(null)
        ).random()

        return listOf(
            Event.Lifecycle.Stopped.WithReason(cause = randomCause),
            Event.Lifecycle.Stopped.AndAborted(cause = randomCause)
        ).random()
    }

    companion object {
        private val fakeTimeMilliseconds = AtomicLong(0)

        private fun fakeNextMillisecond(): Long = fakeTimeMilliseconds.incrementAndGet()
    }
}
