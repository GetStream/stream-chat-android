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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Class that coordinates cache for call. If a call is make another time too soon, the cached call gets propagated
 * instead of making another new call.
 *
 * @param cacheTime Int. The value of milli seconds that the cache is not considered to be old.
 */
internal class CallCacheCoordinator(private val cacheTime: Int) : CacheCoordinator {

    private val cachedCalls: MutableMap<Int, CachedCall<out Any>> = ConcurrentHashMap()
    private val lastRequestTime: AtomicLong = AtomicLong()

    /**
     * Creates a cached [Call] instead of a normal [Call].
     */
    override fun <T : Any> cachedCall(hashCode: Int, forceRefresh: Boolean, call: Call<T>): Call<T> {
        cachedCalls.clearIfStale()
        val callData = cachedCalls[hashCode]
        return if (forceRefresh || callData == null || callData.isStale()) {
            call.also {
                val now = System.currentTimeMillis()
                cachedCalls[hashCode] = CachedCall(now, it)
                lastRequestTime.set(now)
            }
        } else {
            callData.call as Call<T>
        }
    }

    /**
     * Evaluates if the last call is olders than the cache time. If it is, all the cache is cleaned.
     */
    private fun MutableMap<Int, CachedCall<out Any>>.clearIfStale() {
        val lastRequestTime = lastRequestTime.get()
        if (lastRequestTime == 0L) return

        val diff = System.currentTimeMillis() - lastRequestTime
        if (diff > cacheTime) {
            clear()
        }
    }

    /**
     * Evaluates if a call is to old based on its request hash.
     */
    private fun CachedCall<out Any>.isStale(): Boolean {
        val now = System.currentTimeMillis()
        val diff = now - requestTime
        return diff > cacheTime
    }
}

private data class CachedCall<T : Any>(val requestTime: Long, val call: Call<T>)
