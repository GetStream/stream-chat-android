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

package io.getstream.chat.android.client.query.request

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.query.request.ChannelFilterRequest.filterWithOffset
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ChannelFilterRequestTest {

    @Test
    fun testFilterWithOffsetSuccess() = runTest {
        // given
        val filter = Filters.neutral()
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val channels = listOf(randomChannel())
        val chatClient = mock<ChatClient>()
        whenever(chatClient.queryChannelsInternal(any())).thenReturn(RetroSuccess(channels).toRetrofitCall())
        // when
        val result = chatClient.filterWithOffset(filter, offset, limit)
        // then
        val expectedRequest = QueryChannelsRequest(
            filter = filter,
            offset = offset,
            limit = limit,
            messageLimit = 0,
            memberLimit = 0,
        )
        result shouldBeEqualTo channels
        verify(chatClient).queryChannelsInternal(expectedRequest)
    }

    @Test
    fun testFilterWithOffsetFailure() = runTest {
        // given
        val filter = Filters.neutral()
        val offset = positiveRandomInt()
        val limit = positiveRandomInt()
        val errorCode = positiveRandomInt()
        val chatClient = mock<ChatClient>()
        whenever(chatClient.queryChannelsInternal(any())).thenReturn(RetroError<List<Channel>>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.filterWithOffset(filter, offset, limit)
        // then
        val expectedRequest = QueryChannelsRequest(
            filter = filter,
            offset = offset,
            limit = limit,
            messageLimit = 0,
            memberLimit = 0,
        )
        result shouldBeEqualTo emptyList()
        verify(chatClient).queryChannelsInternal(expectedRequest)
    }

    companion object {

        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }
}
