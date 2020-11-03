package com.getstream.sdk.chat.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class StartStopBuffer<T>(
    private val ioDispatcher: CoroutineContext = Dispatchers.IO
) {

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
        GlobalScope.launch(ioDispatcher) {
            while (active.get() && events.isNotEmpty()) {
                events.poll()?.let {
                    withContext(Dispatchers.Main) {
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
