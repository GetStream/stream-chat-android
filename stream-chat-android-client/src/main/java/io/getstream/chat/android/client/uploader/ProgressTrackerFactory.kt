package io.getstream.chat.android.client.uploader

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.util.concurrent.ConcurrentHashMap

@InternalStreamChatApi
public object ProgressTrackerFactory {

    private val instanceMap: MutableMap<String, ProgressTracker> = ConcurrentHashMap()

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
