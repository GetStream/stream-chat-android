package io.getstream.chat.android.client.socket

import kotlinx.coroutines.flow.Flow

internal interface LifecycleObserver {
    val lifecycleEvents: Flow<Timed<Event.Lifecycle>>

    fun observe()

    fun dispose()
}