/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.plugin.logic.channel.internal

import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.test.randomNewMessageEvent
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.internal.assertNotSame
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@Suppress("LargeClass")
internal class ChannelLogicImplTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private const val CURRENT_USER_ID = "currentUserId"
    }

    private lateinit var repos: RepositoryFacade
    private lateinit var channelStateLogic: ChannelStateLogic
    private lateinit var channelMutableState: ChannelMutableState
    private lateinit var channelLogic: ChannelLogicImpl

    private val currentUser = User(id = CURRENT_USER_ID)
    private val userFlow = MutableStateFlow(currentUser)
    private val channelType = "messaging"
    private val channelId = randomString()
    private val cid = "$channelType:$channelId"

    @BeforeEach
    fun setUp() {
        repos = mock()

        channelMutableState = ChannelMutableState(
            channelType = channelType,
            channelId = channelId,
            userFlow = userFlow,
            latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
            baseMessageLimit = null,
            activeLiveLocations = MutableStateFlow(emptyList()),
        ) { System.currentTimeMillis() }

        channelStateLogic = mock {
            on(it.writeChannelState()) doReturn channelMutableState
        }

        channelLogic = ChannelLogicImpl(
            repos = repos,
            userPresence = false,
            channelStateLogic = channelStateLogic,
            coroutineScope = testCoroutines.scope,
            getCurrentUserId = { CURRENT_USER_ID },
        )
    }

    @Test
    fun `cid property should return correct channel identifier`() {
        assertEquals(cid, channelLogic.cid)
    }

    @Test
    fun `state property should return channel mutable state`() {
        assertEquals(channelMutableState, channelLogic.state)
    }

    @Test
    fun `stateLogic property should return channel state logic`() {
        assertEquals(channelStateLogic, channelLogic.stateLogic)
    }

    @Test
    fun `getMessage should return message from state when it exists`() {
        // Given
        val message = randomMessage(id = "messageId")
        channelMutableState.upsertMessage(message)

        // When
        val result = channelLogic.getMessage("messageId")

        // Then
        assertEquals(message, result)
    }

    @Test
    fun `getMessage should return null when message does not exist`() {
        // When
        val result = channelLogic.getMessage("nonExistentMessageId")

        // Then
        assertNull(result)
    }

    @Test
    fun `upsertMessage should delegate to state logic`() {
        // Given
        val message = randomMessage()

        // When
        channelLogic.upsertMessage(message)

        // Then
        verify(channelStateLogic).upsertMessage(message)
    }

    @Test
    fun `deleteMessage should delegate to state logic`() {
        // Given
        val message = randomMessage()

        // When
        channelLogic.deleteMessage(message)

        // Then
        verify(channelStateLogic).deleteMessage(message)
    }

    @Test
    fun `updateDataForChannel should delegate to state logic with correct parameters`() {
        // Given
        val channel = randomChannel(type = channelType, id = channelId)
        val messageLimit = 50
        val shouldRefreshMessages = true
        val scrollUpdate = false
        val isNotificationUpdate = false
        val isChannelsStateUpdate = true

        // When
        channelLogic.updateDataForChannel(
            channel = channel,
            messageLimit = messageLimit,
            shouldRefreshMessages = shouldRefreshMessages,
            scrollUpdate = scrollUpdate,
            isNotificationUpdate = isNotificationUpdate,
            isChannelsStateUpdate = isChannelsStateUpdate,
        )

        // Then
        verify(channelStateLogic).updateDataForChannel(
            channel = channel,
            messageLimit = messageLimit,
            shouldRefreshMessages = shouldRefreshMessages,
            scrollUpdate = scrollUpdate,
            isNotificationUpdate = isNotificationUpdate,
            isChannelsStateUpdate = isChannelsStateUpdate,
        )
    }

    @Test
    fun `handleEvent should delegate to event handler`() {
        // Given
        val user = randomUser()
        val message = randomMessage()
        val event = randomNewMessageEvent(user = user, message = message)

        // When
        channelLogic.handleEvent(event)

        // Then
        // The event handler should update the state logic
        verify(channelStateLogic, times(1)).updateCurrentUserRead(any(), any())
    }

    @Test
    fun `handleEvents should process all events`() {
        // Given
        val events = listOf(
            randomNewMessageEvent(user = randomUser(), message = randomMessage()),
            randomNewMessageEvent(user = randomUser(), message = randomMessage()),
            randomNewMessageEvent(user = randomUser(), message = randomMessage()),
        )

        // When
        channelLogic.handleEvents(events)

        // Then
        verify(channelStateLogic, times(3)).updateCurrentUserRead(any(), any())
    }

    @Test
    fun `setPaginationDirection should set loading older messages when filtering older messages`() {
        // Given
        val query = QueryChannelRequest()
            .withMessages(Pagination.LESS_THAN, "messageId", 30)

        // When
        channelLogic.setPaginationDirection(query)

        // Then
        verify(channelStateLogic).loadingOlderMessages()
    }

    @Test
    fun `setPaginationDirection should set loading newer messages when filtering newer messages`() {
        // Given
        val query = QueryChannelRequest()
            .withMessages(Pagination.GREATER_THAN, "messageId", 30)

        // When
        channelLogic.setPaginationDirection(query)

        // Then
        verify(channelStateLogic).loadingNewerMessages()
    }

    @Test
    fun `setPaginationDirection with LESS_THAN_OR_EQUAL should load older messages`() {
        // Given
        val query = QueryChannelRequest()
            .withMessages(Pagination.LESS_THAN_OR_EQUAL, "messageId", 30)

        // When
        channelLogic.setPaginationDirection(query)

        // Then
        verify(channelStateLogic).loadingOlderMessages()
    }

    @Test
    fun `setPaginationDirection with GREATER_THAN_OR_EQUAL should load newer messages`() {
        // Given
        val query = QueryChannelRequest()
            .withMessages(Pagination.GREATER_THAN_OR_EQUAL, "messageId", 30)

        // When
        channelLogic.setPaginationDirection(query)

        // Then
        verify(channelStateLogic).loadingNewerMessages()
    }

    @Test
    fun `setPaginationDirection should set loading newest messages when not filtering`() {
        // Given
        val query = QueryChannelRequest()
            .withMessages(30)

        // When
        channelLogic.setPaginationDirection(query)

        // Then
        verify(channelStateLogic).loadingNewestMessages()
    }

    @Test
    fun `onQueryChannelResult should propagate success result to state logic`() {
        // Given
        val channel = randomChannel(type = channelType, id = channelId)
        val query = QueryChannelRequest()
        val result = Result.Success(channel)

        // When
        channelLogic.onQueryChannelResult(query, result)

        // Then
        verify(channelStateLogic).propagateChannelQuery(channel, query)
    }

    @Test
    fun `onQueryChannelResult should propagate error result to state logic`() {
        // Given
        val error = Error.GenericError("Test error")
        val query = QueryChannelRequest()
        val result = Result.Failure(error)

        // When
        channelLogic.onQueryChannelResult(query, result)

        // Then
        verify(channelStateLogic).propagateQueryError(error)
    }

    @Test
    fun `updateStateFromDatabase should sync mute state and run query offline for latest messages`() = runTest {
        // Given
        val channel = randomChannel(
            type = channelType,
            id = channelId,
            messages = listOf(randomMessage()),
        )
        val query = QueryChannelRequest()
            .withMessages(30)

        whenever(repos.selectChannel(eq(cid))) doReturn channel
        whenever(repos.selectMessagesForChannel(eq(cid), any())) doReturn channel.messages

        // When
        channelLogic.updateStateFromDatabase(query)

        // Then
        verify(channelStateLogic).syncMuteState()
        verify(repos).selectChannel(cid)
        verify(repos).selectMessagesForChannel(eq(cid), any())
    }

    @Test
    fun `updateStateFromDatabase should not run offline query for notification updates`() = runTest {
        // Given
        val query = QueryChannelRequest()
            .withMessages(30)
            .apply {
                isNotificationUpdate = true
            }

        // When
        channelLogic.updateStateFromDatabase(query)

        // Then
        verify(repos, never()).selectChannel(any())
    }

    @Test
    fun `updateStateFromDatabase should not run offline query when filtering messages`() = runTest {
        // Given
        val query = QueryChannelRequest()
            .withMessages(Pagination.LESS_THAN, "messageId", 30)

        // When
        channelLogic.updateStateFromDatabase(query)

        // Then
        verify(repos, never()).selectChannel(any())
    }

    @Test
    fun `updateStateFromDatabase should handle null channel from database gracefully`() = runTest {
        // Given
        val query = QueryChannelRequest()
            .withMessages(30)

        whenever(repos.selectChannel(eq(cid))) doReturn null

        // When
        channelLogic.updateStateFromDatabase(query)

        // Then
        verify(channelStateLogic).syncMuteState()
        verify(repos).selectChannel(cid)
        // updateDataForChannel should not be called since channel is null
        verify(channelStateLogic, never()).updateDataForChannel(
            channel = any(),
            messageLimit = any(),
            shouldRefreshMessages = any(),
            scrollUpdate = any(),
            isNotificationUpdate = any(),
            isChannelsStateUpdate = any(),
            isWatchChannel = any(),
        )
    }

    @Test
    fun `watch should return failure when another watch request is in progress`() = runTest {
        // Given
        channelMutableState.setLoading(true)

        // When
        val result = channelLogic.watch(limit = 30, userPresence = false)

        // Then
        assertInstanceOf(Result.Failure::class.java, result)
        val error = (result as Result.Failure).value
        assertInstanceOf(Error.GenericError::class.java, error)
        verify(channelStateLogic, never()).propagateChannelQuery(any(), any())
    }

    @Test
    fun `getMessage should return a copy of the message`() {
        // Given
        val message = randomMessage(id = "messageId", text = "original text")
        channelMutableState.upsertMessage(message)

        // When
        val result = channelLogic.getMessage("messageId")

        // Then
        assertNotNull(result)
        // Verify it's a copy, not the same instance
        assertEquals(message, result)
        assertNotSame(message, result)
    }

    @Test
    fun `updateDataForChannel should update channel data with hidden state from database`() = runTest {
        // Given
        val hidden = true
        val hiddenMessagesBefore = Date(1000L)
        val channel = randomChannel(type = channelType, id = channelId).copy(
            hidden = hidden,
            hiddenMessagesBefore = hiddenMessagesBefore,
        )

        whenever(repos.selectChannel(eq(cid))) doReturn channel
        whenever(repos.selectMessagesForChannel(eq(cid), any())) doReturn emptyList()

        val query = QueryChannelRequest()
            .withMessages(30)

        // When
        channelLogic.updateStateFromDatabase(query)

        // Then
        verify(channelStateLogic).setHidden(hidden)
        verify(channelStateLogic).hideMessagesBefore(hiddenMessagesBefore)
    }

    @Test
    fun `updateDataForChannel should not set hidden state when channel hidden is null`() = runTest {
        // Given
        val channel = randomChannel(type = channelType, id = channelId).copy(hidden = null)

        whenever(repos.selectChannel(eq(cid))) doReturn channel
        whenever(repos.selectMessagesForChannel(eq(cid), any())) doReturn emptyList()

        val query = QueryChannelRequest()
            .withMessages(30)

        // When
        channelLogic.updateStateFromDatabase(query)

        // Then
        verify(channelStateLogic, never()).setHidden(any())
    }

    @Test
    fun `updateStateFromDatabase with pagination request should not fetch channel`() = runTest {
        // Given
        val channel = randomChannel(type = channelType, id = channelId)
        val query = QueryChannelRequest()
            .withMessages(Pagination.LESS_THAN, "someMessageId", 25)

        whenever(repos.selectChannel(eq(cid))) doReturn channel

        // When
        channelLogic.updateStateFromDatabase(query)

        // Then
        // Should not fetch from database when filtering messages (pagination in progress)
        verify(repos, never()).selectMessagesForChannel(any(), any())
    }

    @Test
    fun `updateDataForChannel with all parameters should delegate correctly`() {
        // Given
        val channel = randomChannel(type = channelType, id = channelId)
        val messageLimit = 100
        val shouldRefreshMessages = true
        val scrollUpdate = true
        val isNotificationUpdate = true
        val isChannelsStateUpdate = false

        // When
        channelLogic.updateDataForChannel(
            channel = channel,
            messageLimit = messageLimit,
            shouldRefreshMessages = shouldRefreshMessages,
            scrollUpdate = scrollUpdate,
            isNotificationUpdate = isNotificationUpdate,
            isChannelsStateUpdate = isChannelsStateUpdate,
        )

        // Then
        verify(channelStateLogic).updateDataForChannel(
            channel = channel,
            messageLimit = messageLimit,
            shouldRefreshMessages = shouldRefreshMessages,
            scrollUpdate = scrollUpdate,
            isNotificationUpdate = isNotificationUpdate,
            isChannelsStateUpdate = isChannelsStateUpdate,
        )
    }
}
