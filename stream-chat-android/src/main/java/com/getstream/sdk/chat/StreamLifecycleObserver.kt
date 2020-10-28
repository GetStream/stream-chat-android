package com.getstream.sdk.chat

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner

internal class StreamLifecycleObserver(var handler: LifecycleHandler) : LifecycleObserver {
    private var recurringResumeEvent = false

    fun observe() {
        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(this)
    }

    fun dispose() {
        ProcessLifecycleOwner.get()
            .lifecycle
            .removeObserver(this)
        recurringResumeEvent = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        // ignore event when we just started observing the lifecycle
        if (recurringResumeEvent) {
            handler.resume()
        }
        recurringResumeEvent = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStopped() {
        handler.stopped()
    }
}
