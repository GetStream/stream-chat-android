package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest.Companion.data
import io.getstream.chat.android.livedata.BaseConnectedMockedTest
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SendMessageTest : BaseConnectedMockedTest() {

    @Test
    fun `Should send message`() = runBlocking {
        val message1 = data.createMessage().apply { extraData = mutableMapOf("location" to "Amsterdam") }
        val channelState = Fixture().givenChatDomain(chatDomain).givenChatClient(client).build()
        // Check that current state is empty
        Truth.assertThat(channelState.messages.getOrAwaitValue()).isEmpty()

        chatDomain.useCases.sendMessage(message1).execute()

        Truth.assertThat(channelState.messages.getOrAwaitValue()).isEqualTo(message1)
    }

    private class Fixture {
        private lateinit var chatDomain: ChatDomain

        fun givenChatDomain(chatDomain: ChatDomain) = apply {
            this.chatDomain = chatDomain
        }

        fun givenChatClient(chatClient: ChatClient) = apply {
            whenever(chatClient.sendMessage(any(), any(), any())).thenReturn(TestCall(Result(data.message1)))
        }

        fun build(): ChannelController {
            return chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        }
    }
}
