package com.getstream.sdk.chat.utils

import io.getstream.chat.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

internal class StartStopBuffer<T> {

    private val events: Queue<T> = ConcurrentLinkedQueue()
    private var active = AtomicBoolean(true)
    private var func: ((T) -> Unit)? = null

    fun hold() {
        active.set(false)
    }

    fun active() {
        active.set(true)

        if (func != null) {
            propagateData()
        }
    }

    private fun propagateData() {
        CoroutineScope(DispatcherProvider.IO).launch {
            while (active.get() && events.isNotEmpty()) {
                events.poll()?.let {
                    withContext(DispatcherProvider.Main) {
                        func?.invoke(it)
                    }
                }
            }
        }
    }

    fun subscribe(func: (T) -> Unit) {
        this.func = func

        if (active.get()) {
            propagateData()
        }
    }

    fun enqueueData(data: T) {
        events.offer(data)

        if (active.get()) {
            propagateData()
        }
    }
}
