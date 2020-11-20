package io.getstream.chat.android.client

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import io.getstream.chat.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class StreamLifecycleObserver(private val handler: LifecycleHandler) : LifecycleObserver {
    private var recurringResumeEvent = false
    @Volatile private var isObserving = false

    fun observe() {
        if (isObserving.not()) {
            isObserving = true
            GlobalScope.launch(DispatcherProvider.Main) {
                ProcessLifecycleOwner.get()
                    .lifecycle
                    .addObserver(this@StreamLifecycleObserver)
            }
        }
    }

    fun dispose() {
        GlobalScope.launch(DispatcherProvider.Main) {
            ProcessLifecycleOwner.get()
                .lifecycle
                .removeObserver(this@StreamLifecycleObserver)
        }
        isObserving = false
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
