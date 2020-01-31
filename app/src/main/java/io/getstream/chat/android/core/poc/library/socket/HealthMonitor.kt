package io.getstream.chat.android.core.poc.library.socket

import android.os.Handler
import io.getstream.chat.android.core.poc.library.EventType
import io.getstream.chat.android.core.poc.library.events.LocalEvent
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class HealthMonitor(val socket: ChatSocketService) {

    private val delayHandler = Handler()
    private val healthCheckInterval = 30 * 1000L
    private var consecutiveFailures = 0
    var lastEventDate: Date? = null

    private val reconnect = Runnable {
        socket.setupWS()
    }

    private val healthCheck: Runnable = Runnable {
        if (socket.state is ChatSocketService.State.Connected) {
            socket.sendEvent(LocalEvent(EventType.HEALTH_CHECK))
            delayHandler.postDelayed(monitor, 1000)
        }
    }

    private val monitor = Runnable {
        if (socket.state is ChatSocketService.State.Connected) {
            val millisNow = Date().time
            val monitorInterval = 1000L

            lastEventDate?.let {
                val diff = millisNow - it.time
                val checkInterval = healthCheckInterval + 10 * 1000
                if (diff > checkInterval) {
                    consecutiveFailures += 1
                    reconnect()
                }
            }

            delayHandler.postDelayed(healthCheck, monitorInterval)
        }
    }

    fun start() {
        monitor.run()
    }

    fun reset() {
        lastEventDate = null
        consecutiveFailures = 0
    }

    fun onError() {
        reconnect()
    }

    private fun reconnect() {
        delayHandler.postDelayed(
            reconnect,
            getRetryInterval()
        )
    }

    private fun getRetryInterval(): Long {
        val max = min(500 + consecutiveFailures * 2000, 25000)
        val min = min(
            max(250, (consecutiveFailures - 1) * 2000), 25000
        )
        return floor(Math.random() * (max - min) + min).toLong()
    }
}