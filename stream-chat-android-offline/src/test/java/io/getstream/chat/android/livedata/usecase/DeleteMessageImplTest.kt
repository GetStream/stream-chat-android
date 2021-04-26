package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest.Companion.data
import io.getstream.chat.android.livedata.BaseConnectedMockedTest
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be`
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class DeleteMessageImplTest : BaseConnectedMockedTest() {

    @Test
    fun `Given a message was sent When deleting the message Should return the deleted message`() {
        runBlocking {
            val message = data.createMessage()
            val channelController = Fixture(client, chatDomain)
                .givenMockedSendMessageResponse(channelClientMock, message)
                .givenMockedDeleteMessageResponse(message)
                .get()

            chatDomain.sendMessage(message).execute()
            chatDomain.deleteMessage(message).execute()

            val deletedMessage = channelController.messages.value.last()

            deletedMessage.id `should be equal to` message.id
            deletedMessage.deletedAt `should not be` null
        }
    }

    private class Fixture(
        private val chatClient: ChatClient,
        private val chatDomain: ChatDomain,
    ) {

        fun givenMockedDeleteMessageResponse(message: Message) = apply {
            whenever(chatClient.deleteMessage(message.id)) doReturn TestCall(Result(message))
        }

        fun givenMockedSendMessageResponse(channelClient: ChannelClient, message: Message) = apply {
            whenever(channelClient.sendMessage(any())).thenReturn(TestCall(Result(message)))
        }

        fun get(): ChannelController {
            return chatDomain.watchChannel(data.channel1.cid, 10)
                .execute()
                .data()
        }
    }
}
