package io.getstream.chat.android.client

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import io.getstream.chat.android.client.internal.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class StreamLifecycleObserver(private val handler: LifecycleHandler) : LifecycleObserver {
    private var recurringResumeEvent = false

    fun observe() {
        CoroutineScope(DispatcherProvider.Main).launch {
            ProcessLifecycleOwner.get()
                .lifecycle
                .addObserver(this@StreamLifecycleObserver)
        }
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

internal interface LifecycleHandler {
    fun resume()
    fun stopped()
}
