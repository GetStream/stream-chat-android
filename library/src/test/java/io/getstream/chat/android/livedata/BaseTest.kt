package io.getstream.chat.android.livedata

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.entity.ChannelConfigEntity
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.utils.TestDataHelper
import io.getstream.chat.android.livedata.utils.TestLoggerHandler
import io.getstream.chat.android.livedata.utils.waitForSetUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

open class BaseTest {
    lateinit var database: ChatDatabase
    lateinit var repo: ChatRepo
    lateinit var client: ChatClient
    lateinit var data: TestDataHelper
    lateinit var channelRepo: ChannelRepo
    lateinit var db: ChatDatabase
    lateinit var queryRepo: QueryChannelsRepo
    lateinit var query: QueryChannelsEntity
    lateinit var filter: FilterObject

    @get:Rule
    val rule = InstantTaskExecutorRule()

    fun createClient(): ChatClient {
        val client = ChatClient.Builder("b67pax5b2wdq", getApplicationContext())
            .logLevel(
                ChatLogLevel.ALL
            ).loggerHandler(TestLoggerHandler()).build()
        return client
    }

//    fun createMockClient(): ChatClient {
//        mock = MockClientBuilder()
//        client = mock.build()
//        return client
//    }

    fun createRoomDb(): ChatDatabase {
        db = Room.inMemoryDatabaseBuilder(
            getApplicationContext(), ChatDatabase::class.java
        ).build()
        return db
    }

    fun setupRepo(client: ChatClient, setUser: Boolean) {
        data = TestDataHelper()

        if (setUser) {
            waitForSetUser(
                client,
                data.user1,
                data.user1Token
            )
        }

        db = createRoomDb()
        val context = getApplicationContext() as Context
        repo = ChatRepo.Builder(context, client, data.user1).database(db).offlineEnabled().userPresenceEnabled().build()

        repo.errorEvents.observeForever(io.getstream.chat.android.livedata.EventObserver {
            System.out.println("error event$it")
        })
        val config = Config().apply { isTypingEvents=true; isReadEvents=true }
        runBlocking(Dispatchers.IO) {repo.insertConfigs(mutableMapOf("messaging" to config))}
        channelRepo = repo.channel(data.channel1.type, data.channel1.id)
        channelRepo.updateChannel(data.channel1)

        filter = Filters.and(Filters.eq("type", "messaging"), Filters.`in`("members", listOf(data.user1.id)))

        queryRepo = repo.queryChannels(filter)
    }


}