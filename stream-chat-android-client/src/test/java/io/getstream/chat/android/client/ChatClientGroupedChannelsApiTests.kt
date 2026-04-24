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

package io.getstream.chat.android.client

import io.getstream.chat.android.client.chatclient.BaseChatClientTest
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.GroupedChannels
import io.getstream.chat.android.models.GroupedChannelsGroup
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever

/**
 * Tests for the [ChatClient.queryGroupedChannels] endpoint.
 */
internal class ChatClientGroupedChannelsApiTests : BaseChatClientTest() {

    @Test
    fun queryGroupedChannelsSuccess() = runTest {
        // given
        val groupedChannels = GroupedChannels(
            groups = mapOf(
                randomString().let { key ->
                    key to GroupedChannelsGroup(
                        groupKey = key,
                        channels = listOf(randomChannel()),
                        unreadChannels = randomInt(),
                    )
                },
            ),
        )
        val sut = Fixture()
            .givenQueryGroupedChannelsResult(RetroSuccess(groupedChannels).toRetrofitCall())
            .get()
        // when
        val result = sut.queryGroupedChannels().await()
        // then
        verifySuccess(result, groupedChannels)
    }

    @Test
    fun queryGroupedChannelsError() = runTest {
        // given
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenQueryGroupedChannelsResult(RetroError<GroupedChannels>(errorCode).toRetrofitCall())
            .get()
        // when
        val result = sut.queryGroupedChannels().await()
        // then
        verifyNetworkError(result, errorCode)
    }

    internal inner class Fixture {

        fun givenQueryGroupedChannelsResult(
            result: io.getstream.result.call.Call<GroupedChannels>,
        ) = apply {
            whenever(api.queryGroupedChannels(anyOrNull(), any(), any())).thenReturn(result)
        }

        fun get(): ChatClient = chatClient
    }
}
