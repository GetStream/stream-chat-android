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

package io.getstream.chat.android.client.plugin

import io.getstream.chat.android.models.Location
import io.getstream.chat.android.randomString
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ThrottlingPluginTest {

    private val messageId = randomString()

    private fun TestScope.plugin() = ThrottlingPlugin(now = { testScheduler.currentTime })

    private fun location(messageId: String = this.messageId) = Location(messageId = messageId)

    @Test
    fun `first live location update is sent immediately`() = runTest {
        val sut = plugin()

        val result = sut.onUpdateLiveLocationPrecondition(location())

        assertTrue(result is Result.Success)
        assertEquals(0L, testScheduler.currentTime)
    }

    @Test
    fun `live location update after the window is sent immediately`() = runTest {
        val sut = plugin()
        sut.onUpdateLiveLocationPrecondition(location())
        advanceTimeBy(3001)

        val result = sut.onUpdateLiveLocationPrecondition(location())

        assertTrue(result is Result.Success)
        assertEquals(3001L, testScheduler.currentTime)
    }

    @Test
    fun `live location update inside the window is sent when the window ends`() = runTest {
        val sut = plugin()
        sut.onUpdateLiveLocationPrecondition(location())
        advanceTimeBy(1000)

        val result = async { sut.onUpdateLiveLocationPrecondition(location()) }
        advanceTimeBy(1999)
        assertFalse(result.isCompleted)
        advanceUntilIdle()

        assertTrue(result.await() is Result.Success)
        assertEquals(3000L, testScheduler.currentTime)
    }

    @Test
    fun `newer live location update drops the held one at once`() = runTest {
        val sut = plugin()
        sut.onUpdateLiveLocationPrecondition(location())

        val older = async { sut.onUpdateLiveLocationPrecondition(location()) }
        advanceTimeBy(1000)
        val newer = async { sut.onUpdateLiveLocationPrecondition(location()) }
        runCurrent()

        assertTrue(older.isCompleted)
        assertTrue(older.await() is Result.Failure)
        assertEquals(1000L, testScheduler.currentTime)
        advanceUntilIdle()
        assertTrue(newer.await() is Result.Success)
        assertEquals(3000L, testScheduler.currentTime)
    }

    @Test
    fun `cancelling a held live location update does not affect the next one`() = runTest {
        val sut = plugin()
        sut.onUpdateLiveLocationPrecondition(location())
        val cancelled = async { sut.onUpdateLiveLocationPrecondition(location()) }
        advanceTimeBy(1000)

        cancelled.cancel()
        val next = async { sut.onUpdateLiveLocationPrecondition(location()) }
        advanceUntilIdle()

        assertTrue(cancelled.isCancelled)
        assertTrue(next.await() is Result.Success)
        assertEquals(3000L, testScheduler.currentTime)
    }

    @Test
    fun `cancelled live location update does not remove a newer held one`() = runTest {
        val sut = plugin()
        sut.onUpdateLiveLocationPrecondition(location())
        val cancelled = async { sut.onUpdateLiveLocationPrecondition(location()) }
        advanceTimeBy(1000)

        cancelled.cancel()
        val newer = async(start = CoroutineStart.UNDISPATCHED) { sut.onUpdateLiveLocationPrecondition(location()) }
        advanceUntilIdle()

        assertTrue(cancelled.isCancelled)
        assertTrue(newer.await() is Result.Success)
        assertEquals(3000L, testScheduler.currentTime)
    }

    @Test
    fun `sending a pending live location update starts a new window`() = runTest {
        val sut = plugin()
        sut.onUpdateLiveLocationPrecondition(location())
        val pending = async { sut.onUpdateLiveLocationPrecondition(location()) }
        advanceUntilIdle()
        pending.await()

        val next = async { sut.onUpdateLiveLocationPrecondition(location()) }
        advanceUntilIdle()

        assertTrue(next.await() is Result.Success)
        assertEquals(6000L, testScheduler.currentTime)
    }

    @Test
    fun `live location updates of different messages are throttled independently`() = runTest {
        val sut = plugin()
        sut.onUpdateLiveLocationPrecondition(location())

        val result = sut.onUpdateLiveLocationPrecondition(location(messageId = randomString()))

        assertTrue(result is Result.Success)
        assertEquals(0L, testScheduler.currentTime)
    }

    @Test
    fun `held live location updates of different messages are kept independently`() = runTest {
        val sut = plugin()
        val otherId = randomString()
        sut.onUpdateLiveLocationPrecondition(location())
        sut.onUpdateLiveLocationPrecondition(location(otherId))
        val held = async { sut.onUpdateLiveLocationPrecondition(location()) }
        val heldOther = async { sut.onUpdateLiveLocationPrecondition(location(otherId)) }
        advanceTimeBy(1000)

        val newerOther = async { sut.onUpdateLiveLocationPrecondition(location(otherId)) }
        runCurrent()

        assertTrue(heldOther.await() is Result.Failure)
        assertFalse(held.isCompleted)
        advanceUntilIdle()
        assertTrue(held.await() is Result.Success)
        assertTrue(newerOther.await() is Result.Success)
        assertEquals(3000L, testScheduler.currentTime)
    }

    @Test
    fun `stopping live location sharing drops the held update at once and resets the window`() = runTest {
        val sut = plugin()
        sut.onUpdateLiveLocationPrecondition(location())
        val pending = async { sut.onUpdateLiveLocationPrecondition(location()) }
        advanceTimeBy(1000)

        sut.onStopLiveLocationSharingResult(location(), Result.Success(location()))
        runCurrent()

        assertTrue(pending.await() is Result.Failure)
        assertEquals(1000L, testScheduler.currentTime)
        assertTrue(sut.onUpdateLiveLocationPrecondition(location()) is Result.Success)
        assertEquals(1000L, testScheduler.currentTime)
    }

    @Test
    fun `failing to stop live location sharing keeps the pending update`() = runTest {
        val sut = plugin()
        sut.onUpdateLiveLocationPrecondition(location())
        val pending = async { sut.onUpdateLiveLocationPrecondition(location()) }
        advanceTimeBy(1000)

        sut.onStopLiveLocationSharingResult(location(), Result.Failure(Error.GenericError("")))
        advanceUntilIdle()

        assertTrue(pending.await() is Result.Success)
    }

    @Test
    fun `failing to stop live location sharing keeps the window`() = runTest {
        val sut = plugin()
        sut.onUpdateLiveLocationPrecondition(location())
        advanceTimeBy(1000)

        sut.onStopLiveLocationSharingResult(location(), Result.Failure(Error.GenericError("")))
        val next = async { sut.onUpdateLiveLocationPrecondition(location()) }
        advanceUntilIdle()

        assertTrue(next.await() is Result.Success)
        assertEquals(3000L, testScheduler.currentTime)
    }

    @Test
    fun `onUserDisconnected drops the pending update and resets the window`() = runTest {
        val sut = plugin()
        sut.onUpdateLiveLocationPrecondition(location())
        val pending = async { sut.onUpdateLiveLocationPrecondition(location()) }
        advanceTimeBy(1000)

        sut.onUserDisconnected()
        runCurrent()
        assertTrue(pending.isCompleted)
        assertEquals(1000L, testScheduler.currentTime)
        val afterDisconnect = sut.onUpdateLiveLocationPrecondition(location())
        advanceUntilIdle()

        assertTrue(pending.await() is Result.Failure)
        assertTrue(afterDisconnect is Result.Success)
    }
}
