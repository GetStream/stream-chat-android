package io.getstream.chat.android.livedata.controller

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.test.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class QueryChannelsControllerTest : BaseConnectedIntegrationTest() {

    @Test
    fun newChannelAdded() = runBlocking {
        val request = QueryChannelsPaginationRequest(QuerySort(), 0, 30, 10, 0)
        queryControllerImpl.runQuery(request)
        var channels = queryControllerImpl.channels.getOrAwaitValue()
        val oldSize = channels.size
        // verify that a new channel is added to the list
        val addedEvent = data.notificationAddedToChannel3Event
        queryControllerImpl.handleEvent(addedEvent)
        channels = queryControllerImpl.channels.getOrAwaitValue()
        val newSize = channels.size
        Truth.assertThat(newSize - oldSize).isEqualTo(1)
        val channelController = chatDomainImpl.channel(addedEvent.channel)
        val channel = channelController.toChannel()
    }

    @Test
    fun newChannelFiltered() = runBlocking {
        val request = QueryChannelsPaginationRequest(QuerySort(), 0, 30, 10, 0)
        val queryChannelsController = chatDomainImpl.queryChannels(data.filter2, QuerySort())
        queryChannelsController.newChannelEventFilter = { channel: Channel, filterObject: FilterObject ->
            // ignore everything
            false
        }
        queryChannelsController.runQuery(request)
        var channels = queryChannelsController.channels.getOrAwaitValue()
        val oldSize = channels.size
        // verify that a new channel is NOT added to the list
        val addedEvent = data.notificationAddedToChannel3Event
        queryChannelsController.handleEvent(addedEvent)
        channels = queryChannelsController.channels.getOrAwaitValue()
        val newSize = channels.size
        Truth.assertThat(newSize - oldSize).isEqualTo(0)
    }

    @Test
    fun `events for channels not part of the query should be ignored`() {
        runBlocking {
            val request = QueryChannelsPaginationRequest(QuerySort(), 0, 30, 10, 0)
            val queryChannelsController = chatDomainImpl.queryChannels(data.filter2, QuerySort())

            queryChannelsController.runQuery(request)
            val event = data.channelUpdatedEvent2
            Truth.assertThat(event.channel.cid).isNotIn(queryChannelsController.queryChannelsSpec.cids)

            queryChannelsController.handleEvent(event)
            val cids = queryChannelsController.channels.getOrAwaitValue().map { it.cid }
            Truth.assertThat(event.channel.cid).isNotIn(cids)
        }
    }

    @Test
    @Ignore("mock me")
    fun testLoadMore() = runBlocking {
        val paginate = QueryChannelsPaginationRequest(QuerySort(), 0, 2, 10, 0)
        val result = queryControllerImpl.runQuery(paginate)
        assertSuccess(result)
        var channels = queryControllerImpl.channels.getOrAwaitValue()
        Truth.assertThat(channels.size).isEqualTo(2)
        val request = queryControllerImpl.loadMoreRequest(1, 10, 0)
        Truth.assertThat(request.channelOffset).isEqualTo(2)
        val result2 = queryControllerImpl.runQuery(request)
        assertSuccess(result2)
        channels = queryControllerImpl.channels.getOrAwaitValue()
        Truth.assertThat(channels.size).isEqualTo(3)
    }

    @Test
    fun offlineRunQuery() = runBlocking {
        // insert the query result into offline storage
        val query = QueryChannelsSpec(query.filter, query.sort)
        query.cids = listOf(data.channel1.cid)
        chatDomainImpl.repos.queryInsert(query)
        chatDomainImpl.repos.insertMessage(data.message1)
        chatDomainImpl.storeStateForChannel(data.channel1)
        chatDomainImpl.setOffline()
        val channels = queryControllerImpl.runQueryOffline(QueryChannelsPaginationRequest(query.sort, 0, 2, 10, 0))
        // should return 1 since only 1 is stored in offline storage
        Truth.assertThat(channels?.size).isEqualTo(1)
        // verify we load messages correctly
        Truth.assertThat(channels!!.first().messages.size).isEqualTo(1)
    }

    @Test
    @Ignore("mock me")
    fun onlineRunQuery() = runBlocking {
        // insert the query result into offline storage
        val query = QueryChannelsSpec(query.filter, query.sort)
        query.cids = listOf(data.channel1.cid)
        chatDomainImpl.repos.queryInsert(query)
        chatDomainImpl.storeStateForChannel(data.channel1)
        chatDomainImpl.setOffline()
        queryControllerImpl.runQuery(QueryChannelsPaginationRequest(query.sort, 0, 2, 10, 0))
        val channels = queryControllerImpl.channels.getOrAwaitValue()
        // should return 1 since only 1 is stored in offline storage
        Truth.assertThat(channels.size).isEqualTo(1)
    }
}
