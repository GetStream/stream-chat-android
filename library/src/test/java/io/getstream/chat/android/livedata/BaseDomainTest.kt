package io.getstream.chat.android.livedata

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.google.common.truth.Truth
import io.getstream.chat.android.client.utils.Result

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.ChatApiImpl
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.controllers.ChannelController
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.notifications.ChatNotifications
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig
import io.getstream.chat.android.client.parser.ChatParserImpl
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.UuidGeneratorImpl
import io.getstream.chat.android.livedata.controller.QueryChannelsController
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.mockito.Mockito

open class BaseDomainTest {
    lateinit var database: ChatDatabase
    lateinit var chatDomain: ChatDomain
    lateinit var client: ChatClient
    lateinit var channelController: io.getstream.chat.android.livedata.controller.ChannelController
    lateinit var db: ChatDatabase
    lateinit var queryController: QueryChannelsController
    lateinit var query: QueryChannelsEntity
    lateinit var filter: FilterObject


    fun assertSuccess(result: Result<Any>) {
        if (result.isError) {
            Truth.assertWithMessage(result.error().toString()).that(result.isSuccess).isTrue()
        }
    }

    var data = TestDataHelper()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    open fun setup() {
        client = createDisconnectedMockClient()
        setupChatDomain(client, false)
    }

    @After
    open fun tearDown() {
        chatDomain.disconnect()
        db.close()
    }

    fun createClient(): ChatClient {
        val client = ChatClient.Builder(data.apiKey, getApplicationContext())
            .logLevel(
                ChatLogLevel.ALL
            ).loggerHandler(TestLoggerHandler()).build()
        return client
    }

    fun createDisconnectedMockClient(): ChatClient {

        val connectedEvent = DisconnectedEvent().apply {
        }


        val result = Result(listOf(data.channel1), null)
        val channelMock =  mock<ChannelController> {
            on { sendMessage(any())} doReturn ChatCallTestImpl<Message>(Result(data.message1, null))
        }
        val client = mock<ChatClient> {
            on { events() } doReturn JustObservable(connectedEvent)
            on { queryChannels(any())} doReturn ChatCallTestImpl(result)
            on { channel(any(), any())} doReturn channelMock
            on { channel(any())} doReturn channelMock
            on { createChannel(any<String>(), any<String>(), any<Map<String, Any>>())} doReturn ChatCallTestImpl<Channel>(Result(data.channel1, null))
            on { sendReaction(any())} doReturn ChatCallTestImpl<Reaction>(Result(data.reaction1, null))
        }


        return client
    }

    fun createConnectedMockClient(): ChatClient {
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
            on { sendReaction(any())} doReturn ChatCallTestImpl<Reaction>(Result(data.reaction1, null))

        }


        return client
    }

    fun createRoomDb(): ChatDatabase {
        db = Room.inMemoryDatabaseBuilder(
            getApplicationContext(), ChatDatabase::class.java
        ).build()
        return db
    }

    fun setupChatDomain(client: ChatClient, setUser: Boolean) {


        if (setUser) {
            waitForSetUser(
                client,
                data.user1,
                data.user1Token
            )
        }

        db = createRoomDb()
        val context = getApplicationContext() as Context
        chatDomain = ChatDomain.Builder(context, client, data.user1).database(db).offlineEnabled().userPresenceEnabled().build()
        chatDomain.eventHandler = EventHandlerImpl(chatDomain, true)
        chatDomain.retryPolicy = object: RetryPolicy {
            override fun shouldRetry(client: ChatClient, attempt: Int, error: ChatError): Boolean {
                return false
            }

            override fun retryTimeout(client: ChatClient, attempt: Int, error: ChatError): Int? {
                return 1000
            }

        }

        chatDomain.errorEvents.observeForever(io.getstream.chat.android.livedata.EventObserver {
            System.out.println("error event$it")
        })

        runBlocking(Dispatchers.IO) {chatDomain.repos.configs.insertConfigs(mutableMapOf("messaging" to data.config1))}
        channelController = chatDomain.channel(data.channel1.type, data.channel1.id)
        channelController.updateChannel(data.channel1)


        query = QueryChannelsEntity(data.filter1, null)

        queryController = chatDomain.queryChannels(data.filter1)
    }


}