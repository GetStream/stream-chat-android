package com.getstream.sdk.chat.utils.livedata

import androidx.lifecycle.Observer

internal class TestObserver<T> : Observer<T> {
    var lastObservedValue: T? = null
        private set

    override fun onChanged(value: T?) {
        lastObservedValue = value
    }

    fun reset() {
        lastObservedValue = null
    }
}
