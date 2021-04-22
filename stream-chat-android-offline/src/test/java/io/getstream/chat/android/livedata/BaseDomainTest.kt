package io.getstream.chat.android.livedata

import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.annotation.CallSuper
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
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.models.ConnectionData
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.livedata.controller.QueryChannelsSpec
import io.getstream.chat.android.livedata.utils.NoRetryPolicy
import io.getstream.chat.android.livedata.utils.TestDataHelper
import io.getstream.chat.android.livedata.utils.TestLoggerHandler
import io.getstream.chat.android.livedata.utils.waitForSetUser
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.model.ChannelConfig
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import io.getstream.chat.android.offline.repository.database.ChatDatabase
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.util.Date
import java.util.concurrent.Executors

internal open class BaseDomainTest {
    lateinit var channelClientMock: ChannelClient
    lateinit var database: ChatDatabase
    lateinit var chatDomainImpl: ChatDomainImpl
    lateinit var chatDomain: ChatDomain
    lateinit var client: ChatClient
    lateinit var channelControllerImpl: ChannelController
    lateinit var db: ChatDatabase
    lateinit var queryControllerImpl: QueryChannelsController
    lateinit var query: QueryChannelsSpec
    lateinit var filter: FilterObject

    fun assertSuccess(result: Result<*>) {
        if (result.isError) {
            Truth.assertWithMessage(result.error().toString()).that(result.isError).isFalse()
        }
    }

    fun assertFailure(result: Result<*>) {
        if (!result.isError) {
            Truth.assertWithMessage(result.data().toString()).that(result.isError).isTrue()
        }
    }

    var data = TestDataHelper()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutines = TestCoroutineRule()

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
    @CallSuper
    open fun setup() {
        client = createDisconnectedMockClient()
        setupChatDomain(client, false)
    }

    @After
    open fun tearDown() = runBlocking {
        chatDomainImpl.disconnect()
        db.close()
    }

    fun createClient(): ChatClient {
        val logLevel = System.getenv("STREAM_LOG_LEVEL") ?: "ALL"
        return ChatClient.Builder(data.apiKey, getApplicationContext())
            .logLevel(logLevel)
            .loggerHandler(TestLoggerHandler())
            .build()
    }

    fun createDisconnectedMockClient(): ChatClient {

        val connectedEvent = DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date()).apply {
        }

        val result = Result(listOf(data.channel1))
        channelClientMock = mock {
            on { sendMessage(any()) } doReturn TestCall(
                Result(data.message1)
            )
        }
        val events = listOf<ChatEvent>()
        val eventResults = Result(events)

        val client: ChatClient = mock {
            on { subscribe(any()) } doAnswer { invocation ->
                val listener = invocation.arguments[0] as ChatEventListener<ChatEvent>
                listener.onEvent(connectedEvent)
                object : Disposable {
                    override val isDisposed: Boolean = true
                    override fun dispose() {}
                }
            }
            on { getSyncHistory(any(), any()) } doReturn TestCall(eventResults)
            on { queryChannels(any()) } doReturn TestCall(result)
            on { channel(any(), any()) } doReturn channelClientMock
            on { channel(any()) } doReturn channelClientMock
            on { getSyncHistory(any(), anyOrNull()) } doReturn TestCall(data.syncHistoryResult)
            on {
                createChannel(
                    any(),
                    any<String>(),
                    any<Map<String, Any>>()
                )
            } doReturn TestCall(Result(data.channel1))
            on { sendReaction(any(), any<Boolean>()) } doReturn TestCall(
                Result(data.reaction1)
            )
        }
        return client
    }

    fun createConnectedMockClient(): ChatClient {

        val connectedEvent = ConnectedEvent(EventType.HEALTH_CHECK, Date(), data.user1, data.connection1)

        val result = Result(listOf(data.channel1))
        channelClientMock = mock {
            on { query(any()) } doReturn TestCall(
                Result(data.channel1)
            )
            on { watch(any<WatchChannelRequest>()) } doReturn TestCall(
                Result(data.channel1)
            )
        }
        val events = listOf<ChatEvent>()
        val eventResults = Result(events)
        val client = mock<ChatClient> {
            on { subscribe(any()) } doAnswer { invocation ->
                val listener = invocation.arguments[0] as ChatEventListener<ChatEvent>
                listener.onEvent(connectedEvent)
                object : Disposable {
                    override val isDisposed: Boolean = true
                    override fun dispose() {}
                }
            }
            on { getSyncHistory(any(), any()) } doReturn TestCall(eventResults)
            on { queryChannels(any()) } doReturn TestCall(result)
            on { channel(any(), any()) } doReturn channelClientMock
            on { channel(any()) } doReturn channelClientMock
            on { sendReaction(any(), any<Boolean>()) } doReturn TestCall(
                Result(data.reaction1)
            )
        }
        whenever(client.connectUser(any(), any<String>())) doAnswer {
            TestCall(Result(ConnectionData(it.arguments[0] as User, randomString())))
        }
        return client
    }

    fun createRoomDb(): ChatDatabase {
        return Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            ChatDatabase::class.java
        )
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()
    }

    fun setupChatDomain(client: ChatClient, setUser: Boolean) = runBlocking {

        if (setUser) {
            waitForSetUser(client, data.user1, data.user1Token)
        }

        db = createRoomDb()
        val context = getApplicationContext() as Context
        val handler: Handler = mock()
        val offlineEnabled = true
        val userPresence = true
        val recoveryEnabled = false
        val backgroundSyncEnabled = false
        chatDomainImpl = ChatDomainImpl(
            client,
            data.user1,
            db,
            handler,
            offlineEnabled,
            userPresence,
            recoveryEnabled,
            backgroundSyncEnabled,
            context
        )
        chatDomainImpl.retryPolicy = NoRetryPolicy()
        chatDomain = chatDomainImpl

        chatDomainImpl.scope.launch {
            chatDomainImpl.errorEvents.collect {
                println("error event$it")
            }
        }

        chatDomainImpl.repos.insertChannelConfig(ChannelConfig("messaging", data.config1))
        chatDomainImpl.repos.insertUsers(data.userMap.values.toList())
        channelControllerImpl = chatDomainImpl.channel(data.channel1.type, data.channel1.id)
        channelControllerImpl.updateDataFromChannel(data.channel1)

        query = QueryChannelsSpec(data.filter1, QuerySort())

        queryControllerImpl = chatDomainImpl.queryChannels(data.filter1, QuerySort())
    }
}
