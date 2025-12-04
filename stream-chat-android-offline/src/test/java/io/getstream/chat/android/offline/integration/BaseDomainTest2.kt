/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.client.test.SynchronizedCoroutineTest
import io.getstream.chat.android.client.test.utils.TestDataHelper
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.ConnectionData
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.offline.repository.factory.internal.DatabaseRepositoryFactory
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date
import java.util.concurrent.Executors

/**
 * Sets up a ChatDomain object with a mocked ChatClient.
 */
@ExperimentalCoroutinesApi
internal open class BaseDomainTest2 : SynchronizedCoroutineTest {
    private val streamDateFormatter = StreamDateFormatter()

    /** a realistic set of chat data, please only add to this, don't update */
    var data = TestDataHelper()

    /** the mock for the chat client */
    lateinit var clientMock: ChatClient

    /** the query used for the default queryController */
    lateinit var query: QueryChannelsSpec

    /** a mock for the channel client */
    lateinit var channelClientMock: ChannelClient

    private lateinit var db: ChatDatabase

    protected lateinit var repos: RepositoryFacade

    /** single threaded arch components operations */
    @get:Rule
    val testCoroutines = TestCoroutineRule()

    /** single threaded coroutines via DispatcherProvider */
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    override fun getTestScope(): TestScope = testCoroutines.scope

    @Before
    @CallSuper
    open fun setup() {
        clientMock = createClientMock()
        db = createRoomDb()
        createChatDomain(clientMock, db)
    }

    @After
    open fun tearDown() = runTest {
        db.close()
    }

    private fun createClientMock(isConnected: Boolean = true): ChatClient {
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)

        val connectedEvent = if (isConnected) {
            ConnectedEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, data.user1, data.connection1)
        } else {
            DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date(), null)
        }

        val queryChannelsResult = Result.Success(listOf(data.channel1))
        val queryChannelResult = Result.Success(data.channel1)
        channelClientMock = mock {
            on { query(any()) } doReturn TestCall(
                Result.Success(data.channel1),
            )
            on { watch(any<WatchChannelRequest>()) } doReturn TestCall(
                Result.Success(data.channel1),
            )
        }
        val events = listOf<ChatEvent>()
        val eventResults = Result.Success(events)
        val client = mock<ChatClient> {
            on { subscribe(any()) } doAnswer { invocation ->
                val listener = invocation.arguments[0] as ChatEventListener<ChatEvent>
                listener.onEvent(connectedEvent)
                object : Disposable {
                    override val isDisposed: Boolean = true
                    override fun dispose() {}
                }
            }
            on { getSyncHistory(any(), any<Date>()) } doReturn TestCall(eventResults)
            on { queryChannels(any()) } doReturn TestCall(queryChannelsResult)
            on { queryChannelsInternal(any()) } doReturn TestCall(queryChannelsResult)
            on { queryChannel(any(), any(), any(), any()) } doReturn TestCall(queryChannelResult)
            on { channel(any(), any()) } doReturn channelClientMock
            on { channel(any()) } doReturn channelClientMock
            on { sendReaction(any(), any(), any(), any()) } doReturn TestCall(
                Result.Success(data.reaction1),
            )
        }
        whenever(client.connectUser(any(), any<String>(), anyOrNull())) doAnswer {
            TestCall(Result.Success(ConnectionData(it.arguments[0] as User, randomString())))
        }

        return client
    }

    internal fun createRoomDb(): ChatDatabase {
        return Room
            .inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().targetContext,
                ChatDatabase::class.java,
            )
            .allowMainThreadQueries()
            // Use a separate thread for Room transactions to avoid deadlocks
            // This means that tests that run Room transactions can't use testCoroutines.scope.runBlockingTest,
            // and have to simply use runBlocking instead
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .setQueryExecutor(Dispatchers.IO.asExecutor())
            .build()
    }

    private fun createChatDomain(client: ChatClient, db: ChatDatabase): Unit = runTest {
        val context = ApplicationProvider.getApplicationContext() as Context

        repos = RepositoryFacade.create(
            DatabaseRepositoryFactory({ db }, data.user1, this, emptySet()),
            getTestScope(),
            Config(connectEventsEnabled = true, muteEnabled = true),
        )

        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        // TODO: a chat domain without a user set should raise a clear error
        client.connectUser(
            data.user1,
            data.user1Token,
        ).enqueue()

        repos.insertChannelConfig(ChannelConfig("messaging", data.config1))
        repos.insertUsers(data.userMap.values.toList())

        query = QueryChannelsSpec(data.filter1, QuerySortByField())
    }
}
