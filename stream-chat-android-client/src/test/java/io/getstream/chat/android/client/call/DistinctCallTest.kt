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
import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.DistinctCall
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

internal class DistinctCallTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val resultValue = Mother.randomString()
    private val validResult: Result<String> = Result.Success(resultValue)

    @Test
    fun `Call should be executed asynchronous only once and return a valid result`() = runTest {
        val blockedCall = BlockedCall(validResult)
        val spyCallBuilder = SpyCallBuilder(blockedCall)
        val callbacks: List<Call.Callback<String>> = List(positiveRandomInt(10)) { mock() }
        val onFinished: () -> Unit = mock()
        val call = DistinctCall(scope = testCoroutines.scope, callBuilder = spyCallBuilder, onFinished)

        val deferredResults = call.asyncRun { this.await() }
        callbacks.forEach(call::enqueue)
        blockedCall.unblock()

        deferredResults.forEach {
            it.await() `should be equal to` validResult
        }
        callbacks.forEach {
            verify(it, times(1)).onResult(eq(validResult))
        }
        spyCallBuilder.`should be invoked once`()
        verify(onFinished, times(1)).invoke()
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` true
        blockedCall.isCanceled() `should be equal to` false
    }

    @Test
    fun `Call should invoke the inner call after being completed or canceled`() = runTest {
        val blockedCall = BlockedCall(validResult).apply { unblock() }
        val spyCallBuilder = SpyCallBuilder(blockedCall)
        val callbacks: List<Call.Callback<String>> = List(positiveRandomInt(10)) { mock() }
        val onFinished: () -> Unit = mock()
        val call = DistinctCall(scope = testCoroutines.scope, callBuilder = spyCallBuilder, onFinished)

        val result = call.execute()
        call.cancel()
        delay(10)
        blockedCall.block()
        blockedCall.uncancel()
        Mockito.reset(onFinished)
        val deferredResults = call.asyncRun { this.await() }
        callbacks.forEach(call::enqueue)
        blockedCall.unblock()

        result `should be equal to` validResult
        deferredResults.forEach {
            it.await() `should be equal to` validResult
        }
        callbacks.forEach {
            verify(it, times(1)).onResult(eq(validResult))
        }
        verify(onFinished, times(1)).invoke()
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` true
        blockedCall.isCanceled() `should be equal to` false
    }

    @Test
    fun `Canceled Call should only notify sync methods`() = runTest {

        val blockedCall = BlockedCall(validResult)
        val spyCallBuilder = SpyCallBuilder(blockedCall)
        val callbacks: List<Call.Callback<String>> = List(positiveRandomInt(10)) { mock() }
        val onFinished: () -> Unit = mock()
        val call = DistinctCall(scope = testCoroutines.scope, callBuilder = spyCallBuilder, onFinished)

        callbacks.forEach(call::enqueue)
        val deferredResults = call.asyncRun { this.await() }
        delay(10)
        call.cancel()
        blockedCall.unblock()

        deferredResults.forEach {
            it.await() `should be equal to` Call.callCanceledError()
        }
        callbacks.forEach {
            verify(it, never()).onResult(any())
        }
        verify(onFinished, times(1)).invoke()
        spyCallBuilder.`should be invoked once`()
        blockedCall.isStarted() `should be equal to` true
        blockedCall.isCompleted() `should be equal to` false
        blockedCall.isCanceled() `should be equal to` true
    }

    @Test
    fun `Canceled Call should only cancel current execution but run as normal on new exectutions`() = runTest {
        val blockedCall = BlockedCall(validResult)
        val spyCallBuilder = SpyCallBuilder(blockedCall)
        val callbacksPreviousInvocation: List<Call.Callback<String>> = List(positiveRandomInt(10)) { mock() }
        val callbacks: List<Call.Callback<String>> = List(positiveRandomInt(10)) { mock() }
        val onFinished: () -> Unit = mock()
        val call = DistinctCall(scope = testCoroutines.scope, callBuilder = spyCallBuilder, onFinished)

        callbacksPreviousInvocation.forEach(call::enqueue)
        val previousDeferredResults = call.asyncRun { this.await() }
        call.cancel()
        blockedCall.unblock()
        spyCallBuilder.reset()
        Mockito.reset(onFinished)
        blockedCall.uncancel()
        delay(10)
        blockedCall.block()
        callbacks.forEach(call::enqueue)
        val deferredResults = call.asyncRun { this.await() }
        delay(10)
        blockedCall.unblock()

        callbacksPreviousInvocation.forEach {
            verify(it, never()).onResult(any())
        }
        previousDeferredResults.forEach {
            it.await() `should be equal to` Call.callCanceledError()
        }
        deferredResults.forEach {
            it.await() `should be equal to` validResult
        }
        callbacks.forEach {
            verify(it, times(1)).onResult(eq(validResult))
        }
        verify(onFinished, times(1)).invoke()
        spyCallBuilder.`should be invoked once`()
    }

    private fun <T : Any, R> DistinctCall<T>.asyncRun(function: suspend DistinctCall<T>.() -> R): List<Deferred<R>> =
        (0..positiveRandomInt(10)).map {
            testCoroutines.scope.async { this@asyncRun.function() }
        }

    private class SpyCallBuilder<T : Any>(private val call: Call<T>) : () -> Call<T> {
        private var invocations = 0

        fun reset() {
            invocations = 0
        }

        override fun invoke(): Call<T> {
            invocations++
            return call
        }

        fun `should be invoked once`() {
            invocations `should be equal to` 1
        }

        fun `should not be invoked`() {
            if (invocations > 0) {
                throw AssertionError("Consumer never wanted to be invoked but invoked")
            }
        }
    }
}
