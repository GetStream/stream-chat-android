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

package io.getstream.chat.android.ai.compose.util.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

/**
 * Loads each key once at a time: callers that ask for a key while it loads wait for the same load.
 * The load runs in [scope], and is cancelled once every caller waiting for it is.
 *
 * @param scope Where loads run. Give it a `SupervisorJob`, so one failing load leaves the others.
 */
internal class SharedLoads<T>(private val scope: CoroutineScope) {

    private class Load<T>(val result: Deferred<T>) {
        var waiting = 0
    }

    private val lock = Any()
    private val loads = HashMap<String, Load<T>>()

    /** How many loads are under way. */
    val size: Int
        get() = synchronized(lock) { loads.size }

    /**
     * What [fetch] gives for [key], from the load under way when there is one.
     *
     * @param key What is loaded.
     * @param fetch Loads it, when no load of [key] is under way.
     */
    suspend fun load(key: String, fetch: suspend () -> T): T {
        val load = synchronized(lock) {
            loads.getOrPut(key) { Load(scope.async(start = CoroutineStart.LAZY) { fetch() }) }
                .also { it.waiting++ }
        }
        load.result.start()
        try {
            return load.result.await()
        } finally {
            synchronized(lock) {
                load.waiting--
                if (load.waiting == 0) {
                    if (loads[key] === load) loads.remove(key)
                    // Nobody waits for it any more; nothing happens when it is done.
                    load.result.cancel()
                }
            }
        }
    }
}
