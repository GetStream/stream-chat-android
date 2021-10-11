package io.getstream.chat.android.offline.integration

import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.annotation.CallSuper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.models.ConnectionData
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.createRoomDB
import io.getstream.chat.android.offline.experimental.plugin.Config
import io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin
import io.getstream.chat.android.offline.model.ChannelConfig
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import io.getstream.chat.android.offline.querychannels.QueryChannelsSpec
import io.getstream.chat.android.offline.repository.database.ChatDatabase
import io.getstream.chat.android.offline.utils.NoRetryPolicy
import io.getstream.chat.android.offline.utils.TestDataHelper
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.util.Date

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

    private val offlineEnabled = true
    private val userPresence = true
    private val recoveryEnabled = false
    private val backgroundSyncEnabled = false
    private val offlinePlugin = OfflinePlugin(
        Config(
            backgroundSyncEnabled = backgroundSyncEnabled,
            userPresence = userPresence,
            persistenceEnabled = offlineEnabled
        )
    )

    fun assertSuccess(result: Result<*>) {
        if (result.isError) {
            result.isError.shouldBeFalse()
        }
    }

    fun assertFailure(result: Result<*>) {
        if (!result.isError) {
            result.isError.shouldBeTrue()
        }
    }

    var data = TestDataHelper()
    protected val currentUser = data.user1

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    protected fun setupWorkManager() {
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
        setupChatDomain(client)
    }

    @After
    open fun tearDown() = runBlocking {
        chatDomainImpl.disconnect()
        db.close()
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

        return mock<ChatClient> {
            on { subscribe(any()) } doReturn object : Disposable {
                override val isDisposed: Boolean = true
                override fun dispose() = Unit
            }
            on { getSyncHistory(any(), any()) } doReturn TestCall(eventResults)
            on { queryChannels(any()) } doReturn TestCall(result)
            on { channel(any(), any()) } doReturn channelClientMock
            on { channel(any()) } doReturn channelClientMock
            on { sendReaction(any<Reaction>(), any()) } doReturn TestCall(
                Result(data.reaction1)
            )
            on { connectUser(any(), any<String>()) } doAnswer {
                TestCall(Result(ConnectionData(it.arguments[0] as User, randomString())))
            }
            on { plugins } doReturn listOf(offlinePlugin)
        }
    }

    fun setupChatDomain(client: ChatClient, user: User? = null) = runBlocking {
        db = createRoomDB(testCoroutines.dispatcher)
        val context = getApplicationContext() as Context
        val handler: Handler = mock()

        chatDomainImpl = ChatDomainImpl(
            client,
            db,
            handler,
            offlineEnabled,
            userPresence,
            recoveryEnabled,
            backgroundSyncEnabled,
            context,
            offlinePlugin = offlinePlugin,
        )
        chatDomainImpl.scope = testCoroutines.scope
        chatDomainImpl.retryPolicy = NoRetryPolicy()
        chatDomain = chatDomainImpl
        ChatDomain.instance = chatDomainImpl

        chatDomainImpl.scope.launch {
            chatDomainImpl.errorEvents.collect {
                println("error event$it")
            }
        }

        if (user != null) {
            chatDomainImpl.setUser(user)
        }

        chatDomainImpl.repos.insertChannelConfig(ChannelConfig("messaging", data.config1))
        chatDomainImpl.repos.insertUsers(data.userMap.values.toList())
        channelControllerImpl = chatDomainImpl.channel(data.channel1.type, data.channel1.id)
        channelControllerImpl.updateDataFromChannel(data.channel1)

        query = QueryChannelsSpec(data.filter1)

        queryControllerImpl = chatDomainImpl.queryChannels(data.filter1, QuerySort())
    }
}
