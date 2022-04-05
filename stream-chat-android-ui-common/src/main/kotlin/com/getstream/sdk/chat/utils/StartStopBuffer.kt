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
 
package com.getstream.sdk.chat.utils

import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

public class StartStopBuffer<T> {

    private val events: Queue<T> = ConcurrentLinkedQueue()
    private var active = AtomicBoolean(true)
    private var func: ((T) -> Unit)? = null

    public fun hold() {
        active.set(false)
    }

    public fun active() {
        active.set(true)

        if (func != null) {
            propagateData()
        }
    }

    private fun propagateData() {
        CoroutineScope(DispatcherProvider.IO).launch {
            while (active.get() && events.isNotEmpty()) {
                events.poll()?.let {
                    withContext(DispatcherProvider.Main) {
                        func?.invoke(it)
                    }
                }
            }
        }
    }

    public fun subscribe(func: (T) -> Unit) {
        this.func = func

        if (active.get()) {
            propagateData()
        }
    }

    public fun enqueueData(data: T) {
        events.offer(data)

        if (active.get()) {
            propagateData()
        }
    }
}
