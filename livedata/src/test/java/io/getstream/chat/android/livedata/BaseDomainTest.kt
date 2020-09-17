package io.getstream.chat.android.livedata

import android.content.Context
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.controllers.ChannelController
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.controller.QueryChannelsControllerImpl
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.util.Date

open class BaseDomainTest {
    lateinit var database: ChatDatabase
    lateinit var chatDomainImpl: ChatDomainImpl
    lateinit var chatDomain: ChatDomain
    lateinit var client: ChatClient
    lateinit var channelControllerImpl: io.getstream.chat.android.livedata.controller.ChannelControllerImpl
    lateinit var db: ChatDatabase
    lateinit var queryControllerImpl: QueryChannelsControllerImpl
    lateinit var query: QueryChannelsEntity
    lateinit var filter: FilterObject

    fun assertSuccess(result: Result<*>) {
        if (result.isError) {
            Truth.assertWithMessage(result.error().toString()).that(result.isSuccess).isTrue()
        }
    }

    var data = TestDataHelper()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    fun setupWorkManager() {
        val config = Configuration.Builder()
            // Set log level to Log.DEBUG to make it easier to debug
            .setMinimumLoggingLevel(Log.DEBUG)
            // Use a SynchronousExecutor here to make it easier to write tests
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(getApplicationContext(), config)
    }

    @Before
    open fun setup() {
        client = createDisconnectedMockClient()
        setupChatDomain(client, false)
    }

    @After
    open fun tearDown() = runBlocking(Dispatchers.IO) {
        chatDomainImpl.disconnect()
        db.close()
    }

    fun createClient(): ChatClient {
        val logLevel = System.getenv("STREAM_LOG_LEVEL") ?: "ALL"
        val client = ChatClient.Builder(data.apiKey, getApplicationContext())
            .logLevel(
                logLevel
            ).loggerHandler(TestLoggerHandler()).build()
        return client
    }

    fun createDisconnectedMockClient(): ChatClient {

        val connectedEvent = DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date()).apply {
        }

        val result = Result(listOf(data.channel1), null)
        val channelMock = mock<ChannelController> {
            on { sendMessage(any()) } doReturn ChatCallTestImpl<Message>(
                Result(
                    data.message1,
                    null
                )
            )
        }
        val client = mock<ChatClient> {
            on { subscribe(any()) } doAnswer { invocation ->
                val listener = invocation.arguments[0] as (ChatEvent) -> Unit
                listener.invoke(connectedEvent)
                null
            }
            on { queryChannels(any()) } doReturn ChatCallTestImpl(result)
            on { channel(any(), any()) } doReturn channelMock
            on { channel(any()) } doReturn channelMock
            on { replayEvents(any(), anyOrNull(), any(), any()) } doReturn ChatCallTestImpl(data.replayEventsResult)
            on { getSyncHistory(any(), anyOrNull()) } doReturn ChatCallTestImpl(data.replayEventsResult)
            on {
                createChannel(
                    any<String>(),
                    any<String>(),
                    any<Map<String, Any>>()
                )
            } doReturn ChatCallTestImpl<Channel>(Result(data.channel1, null))
            on { sendReaction(any()) } doReturn ChatCallTestImpl<Reaction>(
                Result(
                    data.reaction1,
                    null
                )
            )
        }

        return client
    }

    fun createConnectedMockClient(): ChatClient {

        val connectedEvent = ConnectedEvent(EventType.HEALTH_CHECK, Date(), data.user1, data.connection1)

        val result = Result(listOf(data.channel1), null)
        val channelMock = mock<ChannelController> {
            on { query(any()) } doReturn ChatCallTestImpl<Channel>(
                Result(
                    data.channel1,
                    null
                )
            )
            on { watch(any<WatchChannelRequest>()) } doReturn ChatCallTestImpl<Channel>(
                Result(
                    data.channel1,
                    null
                )
            )
        }
        val client = mock<ChatClient> {
            on { subscribe(any()) } doAnswer { invocation ->
                val listener = invocation.arguments[0] as (ChatEvent) -> Unit
                listener.invoke(connectedEvent)
                null
            }
            on { queryChannels(any()) } doReturn ChatCallTestImpl(result)
            on { channel(any(), any()) } doReturn channelMock
            on { replayEvents(any(), anyOrNull(), any(), any()) } doReturn ChatCallTestImpl(data.replayEventsResult)
            on { sendReaction(any()) } doReturn ChatCallTestImpl<Reaction>(
                Result(
                    data.reaction1,
                    null
                )
            )
        }

        When calling client.setUser(any(), any<String>(), any()) doAnswer {
            (it.arguments[2] as InitConnectionListener).onSuccess(
                InitConnectionListener.ConnectionData(it.arguments[0] as User, randomString())
            )
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
            runBlocking {
                waitForSetUser(
                    client,
                    data.user1,
                    data.user1Token
                )
            }
        }

        db = createRoomDb()
        val context = getApplicationContext() as Context
        chatDomainImpl = ChatDomain.Builder(context, client, data.user1).database(db).offlineEnabled()
            .userPresenceEnabled().buildImpl()

        chatDomainImpl.eventHandler = EventHandlerImpl(chatDomainImpl, true)
        chatDomainImpl.retryPolicy = object :
            RetryPolicy {
            override fun shouldRetry(client: ChatClient, attempt: Int, error: ChatError): Boolean {
                return false
            }

            override fun retryTimeout(client: ChatClient, attempt: Int, error: ChatError): Int {
                return 1000
            }
        }
        chatDomain = chatDomainImpl

        chatDomainImpl.errorEvents.observeForever(
            EventObserver {
                System.out.println("error event$it")
            }
        )

        runBlocking(Dispatchers.IO) { chatDomainImpl.repos.configs.insertConfigs(mutableMapOf("messaging" to data.config1)) }
        channelControllerImpl = chatDomainImpl.channel(data.channel1.type, data.channel1.id)
        channelControllerImpl.updateLiveDataFromChannel(data.channel1)

        query = QueryChannelsEntity(data.filter1, QuerySort())

        queryControllerImpl = chatDomainImpl.queryChannels(data.filter1, QuerySort())
    }
}
