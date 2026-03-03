/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.utils.internal

import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.HealthEvent
import java.util.Date

/**
 * Tracks the offset between the local device clock and the server clock using
 * NTP-style estimation from WebSocket health check round-trips.
 *
 * The algorithm keeps only the sample with the lowest observed RTT, since a
 * smaller round-trip means less room for network asymmetry to distort the
 * measurement. Under the assumption that clock skew is constant for the
 * duration of a session, the estimate monotonically improves over time.
 *
 * Thread-safe: single-field writes use [Volatile] for visibility; compound
 * read-modify-write sequences are guarded by [lock] for atomicity.
 *
 * @param localTimeMs Clock source for the local device time (injectable for tests).
 * @param maxRttMs Upper bound on plausible RTT. Samples exceeding this are
 *   discarded as stale or mismatched. Defaults to the health check cycle
 *   interval (MONITOR_INTERVAL + HEALTH_CHECK_INTERVAL = 11 000 ms).
 */
internal class ServerClockOffset(
    private val localTimeMs: () -> Long = { System.currentTimeMillis() },
    private val maxRttMs: Long = DEFAULT_MAX_RTT_MS,
) {

    private val lock = Any()

    @Volatile
    private var offsetMs: Long = 0L

    @Volatile
    private var bestRttMs: Long = Long.MAX_VALUE

    @Volatile
    private var healthCheckSentAtMs: Long = 0L

    @Volatile
    private var connectionStartedAtMs: Long = 0L

    /**
     * Record the local time immediately before starting a WebSocket connection.
     * When the next [ConnectedEvent] arrives, [onConnected] will pair with this
     * timestamp to compute the offset using the NTP midpoint formula.
     */
    internal fun onConnectionStarted() {
        connectionStartedAtMs = localTimeMs()
    }

    /**
     * Record the local time immediately before sending a health check echo.
     * The next [onHealthCheck] call will pair with this timestamp to compute RTT.
     */
    internal fun onHealthCheckSent() {
        healthCheckSentAtMs = localTimeMs()
    }

    /**
     * Calibration from a [ConnectedEvent].
     *
     * If [onConnectionStarted] was called before this connection (e.g. right before
     * opening the WebSocket), uses the NTP midpoint of (connectionStartedAt, receivedAt)
     * and serverTime for a more accurate offset. Otherwise falls back to a naive
     * `localTime - serverTime` estimate.
     *
     * Resets health check state, since a new connection means any in-flight health
     * check from the previous connection is stale.
     */
    internal fun onConnected(serverTime: Date) {
        synchronized(lock) {
            bestRttMs = Long.MAX_VALUE
            healthCheckSentAtMs = 0L

            val receivedAtMs = localTimeMs()
            val startedAtMs = connectionStartedAtMs
            connectionStartedAtMs = 0L

            if (startedAtMs > 0L) {
                val rtt = receivedAtMs - startedAtMs
                if (rtt in 1..maxRttMs) {
                    offsetMs = (startedAtMs + receivedAtMs) / 2 - serverTime.time
                    bestRttMs = rtt
                    return
                }
            }
            offsetMs = receivedAtMs - serverTime.time
        }
    }

    /**
     * Refine the offset using a [HealthEvent] paired with [onHealthCheckSent].
     *
     * Computes RTT from the stored send time and the current receive time,
     * then applies the NTP midpoint formula:
     * ```
     * offset = (sentAt + receivedAt) / 2 - serverTime
     * ```
     *
     * The sample is accepted only if:
     * - There is a pending [onHealthCheckSent] timestamp.
     * - RTT is positive (guards against clock anomalies).
     * - RTT is below [maxRttMs] (rejects stale / mismatched pairs).
     * - RTT is lower than any previous sample (min-RTT selection).
     */
    internal fun onHealthCheck(serverTime: Date) {
        synchronized(lock) {
            val sentAtMs = healthCheckSentAtMs
            if (sentAtMs <= 0L) return
            healthCheckSentAtMs = 0L

            val receivedAtMs = localTimeMs()
            val rtt = receivedAtMs - sentAtMs
            if (rtt !in 1..maxRttMs) return

            if (rtt < bestRttMs) {
                bestRttMs = rtt
                offsetMs = (sentAtMs + receivedAtMs) / 2 - serverTime.time
            }
        }
    }

    /**
     * Returns the current time adjusted to the server timescale.
     *
     * Before the first [onConnected] call, this returns the raw local time
     * (offset = 0).
     */
    internal fun estimatedServerTime(): Date =
        Date(localTimeMs() - offsetMs)

    internal companion object {
        internal const val DEFAULT_MAX_RTT_MS = 11_000L
    }
}
