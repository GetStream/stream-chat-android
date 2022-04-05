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
 
package io.getstream.chat.android.client

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class StreamLifecycleObserver(private val handler: LifecycleHandler) : LifecycleObserver {
    private var recurringResumeEvent = false
    @Volatile private var isObserving = false

    fun observe() {
        if (isObserving.not()) {
            isObserving = true
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch(DispatcherProvider.Main) {
                ProcessLifecycleOwner.get()
                    .lifecycle
                    .addObserver(this@StreamLifecycleObserver)
            }
        }
    }

    fun dispose() {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(DispatcherProvider.Main) {
            ProcessLifecycleOwner.get()
                .lifecycle
                .removeObserver(this@StreamLifecycleObserver)
        }
        isObserving = false
        recurringResumeEvent = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        // ignore event when we just started observing the lifecycle
        if (recurringResumeEvent) {
            handler.resume()
        }
        recurringResumeEvent = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStopped() {
        handler.stopped()
    }
}

internal interface LifecycleHandler {
    fun resume()
    fun stopped()
}
