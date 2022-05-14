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

    override fun execute(): Result<T> {
        return if (isExecutionAllowed() && !isExecuted) {
            isExecuted = true
            originalCall.execute()
        } else {
            originalCall.clone().execute()
        }
    }

    override fun enqueue(callback: Call.Callback<T>) {
        when {
            !isRunning && isExecutionAllowed() -> {
                observers.add(callback)
                isRunning = true
                originalCall.enqueue(::handleResult)
            }

            isRunning && isExecutionAllowed() -> {
                observers.add(callback)
            }

            !isExecutionAllowed() -> {
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

    private fun isExecutionAllowed(): Boolean =
        System.currentTimeMillis() < creationTime + interval

    private fun handleResult(result: Result<T>) {
        observers.forEach { observer ->
            observer.onResult(result)
        }

        observers.clear()
        isRunning = false
    }
}
