package io.getstream.chat.android.client.uploader

public class ProgressTrackerFactory {

    public companion object {
        private val instanceMap: MutableMap<String, ProgressTracker> = mutableMapOf()

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
}
