package io.getstream.chat.android.livedata.utils

import androidx.lifecycle.Observer

internal class LiveDataTestObserver<T> : Observer<T> {
    var lastObservedValue: T? = null
        private set

    override fun onChanged(value: T?) {
        lastObservedValue = value
    }

    fun reset() {
        lastObservedValue = null
    }
}
