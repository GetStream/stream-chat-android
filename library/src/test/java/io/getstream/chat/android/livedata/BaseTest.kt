package io.getstream.chat.android.livedata

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.ChatApiImpl
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.RetrofitApi
import io.getstream.chat.android.client.api.models.RetrofitCdnApi
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.controllers.ChannelController
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.client.parser.ChatParserImpl
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.UuidGeneratorImpl
import io.getstream.chat.android.livedata.entity.ChannelConfigEntity
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock

open class BaseTest {
    lateinit var database: ChatDatabase
    lateinit var repo: ChatRepo
    lateinit var client: ChatClient
    lateinit var channelRepo: ChannelRepo
    lateinit var db: ChatDatabase
    lateinit var queryRepo: QueryChannelsRepo
    lateinit var query: QueryChannelsEntity
    lateinit var filter: FilterObject

    var data = TestDataHelper()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    fun createClient(): ChatClient {
        val client = ChatClient.Builder(data.apiKey, getApplicationContext())
            .logLevel(
                ChatLogLevel.ALL
            ).loggerHandler(TestLoggerHandler()).build()
        return client
    }

    fun createMockClient(): ChatClient {
        val config = ChatClientConfig(
            data.apiKey,
            "hello.http",
            "cdn.http",
            "socket.url",
            1000,
            1000,
            ChatLogger.Config(ChatLogLevel.NOTHING, null),
            ChatNotificationConfig(getApplicationContext())
        )

        var socket = Mockito.mock(ChatSocket::class.java)
        var retrofitApi = Mockito.mock(RetrofitApi::class.java)
        var retrofitCdnApi = Mockito.mock(RetrofitCdnApi::class.java)
        var notificationsManager = Mockito.mock(ChatNotifications::class.java)
        var api = ChatApiImpl(
            config.apiKey,
            retrofitApi,
            retrofitCdnApi,
            ChatParserImpl(),
            UuidGeneratorImpl()
        )

        val connectedEvent = ConnectedEvent().apply {
            me = data.user1
            connectionId = data.connection1
        }


        val result = Result(listOf(data.channel1), null)
        val channelMock =  mock<ChannelController> {

        }
        val client = mock<ChatClient> {
            on { events() } doReturn JustObservable(connectedEvent)
            on { queryChannels(any())} doReturn ChatCallTestImpl(result)
            on { channel(any(), any())} doReturn channelMock
        }


        return client
    }

    fun createRoomDb(): ChatDatabase {
        db = Room.inMemoryDatabaseBuilder(
            getApplicationContext(), ChatDatabase::class.java
        ).build()
        return db
    }

    fun setupRepo(client: ChatClient, setUser: Boolean) {


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
        repo.eventHandler = EventHandlerImpl(repo, true)

        repo.errorEvents.observeForever(io.getstream.chat.android.livedata.EventObserver {
            System.out.println("error event$it")
        })

        runBlocking(Dispatchers.IO) {repo.repos.configs.insertConfigs(mutableMapOf("messaging" to data.config1))}
        channelRepo = repo.channel(data.channel1.type, data.channel1.id)
        channelRepo.updateChannel(data.channel1)

        filter = Filters.and(Filters.eq("type", "messaging"), Filters.`in`("members", listOf(data.user1.id)))
        query = QueryChannelsEntity(filter, null)

        queryRepo = repo.queryChannels(filter)
    }


}