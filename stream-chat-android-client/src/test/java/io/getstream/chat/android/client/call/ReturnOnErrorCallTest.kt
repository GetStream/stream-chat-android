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

import io.getstream.chat.android.client.BlockedCall
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.onErrorReturn
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
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

internal class ReturnOnErrorCallTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val resultValue = randomString()
    private val errorResultValue = randomString()
    private val validResult: Result<String> = Result.Success(resultValue)
    private val error: Error = Error.GenericError(message = randomString())
    private val errorResult: Result<String> = Result.Failure(error)
    private val onErrorResult: Result<String> = Result.Success(errorResultValue)
    private val spyOnError = SpyOnError(onErrorResult)

    @Test
    fun `Call should be executed and return a valid result`() = runTest {
        val blockedCall = BlockedCall(validResult).apply { unblock() }
        val call = blockedCall.onErrorReturn(testCoroutines.scope, spyOnError)

        val result = call.execute()

        result `should be equal to` validResult
        spyOnError.`should not be invoked`()
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` true
        blockedCall.isCanceled() `should be equal to` false
    }

    @Test
    fun `Call should be executed and return a error result`() = runTest {
        val blockedCall = BlockedCall(errorResult).apply { unblock() }
        val call = blockedCall.onErrorReturn(testCoroutines.scope, spyOnError)

        val result = call.execute()

        result `should be equal to` onErrorResult
        spyOnError `should be invoked with` error
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` true
        blockedCall.isCanceled() `should be equal to` false
    }

    @Test
    fun `Call should be enqueued and return a valid result by the callback`() = runTest {
        val callback: Call.Callback<String> = mock()
        val blockedCall = BlockedCall(validResult).apply { unblock() }
        val call = blockedCall.onErrorReturn(testCoroutines.scope, spyOnError)

        call.enqueue(callback)

        Mockito.verify(callback, only()).onResult(
            org.mockito.kotlin.check {
                it `should be equal to` validResult
            },
        )
        spyOnError.`should not be invoked`()
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` true
        blockedCall.isCanceled() `should be equal to` false
    }

    @Test
    fun `Call should be enqueued and return a error result by the callback`() = runTest {
        val callback: Call.Callback<String> = mock()
        val blockedCall = BlockedCall(errorResult).apply { unblock() }
        val call = blockedCall.onErrorReturn(testCoroutines.scope, spyOnError)

        call.enqueue(callback)

        Mockito.verify(callback, only()).onResult(
            org.mockito.kotlin.check {
                it `should be equal to` onErrorResult
            },
        )
        spyOnError `should be invoked with` error
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` true
        blockedCall.isCanceled() `should be equal to` false
    }

    @Test
    fun `Canceled Call should be enqueued and shouldn't return value on the callback`() = runTest {
        val callback: Call.Callback<String> = spy()
        val blockedCall = BlockedCall(validResult)
        val call = blockedCall.onErrorReturn(testCoroutines.scope, spyOnError)

        call.enqueue(callback)
        call.cancel()
        blockedCall.unblock()

        Mockito.verify(callback, never()).onResult(any())
        spyOnError.`should not be invoked`()
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` false
        blockedCall.isCanceled() `should be equal to` true
    }

    @Test
    fun `Call should be executed asynchronous and return a valid result`() = runTest {
        val blockedCall = BlockedCall(validResult).apply { unblock() }
        val call = blockedCall.onErrorReturn(testCoroutines.scope, spyOnError)

        val result = call.await()

        result `should be equal to` validResult
        spyOnError.`should not be invoked`()
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` true
        blockedCall.isCanceled() `should be equal to` false
    }

    @Test
    fun `Call should be executed asynchronous and return a error result`() = runTest {
        val blockedCall = BlockedCall(errorResult).apply { unblock() }
        val call = blockedCall.onErrorReturn(testCoroutines.scope, spyOnError)

        val result = call.await()

        result `should be equal to` onErrorResult
        spyOnError `should be invoked with` error
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` true
        blockedCall.isCanceled() `should be equal to` false
    }

    @Test
    fun `Canceled Call should be executed asynchronous and return a cancel error`() = runTest {
        val blockedCall = BlockedCall(validResult)
        val call = blockedCall.onErrorReturn(testCoroutines.scope, spyOnError)

        val deferedResult = async { call.await() }
        delay(10)
        call.cancel()
        blockedCall.unblock()
        val result = deferedResult.await()

        result `should be equal to` onErrorResult
        spyOnError `should be invoked with` (Call.callCanceledError<String>() as Result.Failure).value
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` false
        blockedCall.isCanceled() `should be equal to` true
    }

    private class SpyOnError<T : Any>(private val result: Result<T>) : suspend (Error) -> Result<T> {
        private var invocations = 0
        private var error: Error? = null
        override suspend fun invoke(error: Error): Result<T> {
            invocations++
            this.error = error
            return result
        }

        infix fun `should be invoked with`(error: Error) {
            invocations `should be equal to` 1
            this.error `should be equal to` error
        }

        fun `should not be invoked`() {
            if (invocations > 0) {
                throw AssertionError("SpyOnError never wanted to be invoked but invoked")
            }
        }
    }
}
