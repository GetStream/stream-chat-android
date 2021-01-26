package io.getstream.chat.android.client.uploader

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

public object ProgressTrackerFactory {

    private val instanceMap: ConcurrentMap<String, ProgressTracker> = ConcurrentHashMap()

    public fun getOrCreate(id: String): ProgressTracker {
        return if (instanceMap.containsKey(id)) {
            instanceMap[id]!!
        } else {
            ProgressTracker().also { tracker ->
                instanceMap[id] = tracker
            }
        }
    }
}
