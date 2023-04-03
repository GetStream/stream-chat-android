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
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
internal class WhenQueryChannel : BaseChatClientTest() {

    @Test
    fun `Given offline plugin with failing precondition Should not make API call and return error result`() = runTest {
        val plugin = mock<QueryChannelListenerPlugin> {
            onBlocking { it.onQueryChannelPrecondition(any(), any(), any()) } doReturn Result.Failure(Error.GenericError(message = ""))
        }
        var isNetworkApiCalled = false
        val sut = Fixture().givenPlugin(plugin).givenChannelResponse { isNetworkApiCalled = true; mock() }.get()

        val result = sut.queryChannel("channelType", "channelId", QueryChannelRequest()).await()

        result shouldBeInstanceOf Result.Failure::class
        isNetworkApiCalled `should be` false
    }

    @Test
    fun `Given offline plugin with success precondition Should make API call and return it's result`() = runTest {
        val plugin = mock<QueryChannelListenerPlugin> {
            onBlocking { it.onQueryChannelPrecondition(any(), any(), any()) } doReturn Result.Success(Unit)
        }
        var isNetworkApiCalled = false
        val sut = Fixture().givenPlugin(plugin).givenChannelResponse { isNetworkApiCalled = true; mock() }.get()

        val result = sut.queryChannel("channelType", "channelId", QueryChannelRequest()).await()

        result shouldBeInstanceOf Result.Success::class
        isNetworkApiCalled `should be` true
    }

    @Test
    fun `Given offline plugin with success precondition Should invoke methods in right order`() = runTest {
        val list = mutableListOf<Int>()
        val plugin = mock<QueryChannelListenerPlugin> {
            onBlocking { it.onQueryChannelPrecondition(any(), any(), any()) } doAnswer {
                list.add(1)
                Result.Success(Unit)
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

        sut.queryChannel("channelType", "channelId", QueryChannelRequest(), skipOnRequest = false).await()

        list `should be equal to` listOf(1, 2, 3, 4)
    }

    private inner class Fixture {

        init {
            whenever(api.queryChannel(any(), any(), any())) doReturn mock<Channel>().asCall()
        }

        fun givenPlugin(plugin: Plugin) = apply {
            plugins.add(plugin)
        }

        fun givenChannelResponse(channelProvider: () -> Channel) = apply {
            whenever(api.queryChannel(any(), any(), any())) doAnswer {
                CoroutineCall(testCoroutines.scope) {
                    Result.Success(channelProvider())
                }
            }
        }

        fun get(): ChatClient = chatClient.apply {
            plugins = this@WhenQueryChannel.plugins
        }
    }
}

private interface QueryChannelListenerPlugin : Plugin, QueryChannelListener
