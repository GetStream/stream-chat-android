package io.getstream.chat.android.livedata

import androidx.recyclerview.widget.DiffUtil
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.controller.QueryChannelsSpec
import io.getstream.chat.android.livedata.utils.ChannelDiffCallback
import io.getstream.chat.android.livedata.utils.LiveDiffCounter
import io.getstream.chat.android.livedata.utils.MessageDiffCallback
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class PerformanceTest : BaseConnectedMockedTest() {

    @Ignore("Failing for unknown reasons")
    @Test
    fun channels() = runBlocking {
        var channelControllerImpl = chatDomainImpl.channel(data.channel1)
        val queryChannelsControllerImpl = chatDomainImpl.queryChannels(data.filter1, QuerySort())

        val counter = LiveDiffCounter { old: List<Channel>, new: List<Channel> ->
            DiffUtil.calculateDiff(ChannelDiffCallback(old, new), true)
        }

        queryChannelsControllerImpl.channels.observeForever { channels ->
            val ids = channels.map { it.cid }
            println("Channel ids is now equal to $ids")
            counter.onEvent(channels)
        }
        // Insert a query, channel and message into offline storage
        val query = QueryChannelsSpec(data.filter1, QuerySort()).apply { cids = listOf(data.channel1.cid) }
        chatDomainImpl.repos.insertChannel(data.channel1)
        chatDomainImpl.repos.insertMessage(data.message1)
        chatDomainImpl.repos.queryInsert(query)

        // API call to .queryChannels is mocked and returns data.channel1

        // should trigger 2 updates, once for offline and another for online
        queryChannelsControllerImpl.query()

        // adding a new channels, insert
        chatDomainImpl.eventHandler.handleEvent(data.notificationAddedToChannel2Event)

        // adding a message should lead to an changed count increase
        chatDomainImpl.eventHandler.handleEvent(data.newMessageFromUser2)

        // updating a message should lead to a changed count increase
        chatDomainImpl.eventHandler.handleEvent(data.messageUpdatedEvent)

        Truth.assertThat(counter.counts).isEqualTo(mutableMapOf("events" to 5, "changed" to 3, "moved" to 0, "inserted" to 1, "removed" to 0))
    }

    @Test
    fun messages() = runBlocking {
        val channelController = chatDomainImpl.channel(data.channel1)

        val counter = LiveDiffCounter { old: List<Message>, new: List<Message> ->
            DiffUtil.calculateDiff(MessageDiffCallback(old, new), true)
        }

        channelController.messages.observeForever { messages ->
            val messageIds = messages.map { it.id }
            println("Message ids is now equal to $messageIds")
            counter.onEvent(messages)
        }
        // Insert 1 message into offline storage
        chatDomainImpl.repos.insertChannel(data.channel1)
        chatDomainImpl.repos.insertMessage(data.message1)

        // API call to .watch is mocked and returns data.channel1

        // should trigger 2 updates, once for offline and another for online
        channelController.watch()

        // adding a message should lead to an inserted count increase
        chatDomainImpl.eventHandler.handleEvent(data.newMessageFromUser2)

        // updating a message should lead to a changed event
        chatDomainImpl.eventHandler.handleEvent(data.messageUpdatedEvent)

        Truth.assertThat(counter.counts).isEqualTo(mutableMapOf("events" to 4, "changed" to 1, "moved" to 0, "inserted" to 2, "removed" to 0))
    }
}
