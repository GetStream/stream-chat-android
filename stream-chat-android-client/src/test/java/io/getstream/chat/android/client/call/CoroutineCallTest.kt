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

package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.BlockedTask
import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.test.MockRetrofitCall
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.logging.StreamLog
import io.getstream.logging.kotlin.KotlinStreamLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.only
import org.mockito.kotlin.spy

internal class CoroutineCallTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    val resultValue = Mother.randomString()
    val validResult: Result<String> = Result.success(resultValue)

    @BeforeEach
    fun setup() {
        StreamLog.setLogger(KotlinStreamLogger(now = {
            testCoroutines.dispatcher.scheduler.currentTime
        }))
        StreamLog.setValidator { _, _ -> true }
    }

    @Test
    fun `Call should be executed and return a valid result`() = runTest {
        val blockedTask = BlockedTask(validResult).apply { unblock() }
        val call = CoroutineCall(testCoroutines.scope, blockedTask.getSuspendTask())

        val result = call.execute()

        result `should be equal to` validResult
        blockedTask.isStarted() `should be equal to` true
        blockedTask.isCompleted() `should be equal to` true
    }

    @Test
    fun `Call should be enqueued and return a valid result by the callback`() = runTest {
        val callback: Call.Callback<String> = mock()
        val blockedTask = BlockedTask(validResult).apply { unblock() }
        val call = CoroutineCall(testCoroutines.scope, blockedTask.getSuspendTask())

        call.enqueue(callback)

        Mockito.verify(callback, only()).onResult(
            org.mockito.kotlin.check {
                it `should be equal to` validResult
            }
        )
        blockedTask.isStarted() `should be equal to` true
        blockedTask.isCompleted() `should be equal to` true
    }

    @Test
    fun `Canceled Call should be enqueued and shouldn't return value on the callback`() = runTest {
        val callback: Call.Callback<String> = spy()
        val blockedTask = BlockedTask(validResult)
        val call = CoroutineCall(testCoroutines.scope, blockedTask.getSuspendTask())

        call.enqueue(callback)
        call.cancel()
        blockedTask.unblock()

        Mockito.verify(callback, never()).onResult(any())
        blockedTask.isStarted() `should be equal to` true
        blockedTask.isCompleted() `should be equal to` false
    }

    @Test
    fun `Call should be executed asynchronous and return a valid result`() = runTest {
        val blockedTask = BlockedTask(validResult).apply { unblock() }
        val call = CoroutineCall(testCoroutines.scope, blockedTask.getSuspendTask())

        val result = call.await()

        result `should be equal to` validResult
        blockedTask.isStarted() `should be equal to` true
        blockedTask.isCompleted() `should be equal to` true
    }

    @Test
    fun `Canceled Call should be executed asynchronous and return a cancel error`() = runTest {
        val blockedTask = MockRetrofitCall(testCoroutines.scope, validResult) {
            delay(2000L)
        }
        val call = CoroutineCall(testCoroutines.scope) {
            blockedTask.await()
        }

        val localScope = testCoroutines.scope + Job()
        val deferredResult = localScope.async {
            try {
                StreamLog.d("Coroutine-Test") { "[test] #1" }
                call.await().also {
                    StreamLog.d("Coroutine-Test") { "[test] #2" }
                }
            } catch (e: Throwable) {
                StreamLog.e("Coroutine-Test") { "[test] failed: $e" }
                null
            }
        }
        StreamLog.d("Coroutine-Test") { "[test] #3" }
        delay(10)
        StreamLog.d("Coroutine-Test") { "[test] #4" }
        call.cancel()
        StreamLog.d("Coroutine-Test") { "[test] #5" }
        val result = deferredResult.await()
        StreamLog.d("Coroutine-Test") { "[test] #6" }
        result `should be equal to` Call.callCanceledError()

        /*val blockedTask = BlockedTask(validResult)
        val call = CoroutineCall(testCoroutines.scope, blockedTask.getSuspendTask())

        val localScope = testCoroutines.scope + Job()
        val deferredResult = localScope.async { call.await() }
        delay(10)
        call.cancel()
        blockedTask.unblock()
        val result = deferredResult.await()

        result `should be equal to` Call.callCanceledError()
        blockedTask.isStarted() `should be equal to` true
        blockedTask.isCompleted() `should be equal to` false*/
    }
}
