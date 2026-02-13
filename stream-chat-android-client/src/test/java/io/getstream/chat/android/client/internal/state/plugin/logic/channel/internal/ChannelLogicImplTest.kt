/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal

import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.channel.ChannelMessagesUpdateLogic
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateImpl
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.PushPreference
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@Suppress("LargeClass")
internal class ChannelLogicImplTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var stateImpl: ChannelStateImpl
    private lateinit var repository: RepositoryFacade
    private lateinit var mutableGlobalState: MutableGlobalState
    private lateinit var messagesUpdateLogic: ChannelMessagesUpdateLogic
    private lateinit var sut: ChannelLogicImpl

    private val cid = "messaging:123"
    private val currentUserId = "currentUser"

    @BeforeEach
    fun setUp() {
        stateImpl = mock()
        repository = mock()
        mutableGlobalState = mock()
        messagesUpdateLogic = mock()

        // Stub channelData to return a default ChannelData
        val defaultChannelData = ChannelData(id = "123", type = "messaging")
        whenever(stateImpl.channelData).thenReturn(MutableStateFlow(defaultChannelData))
        whenever(stateImpl.channelConfig).thenReturn(MutableStateFlow(Config()))
        whenever(stateImpl.messages).thenReturn(MutableStateFlow(emptyList()))
        whenever(stateImpl.insideSearch).thenReturn(MutableStateFlow(false))

        // Stub global state
        whenever(mutableGlobalState.channelMutes).thenReturn(MutableStateFlow(emptyList()))

        sut = ChannelLogicImpl(
            cid = cid,
            messagesUpdateLogic = messagesUpdateLogic,
            repository = repository,
            state = stateImpl,
            mutableGlobalState = mutableGlobalState,
            userPresence = true,
            coroutineScope = testCoroutines.scope,
            getCurrentUserId = { currentUserId },
            now = { System.currentTimeMillis() },
        )
    }

    // region cid

    @Test
    fun `cid should match the constructor parameter`() {
        assertEquals(cid, sut.cid)
    }

    // endregion

    // region messagesUpdateLogic

    @Test
    fun `messagesUpdateLogic should return the injected instance`() {
        assertSame(messagesUpdateLogic, sut.messagesUpdateLogic)
    }

    // endregion

    // region setPaginationDirection

    @Nested
    inner class SetPaginationDirection {

        @Test
        fun `should set loading older messages when filtering older messages`() {
            // Given
            val query = QueryChannelRequest().withMessages(Pagination.LESS_THAN, "msgId", 30)
            // When
            sut.setPaginationDirection(query)
            // Then
            verify(stateImpl).setLoadingOlderMessages(true)
            verify(stateImpl, never()).setLoadingNewerMessages(true)
        }

        @Test
        fun `should set loading newer messages when filtering newer messages`() {
            // Given
            val query = QueryChannelRequest().withMessages(Pagination.GREATER_THAN, "msgId", 30)
            // When
            sut.setPaginationDirection(query)
            // Then
            verify(stateImpl).setLoadingNewerMessages(true)
            verify(stateImpl, never()).setLoadingOlderMessages(true)
        }

        @Test
        fun `should not set any loading state when not filtering messages`() {
            // Given
            val query = QueryChannelRequest().withMessages(30)
            // When
            sut.setPaginationDirection(query)
            // Then
            verify(stateImpl, never()).setLoadingOlderMessages(true)
            verify(stateImpl, never()).setLoadingNewerMessages(true)
        }
    }

    // endregion

    // region onQueryChannelResult - Success

    @Nested
    inner class OnQueryChannelResultSuccess {

        @Test
        fun `should reset recovery state on success with message limit`() {
            // Given
            val messages = listOf(randomMessage(id = "m1"), randomMessage(id = "m2"))
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).setRecoveryNeeded(false)
        }

        @Test
        fun `should not reset recovery state on success with notification update`() {
            // Given
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(30).apply { isNotificationUpdate = true }
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl, never()).setRecoveryNeeded(false)
        }

        @Test
        fun `should not reset recovery state on success with zero message limit`() {
            // Given
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest()
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl, never()).setRecoveryNeeded(false)
        }

        @Test
        fun `should update channel data on success`() {
            // Given
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 5,
                watcherCount = 3,
            )
            val query = QueryChannelRequest().withMessages(30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).updateChannelData(any<(ChannelData?) -> ChannelData?>())
        }

        @Test
        fun `should update member count on success`() {
            // Given
            val memberCount = 42
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = memberCount,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).setMemberCount(memberCount)
        }

        @Test
        fun `should upsert members on success`() {
            // Given
            val members = listOf(randomMember(), randomMember())
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = members,
                watchers = emptyList(),
                read = emptyList(),
                memberCount = members.size,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).upsertMembers(members)
        }

        @Test
        fun `should upsert watchers on success`() {
            // Given
            val watchers = listOf(User(id = "user1"), User(id = "user2"))
            val watcherCount = 2
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = emptyList(),
                watchers = watchers,
                read = emptyList(),
                memberCount = 0,
                watcherCount = watcherCount,
            )
            val query = QueryChannelRequest().withMessages(30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).upsertWatchers(watchers, watcherCount)
        }

        @Test
        fun `should update reads on success`() {
            // Given
            val reads = listOf(randomChannelUserRead(), randomChannelUserRead())
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = emptyList(),
                watchers = emptyList(),
                read = reads,

                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).updateReads(reads)
        }

        @Test
        fun `should update channel config on success`() {
            // Given
            val config = Config(name = "test-config", typingEventsEnabled = false)
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
                config = config,
            )
            val query = QueryChannelRequest().withMessages(30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).setChannelConfig(config)
        }

        @Test
        fun `should add pinned messages on success`() {
            // Given
            val pinnedMessages = listOf(randomMessage(id = "p1", pinned = true))
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            ).copy(pinnedMessages = pinnedMessages)
            val query = QueryChannelRequest().withMessages(30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).addPinnedMessages(pinnedMessages)
        }

        @Test
        fun `should reset loading states on success`() {
            // Given
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).setLoadingOlderMessages(false)
            verify(stateImpl).setLoadingNewerMessages(false)
        }

        @Test
        fun `should not update messages when limit is zero`() {
            // Given
            val messages = listOf(randomMessage(id = "m1"))
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest() // No message limit set (defaults to 0)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl, never()).setMessages(any())
            verify(stateImpl, never()).upsertMessages(any())
            verify(stateImpl, never()).clearCachedLatestMessages()
            verify(stateImpl, never()).setInsideSearch(any())
        }
    }

    // endregion

    // region onQueryChannelResult - Success - Pagination end

    @Nested
    inner class OnQueryChannelResultPaginationEnd {

        @Test
        fun `should set end of older messages when loading latest and end reached`() {
            // Given (limit=30, messages.size=10, so endReached=true)
            val messages = (1..10).map { randomMessage(id = "m$it") }
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).setEndOfOlderMessages(true)
            verify(stateImpl).setEndOfNewerMessages(true)
        }

        @Test
        fun `should not set end of older messages when loading latest and not reached`() {
            // Given (limit=10, messages.size=10, so endReached=false)
            val messages = (1..10).map { randomMessage(id = "m$it") }
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(10)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).setEndOfOlderMessages(false)
            verify(stateImpl).setEndOfNewerMessages(true)
        }

        @Test
        fun `should set both ends to false when filtering around id`() {
            // Given
            val messages = (1..5).map { randomMessage(id = "m$it") }
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(Pagination.AROUND_ID, "m3", 30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).setEndOfOlderMessages(false)
            verify(stateImpl).setEndOfNewerMessages(false)
        }

        @Test
        fun `should set end of older messages when filtering older and end reached`() {
            // Given (limit=30, messages.size=5, so endReached=true)
            val messages = (1..5).map { randomMessage(id = "m$it") }
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(Pagination.LESS_THAN, "m10", 30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).setEndOfOlderMessages(true)
        }

        @Test
        fun `should set end of newer messages when filtering newer and end reached`() {
            // Given (limit=30, messages.size=5, so endReached=true)
            val messages = (1..5).map { randomMessage(id = "m$it") }
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(Pagination.GREATER_THAN, "m1", 30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).setEndOfNewerMessages(true)
        }
    }

    // endregion

    // region onQueryChannelResult - Success - Message updates

    @Nested
    inner class OnQueryChannelResultMessageUpdates {

        @Test
        fun `should replace messages when loading latest messages (no filtering)`() {
            // Given
            val messages = listOf(randomMessage(id = "m1"), randomMessage(id = "m2"))
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).clearCachedLatestMessages()
            verify(stateImpl).setMessages(messages)
            verify(stateImpl).setInsideSearch(false)
        }

        @Test
        fun `should cache latest messages and set inside search when loading around id while not in search`() {
            // Given
            whenever(stateImpl.insideSearch).thenReturn(MutableStateFlow(false))
            val messages = listOf(randomMessage(id = "m1"), randomMessage(id = "m2"))
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(Pagination.AROUND_ID, "m1", 30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).cacheLatestMessages()
            verify(stateImpl).setMessages(messages)
            verify(stateImpl).setInsideSearch(true)
        }

        @Test
        fun `should replace messages without caching when loading around id while already in search`() {
            // Given
            whenever(stateImpl.insideSearch).thenReturn(MutableStateFlow(true))
            val messages = listOf(randomMessage(id = "m1"), randomMessage(id = "m2"))
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(Pagination.AROUND_ID, "m1", 30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl, never()).cacheLatestMessages()
            verify(stateImpl).setMessages(messages)
        }

        @Test
        fun `should upsert messages and trim oldest when loading newer messages`() {
            // Given
            val messages = (1..30).map { randomMessage(id = "m$it") }
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(Pagination.GREATER_THAN, "m0", 30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).upsertMessages(messages)
            verify(stateImpl).trimOldestMessages()
        }

        @Test
        fun `should clear cache and exit search when loading newer messages reaches end`() {
            // Given (limit=30, messages.size=5, so endReached=true)
            val messages = (1..5).map { randomMessage(id = "m$it") }
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(Pagination.GREATER_THAN, "m0", 30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).clearCachedLatestMessages()
            verify(stateImpl).setInsideSearch(false)
        }

        @Test
        fun `should not clear cache when loading newer messages and not reached end`() {
            // Given (limit=5, messages.size=5, so endReached=false)
            val messages = (1..5).map { randomMessage(id = "m$it") }
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(Pagination.GREATER_THAN, "m0", 5)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl, never()).clearCachedLatestMessages()
            verify(stateImpl, never()).setInsideSearch(any())
        }

        @Test
        fun `should upsert messages and trim newest when loading older messages`() {
            // Given
            val messages = (1..10).map { randomMessage(id = "m$it") }
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            val query = QueryChannelRequest().withMessages(Pagination.LESS_THAN, "m20", 30)
            // When
            sut.onQueryChannelResult(query, Result.Success(channel))
            // Then
            verify(stateImpl).upsertMessages(messages)
            verify(stateImpl).trimNewestMessages()
        }
    }

    // endregion

    // region onQueryChannelResult - Failure

    @Nested
    inner class OnQueryChannelResultFailure {

        @Test
        fun `should set recovery needed for non-permanent error`() {
            // Given
            val error = Error.GenericError("Temporary error")
            val query = QueryChannelRequest().withMessages(30)
            // When
            sut.onQueryChannelResult(query, Result.Failure(error))
            // Then
            verify(stateImpl).setRecoveryNeeded(recoveryNeeded = true)
        }

        @Test
        fun `should not set recovery needed for permanent error`() {
            // Given - NetworkError with a non-temporary status code (e.g. 400) is permanent
            val error = Error.NetworkError(
                message = "Bad request",
                serverErrorCode = 4,
                statusCode = 400,
            )
            val query = QueryChannelRequest().withMessages(30)
            // When
            sut.onQueryChannelResult(query, Result.Failure(error))
            // Then
            verify(stateImpl).setRecoveryNeeded(recoveryNeeded = false)
        }

        @Test
        fun `should reset loading states on failure`() {
            // Given
            val error = Error.GenericError("Error")
            val query = QueryChannelRequest().withMessages(30)
            // When
            sut.onQueryChannelResult(query, Result.Failure(error))
            // Then
            verify(stateImpl).setLoadingOlderMessages(false)
            verify(stateImpl).setLoadingNewerMessages(false)
        }
    }

    // endregion

    // region updateStateFromDatabase

    @Nested
    inner class UpdateStateFromDatabase {

        @Test
        fun `should return early when query is notification update`() = runTest {
            // Given
            val query = QueryChannelRequest().withMessages(30).apply { isNotificationUpdate = true }
            // When
            sut.updateStateFromDatabase(query)
            // Then
            verify(repository, never()).selectChannel(any())
        }

        @Test
        fun `should return early when query is filtering messages`() = runTest {
            // Given
            val query = QueryChannelRequest().withMessages(Pagination.LESS_THAN, "msgId", 30)
            // When
            sut.updateStateFromDatabase(query)
            // Then
            verify(repository, never()).selectChannel(any())
        }

        @Test
        fun `should return early when channel not found in database`() = runTest {
            // Given
            val query = QueryChannelRequest().withMessages(30)
            whenever(repository.selectChannel(cid)).thenReturn(null)
            // When
            sut.updateStateFromDatabase(query)
            // Then
            verify(repository).selectChannel(cid)
            verify(stateImpl, never()).setMessages(any())
        }

        @Test
        fun `should update state when channel found in database`() = runTest {
            // Given
            val dbChannel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 3,
                watcherCount = 0,
            )
            val messages = listOf(randomMessage(id = "m1", createdAt = Date(1000)))
            val query = QueryChannelRequest().withMessages(30)
            whenever(repository.selectChannel(cid)).thenReturn(dbChannel)
            whenever(repository.selectMessagesForChannel(any(), any())).thenReturn(messages)
            // When
            sut.updateStateFromDatabase(query)
            // Then
            verify(repository).selectChannel(cid)
            verify(stateImpl).updateChannelData(any<(ChannelData?) -> ChannelData?>())
        }
    }

    // endregion

    // region getMessage

    @Test
    fun `getMessage should delegate to stateImpl`() {
        // Given
        val messageId = "msg1"
        val message = randomMessage(id = messageId)
        whenever(stateImpl.getMessageById(messageId)).thenReturn(message)
        // When
        val result = sut.getMessage(messageId)
        // Then
        assertEquals(message, result)
        verify(stateImpl).getMessageById(messageId)
    }

    @Test
    fun `getMessage should return null when message not found`() {
        // Given
        whenever(stateImpl.getMessageById("unknown")).thenReturn(null)
        // When
        val result = sut.getMessage("unknown")
        // Then
        assertNull(result)
    }

    // endregion

    // region upsertMessage

    @Test
    fun `upsertMessage should delegate to stateImpl`() {
        // Given
        val message = randomMessage(id = "msg1")
        // When
        sut.upsertMessage(message)
        // Then
        verify(stateImpl).upsertMessage(message)
    }

    // endregion

    // region updateLastMessageAt

    @Test
    fun `updateLastMessageAt should delegate to stateImpl`() {
        // Given
        val message = randomMessage(id = "msg1")
        // When
        sut.updateLastMessageAt(message)
        // Then
        verify(stateImpl).updateLastMessageAt(message)
    }

    // endregion

    // region deleteMessage

    @Test
    fun `deleteMessage should delegate to stateImpl with message id`() {
        // Given
        val message = randomMessage(id = "msg1")
        // When
        sut.deleteMessage(message)
        // Then
        verify(stateImpl).deleteMessage("msg1")
    }

    // endregion

    // region upsertMembers

    @Test
    fun `upsertMembers should delegate to stateImpl`() {
        // Given
        val members = listOf(randomMember(), randomMember())
        // When
        sut.upsertMembers(members)
        // Then
        verify(stateImpl).upsertMembers(members)
    }

    // endregion

    // region setHidden

    @Test
    fun `setHidden should delegate to stateImpl`() {
        // When
        sut.setHidden(true)
        // Then
        verify(stateImpl).setHidden(true)
    }

    @Test
    fun `setHidden false should delegate to stateImpl`() {
        // When
        sut.setHidden(false)
        // Then
        verify(stateImpl).setHidden(false)
    }

    // endregion

    // region hideMessagesBefore

    @Test
    fun `hideMessagesBefore should delegate to stateImpl`() {
        // Given
        val date = randomDate()
        // When
        sut.hideMessagesBefore(date)
        // Then
        verify(stateImpl).hideMessagesBefore(date)
    }

    // endregion

    // region removeMessagesBefore

    @Test
    fun `removeMessagesBefore should delegate to stateImpl with null systemMessage`() {
        // Given
        val date = randomDate()
        // When
        sut.removeMessagesBefore(date)
        // Then
        verify(stateImpl).removeMessagesBefore(date, systemMessage = null)
    }

    // endregion

    // region setPushPreference

    @Test
    fun `setPushPreference should delegate to stateImpl`() {
        // Given
        val preference = PushPreference(level = null, disabledUntil = null)
        // When
        sut.setPushPreference(preference)
        // Then
        verify(stateImpl).setPushPreference(preference)
    }

    // endregion

    // region setRepliedMessage

    @Test
    fun `setRepliedMessage should delegate to stateImpl`() {
        // Given
        val message = randomMessage(id = "msg1")
        // When
        sut.setRepliedMessage(message)
        // Then
        verify(stateImpl).setRepliedMessage(message)
    }

    @Test
    fun `setRepliedMessage with null should delegate to stateImpl`() {
        // When
        sut.setRepliedMessage(null)
        // Then
        verify(stateImpl).setRepliedMessage(null)
    }

    // endregion

    // region markRead

    @Test
    fun `markRead should delegate to stateImpl and return result`() {
        // Given
        whenever(stateImpl.markRead()).thenReturn(true)
        // When
        val result = sut.markRead()
        // Then
        assertTrue(result)
        verify(stateImpl).markRead()
    }

    @Test
    fun `markRead should return false when stateImpl returns false`() {
        // Given
        whenever(stateImpl.markRead()).thenReturn(false)
        // When
        val result = sut.markRead()
        // Then
        assertFalse(result)
    }

    // endregion

    // region typingEventsEnabled

    @Test
    fun `typingEventsEnabled should return config value`() {
        // Given
        val config = Config(typingEventsEnabled = true)
        whenever(stateImpl.channelConfig).thenReturn(MutableStateFlow(config))
        // Recreate SUT to pick up updated config mock
        sut = ChannelLogicImpl(
            cid = cid,
            messagesUpdateLogic = messagesUpdateLogic,
            repository = repository,
            state = stateImpl,
            mutableGlobalState = mutableGlobalState,
            userPresence = true,
            coroutineScope = testCoroutines.scope,
            getCurrentUserId = { currentUserId },
            now = { System.currentTimeMillis() },
        )
        // When
        val result = sut.typingEventsEnabled()
        // Then
        assertTrue(result)
    }

    @Test
    fun `typingEventsEnabled should return false when config disables it`() {
        // Given
        val config = Config(typingEventsEnabled = false)
        whenever(stateImpl.channelConfig).thenReturn(MutableStateFlow(config))
        sut = ChannelLogicImpl(
            cid = cid,
            messagesUpdateLogic = messagesUpdateLogic,
            repository = repository,
            state = stateImpl,
            mutableGlobalState = mutableGlobalState,
            userPresence = true,
            coroutineScope = testCoroutines.scope,
            getCurrentUserId = { currentUserId },
            now = { System.currentTimeMillis() },
        )
        // When
        val result = sut.typingEventsEnabled()
        // Then
        assertFalse(result)
    }

    // endregion

    // region getLastStartTypingEvent / setLastStartTypingEvent

    @Test
    fun `getLastStartTypingEvent should delegate to stateImpl`() {
        // Given
        val date = randomDate()
        whenever(stateImpl.getLastStartTypingEvent()).thenReturn(date)
        // When
        val result = sut.getLastStartTypingEvent()
        // Then
        assertEquals(date, result)
    }

    @Test
    fun `setLastStartTypingEvent should delegate to stateImpl`() {
        // Given
        val date = randomDate()
        // When
        sut.setLastStartTypingEvent(date)
        // Then
        verify(stateImpl).setLastStartTypingEvent(date)
    }

    @Test
    fun `setLastStartTypingEvent with null should delegate to stateImpl`() {
        // When
        sut.setLastStartTypingEvent(null)
        // Then
        verify(stateImpl).setLastStartTypingEvent(null)
    }

    // endregion

    // region setKeystrokeParentMessageId

    @Test
    fun `setKeystrokeParentMessageId should delegate to stateImpl`() {
        // Given
        val messageId = randomString()
        // When
        sut.setKeystrokeParentMessageId(messageId)
        // Then
        verify(stateImpl).setKeystrokeParentMessageId(messageId)
    }

    @Test
    fun `setKeystrokeParentMessageId with null should delegate to stateImpl`() {
        // When
        sut.setKeystrokeParentMessageId(null)
        // Then
        verify(stateImpl).setKeystrokeParentMessageId(null)
    }

    // endregion

    // region updateDataForChannel

    @Nested
    inner class UpdateDataForChannel {

        @Test
        fun `should update channel data`() {
            // Given
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 5,
                watcherCount = 0,
            )
            // When
            sut.updateDataForChannel(
                channel = channel,
                messageLimit = 30,
            )
            // Then
            verify(stateImpl).updateChannelData(any<(ChannelData?) -> ChannelData?>())
        }

        @Test
        fun `should update member count`() {
            // Given
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 42,
                watcherCount = 0,
            )
            // When
            sut.updateDataForChannel(channel = channel, messageLimit = 30)
            // Then
            verify(stateImpl).setMemberCount(42)
        }

        @Test
        fun `should upsert members`() {
            // Given
            val members = listOf(randomMember(), randomMember())
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = members,
                watchers = emptyList(),
                read = emptyList(),
                memberCount = members.size,
                watcherCount = 0,
            )
            // When
            sut.updateDataForChannel(channel = channel, messageLimit = 30)
            // Then
            verify(stateImpl).upsertMembers(members)
        }

        @Test
        fun `should upsert watchers`() {
            // Given
            val watchers = listOf(User(id = "u1"), User(id = "u2"))
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = emptyList(),
                watchers = watchers,
                read = emptyList(),
                memberCount = 0,
                watcherCount = 2,
            )
            // When
            sut.updateDataForChannel(channel = channel, messageLimit = 30)
            // Then
            verify(stateImpl).upsertWatchers(watchers, 2)
        }

        @Test
        fun `should update reads`() {
            // Given
            val reads = listOf(randomChannelUserRead())
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = emptyList(),
                watchers = emptyList(),
                read = reads,

                memberCount = 0,
                watcherCount = 0,
            )
            // When
            sut.updateDataForChannel(channel = channel, messageLimit = 30)
            // Then
            verify(stateImpl).updateReads(reads)
        }

        @Test
        fun `should update channel config`() {
            // Given
            val config = Config(name = "test")
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
                config = config,
            )
            // When
            sut.updateDataForChannel(channel = channel, messageLimit = 30)
            // Then
            verify(stateImpl).setChannelConfig(config)
        }

        @Test
        fun `should set messages when messageLimit is positive`() {
            // Given
            val messages = listOf(randomMessage(id = "m1"), randomMessage(id = "m2"))
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            // When
            sut.updateDataForChannel(channel = channel, messageLimit = 30)
            // Then
            verify(stateImpl).setMessages(messages)
        }

        @Test
        fun `should set end of older messages based on message count vs limit`() {
            // Given (messageLimit=30, messages.size=2, so endOfOlder=true)
            val messages = listOf(randomMessage(id = "m1"), randomMessage(id = "m2"))
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            // When
            sut.updateDataForChannel(channel = channel, messageLimit = 30)
            // Then
            verify(stateImpl).setEndOfOlderMessages(true)
        }

        @Test
        fun `should set end of older messages to false when messages fill the limit`() {
            // Given (messageLimit=2, messages.size=2, so endOfOlder=false)
            val messages = listOf(randomMessage(id = "m1"), randomMessage(id = "m2"))
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            // When
            sut.updateDataForChannel(channel = channel, messageLimit = 2)
            // Then
            verify(stateImpl).setEndOfOlderMessages(false)
        }

        @Test
        fun `should not set messages when messageLimit is zero`() {
            // Given
            val messages = listOf(randomMessage(id = "m1"))
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = messages,
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            // When
            sut.updateDataForChannel(channel = channel, messageLimit = 0)
            // Then
            verify(stateImpl, never()).setMessages(any())
            verify(stateImpl, never()).setEndOfOlderMessages(any())
        }

        @Test
        fun `should add pinned messages`() {
            // Given
            val pinnedMessages = listOf(randomMessage(id = "p1", pinned = true))
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            ).copy(pinnedMessages = pinnedMessages)
            // When
            sut.updateDataForChannel(channel = channel, messageLimit = 0)
            // Then
            verify(stateImpl).addPinnedMessages(pinnedMessages)
        }

        @Test
        fun `should reset loading states`() {
            // Given
            val channel = randomChannel(
                id = "123",
                type = "messaging",
                messages = emptyList(),
                members = emptyList(),
                watchers = emptyList(),
                read = emptyList(),
                memberCount = 0,
                watcherCount = 0,
            )
            // When
            sut.updateDataForChannel(channel = channel, messageLimit = 0)
            // Then
            verify(stateImpl).setLoadingOlderMessages(false)
            verify(stateImpl).setLoadingNewerMessages(false)
        }
    }

    // endregion
}
