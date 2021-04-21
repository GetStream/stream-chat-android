package io.getstream.chat.android.offline.channel.controller

import androidx.recyclerview.widget.DiffUtil
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.BaseConnectedMockedTest
import io.getstream.chat.android.livedata.utils.DiffUtilOperationCounter
import io.getstream.chat.android.livedata.utils.MessageDiffCallback
import io.getstream.chat.android.livedata.utils.UpdateOperationCounts
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ChannelControllerIntegrationTest : BaseConnectedMockedTest() {

    @Test
    fun `When observing messages Should receive the correct number of events with messages`() = runBlocking {
        val counter = DiffUtilOperationCounter { old: List<Message>, new: List<Message> ->
            DiffUtil.calculateDiff(MessageDiffCallback(old, new), true)
        }
        val sut = Fixture(chatDomainImpl, data.channel1)
            .givenChannelInOfflineStorage(data.channel1)
            .givenMessageInOfflineStorage(data.message1)
            .withCounter(counter)
            .get()

        // watching a channel, should trigger 2 updates (once for offline and another for online),
        // but there should be only 1 insert since the messages in our case are the same
        sut.watch()
        Truth.assertThat(counter.counts).isEqualTo(UpdateOperationCounts(events = 2, changed = 0, inserted = 1))

        // adding a message, should trigger 1 "insert" operation
        chatDomainImpl.eventHandler.handleEvent(data.newMessageFromUser2)
        Truth.assertThat(counter.counts).isEqualTo(UpdateOperationCounts(events = 3, changed = 0, inserted = 2))

        // updating a message, should trigger 1 "changed" operation
        chatDomainImpl.eventHandler.handleEvent(data.messageUpdatedEvent)
        Truth.assertThat(counter.counts).isEqualTo(UpdateOperationCounts(events = 4, changed = 1, inserted = 2))
    }

    private class Fixture(
        private val chatDomainImpl: ChatDomainImpl,
        channel: Channel,
    ) {
        private val channelController = chatDomainImpl.channel(channel)

        fun givenChannelInOfflineStorage(channel: Channel): Fixture {
            runBlocking {
                chatDomainImpl.repos.insertChannel(channel)
            }
            return this
        }

        fun givenMessageInOfflineStorage(message: Message): Fixture {
            runBlocking {
                chatDomainImpl.repos.insertMessage(message)
            }
            return this
        }

        suspend fun withCounter(counter: DiffUtilOperationCounter<Message>): Fixture {
            chatDomainImpl.scope.launch {
                channelController.messages.collect { messages ->
                    val messageIds = messages.map { it.id }
                    println("Message ids is now equal to $messageIds")
                    counter.onEvent(messages)
                }
            }
            return this
        }

        fun get(): ChannelController = channelController
    }
}
