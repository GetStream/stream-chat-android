package io.getstream.chat.android.livedata.utils

import java.util.concurrent.atomic.AtomicReference

internal fun <T> AtomicReference<T>.mutate(mutateAction: (T) -> T): T {
    do {
        val copy = get()
    } while (!compareAndSet(copy, mutateAction(copy)))
    return get()
}

internal fun <K, V> AtomicReference<Map<K, V>>.getOrPut(key: K, valueProvider: () -> V): V {
    return get()[key] ?: mutate { map -> map + (key to valueProvider()) }[key]
        ?: error("Value must present in map")
}
