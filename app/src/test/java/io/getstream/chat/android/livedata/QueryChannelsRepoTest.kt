package io.getstream.chat.android.livedata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.Filters.`in`
import io.getstream.chat.android.client.models.Filters.and
import io.getstream.chat.android.client.models.Filters.eq
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.utils.TestDataHelper
import io.getstream.chat.android.livedata.utils.TestLoggerHandler
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import io.getstream.chat.android.livedata.utils.waitForSetUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QueryChannelsRepoTest: BaseTest() {

    @Before
    fun setup() {
        client = createClient()
        setupRepo(client, false)
    }

    @After
    fun tearDown() {
        db.close()
        client.disconnect()
    }
    @Test
    fun newChannelAdded() {
        val request = QueryChannelsRequest(filter, 0, 100, messageLimit = 100)
        runBlocking(Dispatchers.IO) {queryRepo._query(request)}
        // TODO: mock the server response for the queryChannels...
        var channels = queryRepo.channels.getOrAwaitValue()
        val oldSize = channels.size
        // verify that a new channel is added to the list
        runBlocking(Dispatchers.IO) {queryRepo.handleEvent(data.notificationAddedToChannelEvent)}
        channels = queryRepo.channels.getOrAwaitValue()
        val newSize = channels.size
        Truth.assertThat(newSize-oldSize).isEqualTo(1)
    }


}