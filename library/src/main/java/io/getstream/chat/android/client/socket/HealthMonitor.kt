package io.getstream.chat.android.client.socket

import android.os.Handler
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.EventType
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class HealthMonitor(val socket: ChatSocketServiceImpl) {

    private val delayHandler = Handler()
    private val healthCheckInterval = 30 * 1000L
    private var consecutiveFailures = 0
    var lastEventDate: Date? = null

    private val logger = ChatLogger.get("SocketMonitor")

    private val reconnect = Runnable {
        socket.setupSocket()
    }

    private val healthCheck: Runnable = Runnable {
        if (socket.state is ChatSocketServiceImpl.State.Connected) {
            logger.logI("Ok")
            consecutiveFailures = 0
            socket.sendEvent(ChatEvent(EventType.HEALTH_CHECK))
            delayHandler.postDelayed(monitor, healthCheckInterval)
        }
    }

    private val monitor = Runnable {
        if (socket.state is ChatSocketServiceImpl.State.Connected) {
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
        logger.logI("Start")
        monitor.run()
    }

    fun reset() {
        delayHandler.removeCallbacks(monitor)
        delayHandler.removeCallbacks(reconnect)
        delayHandler.removeCallbacks(healthCheck)
        lastEventDate = null
    }

    fun onError() {
        logger.logI("Error")
        consecutiveFailures++
        reconnect()
    }

    private fun reconnect() {
        val retryInterval = getRetryInterval()
        logger.logI("Next connection attempt in $retryInterval ms")
        delayHandler.postDelayed(
            reconnect,
            retryInterval
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