package io.getstream.chat.android.client

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class StreamLifecycleObserver(private val handler: LifecycleHandler) : DefaultLifecycleObserver {
    private var recurringResumeEvent = false

    @Volatile
    private var isObserving = false

    fun observe() {
        if (isObserving.not()) {
            isObserving = true
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch(DispatcherProvider.Main) {
                ProcessLifecycleOwner.get()
                    .lifecycle
                    .addObserver(this@StreamLifecycleObserver)
            }
        }
    }

    fun dispose() {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(DispatcherProvider.Main) {
            ProcessLifecycleOwner.get()
                .lifecycle
                .removeObserver(this@StreamLifecycleObserver)
        }
        isObserving = false
        recurringResumeEvent = false
    }

    override fun onResume(owner: LifecycleOwner) {
        // ignore event when we just started observing the lifecycle
        if (recurringResumeEvent) {
            handler.resume()
        }
        recurringResumeEvent = true
    }

    override fun onStop(owner: LifecycleOwner) {
        handler.stopped()
    }
}

internal interface LifecycleHandler {
    fun resume()
    fun stopped()
}
