package io.getstream.chat.android.client.chatclient

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.plugin.Plugin
import io.getstream.chat.android.client.experimental.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.test.asCall
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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

        fun get(): ChatClient = chatClient.apply {
            addPlugins(this@WhenMarkReadChannel.plugins)
        }
    }
}

private interface ChannelMarkReadListenerPlugin : Plugin, ChannelMarkReadListener
