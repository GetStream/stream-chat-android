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
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.doOnStart
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

internal class DoOnStartCallTest {
    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val resultValue = randomString()
    private val validResult: Result<String> = Result.Success(resultValue)
    private val sideEffect = SpySideEffect()

    @Test
    fun `Call should be executed and return a valid result`() = runTest {
        val blockedCall = BlockedCall(validResult).apply { unblock() }
        val call = blockedCall.doOnStart(testCoroutines.scope, sideEffect)

        val result = call.execute()

        result `should be equal to` validResult
        sideEffect.`should be invoked`()
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` true
        blockedCall.isCanceled() `should be equal to` false
    }

    @Test
    fun `Call should be enqueued and return a valid result by the callback`() = runTest {
        val callback: Call.Callback<String> = mock()
        val blockedCall = BlockedCall(validResult).apply { unblock() }
        val call = blockedCall.doOnStart(testCoroutines.scope, sideEffect)

        call.enqueue(callback)

        Mockito.verify(callback, only()).onResult(
            org.mockito.kotlin.check {
                it `should be equal to` validResult
            },
        )
        sideEffect.`should be invoked`()
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` true
        blockedCall.isCanceled() `should be equal to` false
    }

    @Test
    fun `Canceled Call should be enqueued and shouldn't return value on the callback`() = runTest {
        val callback: Call.Callback<String> = spy()
        val blockedCall = BlockedCall(validResult)
        val call = blockedCall.doOnStart(testCoroutines.scope, sideEffect)

        call.enqueue(callback)
        call.cancel()
        blockedCall.unblock()

        Mockito.verify(callback, never()).onResult(any())
        sideEffect.`should be invoked`()
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` false
        blockedCall.isCanceled() `should be equal to` true
    }

    @Test
    fun `Call should be executed asynchronous and return a valid result`() = runTest {
        val blockedCall = BlockedCall(validResult).apply { unblock() }
        val call = blockedCall.doOnStart(testCoroutines.scope, sideEffect)

        val result = call.await()

        result `should be equal to` validResult
        sideEffect.`should be invoked`()
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` true
        blockedCall.isCanceled() `should be equal to` false
    }

    @Test
    fun `Canceled Call should be executed asynchronous and return a cancel error`() = runTest {
        val blockedCall = BlockedCall(validResult)
        val call = blockedCall.doOnStart(testCoroutines.scope, sideEffect)

        val deferedResult = async { call.await() }
        delay(10)
        call.cancel()
        blockedCall.unblock()
        val result = deferedResult.await()

        result `should be equal to` Call.callCanceledError()
        sideEffect.`should be invoked`()
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` false
        blockedCall.isCanceled() `should be equal to` true
    }

    private class SpySideEffect : suspend () -> Unit {
        private var invocations = 0
        override suspend fun invoke() {
            invocations++
        }

        fun `should be invoked`() {
            invocations `should be equal to` 1
        }

        fun `should not be invoked`() {
            if (invocations > 0) {
                throw AssertionError("SideEffect never wanted to be invoked but invoked")
            }
        }
    }
}
