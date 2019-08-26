package com.getstream.sdk.chat;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

public class StreamLifecycleObserver implements LifecycleObserver {
    LifecycleHandler handler;

    public StreamLifecycleObserver(LifecycleHandler handler) {
        this.handler = handler;
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        handler.resume();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStopped() {
        handler.stopped();
    }
}
