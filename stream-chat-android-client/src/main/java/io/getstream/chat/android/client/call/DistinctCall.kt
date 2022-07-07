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

import io.getstream.chat.android.client.call.Call.Companion.callCanceledError
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
    private val scope: CoroutineScope,
    internal val callBuilder: () -> Call<T>,
    private val onFinished: () -> Unit,
) : Call<T> {

    private val delegate = AtomicReference<Call<T>>()
    private val isRunning = AtomicBoolean(false)
    private val isCancel = AtomicBoolean(false)
    private val subscribers = mapOf<Boolean, MutableList<Call.Callback<T>>>(
        true to mutableListOf(),
        false to mutableListOf()
    )
    override fun execute(): Result<T> = runBlocking { await() }

    override fun enqueue(callback: Call.Callback<T>) {
        subscribeCallback(false, callback)
        scope.launch {
            tryToRun {
                suspendCoroutine { continuation ->
                    this.enqueue { continuation.resume(it) }
                }
            }
        }
    }

    override fun cancel() {
        isCancel.set(true)
        delegate.get()?.cancel()
    }

    private fun doFinally() {
        synchronized(subscribers) {
            subscribers.forEach { it.value.clear() }
        }
        isCancel.set(false)
        isRunning.set(false)
        delegate.set(null)
        onFinished()
    }
    private fun originalCall(): Call<T> = callBuilder().also { delegate.set(it) }

    private fun subscribeCallback(notifyEventOnCancel: Boolean = true, callback: Call.Callback<T>) {
        synchronized(subscribers) {
            subscribers[notifyEventOnCancel]?.add(callback)
        }
    }

    private suspend fun notifyResult(result: Result<T>) =
        withContext(DispatcherProvider.Main) {
            synchronized(subscribers) {
                subscribers.flatMap { it.takeIf { it.key or isCancel.get().not() }?.value ?: emptyList() }
                    .forEach { it.onResult(result) }
            }
            withContext(DispatcherProvider.IO) { doFinally() }
        }

    private suspend fun tryToRun(command: suspend Call<T>.() -> Result<T>): Result<T>? =
        if (!isRunning.getAndSet(true)) {
            originalCall().command()
        } else {
            null
        }?.also { notifyResult(it.takeUnless { isCancel.get() } ?: callCanceledError()) }

    override suspend fun await(): Result<T> = withContext(DispatcherProvider.IO) {
        tryToRun { this.await() }
            ?: suspendCoroutine { continuation ->
                subscribeCallback { continuation.resume(it) }
            }
    }
}
