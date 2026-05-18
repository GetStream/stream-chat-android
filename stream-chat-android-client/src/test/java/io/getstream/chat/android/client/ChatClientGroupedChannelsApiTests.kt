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
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.GroupedChannels
import io.getstream.chat.android.models.GroupedChannelsGroup
import io.getstream.chat.android.models.GroupedChannelsGroupQuery
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for the [ChatClient.queryGroupedChannels] endpoint.
 */
@OptIn(InternalStreamChatApi::class)
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
                        next = randomString(),
                        prev = randomString(),
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

    @Test
    fun `queryGroupedChannelsInternal dispatches request to plugin listeners before issuing the call`() = runTest {
        // given
        val plugin: Plugin = mock()
        plugins.add(plugin)
        val groupedChannels = GroupedChannels(
            groups = mapOf(
                "direct" to GroupedChannelsGroup(
                    groupKey = "direct",
                    channels = listOf(randomChannel()),
                ),
            ),
        )
        val sut = Fixture()
            .givenQueryGroupedChannelsResult(RetroSuccess(groupedChannels).toRetrofitCall())
            .get()
        val groupsParam = mapOf("direct" to GroupedChannelsGroupQuery(limit = 25))
        // when
        sut.queryGroupedChannelsInternal(
            limit = 30,
            groups = groupsParam,
            watch = true,
            presence = false,
        ).await()
        // then - the request hook fires BEFORE the result hook
        inOrder(plugin) {
            verify(plugin).onQueryGroupedChannelsRequest(
                limit = eq(30),
                groups = eq(groupsParam),
                watch = eq(true),
                presence = eq(false),
            )
            verify(plugin).onQueryGroupedChannelsResult(
                result = any(),
                limit = eq(30),
                groups = eq(groupsParam),
                watch = eq(true),
                presence = eq(false),
            )
        }
    }

    @Test
    fun `queryGroupedChannelsInternal dispatches request hook even when the call fails`() = runTest {
        // given
        val plugin: Plugin = mock()
        plugins.add(plugin)
        val errorCode = positiveRandomInt()
        val sut = Fixture()
            .givenQueryGroupedChannelsResult(RetroError<GroupedChannels>(errorCode).toRetrofitCall())
            .get()
        val groupsParam = mapOf("direct" to GroupedChannelsGroupQuery(limit = 25))
        // when
        sut.queryGroupedChannelsInternal(
            limit = 30,
            groups = groupsParam,
            watch = true,
            presence = false,
        ).await()
        // then - request hook ran, giving the state plugin a chance to capture the config
        // before the result hook reports the failure
        verify(plugin).onQueryGroupedChannelsRequest(
            limit = eq(30),
            groups = eq(groupsParam),
            watch = eq(true),
            presence = eq(false),
        )
    }

    @Test
    fun `queryGroupedChannelsInternal dispatches result to plugin listeners`() = runTest {
        // given
        val plugin: Plugin = mock()
        plugins.add(plugin)
        val groupedChannels = GroupedChannels(
            groups = mapOf(
                "direct" to GroupedChannelsGroup(
                    groupKey = "direct",
                    channels = listOf(randomChannel()),
                    unreadChannels = randomInt(),
                    next = randomString(),
                    prev = null,
                ),
            ),
        )
        val sut = Fixture()
            .givenQueryGroupedChannelsResult(RetroSuccess(groupedChannels).toRetrofitCall())
            .get()
        val groupsParam = mapOf("direct" to GroupedChannelsGroupQuery(limit = 25, next = "cursor"))
        // when
        sut.queryGroupedChannelsInternal(
            limit = 30,
            groups = groupsParam,
            watch = true,
            presence = false,
        ).await()
        // then
        verify(plugin).onQueryGroupedChannelsResult(
            result = any(),
            limit = eq(30),
            groups = eq(groupsParam),
            watch = eq(true),
            presence = eq(false),
        )
    }

    @Test
    fun `public queryGroupedChannels delegates to internal with null groups and fires hooks once`() = runTest {
        // given
        val plugin: Plugin = mock()
        plugins.add(plugin)
        val groupedChannels = GroupedChannels(
            groups = mapOf(
                "direct" to GroupedChannelsGroup(
                    groupKey = "direct",
                    channels = listOf(randomChannel()),
                ),
            ),
        )
        val sut = Fixture()
            .givenQueryGroupedChannelsResult(RetroSuccess(groupedChannels).toRetrofitCall())
            .get()
        // when - public entry point (no `groups` argument)
        sut.queryGroupedChannels(
            limit = 30,
            watch = true,
            presence = false,
        ).await()
        // then - hooks fire exactly once, with groups = null, ordered request before result
        inOrder(plugin) {
            verify(plugin, times(1)).onQueryGroupedChannelsRequest(
                limit = eq(30),
                groups = eq(null),
                watch = eq(true),
                presence = eq(false),
            )
            verify(plugin, times(1)).onQueryGroupedChannelsResult(
                result = any(),
                limit = eq(30),
                groups = eq(null),
                watch = eq(true),
                presence = eq(false),
            )
        }
    }

    internal inner class Fixture {

        fun givenQueryGroupedChannelsResult(
            result: io.getstream.result.call.Call<GroupedChannels>,
        ) = apply {
            whenever(api.queryGroupedChannels(anyOrNull(), anyOrNull(), any(), any())).thenReturn(result)
        }

        fun get(): ChatClient = chatClient
    }
}
