package io.getstream.chat.android.client.chatclient

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.plugin.Plugin
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.test.asCall
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

@OptIn(ExperimentalStreamChatApi::class)
internal class WhenQueryChannel : BaseChatClientTest() {

    @Test
    fun `Given offline plugin with failing precondition Should not make API call and return error result`() {
        val plugin = mock<Plugin> {
            onBlocking { it.onQueryChannelPrecondition(any(), any(), any()) } doReturn Result.error(ChatError())
        }
        var isNetworkApiCalled = false
        val sut = Fixture().givenPlugin(plugin).givenChannelResponse { isNetworkApiCalled = true; mock() }.get()

        val result = sut.queryChannel("channelType", "channelId", QueryChannelRequest()).execute()

        result.isError `should be` true
        isNetworkApiCalled `should be` false
    }

    @Test
    fun `Given offline plugin with success precondition Should make API call and return it's result`() {
        val plugin = mock<Plugin> {
            onBlocking { it.onQueryChannelPrecondition(any(), any(), any()) } doReturn Result.success(Unit)
        }
        var isNetworkApiCalled = false
        val sut = Fixture().givenPlugin(plugin).givenChannelResponse { isNetworkApiCalled = true; mock() }.get()

        val result = sut.queryChannel("channelType", "channelId", QueryChannelRequest()).execute()

        result.isSuccess `should be` true
        isNetworkApiCalled `should be` true
    }

    @Test
    fun `Given offline plugin with success precondition Should invoke methods in right order`() {
        val list = mutableListOf<Int>()
        val plugin = mock<Plugin> {
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
            mock<Channel>()
        }.get()

        sut.queryChannel("channelType", "channelId", QueryChannelRequest()).execute()

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
                CoroutineCall(coroutineRule.scope) {
                    Result.success(channelProvider())
                }
            }
        }

        fun get(): ChatClient = chatClient
    }
}
