package io.getstream.chat.android.core.internal.coroutines

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@InternalStreamChatApi
public class Tube<T> : Flow<T>, FlowCollector<T> {

    private val mutex = Mutex()
    private val collectors = hashSetOf<FlowCollector<T>>()

    override suspend fun collect(collector: FlowCollector<T>) {
        try {
            mutex.withLock {
                collectors.add(collector)
            }
            awaitCancellation()
        } catch (e: Throwable) {
            mutex.withLock {
                collectors.remove(collector)
            }
        }
    }

    override suspend fun emit(value: T) {
        mutex.withLock {
            collectors.forEach {
                it.emit(value)
            }
        }
    }
}