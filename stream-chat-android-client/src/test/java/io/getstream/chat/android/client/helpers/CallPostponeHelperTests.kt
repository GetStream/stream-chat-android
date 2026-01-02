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

package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.scope.UserScope
import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class CallPostponeHelperTests {

    private lateinit var userScope: UserScope

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @BeforeEach
    fun setUp() {
        userScope = UserTestScope(testCoroutines.scope)
    }

    @Test
    fun `Given connected state When query channels Should return channels from api`() = runTest {
        val sut = buildCallPostponeHelper(timeout = 5000L, delayTime = 0L)
        val expectedResult = List(positiveRandomInt(10)) { randomChannel() }
        val queryChannelsCallMock = mock<() -> Call<List<Channel>>>()
        whenever(queryChannelsCallMock.invoke()) doReturn expectedResult.asCall()

        val result = (sut.postponeCall(queryChannelsCallMock).await() as Result.Success).value

        verify(queryChannelsCallMock).invoke()
        result shouldBeEqualTo expectedResult
    }

    @Test
    fun `Given long connection process When query channels Should return a Error Call`() = runTest {
        val sut = buildCallPostponeHelper(timeout = 5000L, delayTime = 10_000L)
        val expectedErrorResult =
            "Failed to perform call. Waiting for WS connection was too long."

        val queryChannelsCallMock = mock<() -> Call<List<Channel>>>()
        val result = (sut.postponeCall(queryChannelsCallMock).await() as Result.Failure).value
        result.message `should be` expectedErrorResult
    }

    @Test
    fun `Given short connection process When query channel Should return channel from api`() = runTest {
        val sut = buildCallPostponeHelper(timeout = 5_000L, delayTime = 800L)
        val expectedResult = randomChannel()
        val queryChannelCallMock = mock<() -> Call<Channel>>()
        whenever(queryChannelCallMock.invoke()) doReturn expectedResult.asCall()

        val result = (sut.postponeCall(queryChannelCallMock).await() as Result.Success).value

        verify(queryChannelCallMock).invoke()
        result shouldBeEqualTo expectedResult
    }

    @Test
    fun `Given long connection process When query channel Should return a Error Call`() = runTest {
        val sut = buildCallPostponeHelper(timeout = 5_000L, delayTime = 10_000L)
        val expectedErrorResult =
            "Failed to perform call. Waiting for WS connection was too long."

        val queryChannelCallMock = mock<() -> Call<Channel>>()
        val result = (sut.postponeCall(queryChannelCallMock).await() as Result.Failure).value
        result.message `should be` expectedErrorResult
    }

    private fun buildCallPostponeHelper(timeout: Long, delayTime: Long) = CallPostponeHelper(userScope, timeout) {
        delay(delayTime)
    }
}
