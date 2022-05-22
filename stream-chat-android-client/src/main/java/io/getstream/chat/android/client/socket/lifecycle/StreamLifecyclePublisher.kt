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

package io.getstream.chat.android.client.socket.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.socket.Event
import io.getstream.chat.android.client.socket.ShutdownReason
import io.getstream.chat.android.client.socket.Timed
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class StreamLifecyclePublisher : DefaultLifecycleObserver, LifecyclePublisher {
    private var recurringResumeEvent = false

    @Volatile
    private var isObserving = false

    private var _lifecycleEvents = MutableStateFlow<Timed<Event.Lifecycle>?>(null)
    override val lifecycleEvents = _lifecycleEvents.asStateFlow().filterNotNull().onEach {
        println("Lifecycle - Stream: $it")
    }

    override fun observe() {
        if (isObserving.not()) {
            isObserving = true
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch(DispatcherProvider.Main) {
                ProcessLifecycleOwner.get()
                    .lifecycle
                    .addObserver(this@StreamLifecyclePublisher)
            }
        }
    }

    override fun dispose() {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(DispatcherProvider.Main) {
            ProcessLifecycleOwner.get()
                .lifecycle
                .removeObserver(this@StreamLifecyclePublisher)
        }
        isObserving = false
        recurringResumeEvent = false
    }

    override fun onResume(owner: LifecycleOwner) {
        _lifecycleEvents.tryEmit(Timed(Event.Lifecycle.Started, System.currentTimeMillis()))
    }

    override fun onStop(owner: LifecycleOwner) {
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
