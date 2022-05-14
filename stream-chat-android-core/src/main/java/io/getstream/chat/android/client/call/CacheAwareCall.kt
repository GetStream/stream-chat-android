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

package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result

public class CacheAwareCall<T : Any>(
    private val originalCall: Call<T>,
    public val creationTime: Long,
    private val interval: Long,
    private val observers: MutableList<Call.Callback<T>> = mutableListOf(),
) : Call<T> {

    private var isExecuted: Boolean = false
    private var isRunning = false
    private var cachedData: Result<T>? = null

    override fun execute(): Result<T> {
        return if (callUpdated() && !isExecuted) {
            isExecuted = true
            originalCall.execute()
        } else {
            originalCall.clone().execute()
        }
    }

    override fun enqueue(callback: Call.Callback<T>) {

        when {
            // The call has already run and the cache is updarted
            hasCache() && callUpdated()  -> {
                callback.onResult(cachedData!!)
            }

            /* The call didn't run yet, is not running and is updated. This is the first run of this call. */
            !hasCache() && !isRunning -> {
                observers.add(callback)
                isRunning = true
                originalCall.enqueue(::handleResult)
            }

            /* The call was called more than once before completing */
            !hasCache() && callUpdated() && isRunning-> {
                observers.add(callback)
            }

            /* The call has already run, but its cache is too old. Clone it and run again. */
            !callUpdated() && hasCache() -> {
                observers.add(callback)
                originalCall.clone().enqueue(::handleResult)
            }
        }
    }

    override fun clone(): Call<T> {
        val clonedObservers = mutableListOf<Call.Callback<T>>().apply {
            addAll(observers)
        }

        return CacheAwareCall(
            originalCall.clone(),
            System.currentTimeMillis(),
            interval,
            clonedObservers
        )
    }

    override fun cancel() {
        originalCall.cancel()
    }

    private fun callUpdated(): Boolean =
        System.currentTimeMillis() < creationTime + interval

    private fun hasCache() = cachedData != null && cachedData?.isSuccess == true

    private fun handleResult(result: Result<T>) {
        cachedData = result

        observers.forEach { observer ->
            observer.onResult(result)
        }

        observers.clear()
        isRunning = false
    }
}
