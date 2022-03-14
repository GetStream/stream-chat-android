package io.getstream.chat.android.offline.integration

import android.content.Context
import androidx.annotation.CallSuper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.testing.WorkManagerTestInitHelper
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.ConnectionData
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.SynchronizedCoroutineTest
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.model.ChannelConfig
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import io.getstream.chat.android.offline.querychannels.QueryChannelsSpec
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.repository.creation.factory.RepositoryFactory
import io.getstream.chat.android.offline.repository.database.ChatDatabase
import io.getstream.chat.android.offline.utils.TestDataHelper
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date
import java.util.concurrent.Executors

/**
 * Sets up a ChatDomain object with a mocked ChatClient.
 */
internal open class BaseDomainTest2 : SynchronizedCoroutineTest {

    /** a realistic set of chat data, please only add to this, don't update */
    var data = TestDataHelper()

    /** the chat domain impl */
    lateinit var chatDomainImpl: ChatDomainImpl

    /** the chat domain interface */
    lateinit var chatDomain: ChatDomain

    /** the mock for the chat client */
    lateinit var clientMock: ChatClient

    /** a channel controller for data.channel1 */
    lateinit var channelControllerImpl: ChannelController

    /** a queryControllerImpl for the query */
    lateinit var queryControllerImpl: QueryChannelsController

    /** the query used for the default queryController */
    lateinit var query: QueryChannelsSpec

    /** a mock for the channel client */
    lateinit var channelClientMock: ChannelClient

    private lateinit var db: ChatDatabase

    /** single threaded arch components operations */
    @get:Rule
    val testCoroutines = TestCoroutineRule()

    /** single threaded coroutines via DispatcherProvider */
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    override fun getTestScope(): TestCoroutineScope = testCoroutines.scope

    @Before
    @CallSuper
    open fun setup() {
        clientMock = createClientMock()
        db = createRoomDb()
        createChatDomain(clientMock, db)
    }

    @After
    open fun tearDown() = runBlocking {
        chatDomainImpl.disconnect()
        db.close()
    }

    /**
     * checks if a response is succesful and raises a clear error message if it's not
     */
    fun assertSuccess(result: Result<*>) {
        if (result.isError) {
            result.isError.shouldBeFalse()
        }
    }

    /**
     * checks if a response failed and raises a clear error message if it succeeded
     */
    fun assertFailure(result: Result<*>) {
        if (!result.isError) {
            result.isError.shouldBeTrue()
        }
    }

    private fun createClientMock(isConnected: Boolean = true): ChatClient {
        val connectedEvent = if (isConnected) {
            ConnectedEvent(EventType.HEALTH_CHECK, Date(), data.user1, data.connection1)
        } else {
            DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date())
        }

        val queryChannelsResult = Result.success(listOf(data.channel1))
        val queryChannelResult = Result.success(data.channel1)
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
            on { queryChannels(any()) } doReturn TestCall(queryChannelsResult)
            on { queryChannelsInternal(any()) } doReturn TestCall(queryChannelsResult)
            on { queryChannelInternal(any(), any(), any()) } doReturn TestCall(queryChannelResult)
            on { channel(any(), any()) } doReturn channelClientMock
            on { channel(any()) } doReturn channelClientMock
            on { sendReaction(any(), any(), any()) } doReturn TestCall(
                Result(data.reaction1)
            )
        }
        whenever(client.connectUser(any(), any<String>())) doAnswer {
            TestCall(Result(ConnectionData(it.arguments[0] as User, randomString())))
        }

        return client
    }

    internal fun createRoomDb(): ChatDatabase {
        return Room
            .inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().targetContext,
                ChatDatabase::class.java
            )
            .allowMainThreadQueries()
            // Use a separate thread for Room transactions to avoid deadlocks
            // This means that tests that run Room transactions can't use testCoroutines.scope.runBlockingTest,
            // and have to simply use runBlocking instead
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .setQueryExecutor(testCoroutines.dispatcher.asExecutor())
            .build()
    }

    private fun createChatDomain(client: ChatClient, db: ChatDatabase): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext() as Context
        chatDomainImpl = ChatDomain.Builder(context, client)
            .userPresenceEnabled()
            .buildImpl()
        ChatDomain.instance = chatDomainImpl

        chatDomainImpl.repos = RepositoryFacade.create(
            RepositoryFactory(db, data.user1),
            chatDomainImpl.scope,
            Config(connectEventsEnabled = true, muteEnabled = true)
        )

        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        // TODO: a chat domain without a user set should raise a clear error
        client.connectUser(
            data.user1,
            data.user1Token
        ).enqueue()
        // manually configure the user since client is mocked
        GlobalMutableState.get()._user.value = data.user1
        chatDomainImpl.userConnected(data.user1)

        chatDomain = chatDomainImpl

        chatDomainImpl.scope.launch {
            chatDomainImpl.errorEvents.collect { println("error event$it") }
        }

        chatDomainImpl.repos.insertChannelConfig(ChannelConfig("messaging", data.config1))
        chatDomainImpl.repos.insertUsers(data.userMap.values.toList())

        channelControllerImpl = chatDomainImpl.channel(data.channel1.type, data.channel1.id)

        channelControllerImpl.updateDataFromChannel(data.channel1)

        query = QueryChannelsSpec(data.filter1, QuerySort())

        queryControllerImpl = chatDomainImpl.queryChannels(data.filter1, QuerySort())
    }
}
