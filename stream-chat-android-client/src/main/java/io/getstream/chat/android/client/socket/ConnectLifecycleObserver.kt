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

package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.clientstate.DisconnectCause
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class ConnectLifecycleObserver: LifecycleObserver {

    @Volatile
    private var isObserving = false

    private var _lifecycleEvents = MutableSharedFlow<Timed<Event.Lifecycle>>(extraBufferCapacity = 1)
    override val lifecycleEvents = _lifecycleEvents.asSharedFlow()

    override fun observe() {

    }

    override fun dispose() {

    }

    fun onConnect() {
        _lifecycleEvents.tryEmit(Timed(Event.Lifecycle.Started, System.currentTimeMillis()))
    }

    fun onDisconnect() {
        _lifecycleEvents.tryEmit(
            Timed(
                Event.Lifecycle.Stopped.WithReason(
                    shutdownReason = ShutdownReason(
                        1000,
                        "App is paused"
                    ),
                    cause = DisconnectCause.ConnectionReleased
                ),
                System.currentTimeMillis()
            )
        )
    }
}
