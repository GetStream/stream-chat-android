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

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.experimental.socket.Event
import io.getstream.chat.android.client.experimental.socket.ShutdownReason
import io.getstream.chat.android.client.experimental.socket.Timed
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.logging.StreamLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

internal class StreamLifecyclePublisher(
    private val lifecycle: Lifecycle,
) : DefaultLifecycleObserver, LifecyclePublisher {
    private val logger = StreamLog.getLogger("Chat:StreamLifecycle")

    @Volatile
    private var isObserving = false

    private var _lifecycleEvents = MutableStateFlow<Timed<Event.Lifecycle>?>(null)
    override val lifecycleEvents = _lifecycleEvents.asStateFlow().filterNotNull().onEach {
        logger.d { "$it" }
    }

    override suspend fun observe() {
        if (isObserving.not()) {
            isObserving = true
            withContext(DispatcherProvider.Main) {
                lifecycle.addObserver(this@StreamLifecyclePublisher)
            }
        }
    }

    override suspend fun dispose() {
        if (isObserving) {
            withContext(DispatcherProvider.Main) {
                lifecycle.removeObserver(this@StreamLifecyclePublisher)
            }
        }
        isObserving = false
    }

    override fun onResume(owner: LifecycleOwner) {
        _lifecycleEvents.tryEmit(Timed(Event.Lifecycle.Started, System.currentTimeMillis()))
    }

    override fun onStop(owner: LifecycleOwner) {
        _lifecycleEvents.tryEmit(
            Timed(
                Event.Lifecycle.Stopped.WithReason(
                    shutdownReason = ShutdownReason.GRACEFUL.copy(reason = "App is paused"),
                    cause = DisconnectCause.ConnectionReleased
                ),
                System.currentTimeMillis()
            )
        )
    }
}
