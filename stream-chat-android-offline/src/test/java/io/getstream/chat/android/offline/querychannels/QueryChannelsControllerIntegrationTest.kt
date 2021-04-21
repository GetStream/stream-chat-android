package io.getstream.chat.android.offline.querychannels

import androidx.recyclerview.widget.DiffUtil
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.BaseConnectedMockedTest
import io.getstream.chat.android.livedata.controller.QueryChannelsSpec
import io.getstream.chat.android.livedata.utils.ChannelDiffCallback
import io.getstream.chat.android.livedata.utils.DiffUtilOperationCounter
import io.getstream.chat.android.livedata.utils.UpdateOperationCounts
import io.getstream.chat.android.offline.ChatDomainImpl
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class QueryChannelsControllerIntegrationTest : BaseConnectedMockedTest() {

    @Test
    fun `When observing channels Should receive the correct number of events with channels`() = runBlocking {
        val counter = DiffUtilOperationCounter { old: List<Channel>, new: List<Channel> ->
            DiffUtil.calculateDiff(ChannelDiffCallback(old, new), true)
        }
        val sut = Fixture(chatDomainImpl, data.filter1)
            .givenChannelInOfflineStorage(data.channel1)
            .givenMessageInOfflineStorage(data.message1)
            .withCounter(counter)
            .get()

        // querying channels, should trigger 2 events (once for offline and another for online),
        // but there should be only 1 insert since the channels in our case are the same
        sut.query()
        Truth.assertThat(counter.counts).isEqualTo(UpdateOperationCounts(events = 2, changed = 0, inserted = 1))

        // adding a new channel, should trigger 1 "insert" operation
        chatDomainImpl.eventHandler.handleEvent(data.notificationAddedToChannel2Event)
        Truth.assertThat(counter.counts).isEqualTo(UpdateOperationCounts(events = 3, changed = 0, inserted = 2))

        // adding a new message, should trigger 1 "changed" operation
        chatDomainImpl.eventHandler.handleEvent(data.newMessageFromUser2)
        Truth.assertThat(counter.counts).isEqualTo(UpdateOperationCounts(events = 4, changed = 1, inserted = 2))

        // updating the last message, should should trigger 1 "changed" operation
        chatDomainImpl.eventHandler.handleEvent(data.messageUpdatedEvent)
        Truth.assertThat(counter.counts).isEqualTo(UpdateOperationCounts(events = 5, changed = 2, inserted = 2))
    }

    private class Fixture(
        private val chatDomainImpl: ChatDomainImpl,
        private val filter: FilterObject,
    ) {
        private val queryChannelsControllerImpl = chatDomainImpl.queryChannels(filter, QuerySort())

        fun givenChannelInOfflineStorage(channel: Channel): Fixture {
            runBlocking {
                val query = QueryChannelsSpec(filter, QuerySort()).apply { cids = listOf(channel.cid) }
                chatDomainImpl.repos.insertChannel(channel)
                chatDomainImpl.repos.insertQueryChannels(query)
            }
            return this
        }

        fun givenMessageInOfflineStorage(message: Message): Fixture {
            runBlocking {
                chatDomainImpl.repos.insertMessage(message)
            }
            return this
        }

        suspend fun withCounter(counter: DiffUtilOperationCounter<Channel>): Fixture {
            chatDomainImpl.scope.launch {
                queryChannelsControllerImpl.channels.collect { channels ->
                    val ids = channels.map { it.cid }
                    println("Channel ids is now equal to $ids")
                    counter.onEvent(channels)
                }
            }
            return this
        }

        fun get(): QueryChannelsController = queryChannelsControllerImpl
    }
}
