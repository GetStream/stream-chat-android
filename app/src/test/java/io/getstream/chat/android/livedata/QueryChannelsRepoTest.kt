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
class QueryChannelsRepoTest {
    lateinit var database: ChatDatabase
    lateinit var repo: ChatRepo
    lateinit var client: ChatClient
    lateinit var data: TestDataHelper
    lateinit var channelRepo: ChannelRepo
    lateinit var db: ChatDatabase
    lateinit var queryRepo: QueryChannelsRepo
    lateinit var query: QueryChannelsEntity


    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        client = ChatClient.Builder("b67pax5b2wdq", ApplicationProvider.getApplicationContext())
            .logLevel(
                ChatLogLevel.ALL
            ).loggerHandler(TestLoggerHandler()).build()

        // TODO: How do I mock the client?


        // TODO: make all this test setup stuff dry
        data = TestDataHelper()

        val user = data.user1
        val token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJvYWQtbGFrZS0zIn0.SIb263bpikToka22ofV-9AakJhXzfeF8pU9cstvzInE"

        waitForSetUser(
            client,
            user,
            token
        )
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), ChatDatabase::class.java).build()

        repo = ChatRepo(client, data.user1, db)
        repo.errorEvents.observeForever(io.getstream.chat.android.livedata.EventObserver {
            System.out.println("error event$it")
        })
        channelRepo = repo.channel(data.channel1.type, data.channel1.id)
        // TODO: should this be part of the constructor?
        channelRepo.updateChannel(data.channel1)

        val filter = and(eq("type", "messaging"), `in`("members", listOf(user.id)))


        queryRepo = repo.queryChannels(filter)
    }

    @After
    fun tearDown() {
        db.close()
        client.disconnect()
    }

    @Test
    fun newChannelAdded() {
        val request = QueryChannelsRequest(query.filter, 0, 100, messageLimit = 100)
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