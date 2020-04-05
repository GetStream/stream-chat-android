package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
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
    fun newChannelAdded() {
        val request = QueryChannelsPaginationRequest()
        runBlocking(Dispatchers.IO) {queryRepo.runQuery(request)}
        // TODO: mock the server response for the queryChannels...
        var channels = queryRepo.channels.getOrAwaitValue()
        val oldSize = channels.size
        // verify that a new channel is added to the list
        runBlocking(Dispatchers.IO) {queryRepo.handleEvent(data.notificationAddedToChannelEvent)}
        channels = queryRepo.channels.getOrAwaitValue()
        val newSize = channels.size
        Truth.assertThat(newSize-oldSize).isEqualTo(1)
    }

    @Test
    fun testQuery() = runBlocking(Dispatchers.IO) {
        val paginate = QueryChannelsPaginationRequest(3)
        queryRepo.runQuery(paginate)
        val channels = queryRepo.channels.getOrAwaitValue()
        Truth.assertThat(channels.size).isEqualTo(3)
    }

    @Test
    fun testLoadMore() = runBlocking(Dispatchers.IO) {
        val paginate = QueryChannelsPaginationRequest(0, 2)
        queryRepo.runQuery(paginate)
        var channels = queryRepo.channels.getOrAwaitValue()
        Truth.assertThat(channels.size).isEqualTo(2)
        val request = queryRepo.loadMoreRequest(1)
        Truth.assertThat(request.offset).isEqualTo(2)
        queryRepo.runQuery(request)
        channels = queryRepo.channels.getOrAwaitValue()
        Truth.assertThat(channels.size).isEqualTo(3)
    }


}