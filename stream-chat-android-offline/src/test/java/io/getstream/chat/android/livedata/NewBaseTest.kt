package io.getstream.chat.android.livedata

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.livedata.controller.QueryChannelsControllerImpl
import io.getstream.chat.android.livedata.controller.QueryChannelsSpec
import io.getstream.chat.android.livedata.utils.EventObserver
import io.getstream.chat.android.livedata.utils.RetryPolicy
import io.getstream.chat.android.livedata.utils.TestCall
import io.getstream.chat.android.livedata.utils.TestDataHelper
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.mock
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import java.util.concurrent.Executors

/**
 * BaseDomainTest2 creates an easy to use test environment
 *
 * - Sets up a chat Domain object with a mocked Chat Client.
 * - We pass a TestCoroutineScope to the chatDomain to correctly handle new coroutines starting
 * - The val rule = InstantTaskExecutorRule() ensures that architecture components immediately
 * - Use Executors.newSingleThreadExecutor to prevent room transactions from causing deadlocks
 * execute instead of running on a different thread
 *
 * Handling Room and Coroutines is unfortunately quite tricky.
 * - Using transactions can cause the tests to deadlock
 * * https://medium.com/@eyalg/testing-androidx-room-kotlin-coroutines-2d1faa3e674f
 * * https://medium.com/androiddevelopers/threading-models-in-coroutines-and-android-sqlite-api-6cab11f7eb90
 * * https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
 * * https://codelabs.developers.google.com/codelabs/kotlin-coroutines#9
 * * https://craigrussell.io/2019/11/unit-testing-coroutine-suspend-functions-using-testcoroutinedispatcher/
 *
 */
@RunWith(AndroidJUnit4::class)
internal open class BaseDomainTest2 {
    private val testIODispatcher = TestCoroutineDispatcher()
    private val testIOExecutor = testIODispatcher.asExecutor()
    internal val testIOScope = TestCoroutineScope(testIODispatcher)

    private val testMainDispatcher = TestCoroutineDispatcher()
    private val testMainExecutor = testMainDispatcher.asExecutor()
    internal val testMainScope = TestCoroutineScope(testMainDispatcher)

    val singleThreadExecutor = Executors.newSingleThreadExecutor()

    lateinit var channelClientMock: ChannelClient
    lateinit var chatDomainImpl: ChatDomainImpl
    lateinit var chatDomain: ChatDomain
    lateinit var client: ChatClient
    lateinit var channelControllerImpl: io.getstream.chat.android.livedata.controller.ChannelControllerImpl
    lateinit var db: ChatDatabase
    lateinit var queryControllerImpl: QueryChannelsControllerImpl
    lateinit var query: QueryChannelsSpec

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
    val rule = InstantTaskExecutorRule()

    @Before
    open fun setup() {
        client = createClientMock()
        db = createRoomDb()
        createChatDomain(client, db)
    }

    @After
    open fun tearDown() = runBlocking(Dispatchers.IO) {
        testIOScope.cleanupTestCoroutines()
        testMainScope.cleanupTestCoroutines()
        chatDomainImpl.disconnect()
        db.close()
    }

    @Test
    internal fun `test that room testing setup is configured correctly`() = testIODispatcher.runBlockingTest {
        testIOScope.launch {
            chatDomainImpl.repos.channels.select(listOf(data.channel1.cid))
            queryControllerImpl.query(10)
        }
    }

    @Test
    internal fun `channel controller edit message event`() = testIOScope.runBlockingTest {
        testIOScope.launch {
            // setup the queryControllerImpl
            queryControllerImpl.query(10)

            // update the last message
            chatDomainImpl.eventHandler.handleEvent(data.messageUpdatedEvent)
            // channelControllerImpl.handleEvent(data.messageUpdatedEvent)
            // queryControllerImpl.handleEvent(data.messageUpdatedEvent)

            // verify that the last message is now updated
            val channelMap = queryControllerImpl.channels.getOrAwaitValue().associateBy { it.cid }
            val channel1 = channelMap[data.channel1.cid]
            Truth.assertThat(channel1!!.messages.last().text).isEqualTo(data.messageUpdatedEvent.message.text)
        }
    }

    internal fun createClientMock(isConnected: Boolean = true): ChatClient {

        val connectedEvent = if (isConnected) {
            ConnectedEvent(EventType.HEALTH_CHECK, Date(), data.user1, data.connection1)
        } else {
            DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date())
        }

        val result = Result(listOf(data.channel1), null)
        channelClientMock = mock {
            on { query(any()) } doReturn TestCall(
                Result(
                    data.channel1,
                    null
                )
            )
            on { watch(any<WatchChannelRequest>()) } doReturn TestCall(
                Result(
                    data.channel1,
                    null
                )
            )
        }
        val events = listOf<ChatEvent>()
        val eventResults = Result(events)
        val client = mock<ChatClient> {
            on { subscribe(any()) } doAnswer { invocation ->
                val listener = invocation.arguments[0] as (ChatEvent) -> Unit
                listener.invoke(connectedEvent)
                object : Disposable {
                    override val isDisposed: Boolean = true
                    override fun dispose() { }
                }
            }
            on { getSyncHistory(any(), any()) } doReturn TestCall(eventResults)
            on { queryChannels(any()) } doReturn TestCall(result)
            on { channel(any(), any()) } doReturn channelClientMock
            on { channel(any()) } doReturn channelClientMock
            on { replayEvents(any(), anyOrNull(), any(), any()) } doReturn TestCall(data.replayEventsResult)
            on { sendReaction(any()) } doReturn TestCall(
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

    internal fun createRoomDb(): ChatDatabase {
        return Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            ChatDatabase::class.java
        )
            .allowMainThreadQueries()
            .setTransactionExecutor(singleThreadExecutor)
            // .setTransactionExecutor(testIOExecutor)
            .setQueryExecutor(singleThreadExecutor)
            .build()
    }

    internal fun createChatDomain(client: ChatClient, db: ChatDatabase) {

        val context = ApplicationProvider.getApplicationContext() as Context
        chatDomainImpl = ChatDomain.Builder(context, client).database(db).offlineEnabled()
            .userPresenceEnabled().withIOScope(testIOScope).withMainScope(testMainScope).buildImpl()

        // TODO: a chat domain without a user set should raise a clear error
        client.setUser(
            data.user1,
            data.user1Token
        )
        // manually configure the user since client is mocked
        chatDomainImpl.setUser(data.user1)

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
                println("error event$it")
            }
        )

        testIOScope.launch {
            chatDomainImpl.repos.configs.insertConfigs(mutableMapOf("messaging" to data.config1))
        }

        channelControllerImpl = chatDomainImpl.channel(data.channel1.type, data.channel1.id)
        channelControllerImpl.updateLiveDataFromChannel(data.channel1)

        query = QueryChannelsSpec(data.filter1, QuerySort())

        queryControllerImpl = chatDomainImpl.queryChannels(data.filter1, QuerySort())

        // testIOScope.launch { queryControllerImpl.query(10) }
    }
}
