package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
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
        /*val message = data.createMessage()
        val fixture = Fixture(chatDomain).givenMessage(message)
        val message1 = data.createMessage()

        val channelState = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()

        val result =
        assertSuccess(result)
        var messages = channelState.messages.getOrAwaitValue()
        Truth.assertThat(messages.last()).isEqualTo(message1)
        val result2 = chatDomain.useCases.deleteMessage(message1).execute()
        assertSuccess(result2)
        messages = channelState.messages.getOrAwaitValue()
        Truth.assertThat(messages.last().id).isEqualTo(result.data().id)
        Truth.assertThat(messages.last().deletedAt).isNotNull()*/
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

    fun build(): List<Message> {
        return channelState.messages.getOrAwaitValue()
    }
}
