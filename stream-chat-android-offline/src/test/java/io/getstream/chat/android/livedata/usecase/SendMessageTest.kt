package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
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
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SendMessageTest : BaseConnectedMockedTest() {

    @Test
    fun `Given a message was sent When subscribing message updates Should emit the sent message`() {
        runBlocking {
            val message = data.createMessage().apply { extraData = mutableMapOf("location" to "Amsterdam") }
            val channelController = Fixture(chatDomain)
                .givenMockedSendMessageResponse(channelClientMock, message)
                .get()

            // check that current state is empty
            channelController.messages.value.size `should be equal to` 0

            chatDomain.sendMessage(message).execute()

            channelController.messages.value.last() `should be equal to` message
        }
    }

    private class Fixture(private val chatDomain: ChatDomain) {

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
