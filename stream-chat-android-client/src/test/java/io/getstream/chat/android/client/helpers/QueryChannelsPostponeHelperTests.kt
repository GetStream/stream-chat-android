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

package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.clientstate.SocketState
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.positiveRandomInt
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class QueryChannelsPostponeHelperTests {
    private lateinit var socketStateService: SocketStateService

    private lateinit var sut: QueryChannelsPostponeHelper

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private const val ATTEMPTS_COUNT = 2
        private const val DELAY_DURATION = 30L
    }

    @BeforeEach
    fun setUp() {
        socketStateService = mock()
        sut = QueryChannelsPostponeHelper(socketStateService, testCoroutines.scope, DELAY_DURATION, ATTEMPTS_COUNT)
    }

    @Test
    fun `Given connected state When query channels Should return channels from api`() {
        val expectedResult = List(positiveRandomInt(10)) { Mother.randomChannel() }
        val queryChannelsCallMock = mock<() -> Call<List<Channel>>>()
        whenever(queryChannelsCallMock.invoke()) doReturn expectedResult.asCall()
        whenever(socketStateService.state) doReturn SocketState.Connected(Mother.randomString())

        val result = sut.postponeQueryChannels(queryChannelsCallMock).execute().data()

        verify(queryChannelsCallMock).invoke()
        result shouldBeEqualTo expectedResult
    }

    @Test
    fun `Given idle connection state When query channel Should return a Error Call`() {
        val expectedErrorResult =
            "Failed to perform job. Waiting for set user completion was too long. Limit of attempts was reached."
        whenever(socketStateService.state) doReturn SocketState.Idle

        val result = sut.postponeQueryChannels(mock()).execute().error()
        result.message `should be` expectedErrorResult
    }

    @Test
    fun `Given long pending socket state When query channel Should return a Error Call`() {
        val expectedErrorResult =
            "Failed to perform job. Waiting for set user completion was too long. Limit of attempts was reached."
        whenever(socketStateService.state) doReturn SocketState.Pending

        val result = sut.postponeQueryChannels(mock()).execute().error()
        result.message `should be` expectedErrorResult
    }

    @Test
    fun `Given pending state and connected then When query channel Should query to api and return result`() {
        val expectedResult = List(positiveRandomInt(10)) { Mother.randomChannel() }
        val queryChannelsCallMock = mock<() -> Call<List<Channel>>>()
        whenever(queryChannelsCallMock.invoke()) doReturn expectedResult.asCall()
        whenever(socketStateService.state)
            .thenReturn(SocketState.Pending)
            .thenReturn(SocketState.Connected(Mother.randomString()))

        val result = sut.postponeQueryChannels(queryChannelsCallMock).execute().data()

        verify(queryChannelsCallMock).invoke()
        result shouldBeEqualTo expectedResult
    }
}
