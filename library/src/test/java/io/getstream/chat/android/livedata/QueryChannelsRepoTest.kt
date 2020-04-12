package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.requests.QueryChannelsPaginationRequest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QueryChannelsRepoTest: BaseTest() {

    @Before
    fun setup() {
        client = createClient()
        setupRepo(client, true)
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
        queryRepo.runQuery(request)
        var channels = queryRepo.channels.getOrAwaitValue()
        val oldSize = channels.size
        // verify that a new channel is added to the list
        queryRepo.handleEvent(data.notificationAddedToChannelEvent)
        channels = queryRepo.channels.getOrAwaitValue()
        val newSize = channels.size
        Truth.assertThat(newSize-oldSize).isEqualTo(1)
    }

    @Test
    fun testChannelIdPagination() {
        val list = listOf("a", "b", "c")

        var sub = queryRepo.paginateChannelIds(list, QueryChannelsPaginationRequest(0, 5))
        Truth.assertThat(sub).isEqualTo(listOf("a", "b", "c"))

        sub = queryRepo.paginateChannelIds(list, QueryChannelsPaginationRequest(1, 2))
        Truth.assertThat(sub).isEqualTo(listOf("b", "c"))

        sub = queryRepo.paginateChannelIds(list, QueryChannelsPaginationRequest(3, 2))
        Truth.assertThat(sub).isEqualTo(listOf<String>())

        sub = queryRepo.paginateChannelIds(list, QueryChannelsPaginationRequest(4, 2))
        Truth.assertThat(sub).isEqualTo(listOf<String>())
    }

    @Test
    fun testLoadMore() = runBlocking(Dispatchers.IO) {
        val paginate = QueryChannelsPaginationRequest(0, 2)
        queryRepo.runQuery(paginate)
        var channels = queryRepo.channels.getOrAwaitValue()
        Truth.assertThat(channels.size).isEqualTo(2)
        val request = queryRepo.loadMoreRequest(1)
        Truth.assertThat(request.channelOffset).isEqualTo(2)
        queryRepo.runQuery(request)
        channels = queryRepo.channels.getOrAwaitValue()
        Truth.assertThat(channels.size).isEqualTo(3)

    }

    @Test
    fun offlineRunQuery() = runBlocking(Dispatchers.IO) {
        // insert the query result into offline storage
        val query = QueryChannelsEntity(query.filter, query.sort)
        query.channelCIDs = listOf(data.channel1.cid).toMutableList()
        repo.insertQuery(query)
        repo.storeStateForChannel(data.channel1)
        repo.setOffline()
        val channels = queryRepo.runQueryOffline(QueryChannelsPaginationRequest(0, 2))
        // should return 1 since only 1 is stored in offline storage
        Truth.assertThat(channels?.size).isEqualTo(1)
    }

    @Test
    fun onlineRunQuery() = runBlocking(Dispatchers.IO) {
        // insert the query result into offline storage
        val query = QueryChannelsEntity(query.filter, query.sort)
        query.channelCIDs = listOf(data.channel1.cid).toMutableList()
        repo.insertQuery(query)
        repo.storeStateForChannel(data.channel1)
        repo.setOffline()
        queryRepo.runQuery(QueryChannelsPaginationRequest(0, 2))
        val channels = queryRepo.channels.getOrAwaitValue()
        // should return 1 since only 1 is stored in offline storage
        Truth.assertThat(channels.size).isEqualTo(1)
    }



}