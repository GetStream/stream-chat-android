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

package io.getstream.chat.android.client.chatclient

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.plugin.Plugin
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.test.asCall
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class WhenQueryChannel : BaseChatClientTest() {

    @Test
    fun `Given offline plugin with failing precondition Should not make API call and return error result`() {
        val plugin = mock<QueryChannelListenerPlugin> {
            onBlocking { it.onQueryChannelPrecondition(any(), any(), any()) } doReturn Result.error(ChatError())
        }
        var isNetworkApiCalled = false
        val sut = Fixture().givenPlugin(plugin).givenChannelResponse { isNetworkApiCalled = true; mock() }.get()

        val result = sut.queryChannel("channelType", "channelId", QueryChannelRequest(), true).execute()

        result.isError `should be` true
        isNetworkApiCalled `should be` false
    }

    @Test
    fun `Given offline plugin with success precondition Should make API call and return it's result`() {
        val plugin = mock<QueryChannelListenerPlugin> {
            onBlocking { it.onQueryChannelPrecondition(any(), any(), any()) } doReturn Result.success(Unit)
        }
        var isNetworkApiCalled = false
        val sut = Fixture().givenPlugin(plugin).givenChannelResponse { isNetworkApiCalled = true; mock() }.get()

        val result = sut.queryChannel("channelType", "channelId", QueryChannelRequest(), true).execute()

        result.isSuccess `should be` true
        isNetworkApiCalled `should be` true
    }

    @Test
    fun `Given offline plugin with success precondition Should invoke methods in right order`() {
        val list = mutableListOf<Int>()
        val plugin = mock<QueryChannelListenerPlugin> {
            onBlocking { it.onQueryChannelPrecondition(any(), any(), any()) } doAnswer {
                list.add(1)
                Result.success(Unit)
            }
            onBlocking { it.onQueryChannelRequest(any(), any(), any()) } doAnswer {
                list.add(2)
                Unit
            }
            onBlocking { it.onQueryChannelResult(any(), any(), any(), any()) } doAnswer {
                list.add(4)
                Unit
            }
        }
        val sut = Fixture().givenPlugin(plugin).givenChannelResponse {
            println("Executing request")
            list.add(3)
            mock()
        }.get()

        sut.queryChannel("channelType", "channelId", QueryChannelRequest(), true).execute()

        list `should be equal to` listOf(1, 2, 3, 4)
    }

    private inner class Fixture {

        init {
            whenever(api.queryChannel(any(), any(), any(), any())) doReturn mock<Channel>().asCall()
        }

        fun givenPlugin(plugin: Plugin) = apply {
            plugins.add(plugin)
        }

        fun givenChannelResponse(channelProvider: () -> Channel) = apply {
            whenever(api.queryChannel(any(), any(), any(), any())) doAnswer {
                CoroutineCall(coroutineRule.scope) {
                    Result.success(channelProvider())
                }
            }
        }

        fun get(): ChatClient = chatClient.apply {
            addPlugins(this@WhenQueryChannel.plugins)
        }
    }
}

private interface QueryChannelListenerPlugin : Plugin, QueryChannelListener
