package io.getstream.chat.android.test

import androidx.lifecycle.Observer

public class TestObserver<T> : Observer<T> {
    public var lastObservedValue: T? = null
        private set

    override fun onChanged(value: T?) {
        lastObservedValue = value
    }

    public fun reset() {
        lastObservedValue = null
    }
}
