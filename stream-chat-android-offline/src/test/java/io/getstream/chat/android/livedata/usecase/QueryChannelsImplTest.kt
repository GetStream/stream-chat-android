package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.utils.calendar
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
internal class QueryChannelsImplTest : BaseConnectedIntegrationTest() {

    @Test
    @Ignore("mock me")
    fun filter() = runBlocking(Dispatchers.IO) {
        // use case style syntax
        val queryChannelResult = chatDomain.useCases.queryChannels(data.filter1, QuerySort()).execute()
        assertSuccess(queryChannelResult)
        val queryChannelsController = queryChannelResult.data()
        val channels = queryChannelsController.channels.getOrAwaitValue()
        Truth.assertThat(channels).isNotEmpty()
        for (channel in channels) {
            Truth.assertThat(channel.unreadCount).isNotNull()
        }
    }

    @Test
    @Ignore("mock me")
    fun unreadCountNewMessage() = runBlocking(Dispatchers.IO) {
        val queryChannelResult = chatDomain.useCases.queryChannels(data.filter1, QuerySort()).execute()
        assertSuccess(queryChannelResult)
        val queryChannelsController = queryChannelResult.data()
        val channels = queryChannelsController.channels.getOrAwaitValue()
        Truth.assertThat(channels).isNotEmpty()
        val channel = channels.first()
        val initialCount = channel.unreadCount!!
        val message2 = Message().apply { text = "it's a beautiful world"; cid = channel.cid; user = data.user2; createdAt = calendar(2021, 5, 14) }
        val messageEvent = NewMessageEvent(EventType.MESSAGE_NEW, Date(), data.user2, channel.cid, channel.type, channel.id, message2, null, null, null)
        val channelController = chatDomainImpl.channel(channel)
        chatDomainImpl.eventHandler.handleEvent(messageEvent)
        // new message should increase the count by 1
        Truth.assertThat(channelController.unreadCount.getOrAwaitValue()).isEqualTo(initialCount + 1)
        Truth.assertThat(queryChannelsController.channels.getOrAwaitValue().first().unreadCount).isEqualTo(initialCount + 1)
        // mark read should set it to zero
        val readEvent = MessageReadEvent(EventType.MESSAGE_READ, Date(), data.user1, channel.cid, channel.type, channel.id, null)
        chatDomainImpl.eventHandler.handleEvent(readEvent)
        val read = channelController.read.getOrAwaitValue()
        Truth.assertThat(read!!.lastRead).isEqualTo(readEvent.createdAt)
        Truth.assertThat(channelController.unreadCount.getOrAwaitValue()).isEqualTo(0)
        Truth.assertThat(queryChannelsController.channels.getOrAwaitValue().first().unreadCount).isEqualTo(0)
    }
}
