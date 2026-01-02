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

package io.getstream.chat.android.client.api

import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Rule
import org.junit.Test

internal class ErrorCallTest {

    @get:Rule
    val coroutinesTestRule = TestCoroutineRule()

    @Test
    fun testErrorCallExecute() {
        // given
        val error = Error.GenericError(randomString())
        val errorCall = ErrorCall<Unit>(TestScope(), error)
        // when
        val result = errorCall.execute()
        // then
        result `should be instance of` Result.Failure::class
        result as Result.Failure
        result.value shouldBeEqualTo error
    }

    @Test
    fun testErrorCallEnqueue() = runTest {
        // given
        val error = Error.GenericError(randomString())
        val errorCall = ErrorCall<Unit>(backgroundScope, error)
        // when
        errorCall.enqueue { result ->
            // then
            result `should be instance of` Result.Failure::class
            result as Result.Failure
            result.value shouldBeEqualTo error
        }
    }

    @Test
    fun testErrorCallAwait() = runTest {
        // given
        val error = Error.GenericError(randomString())
        val errorCall = ErrorCall<Unit>(TestScope(), error)
        // when
        val result = errorCall.await()
        // then
        result `should be instance of` Result.Failure::class
        result as Result.Failure
        result.value shouldBeEqualTo error
    }
}
