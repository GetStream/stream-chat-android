/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.plugin

import io.getstream.chat.android.client.receipts.MessageReceiptManager
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.randomChannel
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.verification.VerificationMode

internal class MessageDeliveredPluginTest {

    @Test
    fun `on query channels with successful result, should mark channels as delivered`() = runTest {
        val channels = listOf(randomChannel())
        val fixture = Fixture()
        val sut = fixture.get()

        sut.onQueryChannelsResult(result = Result.Success(channels), request = mock())

        fixture.verifyMarkChannelsAsDeliveredCalled(channels = channels)
    }

    @Test
    fun `on query channels with failure result, should not mark channels as delivered`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get()

        sut.onQueryChannelsResult(result = Result.Failure(mock()), request = mock())

        fixture.verifyMarkChannelsAsDeliveredCalled(never())
    }

    @Test
    fun `on query channel with successful result and null pagination, should mark channel as delivered`() = runTest {
        val channel = randomChannel()
        val fixture = Fixture()
        val sut = fixture.get()

        sut.onQueryChannelResult(
            result = Result.Success(channel),
            channelType = channel.type,
            channelId = channel.id,
            request = mock { on { pagination() } doReturn null },
        )

        fixture.verifyMarkChannelsAsDeliveredCalled(channels = listOf(channel))
    }

    @Test
    fun `on query channel with successful result and non-null pagination, should not mark channel as delivered`() =
        runTest {
            val channel = randomChannel()
            val fixture = Fixture()
            val sut = fixture.get()

            sut.onQueryChannelResult(
                result = Result.Success(channel),
                channelType = channel.type,
                channelId = channel.id,
                request = mock { on { pagination() } doReturn mock() },
            )

            fixture.verifyMarkChannelsAsDeliveredCalled(never())
        }

    @Test
    fun `on query channel with failure result, should not mark channel as delivered`() = runTest {
        val channel = randomChannel()
        val fixture = Fixture()
        val sut = fixture.get()

        sut.onQueryChannelResult(
            result = Result.Failure(mock()),
            channelType = channel.type,
            channelId = channel.id,
            request = mock(),
        )

        fixture.verifyMarkChannelsAsDeliveredCalled(never())
    }

    private class Fixture {
        private val mockMessageReceiptManager = mock<MessageReceiptManager>()

        fun verifyMarkChannelsAsDeliveredCalled(
            mode: VerificationMode = times(1),
            channels: List<Channel>? = null,
        ) {
            verify(mockMessageReceiptManager, mode).markChannelsAsDelivered(channels ?: any())
        }

        fun get() = MessageDeliveredPlugin(
            chatClient = mock(),
            messageReceiptManager = mockMessageReceiptManager,
        )
    }
}
