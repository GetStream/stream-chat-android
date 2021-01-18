package com.getstream.sdk.chat.utils

import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

public class StartStopBuffer<T> {

    private val events: Queue<T> = ConcurrentLinkedQueue()
    private var active = AtomicBoolean(true)
    private var func: ((T) -> Unit)? = null
    private var dataFilter: (T) -> T = { it }

    public fun hold() {
        active.set(false)
    }

    public fun active() {
        active.set(true)

        if (func != null) {
            propagateData()
        }
    }

    private fun propagateData() {
        CoroutineScope(DispatcherProvider.IO).launch {
            while (active.get() && events.isNotEmpty()) {
                events.poll()?.let { dataFilter(it) }?.let {
                    withContext(DispatcherProvider.Main) {
                        func?.invoke(it)
                    }
                }
            }
        }
    }

    public fun subscribe(func: (T) -> Unit) {
        this.func = func

        if (active.get()) {
            propagateData()
        }
    }

    public fun enqueueData(data: T) {
        events.offer(data)

        if (active.get()) {
            propagateData()
        }
    }

    public fun setDataFilter(dataFilter: (T) -> T) {
        this.dataFilter = dataFilter
    }
}
