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

package io.getstream.chat.android.ui.common.utils.typing.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultTypingUpdatesBufferTest {

    @Test
    fun `first keystroke sends a typing started event`() = runTest {
        val fixture = Fixture(this)

        fixture.buffer.onKeystroke("a")
        runCurrent()

        assertEquals(1, fixture.startedCount)
        assertEquals(0, fixture.stoppedCount)
    }

    @Test
    fun `typing stopped is sent after the user stops typing`() = runTest {
        val fixture = Fixture(this)

        fixture.buffer.onKeystroke("a")
        runCurrent()
        advanceTimeBy(SEND_TYPING_UPDATES_INTERVAL - 1)
        runCurrent()
        assertEquals(0, fixture.stoppedCount)

        advanceTimeBy(1)
        runCurrent()

        assertEquals(1, fixture.startedCount)
        assertEquals(1, fixture.stoppedCount)
    }

    @Test
    fun `continuous typing sends periodic typing started events without a stop`() = runTest {
        val fixture = Fixture(this)

        fixture.buffer.onKeystroke("a")
        runCurrent()
        repeat(3) {
            // Keep each keystroke gap below the stop threshold so no stop event fires.
            advanceTimeBy(BUFFER_TYPING_UPDATES_INTERVAL - 100)
            runCurrent()
            fixture.buffer.onKeystroke("a".repeat(it + 2))
        }
        // 3 * 900 + 400 = 3100ms, just past the periodic send boundary.
        advanceTimeBy(400)
        runCurrent()

        assertEquals(2, fixture.startedCount)
        assertEquals(0, fixture.stoppedCount)
    }

    @Test
    fun `empty input sends typing stopped immediately`() = runTest {
        val fixture = Fixture(this)

        fixture.buffer.onKeystroke("")

        assertEquals(0, fixture.startedCount)
        assertEquals(1, fixture.stoppedCount)
    }

    @Test
    fun `clearing the input while typing sends typing stopped`() = runTest {
        val fixture = Fixture(this)

        fixture.buffer.onKeystroke("a")
        runCurrent()
        fixture.buffer.onKeystroke("")

        assertEquals(1, fixture.startedCount)
        assertEquals(1, fixture.stoppedCount)
    }

    @Test
    fun `clear cancels pending updates and sends typing stopped`() = runTest {
        val fixture = Fixture(this)
        fixture.buffer.onKeystroke("a")
        runCurrent()

        fixture.buffer.clear()
        assertEquals(1, fixture.stoppedCount)

        advanceTimeBy(SEND_TYPING_UPDATES_INTERVAL * 3)
        runCurrent()

        assertEquals(1, fixture.startedCount)
        assertEquals(1, fixture.stoppedCount)
    }

    private class Fixture(testScope: TestScope) {
        var startedCount = 0
            private set
        var stoppedCount = 0
            private set

        val buffer = DefaultTypingUpdatesBuffer(
            coroutineScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScope.testScheduler)),
            onTypingStarted = { startedCount++ },
            onTypingStopped = { stoppedCount++ },
        )
    }

    private companion object {
        /**
         * Mirrors DEFAULT_SEND_TYPING_UPDATES_INTERVAL in [DefaultTypingUpdatesBuffer].
         */
        private const val SEND_TYPING_UPDATES_INTERVAL = 3000L

        /**
         * Mirrors DEFAULT_BUFFER_TYPING_UPDATES_INTERVAL in [DefaultTypingUpdatesBuffer].
         */
        private const val BUFFER_TYPING_UPDATES_INTERVAL = 1000L
    }
}
