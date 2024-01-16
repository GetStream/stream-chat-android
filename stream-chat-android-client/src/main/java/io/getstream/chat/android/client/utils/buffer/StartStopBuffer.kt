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

package io.getstream.chat.android.client.utils.buffer

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

private const val NO_LIMIT = -1

@InternalStreamChatApi
public class StartStopBuffer<T>(
    suffix: String = "Default",
    private val bufferLimit: Int = NO_LIMIT,
    customTrigger: StateFlow<Boolean>? = null,
) {

    private val logger by taggedLogger("Chat:StartStopBuffer-$suffix")

    private val events: Queue<T> = ConcurrentLinkedQueue()
    private var active = AtomicBoolean(true)
    private var func: ((T) -> Unit)? = null

    init {
        logger.i { "<init> customTrigger: $customTrigger" }
        CoroutineScope(DispatcherProvider.IO).launch {
            customTrigger?.collectLatest { active ->
                logger.v { "<init> active: $active" }
                if (active) {
                    active(src = "init")
                } else {
                    hold()
                }
            }
        }
    }

    public fun hold() {
        logger.d { "[hold] no args" }
        active.set(false)
    }

    public fun active(src: String = "active") {
        logger.d { "[active] no args" }
        active.set(true)
        val func = func
        logger.v { "[active] func: $func" }
        if (func != null) {
            propagateData(src = src)
        }
    }

    public fun subscribe(func: (T) -> Unit) {
        this.func = func

        val isActive = active.get()
        logger.d { "[active] isActive: $isActive, func: $func" }
        if (isActive) {
            propagateData(src = "subscribe")
        }
    }

    public fun enqueueData(data: T) {
        logger.d { "[enqueueData] data: ${data?.let { it::class.simpleName }}" }
        events.offer(data)

        val isActive = active.get()
        val aboveSafetyThreshold = aboveSafetyThreshold()
        logger.v { "[enqueueData] isActive: $isActive, aboveSafetyThreshold: $aboveSafetyThreshold" }
        if (isActive || aboveSafetyThreshold) {
            propagateData(src = "enqueue")
        }
    }

    private fun aboveSafetyThreshold(): Boolean = events.size > bufferLimit && bufferLimit != NO_LIMIT

    private fun propagateData(src: String) {
        CoroutineScope(DispatcherProvider.IO).launch {
            val isActive = active.get()
            val hasEvents = events.isNotEmpty()
            val aboveSafetyThreshold = aboveSafetyThreshold()
            val result = isActive && hasEvents || aboveSafetyThreshold()
            logger.d { "[propagateData] #$src; result: $result, isActive: $isActive, " +
                "hasEvents: $hasEvents, aboveSafetyThreshold: $aboveSafetyThreshold" }
            while (result) {
                events.poll()?.let {
                    withContext(DispatcherProvider.Main) {
                        logger.v { "[propagateData] #$src; data: $it" }
                        func?.invoke(it)
                    }
                }
            }
        }
    }
}
