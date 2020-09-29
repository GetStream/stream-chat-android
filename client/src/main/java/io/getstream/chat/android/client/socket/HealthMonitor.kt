package io.getstream.chat.android.client.socket

import android.os.Handler
import android.os.Looper
import io.getstream.chat.android.client.logger.ChatLogger
import java.util.Date
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

private const val HEALTH_CHECK_INTERVAL = 10 * 1000L
private const val MONITOR_INTERVAL = 1000L
private const val NO_EVENT_INTERVAL_THRESHOLD = HEALTH_CHECK_INTERVAL + 10 * MONITOR_INTERVAL
internal class HealthMonitor(private val healthCallback: HealthCallback) {

    private val delayHandler = Handler(Looper.getMainLooper())
    private var consecutiveFailures = 0
    private var lastEventDate: Date = Date()

    private val logger = ChatLogger.get("SocketMonitor")

    private val reconnect = Runnable {
        healthCallback.reconnect()
    }

    private val healthCheck: Runnable = Runnable {
        logger.logI("Ok")
        consecutiveFailures = 0
        healthCallback.check()
        delayHandler.postDelayed(monitor, HEALTH_CHECK_INTERVAL)
    }

    private val monitor = Runnable {
        if (needToReconnect()) reconnect()
        delayHandler.postDelayed(healthCheck, MONITOR_INTERVAL)
    }

    fun start() {
        logger.logI("Start")
        lastEventDate = Date()
        monitor.run()
    }

    fun stop() {
        delayHandler.removeCallbacks(monitor)
        delayHandler.removeCallbacks(reconnect)
        delayHandler.removeCallbacks(healthCheck)
    }

    fun ack() {
        lastEventDate = Date()
    }

    fun onError() {
        logger.logI("Error")
        reconnect()
    }

    private fun reconnect() {
        val retryInterval = getRetryInterval(++consecutiveFailures)
        logger.logI("Next connection attempt in $retryInterval ms")
        delayHandler.postDelayed(reconnect, retryInterval)
    }

    private fun needToReconnect() = (Date().time - lastEventDate.time) >= NO_EVENT_INTERVAL_THRESHOLD

    private fun getRetryInterval(consecutiveFailures: Int): Long {
        val max = min(500 + consecutiveFailures * 2000, 25000)
        val min = min(
            max(250, (consecutiveFailures - 1) * 2000),
            25000
        )
        return floor(Math.random() * (max - min) + min).toLong()
    }

    interface HealthCallback {
        fun check()
        fun reconnect()
    }
}
