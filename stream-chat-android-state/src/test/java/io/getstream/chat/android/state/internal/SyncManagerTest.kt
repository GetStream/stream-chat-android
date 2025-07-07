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
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.sync.SyncState
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.core.internal.coroutines.Tube
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.TimeDuration
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.state.sync.internal.SyncManager
import io.getstream.chat.android.test.TestCall
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should not be`
import org.amshove.kluent.shouldBeGreaterThan
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.Date

@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SyncManagerTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var chatClient: ChatClient
    private lateinit var logicRegistry: LogicRegistry
    private lateinit var stateRegistry: StateRegistry
    private lateinit var clientState: ClientState
    private lateinit var mutableGlobalState: MutableGlobalState
    private lateinit var repositoryFacade: RepositoryFacade
    private lateinit var user: User

    private val _syncEvents: Tube<List<ChatEvent>> = mock()
    private val _syncState: MutableStateFlow<SyncState?> = MutableStateFlow(null)

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
        mutableGlobalState = mock()
        repositoryFacade = mock {
            runBlocking {
                on(it.selectChannelCidsBySyncNeeded()) doReturn emptyList()
                on(it.selectMessageIdsBySyncState(any())) doReturn emptyList()
                on(it.selectReactionIdsBySyncStatus(any())) doReturn emptyList()
            }
        }
    }

    @Test
    fun `when a health check event happens, a request to retry failed entities should happen`() =
        runTest(testDispatcher) {
            /* Given */
            val createdAt = Date()
            val rawCreatedAt = streamDateFormatter.format(createdAt)

            val syncManager = buildSyncManager()

            /* When */

            syncManager.onEvent(
                HealthEvent(
                    type = "type",
                    createdAt = createdAt,
                    rawCreatedAt = rawCreatedAt,
                    connectionId = randomString(),
                ),
            )

            /* Then */
            verify(repositoryFacade).selectChannelCidsBySyncNeeded()
            verify(repositoryFacade).selectMessageIdsBySyncState(SyncStatus.SYNC_NEEDED)
            verify(repositoryFacade).selectMessageIdsBySyncState(SyncStatus.AWAITING_ATTACHMENTS)
            verify(repositoryFacade).selectReactionIdsBySyncStatus(SyncStatus.SYNC_NEEDED)
        }

    @Test
    fun `when one event of exact same raw time of last sync arrive, it should not be propagated`() =
        runTest(testDispatcher) {
            /*
             *  This checks if the SDK is avoiding loops in the sync. We don't want to handle the same event on every sync,
             *  because this can waste resource and/or some events may not be idempotent.
             */
            val createdAt = localDate()
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

            whenever(repositoryFacade.selectMessages(any())) doReturn listOf(randomMessage())
            whenever(repositoryFacade.selectChannels(any())) doReturn listOf(randomChannel())
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
    fun `test initial syncing when rawLastSyncedAt is null`() = runTest(testDispatcher) {
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

    @Test
    fun `test sync max threshold for messages`() = runTest(testDispatcher) {
        /* Given */
        val message1 = localRandomMessage()
        val message2 = randomMessage(deletedAt = localDate())
        val message3 = randomMessage(deletedAt = null)
        val message4 = localRandomMessage()
        whenever(repositoryFacade.selectMessageIdsBySyncState(SyncStatus.SYNC_NEEDED)) doReturn listOf(
            message1.id, message2.id, message3.id,
        )
        whenever(repositoryFacade.selectMessageIdsBySyncState(SyncStatus.AWAITING_ATTACHMENTS)) doReturn
            listOf(message4.id)

        whenever(repositoryFacade.selectMessage(message1.id)) doReturn message1
        whenever(repositoryFacade.selectMessage(message2.id)) doReturn message2
        whenever(repositoryFacade.selectMessage(message3.id)) doReturn message3
        whenever(repositoryFacade.selectMessage(message4.id)) doReturn message4

        val syncManager = buildSyncManager()

        /* When */
        delay(7_000)
        syncManager.onEvent(
            HealthEvent(
                type = "type",
                createdAt = Date(currentTime),
                rawCreatedAt = streamDateFormatter.format(Date(currentTime)),
                connectionId = randomString(),
            ),
        )

        /* Then */
        verify(repositoryFacade).deleteChannelMessage(message1)
        verify(repositoryFacade).deleteChannelMessage(message2)
        verify(repositoryFacade).deleteChannelMessage(message3)
        verify(repositoryFacade).deleteChannelMessage(message4)

        verify(chatClient, never()).sendMessage(any(), any(), any(), any())
        verify(chatClient, never()).deleteMessage(any(), any())
        verify(chatClient, never()).updateMessage(any())
    }

    @Test
    fun `test sync max threshold for reactions`() = runTest(testDispatcher) {
        /* Given */
        val reactionId1 = 1
        val reactionId2 = 2
        val reaction1 = randomReaction(deletedAt = null)
        val reaction2 = randomReaction()
        whenever(repositoryFacade.selectReactionIdsBySyncStatus(SyncStatus.SYNC_NEEDED)) doReturn listOf(
            reactionId1, reactionId2,
        )
        whenever(repositoryFacade.selectReactionById(reactionId1)) doReturn reaction1
        whenever(repositoryFacade.selectReactionById(reactionId2)) doReturn reaction2

        val syncManager = buildSyncManager()

        /* When */
        delay(7_000)
        syncManager.onEvent(
            HealthEvent(
                type = "type",
                createdAt = Date(currentTime),
                rawCreatedAt = streamDateFormatter.format(Date(currentTime)),
                connectionId = randomString(),
            ),
        )

        /* Then */
        verify(repositoryFacade).deleteReaction(reaction1)
        verify(repositoryFacade).deleteReaction(reaction1)

        verify(chatClient, never()).sendReaction(any(), any(), any())
        verify(chatClient, never()).deleteReaction(any(), any(), any())
    }

    @Test
    fun `test too many events to sync error`() = runTest(testDispatcher) {
        /* Given */
        val createdAt = localDate()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        val testSyncState = SyncState(
            userId = randomString(),
            activeChannelIds = emptyList(),
            lastSyncedAt = createdAt,
            rawLastSyncedAt = rawCreatedAt,
            markedAllReadAt = createdAt,
        )

        val connectingEvent = ConnectedEvent(
            type = "type",
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt,
            connectionId = randomString(),
            me = randomUser(),
        )

        val error = Error.NetworkError(
            serverErrorCode = ChatErrorCode.VALIDATION_ERROR.code,
            message = "Too many events to sync, please use a more recent last_sync_at parameter",
            statusCode = 400,
        )
        val result = Result.Failure(error)

        whenever(repositoryFacade.selectMessages(any())) doReturn listOf(randomMessage())
        whenever(repositoryFacade.selectChannels(any())) doReturn listOf(randomChannel())
        whenever(repositoryFacade.selectSyncState(any())) doReturn testSyncState
        whenever(chatClient.getSyncHistory(any(), any<String>())) doReturn TestCall(result)
        whenever(chatClient.getSyncHistory(any(), any<Date>())) doReturn TestCall(result)

        val syncManager = buildSyncManager()

        /* When */
        delay(1000)
        syncManager.onEvent(connectingEvent)
        delay(1000)
        syncManager.performSync(cids = listOf("1", "2"))
        delay(1000)

        /* Then */
        verifyNoInteractions(_syncEvents)
        _syncState.value `should not be` testSyncState
        _syncState.value.shouldNotBeNull()
        _syncState.value!!.lastSyncedAt.shouldNotBeNull()
        _syncState.value!!.lastSyncedAt!! shouldBeGreaterThan testSyncState.lastSyncedAt!!
    }

    @Test
    fun `start should subscribe to chatClient events if not already subscribed`() = runTest {
        val sut = buildSyncManager()
        whenever(chatClient.subscribe(any())).thenReturn(mock())

        sut.start()

        verify(chatClient).subscribe(any())
    }

    @Test
    fun `start should not subscribe again if already subscribed`() = runTest {
        val sut = buildSyncManager()
        val disposable = mock<Disposable> { on { isDisposed } doReturn true }
        whenever(chatClient.subscribe(any())) doReturn disposable
        sut.start()

        whenever(disposable.isDisposed) doReturn false

        sut.start()

        verify(chatClient, times(1)).subscribe(any())
    }

    @Test
    fun `start should schedule live location expiration`() = runTest(testDispatcher) {
        val sut = buildSyncManager()
        whenever(repositoryFacade.selectDraftMessages()).thenReturn(emptyList())

        sut.start()

        verify(mutableGlobalState).removeExpiredLiveLocations()
    }

    @Test
    fun `start should sync offline draft messages`() = runTest(testDispatcher) {
        val sut = buildSyncManager()
        val draftMessages = listOf(randomDraftMessage(), randomDraftMessage())
        whenever(repositoryFacade.selectDraftMessages()).thenReturn(draftMessages)

        sut.start()

        draftMessages.forEach(verify(mutableGlobalState)::updateDraftMessage)
    }

    private fun TestScope.localRandomMessage() = randomMessage(
        createdLocallyAt = Date(currentTime),
        createdAt = null,
        updatedAt = null,
        deletedAt = null,
    )

    private fun TestScope.localDate() = Date(currentTime)

    private fun TestScope.buildSyncManager(syncMaxThreshold: TimeDuration = TimeDuration.seconds(5)): SyncManager {
        return SyncManager(
            currentUserId = user.id,
            scope = backgroundScope,
            logicRegistry = logicRegistry,
            stateRegistry = stateRegistry,
            repos = repositoryFacade,
            chatClient = chatClient,
            clientState = clientState,
            mutableGlobalState = mutableGlobalState,
            userPresence = true,
            events = _syncEvents,
            syncState = _syncState,
            syncMaxThreshold = syncMaxThreshold,
            now = { currentTime },
        )
    }
}
