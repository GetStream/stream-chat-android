package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result
import io.getstream.logging.StreamLog
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Reusable wrapper around [Call] which delivers a single result to all subscribers.
 */
internal class DistinctCall<T : Any>(
    private val callBuilder: () -> Call<T>,
    private val uniqueKey: Int,
    private val onFinished: () -> Unit,
) : Call<T> {

    init {
        StreamLog.i(TAG) { "<init> uniqueKey: $uniqueKey" }
    }

    private val delegate = AtomicReference<Call<T>>()
    private val isRunning = AtomicBoolean()
    private val subscribers = arrayListOf<Call.Callback<T>>()

    override fun execute(): Result<T> {
        return runBlocking {
            StreamLog.d(TAG) { "[execute] uniqueKey: $uniqueKey" }
            suspendCoroutine { continuation ->
                enqueue { result ->
                    StreamLog.v(TAG) { "[execute] completed($uniqueKey)" }
                    continuation.resume(result)
                }
            }
        }
    }

    override fun enqueue(callback: Call.Callback<T>) {
        StreamLog.d(TAG) { "[enqueue] callback($$uniqueKey): $callback" }
        synchronized(subscribers) {
            subscribers.add(callback)
        }
        if (isRunning.compareAndSet(false, true)) {
            delegate.set(
                callBuilder().apply {
                    enqueue { result ->
                        try {
                            synchronized(subscribers) {
                                StreamLog.v(TAG) { "[enqueue] completed($uniqueKey): ${subscribers.size}" }
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
            StreamLog.d(TAG) { "[cancel] uniqueKey: $uniqueKey" }
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

    private companion object {
        private const val TAG = "Chat:DistinctCall"
    }
}
