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

package io.getstream.chat.android.client.cache

import io.getstream.chat.android.client.call.Call
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

internal class CallCacheCoordinator(private val cacheTime: Int) : CacheCoordinator {

    private val requestTimeMap: MutableMap<Int, CallData<out Any>> = ConcurrentHashMap()
    private var globalLastRequest: AtomicReference<Date?> = AtomicReference()

    override fun <T : Any> cachedCall(hashCode: Int, forceRefresh: Boolean, call: Call<T>): Call<T> {
        evaluateGlobalState()

        return if (isStateOld(hashCode) || forceRefresh) {
            val now = Date()

            requestTimeMap[hashCode] = CallData(now, call)
            globalLastRequest.set(now)

            call
        } else {
            requestTimeMap[hashCode]!!.call.let { data ->
                data as Call<T>
            }
        }
    }

    private fun evaluateGlobalState() {
        val lastRequest = globalLastRequest.get() ?: return

        val now = Date()
        val diff = now.time - lastRequest.time

        if (diff > cacheTime) {
            requestTimeMap.clear()
        }
    }

    private fun isStateOld(requestHash: Int): Boolean {
        if (!requestTimeMap.containsKey(requestHash)) return true

        val now = Date()
        val diff = now.time - requestTimeMap[requestHash]!!.requestTime.time

        return diff > cacheTime
    }
}

private data class CallData<T : Any>(val requestTime: Date, val call: Call<T>)
