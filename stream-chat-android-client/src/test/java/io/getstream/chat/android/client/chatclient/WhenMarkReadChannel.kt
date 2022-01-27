package io.getstream.chat.android.client.chatclient

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.plugin.Plugin
import io.getstream.chat.android.client.experimental.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.test.asCall
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.Test

@OptIn(ExperimentalStreamChatApi::class)
internal class WhenMarkReadChannel : BaseChatClientTest() {

    @Test
    fun `Given offline plugin with failing precondition Should not make API call and return error result`() {
        val plugin = mock<ChannelMarkReadListenerPlugin> {
            onBlocking { it.onChannelMarkReadPrecondition(any(), any()) } doReturn Result.error(ChatError())
        }
        val sut = Fixture().givenPlugin(plugin).get()

        val result = sut.markRead("channelType", "channelId").execute()

        result.isError `should be` true
        result.isSuccess `should be` false
    }

    @Test
    fun `Given offline plugin with success precondition Should invoke API call`() {
        val plugin = mock<ChannelMarkReadListenerPlugin> {
            onBlocking { it.onChannelMarkReadPrecondition(any(), any()) } doReturn Result.success(Unit)
        }
        val sut = Fixture().givenPlugin(plugin).get()

        val result = sut.markRead("channelType", "channelId").execute()

        result.isError `should be` false
        result.isSuccess `should be` true
    }

    private inner class Fixture {

        init {
            whenever(api.markRead(any(), any(), any())) doReturn mock<Unit>().asCall()
        }

        fun givenPlugin(plugin: Plugin) = apply {
            plugins.add(plugin)
        }

        fun get(): ChatClient = chatClient
    }
}

@OptIn(ExperimentalStreamChatApi::class)
private interface ChannelMarkReadListenerPlugin: Plugin, ChannelMarkReadListener
