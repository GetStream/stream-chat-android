package com.getstream.sdk.chat;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

public class StreamLifecycleObserver implements LifecycleObserver {
    LifecycleHandler handler;

    private boolean recurringResumeEvent = false;

    public StreamLifecycleObserver(LifecycleHandler handler) {
        this.handler = handler;
    }

    public void observe() {
        ProcessLifecycleOwner.get()
                .getLifecycle()
                .addObserver(this);
    }

    public void dispose() {
        ProcessLifecycleOwner.get()
                .getLifecycle()
                .removeObserver(this);
        recurringResumeEvent = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        // ignore event when we just started observing the lifecycle
        if (recurringResumeEvent) {
            handler.resume();
        }
        recurringResumeEvent = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStopped() {
        handler.stopped();
    }
}
