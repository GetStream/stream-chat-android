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

import io.getstream.chat.android.client.BlockedRetrofit2Call
import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.randomString
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.only
import java.io.IOException

internal class RetrofitCallTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    val resultValue = Mother.randomString()
    val validResult: Result<String> = Result.Success(resultValue)
    val parser: ChatParser = mock()

    @Test
    fun `Call should be executed and return a valid result`() = runTest {
        val blockedRetrofit2Call = BlockedRetrofit2Call(testCoroutines.scope, value = resultValue).apply { unblock() }
        val call = RetrofitCall(blockedRetrofit2Call, parser, testCoroutines.scope)

        val result = call.execute()

        result `should be equal to` validResult
        blockedRetrofit2Call.isStarted() `should be equal to` true
        blockedRetrofit2Call.isCompleted() `should be equal to` true
    }

    @Test
    fun `Call should be executed and return a failure result`() = runTest {
        val blockedRetrofit2Call =
            BlockedRetrofit2Call<String>(testCoroutines.scope, error = IOException(randomString())).apply { unblock() }
        val call = RetrofitCall(blockedRetrofit2Call, parser, testCoroutines.scope)

        val result = call.execute()

        result.shouldBeInstanceOf(Result.Failure::class)
        (result as Result.Failure).value `should be instance of` Error::class
        blockedRetrofit2Call.isStarted() `should be equal to` true
        blockedRetrofit2Call.isCompleted() `should be equal to` true
    }

    @Test
    fun `Canceled Call should be executed and return a cancel error`() = runTest {
        val blockedRetrofit2Call =
            BlockedRetrofit2Call<String>(testCoroutines.scope, error = IOException(randomString()))
        val call = RetrofitCall(blockedRetrofit2Call, parser, testCoroutines.scope)

        val deferedResult = async { call.execute() }
        call.cancel()
        blockedRetrofit2Call.unblock()
        val result = deferedResult.await()

        result.shouldBeInstanceOf(Result.Failure::class)
        (result as Result.Failure).value `should be instance of` Error::class
        blockedRetrofit2Call.isStarted() `should be equal to` true
        blockedRetrofit2Call.isCompleted() `should be equal to` false
        blockedRetrofit2Call.isCanceled() `should be equal to` true
    }

    @Test
    fun `Call should be enqueued and return a valid result by the callback`() = runTest {
        val callback: Call.Callback<String> = mock()
        val blockedRetrofit2Call = BlockedRetrofit2Call(testCoroutines.scope, value = resultValue).apply { unblock() }
        val call = RetrofitCall(blockedRetrofit2Call, parser, testCoroutines.scope)

        call.enqueue(callback)

        Mockito.verify(callback, only()).onResult(
            org.mockito.kotlin.check {
                it `should be equal to` validResult
            }
        )
        blockedRetrofit2Call.isStarted() `should be equal to` true
        blockedRetrofit2Call.isCompleted() `should be equal to` true
    }

    @Test
    fun `Canceled Call should be enqueued and shouldn't return value on the callback`() = runTest {
        val callback: Call.Callback<String> = mock()
        val blockedRetrofit2Call = BlockedRetrofit2Call(testCoroutines.scope, value = resultValue)
        val call = RetrofitCall(blockedRetrofit2Call, parser, testCoroutines.scope)

        call.enqueue(callback)
        call.cancel()
        blockedRetrofit2Call.unblock()

        Mockito.verify(callback, never()).onResult(any())
        blockedRetrofit2Call.isStarted() `should be equal to` true
        blockedRetrofit2Call.isCompleted() `should be equal to` false
        blockedRetrofit2Call.isCanceled() `should be equal to` true
    }

    @Test
    fun `Call should be executed asynchronous and return a valid result`() = runTest {
        val blockedRetrofit2Call = BlockedRetrofit2Call(testCoroutines.scope, value = resultValue).apply { unblock() }
        val call = RetrofitCall(blockedRetrofit2Call, parser, testCoroutines.scope)

        val result = call.await()

        result `should be equal to` validResult
        blockedRetrofit2Call.isStarted() `should be equal to` true
        blockedRetrofit2Call.isCompleted() `should be equal to` true
    }

    @Test
    fun `Canceled Call should be executed asynchronous and return a cancel error`() = runTest {
        val blockedRetrofit2Call = BlockedRetrofit2Call(testCoroutines.scope, value = resultValue)
        val call = RetrofitCall(blockedRetrofit2Call, parser, testCoroutines.scope)

        val deferedResult = async { call.await() }
        delay(10)
        call.cancel()
        blockedRetrofit2Call.unblock()
        val result = deferedResult.await()

        result `should be equal to` Call.callCanceledError()
        blockedRetrofit2Call.isStarted() `should be equal to` true
        blockedRetrofit2Call.isCompleted() `should be equal to` false
        blockedRetrofit2Call.isCanceled() `should be equal to` true
    }
}
