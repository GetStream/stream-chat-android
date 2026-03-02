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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

internal class ServerClockOffsetTest {

    // ── estimatedServerTime before any calibration ──────────────────────

    @Test
    fun `estimatedServerTime equals local time before any calibration`() {
        val sut = ServerClockOffset(localTimeMs = { 1_000_000L })

        assertEquals(Date(1_000_000L), sut.estimatedServerTime())
    }

    // ── onConnected (naive one-way estimate) ────────────────────────────

    @Test
    fun `onConnected calibrates when local clock is ahead`() {
        val sut = ServerClockOffset(localTimeMs = { 10_000L })

        sut.onConnected(serverTime = Date(7_000L))

        assertEquals(Date(7_000L), sut.estimatedServerTime())
    }

    @Test
    fun `onConnected calibrates when local clock is behind`() {
        val sut = ServerClockOffset(localTimeMs = { 5_000L })

        sut.onConnected(serverTime = Date(8_000L))

        assertEquals(Date(8_000L), sut.estimatedServerTime())
    }

    @Test
    fun `onConnected resets health check state from previous connection`() {
        var localTime = 10_000L
        val sut = ServerClockOffset(localTimeMs = { localTime })

        sut.onHealthCheckSent()

        localTime = 10_200L
        sut.onConnected(serverTime = Date(10_100L))

        localTime = 10_400L
        sut.onHealthCheck(serverTime = Date(10_300L))
        assertEquals(Date(10_100L + (10_400L - 10_200L)), sut.estimatedServerTime())
    }

    // ── onConnectionStarted + onConnected (NTP for initial connection) ───

    @Test
    fun `onConnected uses NTP midpoint when onConnectionStarted was called`() {
        val skew = 3_000L
        var localTime = 10_000L
        val sut = ServerClockOffset(localTimeMs = { localTime })

        sut.onConnectionStarted()

        localTime = 10_200L
        val serverTimeAtMidpoint = (10_000L + 10_200L) / 2 - skew
        sut.onConnected(serverTime = Date(serverTimeAtMidpoint))

        // offset = (10_000 + 10_200) / 2 - serverTimeAtMidpoint = 3_000
        localTime = 15_000L
        assertEquals(Date(15_000L - skew), sut.estimatedServerTime())
    }

    @Test
    fun `onConnected falls back to naive when onConnectionStarted was not called`() {
        val localTime = 10_000L
        val sut = ServerClockOffset(localTimeMs = { localTime })

        sut.onConnected(serverTime = Date(7_000L))

        assertEquals(Date(7_000L), sut.estimatedServerTime())
    }

    @Test
    fun `onConnected rejects connection pair when RTT exceeds maxRttMs and uses naive`() {
        var localTime = 0L
        val sut = ServerClockOffset(localTimeMs = { localTime }, maxRttMs = 100L)

        sut.onConnectionStarted()

        localTime = 500L
        sut.onConnected(serverTime = Date(250L))

        // RTT = 500 > maxRttMs = 100 → rejected, naive used: offset = 500 - 250 = 250
        assertEquals(Date(500L - 250L), sut.estimatedServerTime())
    }

    @Test
    fun `onConnectionStarted is consumed so second onConnected uses naive`() {
        var localTime = 0L
        val sut = ServerClockOffset(localTimeMs = { localTime })

        sut.onConnectionStarted()
        localTime = 100L
        sut.onConnected(serverTime = Date(50L))

        localTime = 1_000L
        sut.onConnected(serverTime = Date(999L))

        // No connectionStartedAtMs (consumed), so naive: offset = 1000 - 999 = 1
        assertEquals(Date(1_000L - 1L), sut.estimatedServerTime())
    }

    // ── onHealthCheck (NTP midpoint with min-RTT selection) ─────────────

    @Test
    fun `onHealthCheck computes NTP midpoint offset`() {
        val skew = 3_000L
        var localTime = 10_000L
        val sut = ServerClockOffset(localTimeMs = { localTime })
        sut.onConnected(serverTime = Date(localTime - skew))

        localTime = 20_000L
        sut.onHealthCheckSent()

        localTime = 20_200L
        sut.onHealthCheck(serverTime = Date(17_100L))

        // offset = (20_000 + 20_200) / 2 - 17_100 = 3_000
        // estimatedServerTime = 20_200 - 3_000 = 17_200
        assertEquals(Date(17_200L), sut.estimatedServerTime())
    }

    @Test
    fun `onHealthCheck keeps lowest RTT sample`() {
        var localTime = 0L
        val sut = ServerClockOffset(localTimeMs = { localTime })
        sut.onConnected(serverTime = Date(0L))

        // First health check: RTT = 500
        localTime = 1_000L
        sut.onHealthCheckSent()
        localTime = 1_500L
        sut.onHealthCheck(serverTime = Date(1_250L))
        val offsetAfterFirst = (1_000L + 1_500L) / 2 - 1_250L

        // Second health check: RTT = 100 (better)
        localTime = 2_000L
        sut.onHealthCheckSent()
        localTime = 2_100L
        sut.onHealthCheck(serverTime = Date(2_050L))
        val offsetAfterSecond = (2_000L + 2_100L) / 2 - 2_050L

        localTime = 5_000L
        assertEquals(Date(5_000L - offsetAfterSecond), sut.estimatedServerTime())
    }

    @Test
    fun `onHealthCheck ignores higher RTT sample`() {
        var localTime = 0L
        val sut = ServerClockOffset(localTimeMs = { localTime })
        sut.onConnected(serverTime = Date(0L))

        // First health check: RTT = 100 (best)
        localTime = 1_000L
        sut.onHealthCheckSent()
        localTime = 1_100L
        sut.onHealthCheck(serverTime = Date(1_050L))
        val bestOffset = (1_000L + 1_100L) / 2 - 1_050L

        // Second health check: RTT = 500 (worse -- ignored)
        localTime = 2_000L
        sut.onHealthCheckSent()
        localTime = 2_500L
        sut.onHealthCheck(serverTime = Date(2_250L))

        localTime = 5_000L
        assertEquals(Date(5_000L - bestOffset), sut.estimatedServerTime())
    }

    @Test
    fun `onHealthCheck overrides naive onConnected estimate`() {
        val skew = 3_000L
        var localTime = 10_000L
        val sut = ServerClockOffset(localTimeMs = { localTime })
        sut.onConnected(serverTime = Date(localTime - skew))

        // Naive estimate at localTime = 10_000: offset = 3_000
        assertEquals(Date(7_000L), sut.estimatedServerTime())

        localTime = 20_000L
        sut.onHealthCheckSent()
        localTime = 20_200L
        sut.onHealthCheck(serverTime = Date(17_100L))

        // NTP offset = (20_000 + 20_200) / 2 - 17_100 = 3_000
        // At localTime = 20_200: estimated = 20_200 - 3_000 = 17_200
        assertEquals(Date(17_200L), sut.estimatedServerTime())
    }

    // ── Guards: mismatched / stale / implausible pairs ──────────────────

    @Test
    fun `onHealthCheck is no-op without prior onHealthCheckSent`() {
        var localTime = 10_000L
        val sut = ServerClockOffset(localTimeMs = { localTime })
        sut.onConnected(serverTime = Date(7_000L))

        localTime = 20_000L
        sut.onHealthCheck(serverTime = Date(17_000L))

        // Offset unchanged from onConnected: 10_000 - 7_000 = 3_000
        assertEquals(Date(20_000L - 3_000L), sut.estimatedServerTime())
    }

    @Test
    fun `onHealthCheck consumes sentAt so second call is no-op`() {
        var localTime = 0L
        val sut = ServerClockOffset(localTimeMs = { localTime })
        sut.onConnected(serverTime = Date(0L))

        localTime = 1_000L
        sut.onHealthCheckSent()

        localTime = 1_100L
        sut.onHealthCheck(serverTime = Date(1_050L))
        val offsetAfterFirst = (1_000L + 1_100L) / 2 - 1_050L

        localTime = 50_000L
        sut.onHealthCheck(serverTime = Date(99_999L))

        // Offset unchanged -- second call was a no-op (sentAtMs consumed)
        assertEquals(Date(50_000L - offsetAfterFirst), sut.estimatedServerTime())
    }

    @Test
    fun `onHealthCheck rejects RTT exceeding maxRttMs`() {
        var localTime = 0L
        val sut = ServerClockOffset(localTimeMs = { localTime }, maxRttMs = 500L)
        sut.onConnected(serverTime = Date(0L))

        localTime = 1_000L
        sut.onHealthCheckSent()
        localTime = 2_000L
        sut.onHealthCheck(serverTime = Date(1_500L))

        // RTT = 1_000 > maxRttMs = 500 → rejected, offset unchanged (= 0)
        assertEquals(Date(2_000L), sut.estimatedServerTime())
    }

    @Test
    fun `onHealthCheck rejects non-positive RTT`() {
        val localTime = 1_000L
        val sut = ServerClockOffset(localTimeMs = { localTime })
        sut.onConnected(serverTime = Date(1_000L))

        sut.onHealthCheckSent()
        // localTime hasn't advanced → RTT = 0 → rejected
        sut.onHealthCheck(serverTime = Date(1_000L))

        assertEquals(Date(1_000L), sut.estimatedServerTime())
    }

    // ── Reconnect resets ────────────────────────────────────────────────

    @Test
    fun `onConnected resets bestRtt so health checks re-converge`() {
        var localTime = 0L
        val sut = ServerClockOffset(localTimeMs = { localTime })
        sut.onConnected(serverTime = Date(0L))

        // Excellent RTT on first connection
        localTime = 1_000L
        sut.onHealthCheckSent()
        localTime = 1_050L
        sut.onHealthCheck(serverTime = Date(1_025L))

        // Reconnect resets bestRtt
        localTime = 50_000L
        sut.onConnected(serverTime = Date(50_000L))

        // Worse RTT on new connection should still be accepted
        localTime = 51_000L
        sut.onHealthCheckSent()
        localTime = 51_200L
        sut.onHealthCheck(serverTime = Date(51_100L))

        val expectedOffset = (51_000L + 51_200L) / 2 - 51_100L
        localTime = 60_000L
        assertEquals(Date(60_000L - expectedOffset), sut.estimatedServerTime())
    }

    // ── Clock directions with health check ──────────────────────────────

    @Test
    fun `clock 1 hour ahead is corrected by health check`() {
        val skew = 3_600_000L
        var localTime = 36_000_000L
        val sut = ServerClockOffset(localTimeMs = { localTime })
        sut.onConnected(serverTime = Date(localTime - skew))

        localTime = 36_010_000L
        sut.onHealthCheckSent()
        localTime = 36_010_200L
        val serverTimeAtMidpoint = (36_010_000L + 36_010_200L) / 2 - skew
        sut.onHealthCheck(serverTime = Date(serverTimeAtMidpoint))

        localTime = 36_020_000L
        val expected = 36_020_000L - skew
        assertEquals(Date(expected), sut.estimatedServerTime())
    }

    @Test
    fun `clock 1 hour behind is corrected by health check`() {
        val skew = -3_600_000L
        var localTime = 28_800_000L
        val sut = ServerClockOffset(localTimeMs = { localTime })
        sut.onConnected(serverTime = Date(localTime - skew))

        localTime = 28_810_000L
        sut.onHealthCheckSent()
        localTime = 28_810_200L
        val serverTimeAtMidpoint = (28_810_000L + 28_810_200L) / 2 - skew
        sut.onHealthCheck(serverTime = Date(serverTimeAtMidpoint))

        localTime = 28_820_000L
        val expected = 28_820_000L - skew
        assertEquals(Date(expected), sut.estimatedServerTime())
    }
}
