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

package io.getstream.chat.android.state.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.sync.SyncState
import io.getstream.chat.android.core.internal.coroutines.Tube
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.sync.internal.SyncManager
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SyncManagerTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var chatClient: ChatClient
    private lateinit var logicRegistry: LogicRegistry
    private lateinit var stateRegistry: StateRegistry
    private lateinit var clientState: ClientState
    private lateinit var repositoryFacade: RepositoryFacade
    private lateinit var user: User

    private val _syncEvents: Tube<List<ChatEvent>> = mock()

    private val connectionState = MutableStateFlow(ConnectionState.Offline)
    private val streamDateFormatter = StreamDateFormatter()

    @BeforeEach
    fun setUp() {
        reset(_syncEvents)

        val channelLogic: ChannelLogic = mock {
            on(it.cid) doReturn randomCID()
        }

        user = randomUser()
        chatClient = mock()
        logicRegistry = mock {
            on(it.getActiveChannelsLogic()) doReturn listOf(channelLogic)
        }
        stateRegistry = mock()
        clientState = mock {
            on(it.connectionState) doReturn connectionState
        }
        repositoryFacade = mock {
            runBlocking {
                on(it.selectChannelCidsBySyncNeeded()) doReturn emptyList()
                on(it.selectMessageIdsBySyncState(any())) doReturn emptyList()
                on(it.selectReactionIdsBySyncStatus(any())) doReturn emptyList()
            }
        }
    }

    @Test
    fun `when a health check event happens, a request to retry failed entities should happen`() = runTest {
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)

        val syncManager = buildSyncManager()
        whenever(repositoryFacade.selectMessages(any(), any())) doReturn listOf(randomMessage())
        whenever(repositoryFacade.selectChannels(any(), any())) doReturn listOf(randomChannel())

        val connectingEvent = HealthEvent(
            type = "type",
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt,
            connectionId = randomString(),
        )

        syncManager.onEvent(connectingEvent)

        verify(repositoryFacade).selectChannelCidsBySyncNeeded()
        verify(repositoryFacade).selectMessageIdsBySyncState(SyncStatus.SYNC_NEEDED)
        verify(repositoryFacade).selectMessageIdsBySyncState(SyncStatus.AWAITING_ATTACHMENTS)
        verify(repositoryFacade).selectReactionIdsBySyncStatus(SyncStatus.SYNC_NEEDED)
    }

    @Test
    fun `when one event of exact same raw time of last sync arrive, it should not be propagated`() = runTest {
        /*
         *  This checks if the SDK is avoiding loops in the sync. We don't want to handle the same event on every sync,
         *  because this can waste resource and/or some events may not be idempotent.
         */
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        val testSyncState = SyncState(
            userId = randomString(),
            activeChannelIds = emptyList(),
            lastSyncedAt = createdAt,
            rawLastSyncedAt = rawCreatedAt,
            markedAllReadAt = createdAt,
        )

        val syncManager = buildSyncManager()

        val mockedChatEvent: ChatEvent = mock {
            on(it.createdAt) doReturn createdAt
            on(it.rawCreatedAt) doReturn rawCreatedAt
        }

        whenever(repositoryFacade.selectMessages(any(), any())) doReturn listOf(randomMessage())
        whenever(repositoryFacade.selectChannels(any(), any())) doReturn listOf(randomChannel())
        whenever(repositoryFacade.selectSyncState(any())) doReturn testSyncState

        whenever(chatClient.getSyncHistory(any(), any<String>())) doReturn TestCall(
            Result.Success(listOf(mockedChatEvent)),
        )
        val connectingEvent = ConnectedEvent(
            type = "type",
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt,
            connectionId = randomString(),
            me = randomUser(),
        )

        syncManager.onEvent(connectingEvent)

        verify(_syncEvents, never()).emit(any())
    }

    @Test
    fun `test initial syncing when rawLastSyncedAt is null`() = runTest {
        /* Given */
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)

        val mockedChatEvent: ChatEvent = mock {
            on(it.createdAt) doReturn createdAt
            on(it.rawCreatedAt) doReturn rawCreatedAt
        }

        whenever(chatClient.getSyncHistory(any(), any<String>())) doReturn TestCall(
            Result.Success(listOf(mockedChatEvent)),
        )
        whenever(chatClient.getSyncHistory(any(), any<Date>())) doReturn TestCall(
            Result.Success(listOf(mockedChatEvent)),
        )

        val syncManager = buildSyncManager()

        /* When */
        syncManager.performSync(cids = listOf("1", "2"))

        /* Then */
        verify(_syncEvents).emit(listOf(mockedChatEvent))
    }

    private fun buildSyncManager(): SyncManager {
        return SyncManager(
            currentUserId = user.id,
            scope = testCoroutines.scope,
            logicRegistry = logicRegistry,
            stateRegistry = stateRegistry,
            repos = repositoryFacade,
            chatClient = chatClient,
            clientState = clientState,
            userPresence = true,
            events = _syncEvents,
        )
    }
}
