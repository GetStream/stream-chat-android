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
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomString
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

@ExtendWith(TestCoroutineExtension::class)
internal class ZipCallTest {

    private val resultValueA = positiveRandomInt()
    private val validResultA: Result<Int> = Result.success(resultValueA)
    private val resultValueB = randomString()
    private val validResultB: Result<String> = Result.success(resultValueB)
    private val expectedResult: Result<Pair<Int, String>> = Result.success(Pair(resultValueA, resultValueB))
    private val errorA = ChatError(randomString(), Exception())
    private val errorB = ChatError(randomString(), Exception())
    private val errorResultA = Result.error<Int>(errorA)
    private val errorResultB = Result.error<String>(errorB)
    private val expectedErrorResultA = Result.error<Pair<Int, String>>(errorA)
    private val expectedErrorResultB = Result.error<Pair<Int, String>>(errorB)

    @Test
    fun `Call should be executed and return a valid result`() = runTest {
        val blockedCallA = BlockedCall(validResultA).apply { unblock() }
        val blockedCallB = BlockedCall(validResultB).apply { unblock() }
        val call = blockedCallA.zipWith(blockedCallB)

        val result = call.execute()

        result `should be equal to` expectedResult
        blockedCallA.isStarted() `should be equal to` true
        blockedCallA.isCompleted() `should be equal to` true
        blockedCallA.isCanceled() `should be equal to` false
        blockedCallB.isStarted() `should be equal to` true
        blockedCallB.isCompleted() `should be equal to` true
        blockedCallB.isCanceled() `should be equal to` false
    }

    @Test
    fun `Call should be executed and return an error result from callB`() = runTest {
        val blockedCallA = BlockedCall(validResultA).apply { unblock() }
        val blockedCallB = BlockedCall(errorResultB).apply { unblock() }
        val call = blockedCallA.zipWith(blockedCallB)

        val result = call.execute()

        result `should be equal to` expectedErrorResultB
        blockedCallA.isStarted() `should be equal to` true
        blockedCallA.isCompleted() `should be equal to` true
        blockedCallA.isCanceled() `should be equal to` false
        blockedCallB.isStarted() `should be equal to` true
        blockedCallB.isCompleted() `should be equal to` true
        blockedCallB.isCanceled() `should be equal to` false
    }

    @Test
    fun `Call should be executed and return an error result from callA`() = runTest {
        val blockedCallA = BlockedCall(errorResultA).apply { unblock() }
        val blockedCallB = BlockedCall(validResultB).apply { unblock() }
        val call = blockedCallA.zipWith(blockedCallB)

        val result = call.execute()

        result `should be equal to` expectedErrorResultA
        blockedCallA.isStarted() `should be equal to` true
        blockedCallA.isCompleted() `should be equal to` true
        blockedCallA.isCanceled() `should be equal to` false
        blockedCallB.isStarted() `should be equal to` true
        blockedCallB.isCompleted() `should be equal to` true
        blockedCallB.isCanceled() `should be equal to` false
    }

    @Test
    fun `Canceled Call should be executed and return a cancel error`() = runTest {
        val blockedCallA = BlockedCall(validResultA)
        val blockedCallB = BlockedCall(validResultB)
        val call = blockedCallA.zipWith(blockedCallB)

        val deferedResult = async { call.execute() }
        call.cancel()
        blockedCallA.unblock()
        blockedCallB.unblock()
        val result = deferedResult.await()

        result `should be equal to` Call.callCanceledError()
        blockedCallA.isStarted() `should be equal to` true
        blockedCallA.isCompleted() `should be equal to` false
        blockedCallA.isCanceled() `should be equal to` true
        blockedCallB.isStarted() `should be equal to` true
        blockedCallB.isCompleted() `should be equal to` false
        blockedCallB.isCanceled() `should be equal to` true
    }

    @Test
    fun `Call should be enqueued and return an error result from callA by the callback`() = runTest {
        val callback: Call.Callback<Pair<Int, String>> = mock()
        val blockedCallA = BlockedCall(errorResultA).apply { unblock() }
        val blockedCallB = BlockedCall(validResultB).apply { unblock() }
        val call = blockedCallA.zipWith(blockedCallB)

        call.enqueue(callback)

        Mockito.verify(callback, only()).onResult(
            org.mockito.kotlin.check {
                it `should be equal to` expectedErrorResultA
            }
        )
        blockedCallA.isStarted() `should be equal to` true
        blockedCallA.isCompleted() `should be equal to` true
        blockedCallA.isCanceled() `should be equal to` false
        blockedCallB.isStarted() `should be equal to` false
        blockedCallB.isCompleted() `should be equal to` false
        blockedCallB.isCanceled() `should be equal to` true
    }

    @Test
    fun `Call should be enqueued and return an error result from callB by the callback`() = runTest {
        val callback: Call.Callback<Pair<Int, String>> = mock()
        val blockedCallA = BlockedCall(validResultA).apply { unblock() }
        val blockedCallB = BlockedCall(errorResultB).apply { unblock() }
        val call = blockedCallA.zipWith(blockedCallB)

        call.enqueue(callback)

        Mockito.verify(callback, only()).onResult(
            org.mockito.kotlin.check {
                it `should be equal to` expectedErrorResultB
            }
        )
        blockedCallA.isStarted() `should be equal to` true
        blockedCallA.isCompleted() `should be equal to` true
        blockedCallA.isCanceled() `should be equal to` false
        blockedCallB.isStarted() `should be equal to` true
        blockedCallB.isCompleted() `should be equal to` true
        blockedCallB.isCanceled() `should be equal to` false
    }

    @Test
    fun `Call should be enqueued and return a valid result by the callback`() = runTest {
        val callback: Call.Callback<Pair<Int, String>> = mock()
        val blockedCallA = BlockedCall(validResultA).apply { unblock() }
        val blockedCallB = BlockedCall(validResultB).apply { unblock() }
        val call = blockedCallA.zipWith(blockedCallB)

        call.enqueue(callback)

        Mockito.verify(callback, only()).onResult(
            org.mockito.kotlin.check {
                it `should be equal to` expectedResult
            }
        )
        blockedCallA.isStarted() `should be equal to` true
        blockedCallA.isCompleted() `should be equal to` true
        blockedCallA.isCanceled() `should be equal to` false
        blockedCallB.isStarted() `should be equal to` true
        blockedCallB.isCompleted() `should be equal to` true
        blockedCallB.isCanceled() `should be equal to` false
    }

    @Test
    fun `Canceled Call should be enqueued and shouldn't return value on the callback`() = runTest {
        val callback: Call.Callback<Pair<Int, String>> = mock()
        val blockedCallA = BlockedCall(validResultA)
        val blockedCallB = BlockedCall(validResultB)
        val call = blockedCallA.zipWith(blockedCallB)

        call.enqueue(callback)
        call.cancel()
        blockedCallA.unblock()
        blockedCallB.unblock()

        Mockito.verify(callback, never()).onResult(any())
        blockedCallA.isStarted() `should be equal to` true
        blockedCallA.isCompleted() `should be equal to` false
        blockedCallA.isCanceled() `should be equal to` true
        blockedCallB.isStarted() `should be equal to` false
        blockedCallB.isCompleted() `should be equal to` false
        blockedCallB.isCanceled() `should be equal to` true
    }

    @Test
    fun `Call should be executed asynchronous and return a valid result`() = runTest {
        val blockedCallA = BlockedCall(validResultA).apply { unblock() }
        val blockedCallB = BlockedCall(validResultB).apply { unblock() }
        val call = blockedCallA.zipWith(blockedCallB)

        val result = call.await()

        result `should be equal to` expectedResult
        blockedCallA.isStarted() `should be equal to` true
        blockedCallA.isCompleted() `should be equal to` true
        blockedCallA.isCanceled() `should be equal to` false
        blockedCallB.isStarted() `should be equal to` true
        blockedCallB.isCompleted() `should be equal to` true
        blockedCallB.isCanceled() `should be equal to` false
    }

    @Test
    fun `Call should be executed asynchronous and return an error result from callB`() = runTest {
        val blockedCallA = BlockedCall(validResultA).apply { unblock() }
        val blockedCallB = BlockedCall(errorResultB).apply { unblock() }
        val call = blockedCallA.zipWith(blockedCallB)

        val result = call.await()

        result `should be equal to` expectedErrorResultB
        blockedCallA.isStarted() `should be equal to` true
        blockedCallA.isCompleted() `should be equal to` true
        blockedCallA.isCanceled() `should be equal to` false
        blockedCallB.isStarted() `should be equal to` true
        blockedCallB.isCompleted() `should be equal to` true
        blockedCallB.isCanceled() `should be equal to` false
    }

    @Test
    fun `Call should be executed asynchronous and return an error result from callA`() = runTest {
        val blockedCallA = BlockedCall(errorResultA).apply { unblock() }
        val blockedCallB = BlockedCall(validResultB).apply { unblock() }
        val call = blockedCallA.zipWith(blockedCallB)

        val result = call.await()

        result `should be equal to` expectedErrorResultA
        blockedCallA.isStarted() `should be equal to` true
        blockedCallA.isCompleted() `should be equal to` true
        blockedCallA.isCanceled() `should be equal to` false
    }

    @Test
    fun `Canceled Call should be executed asynchronous and return a cancel error`() = runTest {
        val blockedCallA = BlockedCall(validResultA)
        val blockedCallB = BlockedCall(validResultB)
        val call = blockedCallA.zipWith(blockedCallB)

        val deferedResult = async { call.await() }
        call.cancel()
        blockedCallA.unblock()
        blockedCallB.unblock()
        val result = deferedResult.await()

        result `should be equal to` Call.callCanceledError()
        blockedCallA.isStarted() `should be equal to` true
        blockedCallA.isCompleted() `should be equal to` false
        blockedCallA.isCanceled() `should be equal to` true
        blockedCallB.isStarted() `should be equal to` true
        blockedCallB.isCompleted() `should be equal to` false
        blockedCallB.isCanceled() `should be equal to` true
    }
}
