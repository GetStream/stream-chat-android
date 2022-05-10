package io.getstream.chat.android.offline.plugin.state.internal

import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

private const val MAX_TIME_OF_STATE_MILLIS = 300

internal class CacheCoordinator {

    private val requestTimeMap: MutableMap<Int, Date> = ConcurrentHashMap()
    private var globalLastRequest: AtomicReference<Date?> = AtomicReference()

    internal fun cachedBlock(queryHashCode: Int, forceRefresh: Boolean, block: () -> Unit) {
        evaluateGlobalState()

        if (isStateOld(queryHashCode) || forceRefresh) {
            val now = Date()

            block.invoke()

            requestTimeMap[queryHashCode] = now
            globalLastRequest.set(now)
        }
    }

    private fun evaluateGlobalState() {
        val lastRequest = globalLastRequest.get() ?: return

        val now = Date()
        val diff = now.time - lastRequest.time

        if (diff > MAX_TIME_OF_STATE_MILLIS) {
            requestTimeMap.clear()
        }
    }

    private fun isStateOld(requestHash: Int): Boolean {
        if (!requestTimeMap.containsKey(requestHash)) return true

        val now = Date()
        val diff = now.time - requestTimeMap[requestHash]!!.time

        return diff > MAX_TIME_OF_STATE_MILLIS
    }
}
