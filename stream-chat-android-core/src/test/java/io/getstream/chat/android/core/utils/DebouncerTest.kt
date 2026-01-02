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

package io.getstream.chat.android.core.utils

import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class DebouncerTest {

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        DispatcherProvider.set(
            mainDispatcher = testDispatcher,
            ioDispatcher = testDispatcher,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        DispatcherProvider.reset()
    }

    @Test
    fun testWorkInDebounceInterval() = runTest {
        // given
        val debouncer = Debouncer(200)
        var acc = 0
        // when
        debouncer.submit {
            acc += 1
        }
        delay(100)
        debouncer.submit {
            acc += 1
        }
        delay(300)
        // then
        acc `should be equal to` 1
    }

    @Test
    fun testWorkOutsideDebounceInterval() = runTest {
        // given
        val debouncer = Debouncer(200)
        var acc = 0
        // when
        debouncer.submit {
            acc += 1
        }
        delay(300)
        debouncer.submit {
            acc += 1
        }
        delay(300)
        // then
        acc `should be equal to` 2
    }

    @Test
    fun testSuspendableWorkInDebounceInterval() = runTest {
        // given
        val debouncer = Debouncer(200)
        var acc = 0
        // when
        debouncer.submitSuspendable {
            acc += 1
        }
        delay(100)
        debouncer.submitSuspendable {
            acc += 1
        }
        delay(300)
        // then
        acc `should be equal to` 1
    }

    @Test
    fun testSuspendableWorkOutsideDebounceInterval() = runTest {
        // given
        val debouncer = Debouncer(200)
        var acc = 0
        // when
        debouncer.submitSuspendable {
            acc += 1
        }
        delay(300)
        debouncer.submitSuspendable {
            acc += 1
        }
        delay(300)
        // then
        acc `should be equal to` 2
    }

    @Test
    fun testCancelLastDebounce() = runTest {
        // given
        val debouncer = Debouncer(200)
        var acc = 0
        // when
        debouncer.submit {
            acc += 1
        }
        delay(100)
        debouncer.cancelLastDebounce()
        delay(300)
        // then
        acc `should be equal to` 0
    }

    @Test
    fun testShutdown() = runTest {
        // given
        val debouncer = Debouncer(200)
        var acc = 0
        // when
        debouncer.submit {
            acc += 1
        }
        debouncer.shutdown()
        delay(300)
        // then
        acc `should be equal to` 0
    }
}
