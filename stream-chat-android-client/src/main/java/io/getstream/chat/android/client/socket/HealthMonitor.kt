/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.socket

import io.getstream.logging.StreamLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

private const val HEALTH_CHECK_INTERVAL = 1_000L
private const val MONITOR_INTERVAL = 10_000L
private const val NO_EVENT_INTERVAL_THRESHOLD = 30_000L

internal class HealthMonitor(
    private val coroutineScope: CoroutineScope,
    private val checkCallback: () -> Unit,
    private val reconnectCallback: () -> Unit,
) {

    private var consecutiveFailures = 0
    private var lastAckDate: Date = Date()
    private var healthMonitorJob: Job? = null
    private var healthCheckJob: Job? = null
    private var reconnectJob: Job? = null

    private val logger = StreamLog.getLogger("Chat:SocketMonitor")

    /**
     * Start monitoring connection.
     */
    fun start() {
        logger.d { "Starting Health Monitor" }
        resetHealthMonitor()
    }

    /**
     * Stop monitoring connection.
     */
    fun stop() {
        stopAllJobs()
    }

    /**
     * Notify that connection keeps alive.
     */
    fun ack() {
        resetHealthMonitor()
    }

    /**
     * Notify connection is disconnected.
     */
    fun onDisconnected() {
        stopAllJobs()
        postponeReconnect()
    }

    /**
     * Reset health monitor process.
     */
    private fun resetHealthMonitor() {
        stopAllJobs()
        lastAckDate = Date()
        consecutiveFailures = 0
        postpoeHealthMonitor()
    }

    /**
     * Postpone the action to check if connection keeps alive.
     * If the connection is not alive anymore, an action to reconnect is postponed.
     * In another case the healthCheck is postponed.
     */
    private fun postpoeHealthMonitor() {
        healthMonitorJob?.cancel()
        healthMonitorJob = coroutineScope.launchDelayed(MONITOR_INTERVAL) {
            if (needToReconnect()) {
                postponeReconnect()
            } else {
                postponeHealthCheck()
            }
        }
    }

    /**
     * Postpone the action to send an "echo event" that will keep the connection alive.
     * Just after the event is sent, an action is postponed to verify the connection is alive.
     */
    private fun postponeHealthCheck() {
        healthCheckJob?.cancel()
        healthCheckJob = coroutineScope.launchDelayed(HEALTH_CHECK_INTERVAL) {
            checkCallback()
            postpoeHealthMonitor()
        }
    }

    /**
     * Postpone the action to reconnect the socket.
     * Just after the reconnection of the socket is started, an action to monitor the connection is started.
     */
    private fun postponeReconnect() {
        reconnectJob?.cancel()
        val retryInterval = getRetryInterval(++consecutiveFailures)
        logger.i { "Next connection attempt in $retryInterval ms" }
        reconnectJob = coroutineScope.launchDelayed(retryInterval) {
            reconnectCallback()
            postpoeHealthMonitor()
        }
    }

    /**
     * Stop all launched job on this health monitor.
     */
    private fun stopAllJobs() {
        reconnectJob?.cancel()
        healthCheckJob?.cancel()
        healthMonitorJob?.cancel()
    }

    /**
     * Check if time elapsed since the last received event is greater than [NO_EVENT_INTERVAL_THRESHOLD].
     *
     * @return True if time elapsed is bigger and we need to start reconnection process.
     */
    private fun needToReconnect(): Boolean = (Date().time - lastAckDate.time) >= NO_EVENT_INTERVAL_THRESHOLD

    @Suppress("MagicNumber")
    private fun getRetryInterval(consecutiveFailures: Int): Long {
        val max = min(500 + consecutiveFailures * 2000, 25000)
        val min = min(
            max(250, (consecutiveFailures - 1) * 2000),
            25000
        )
        return floor(Math.random() * (max - min) + min).toLong()
    }

    private fun CoroutineScope.launchDelayed(
        delayMiliseconds: Long,
        block: suspend CoroutineScope.() -> Unit
    ): Job = launch {
        delay(delayMiliseconds)
        block()
    }
}
