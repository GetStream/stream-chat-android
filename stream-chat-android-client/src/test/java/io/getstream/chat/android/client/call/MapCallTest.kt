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
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.map
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.only
import org.mockito.kotlin.spy

@ExtendWith(TestCoroutineExtension::class)
internal class MapCallTest {

    private val resultValue = positiveRandomInt()
    private val validResult: Result<Int> = Result.Success(resultValue)
    private val expectedResult: Result<String> = Result.Success("$resultValue")
    private val mapper: SpyMapper<Int, String> = SpyMapper { "$it" }

    @Test
    fun `Call should be executed and return a valid result`() = runTest {
        val blockedCall = BlockedCall(validResult).apply { unblock() }
        val call = blockedCall.map(mapper)

        val result = call.execute()

        result `should be equal to` expectedResult
        mapper `should be invoked with` resultValue
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` true
        blockedCall.isCanceled() `should be equal to` false
    }

    @Test
    fun `Canceled Call should be executed and return a cancel error`() = runTest {
        val blockedCall = BlockedCall(validResult)
        val call = blockedCall.map(mapper)

        val deferedResult = async { call.execute() }
        call.cancel()
        blockedCall.unblock()
        val result = deferedResult.await()

        result `should be equal to` Call.callCanceledError()
        mapper.`should not be invoked`()
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` false
        blockedCall.isCanceled() `should be equal to` true
    }

    @Test
    fun `Call should be enqueued and return a valid result by the callback`() = runTest {
        val callback: Call.Callback<String> = mock()
        val blockedCall = BlockedCall(validResult).apply { unblock() }
        val call = blockedCall.map(mapper)

        call.enqueue(callback)

        Mockito.verify(callback, only()).onResult(
            org.mockito.kotlin.check {
                it `should be equal to` expectedResult
            },
        )
        mapper `should be invoked with` resultValue
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` true
        blockedCall.isCanceled() `should be equal to` false
    }

    @Test
    fun `Canceled Call should be enqueued and shouldn't return value on the callback`() = runTest {
        val callback: Call.Callback<String> = spy()
        val blockedCall = BlockedCall(validResult)
        val call = blockedCall.map(mapper)

        call.enqueue(callback)
        call.cancel()
        blockedCall.unblock()

        Mockito.verify(callback, never()).onResult(any())
        mapper.`should not be invoked`()
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` false
        blockedCall.isCanceled() `should be equal to` true
    }

    @Test
    fun `Call should be executed asynchronous and return a valid result`() = runTest {
        val blockedCall = BlockedCall(validResult).apply { unblock() }
        val call = blockedCall.map(mapper)

        val result = call.await()

        result `should be equal to` expectedResult
        mapper `should be invoked with` resultValue
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` true
        blockedCall.isCanceled() `should be equal to` false
    }

    @Test
    fun `Canceled Call should be executed asynchronous and return a cancel error`() = runTest {
        val blockedCall = BlockedCall(validResult)
        val call = blockedCall.map(mapper)

        val deferedResult = async { call.await() }
        call.cancel()
        blockedCall.unblock()
        val result = deferedResult.await()

        result `should be equal to` Call.callCanceledError()
        mapper.`should not be invoked`()
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` false
        blockedCall.isCanceled() `should be equal to` true
    }

    private class SpyMapper<T : Any, R : Any>(private val mapFunction: (T) -> R) : (T) -> R {
        private var invocations = 0
        private var input: T? = null
        override fun invoke(input: T): R {
            invocations++
            this.input = input
            return mapFunction(input)
        }

        infix fun `should be invoked with`(input: T) {
            invocations `should be equal to` 1
            this.input `should be equal to` input
        }

        fun `should not be invoked`() {
            if (invocations > 0) {
                throw AssertionError("SpyMapper never wanted to be invoked but invoked")
            }
        }
    }
}
