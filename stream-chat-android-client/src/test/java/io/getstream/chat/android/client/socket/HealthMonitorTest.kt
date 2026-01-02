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

package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.client.utils.TimeProvider
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.internal.verification.Times
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class HealthMonitorTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @Test
    fun `While connection is alive, only checkCallback should be called`() = runTest {
        val cycles = positiveRandomInt(100)
        val fixture = Fixture.Builder(testCoroutines.scope)
            .keepCallingAckFor(cycles)
            .build()
        val healthMonitor = fixture.healthMonitor

        healthMonitor.ack()
        testCoroutines.scope.advanceTimeBy(getNormalTimeToAdvance(cycles))
        healthMonitor.stop()

        verify(fixture.checkCallback, Times(cycles)).invoke()
        verify(fixture.reconnectCallback, never()).invoke()
    }

    @Test
    fun `When HealthMonitor_ack is not called for more than 3 cycles, reconnectCallback should be called`() = runTest {
        val normalCycles = positiveRandomInt(100)
        val cyclesUntilReconnection = normalCycles + 3
        val fixture = Fixture.Builder(testCoroutines.scope)
            .keepCallingAckFor(normalCycles)
            .build()
        val healthMonitor = fixture.healthMonitor

        healthMonitor.ack()
        testCoroutines.scope.advanceTimeBy(getNormalTimeToAdvance(cyclesUntilReconnection, 1))
        healthMonitor.stop()

        verify(fixture.checkCallback, Times(cyclesUntilReconnection - 1)).invoke()
        verify(fixture.reconnectCallback, Times(1)).invoke()
    }

    @Test
    fun `When reconnection is successful, monitor keeps working`() = runTest {
        val normalCycles = positiveRandomInt(100)
        val cyclesUntilReconnection = normalCycles + 3
        val cyclesAfterReconnection = cyclesUntilReconnection + 1
        val fixture = Fixture.Builder(testCoroutines.scope)
            .keepCallingAckFor(normalCycles)
            .withSuccessfulReconnect()
            .build()
        val healthMonitor = fixture.healthMonitor

        healthMonitor.ack()
        testCoroutines.scope.advanceTimeBy(getNormalTimeToAdvance(cyclesAfterReconnection, 1))
        healthMonitor.stop()

        verify(fixture.checkCallback, Times(cyclesUntilReconnection)).invoke()
        verify(fixture.reconnectCallback, Times(1)).invoke()
    }

    @Test
    fun `When HealthMonitor_onDisconnect is not called, reconnectCallback should be called`() = runTest {
        val normalCycles = positiveRandomInt(100)
        val fixture = Fixture.Builder(testCoroutines.scope)
            .build()
        val healthMonitor = fixture.healthMonitor

        healthMonitor.ack()
        testCoroutines.scope.advanceTimeBy(getNormalTimeToAdvance(normalCycles))
        healthMonitor.onDisconnected()
        testCoroutines.scope.advanceTimeBy(getNormalTimeToAdvance(0, 1))
        healthMonitor.stop()

        verify(fixture.checkCallback, Times(normalCycles)).invoke()
        verify(fixture.reconnectCallback, Times(1)).invoke()
    }

    @Test
    fun `When HealthMonitor_onDisconnect and reconnect is successful, monitor keeps working`() = runTest {
        val cyclesBeforeDisconnect = positiveRandomInt(100)
        val cyclesAfterDisconnect = positiveRandomInt(100)
        val fixture = Fixture.Builder(testCoroutines.scope)
            .withSuccessfulReconnect()
            .build()
        val healthMonitor = fixture.healthMonitor

        healthMonitor.ack()
        testCoroutines.scope.advanceTimeBy(getNormalTimeToAdvance(cyclesBeforeDisconnect))
        healthMonitor.onDisconnected()
        testCoroutines.scope.advanceTimeBy(getNormalTimeToAdvance(cyclesAfterDisconnect, 1))
        healthMonitor.stop()

        verify(fixture.checkCallback, Times(cyclesBeforeDisconnect + cyclesAfterDisconnect)).invoke()
        verify(fixture.reconnectCallback, Times(1)).invoke()
    }

    internal class Fixture private constructor(
        val healthMonitor: HealthMonitor,
        val checkCallback: () -> Unit,
        val reconnectCallback: () -> Unit,
    ) {

        class Builder(private val testScope: TestScope) {
            private val checkCallback: () -> Unit = mock()
            private val reconnectCallback: () -> Unit = mock()
            private val timeProvider = mock<TimeProvider>().apply {
                whenever(this.provideCurrentTimeInMilliseconds()) doAnswer { testScope.currentTime }
            }
            private val retryInterval = HealthMonitor.RetryInterval { (it + 1).toLong() }
            private var callAckPrecondition: () -> Boolean = { true }
            private var successfullReconnect = false

            fun keepCallingAckFor(cycles: Int): Builder = apply {
                var currentCycle = 0
                callAckPrecondition = {
                    currentCycle++ < cycles
                }
            }

            fun withSuccessfulReconnect(): Builder = apply {
                successfullReconnect = true
            }

            fun build(): Fixture {
                val healthMonitor = HealthMonitor(
                    timeProvider = timeProvider,
                    retryInterval = retryInterval,
                    userScope = UserTestScope(testScope),
                    checkCallback = checkCallback,
                    reconnectCallback = reconnectCallback,
                )
                whenever(checkCallback.invoke()) doAnswer {
                    if (callAckPrecondition()) {
                        healthMonitor.ack()
                    }
                }
                whenever(reconnectCallback.invoke()) doAnswer {
                    if (successfullReconnect) {
                        healthMonitor.ack()
                    }
                }
                return Fixture(healthMonitor, checkCallback, reconnectCallback)
            }
        }
    }

    private fun getNormalTimeToAdvance(cycles: Int, retriesFailures: Int = 0): Long {
        val checkTime = cycles * 1_000L
        val monitorTime = cycles * 10_000L
        val processingTime = cycles * 1L + 1L
        return checkTime + monitorTime + processingTime + retriesFailures
    }
}
