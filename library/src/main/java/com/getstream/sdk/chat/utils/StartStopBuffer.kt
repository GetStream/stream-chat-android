package com.getstream.sdk.chat.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

internal class StartStopBuffer<T>(
    private val ioDispatcher: CoroutineContext = Dispatchers.IO
) {

    private val events: Queue<T> = LinkedList<T>()
    private var active = false
    private var func: ((T) -> Unit)? = null

    fun hold() {
        active = false
    }

    fun active() {
        active = true

        if (func != null) {
            propagateData()
        }
    }

    private fun propagateData() {
        while (active && events.isNotEmpty()) {
            events.poll()?.let(func!!)
        }
    }

    fun subscribe(func: (T) -> Unit) {
        this.func = func

        if (active) {
            propagateData()
        }
    }

    fun enqueueData(data: T) {
        events.offer(data)

        GlobalScope.launch(ioDispatcher) {
            active()
        }
    }
}
