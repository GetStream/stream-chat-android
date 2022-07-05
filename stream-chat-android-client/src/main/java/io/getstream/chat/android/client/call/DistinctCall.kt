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
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Reusable wrapper around [Call] which delivers a single result to all subscribers.
 */
internal class DistinctCall<T : Any>(
    internal val callBuilder: () -> Call<T>,
    private val onFinished: () -> Unit,
) : Call<T> {

    private val delegate = AtomicReference<Call<T>>()
    private val isRunning = AtomicBoolean()
    private val subscribers = arrayListOf<Call.Callback<T>>()

    override fun execute(): Result<T> {
        return runBlocking {
            suspendCoroutine { continuation ->
                enqueue { result ->
                    continuation.resume(result)
                }
            }
        }
    }

    override fun enqueue(callback: Call.Callback<T>) {
        synchronized(subscribers) {
            subscribers.add(callback)
        }
        if (isRunning.compareAndSet(false, true)) {
            delegate.set(
                callBuilder().apply {
                    enqueue { result ->
                        try {
                            synchronized(subscribers) {
                                subscribers.onResultCatching(result)
                            }
                        } finally {
                            doFinally()
                        }
                    }
                }
            )
        }
    }

    override fun cancel() {
        try {
            delegate.get()?.cancel()
        } finally {
            doFinally()
        }
    }

    private fun doFinally() {
        synchronized(subscribers) {
            subscribers.clear()
        }
        isRunning.set(false)
        delegate.set(null)
        onFinished()
    }

    private fun Collection<Call.Callback<T>>.onResultCatching(result: Result<T>) = forEach { callback ->
        try {
            callback.onResult(result)
        } catch (_: Throwable) {
            /* no-op */
        }
    }

    override suspend fun await(): Result<T> = withContext(DispatcherProvider.IO) {
        execute()
    }
}
