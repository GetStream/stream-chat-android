package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message
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
internal class DeleteMessageImplTest : BaseConnectedMockedTest() {

    @Test
    fun deleteMessageUseCase() = runBlocking {
        val message = data.createMessage()
        val channelController = Fixture().givenChatClient(client).givenChatDomain(chatDomain).givenMessage(message).build()

        chatDomain.useCases.deleteMessage(message).execute()

        val deletedMessage = channelController.messages.getOrAwaitValue().last()

        Truth.assertThat(deletedMessage.id).isEqualTo(message.id)
        Truth.assertThat(deletedMessage.deletedAt).isNotNull()
    }
}

private class Fixture {
    private lateinit var chatDomain: ChatDomain
    private lateinit var chatClient: ChatClient

    private lateinit var channelState: ChannelController

    fun givenChatClient(chatClient: ChatClient) = apply {
        this.chatClient = chatClient
    }

    fun givenChatDomain(chatDomain: ChatDomain) = apply {
        this.chatDomain = chatDomain
        channelState = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
    }

    fun givenMessage(message: Message) = apply {
        whenever(chatClient.sendMessage(any(), any(), eq(message))).thenReturn(TestCall(Result(message)))
        chatDomain.useCases.sendMessage(message).execute()
    }

    fun build(): ChannelController {
        return channelState
    }
}
