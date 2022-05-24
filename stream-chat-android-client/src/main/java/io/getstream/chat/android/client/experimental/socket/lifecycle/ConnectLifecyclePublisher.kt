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
import io.getstream.chat.android.client.experimental.socket.ShutdownReason
import io.getstream.chat.android.client.experimental.socket.Timed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach

internal class ConnectLifecyclePublisher : LifecyclePublisher {

    private var _lifecycleEvents = MutableStateFlow<Timed<Event.Lifecycle>?>(null)
    override val lifecycleEvents = _lifecycleEvents.asStateFlow().filterNotNull().onEach {
        println("Lifecycle - Connect: $it")
    }

    override fun observe() {
    }

    override fun dispose() {
    }

    fun onConnect() {
        _lifecycleEvents.tryEmit(Timed(Event.Lifecycle.Started, System.currentTimeMillis()))
    }

    fun onDisconnect(cause: DisconnectCause?) {
        _lifecycleEvents.tryEmit(
            Timed(
                Event.Lifecycle.Stopped.WithReason(
                    shutdownReason = ShutdownReason(
                        1000,
                        "Disconnected by request"
                    ),
                    cause = cause ?: DisconnectCause.ConnectionReleased
                ),
                System.currentTimeMillis()
            )
        )
    }
}
