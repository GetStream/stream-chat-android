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
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
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
    val validResult: Result<String> = Result.Success(resultValue)

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
        val blockedTask = BlockedTask(validResult)
        val call = CoroutineCall(testCoroutines.scope, blockedTask.getSuspendTask())

        val localScope = testCoroutines.scope + Job()
        val deferredResult = localScope.async {
            call.await()
        }
        delay(10)
        call.cancel()
        blockedTask.unblock()
        val result = deferredResult.await()

        result `should be equal to` Call.callCanceledError()
        blockedTask.isStarted() `should be equal to` true
        blockedTask.isCompleted() `should be equal to` false
    }
}
