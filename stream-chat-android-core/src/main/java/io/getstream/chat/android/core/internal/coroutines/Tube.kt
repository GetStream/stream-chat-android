/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.core.internal.coroutines

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A synchronous data stream that emits values and completes normally.
 */
@InternalStreamChatApi
public class Tube<T> : Flow<T>, FlowCollector<T> {

    private val mutex = Mutex()
    private val collectors = hashSetOf<FlowCollector<T>>()

    /**
     * Adds the given [collector] into [collectors] and suspends until cancellation.
     *
     * This method is **thread-safe** and can be safely invoked from concurrent coroutines without
     * external synchronization.
     */
    override suspend fun collect(collector: FlowCollector<T>) {
        try {
            mutex.withLock {
                collectors.add(collector)
            }
            awaitCancellation()
        } catch (_: Throwable) {
            /* no-op */
        } finally {
            mutex.withLock {
                collectors.remove(collector)
            }
        }
    }

    /**
     * Emits a [value] to the [collectors], suspending until the [value] is fully consumed.
     *
     * This method is **thread-safe** and can be safely invoked from concurrent coroutines without
     * external synchronization.
     */
    override suspend fun emit(value: T) {
        mutex.withLock {
            collectors.forEach {
                try {
                    it.emit(value)
                } catch (_: Throwable) {
                    /* no-op */
                }
            }
        }
    }
}
