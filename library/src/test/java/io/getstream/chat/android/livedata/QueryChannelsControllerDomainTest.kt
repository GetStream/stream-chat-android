package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QueryChannelsControllerDomainTest: BaseDomainTest() {

    @Before
    fun setup() {
        client = createClient()
        setupChatDomain(client, true)
    }

    @After
    fun tearDown() {
        db.close()
        client.disconnect()
    }
    @Test
    fun newChannelAdded() = runBlocking(Dispatchers.IO) {
        // TODO: mock the server response for the queryChannels...
        val request = QueryChannelsPaginationRequest()
        queryController.runQuery(request)
        var channels = queryController.channels.getOrAwaitValue()
        val oldSize = channels.size
        // verify that a new channel is added to the list
        queryController.handleEvent(data.notificationAddedToChannelEvent)
        channels = queryController.channels.getOrAwaitValue()
        val newSize = channels.size
        Truth.assertThat(newSize-oldSize).isEqualTo(1)
    }

    @Test
    fun testChannelIdPagination() {
        val list = sortedSetOf("a", "b", "c")

        var sub = queryController.paginateChannelIds(list, QueryChannelsPaginationRequest(0, 5))
        Truth.assertThat(sub).isEqualTo(listOf("a", "b", "c"))

        sub = queryController.paginateChannelIds(list, QueryChannelsPaginationRequest(1, 2))
        Truth.assertThat(sub).isEqualTo(listOf("b", "c"))

        sub = queryController.paginateChannelIds(list, QueryChannelsPaginationRequest(3, 2))
        Truth.assertThat(sub).isEqualTo(listOf<String>())

        sub = queryController.paginateChannelIds(list, QueryChannelsPaginationRequest(4, 2))
        Truth.assertThat(sub).isEqualTo(listOf<String>())
    }

    @Test
    fun testLoadMore() = runBlocking(Dispatchers.IO) {
        val paginate = QueryChannelsPaginationRequest(0, 2)
        queryController.runQuery(paginate)
        var channels = queryController.channels.getOrAwaitValue()
        Truth.assertThat(channels.size).isEqualTo(2)
        val request = queryController.loadMoreRequest(1)
        Truth.assertThat(request.channelOffset).isEqualTo(2)
        queryController.runQuery(request)
        channels = queryController.channels.getOrAwaitValue()
        Truth.assertThat(channels.size).isEqualTo(3)

    }

    @Test
    fun offlineRunQuery() = runBlocking(Dispatchers.IO) {
        // insert the query result into offline storage
        val query = QueryChannelsEntity(query.filter, query.sort)
        query.channelCIDs = sortedSetOf(data.channel1.cid)
        chatDomain.repos.queryChannels.insert(query)
        chatDomain.storeStateForChannel(data.channel1)
        chatDomain.setOffline()
        val channels = queryController.runQueryOffline(QueryChannelsPaginationRequest(0, 2))
        // should return 1 since only 1 is stored in offline storage
        Truth.assertThat(channels?.size).isEqualTo(1)
    }

    @Test
    fun onlineRunQuery() = runBlocking(Dispatchers.IO) {
        // insert the query result into offline storage
        val query = QueryChannelsEntity(query.filter, query.sort)
        query.channelCIDs = sortedSetOf(data.channel1.cid)
        chatDomain.repos.queryChannels.insert(query)
        chatDomain.storeStateForChannel(data.channel1)
        chatDomain.setOffline()
        queryController.runQuery(QueryChannelsPaginationRequest(0, 2))
        val channels = queryController.channels.getOrAwaitValue()
        // should return 1 since only 1 is stored in offline storage
        Truth.assertThat(channels.size).isEqualTo(1)
    }



}