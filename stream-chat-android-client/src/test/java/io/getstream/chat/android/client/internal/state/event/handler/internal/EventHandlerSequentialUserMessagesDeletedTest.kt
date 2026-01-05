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

package io.getstream.chat.android.client.internal.state.event.handler.internal

import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.UserMessagesDeletedEvent
import io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomUserMessagesDeletedEvent
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

/**
 * Stand-alone test class covering the [EventHandlerSequential] logic for handling [UserMessagesDeletedEvent].
 */
internal class EventHandlerSequentialUserMessagesDeletedTest {

    private val currentUser = randomUser()
    private val targetUser = randomUser()
    private val testCid = "messaging:123"
    private val deletedAt = randomDate()

    @Test
    fun `When UserMessagesDeletedEvent has cid, should delegate to specific channel logic`() = runTest {
        // Given
        val event = randomUserMessagesDeletedEvent(
            cid = testCid,
            user = targetUser,
            hardDelete = false,
            createdAt = deletedAt,
        )
        val channelLogic: ChannelLogic = mock()
        val logicRegistry: LogicRegistry = mock {
            on { isActiveChannel(any(), any()) } doReturn true
            on { channel(any(), any()) } doReturn channelLogic
        }

        val handler = createEventHandler(scope = this, logicRegistry = logicRegistry)

        // When
        handler.handleEvents(event)

        // Then
        verify(logicRegistry).isActiveChannel("messaging", "123") // Assuming testCid = "messaging:123"
        verify(logicRegistry).channel("messaging", "123")
        verify(channelLogic).handleEvent(event)
    }

    @Test
    fun `When UserMessagesDeletedEvent has cid but channel is not active, should not delegate to channel logic`() =
        runTest {
            // Given
            val event = randomUserMessagesDeletedEvent(
                cid = testCid,
                user = targetUser,
                hardDelete = false,
                createdAt = deletedAt,
            )
            val channelLogic: ChannelLogic = mock()
            val logicRegistry: LogicRegistry = mock {
                on { isActiveChannel(any(), any()) } doReturn false
            }

            val handler = createEventHandler(scope = this, logicRegistry = logicRegistry)

            // When
            handler.handleEvents(event)

            // Then
            verify(logicRegistry).isActiveChannel("messaging", "123")
            verify(logicRegistry, never()).channel(any(), any())
            verify(channelLogic, never()).handleEvent(any())
        }

    @Test
    fun `When UserMessagesDeletedEvent has no cid, should delegate to all active channels`() = runTest {
        // Given
        val event = randomUserMessagesDeletedEvent(
            cid = null,
            user = targetUser,
            hardDelete = true,
            createdAt = deletedAt,
        )
        val channelLogic1: ChannelLogic = mock()
        val channelLogic2: ChannelLogic = mock()
        val channelLogic3: ChannelLogic = mock()
        val activeChannels = listOf(channelLogic1, channelLogic2, channelLogic3)

        val logicRegistry: LogicRegistry = mock {
            on { getActiveChannelsLogic() } doReturn activeChannels
        }

        val handler = createEventHandler(scope = this, logicRegistry = logicRegistry)

        // When
        handler.handleEvents(event)

        // Then
        verify(logicRegistry).getActiveChannelsLogic()
        verify(channelLogic1).handleEvent(event)
        verify(channelLogic2).handleEvent(event)
        verify(channelLogic3).handleEvent(event)
    }

    @Test
    fun `When UserMessagesDeletedEvent is processed for repository updates with cid, should delete messages from specific channel`() =
        runTest {
            // Given
            val event = randomUserMessagesDeletedEvent(
                cid = testCid,
                user = targetUser,
                hardDelete = false,
                createdAt = deletedAt,
            )
            val channelMessages = listOf(
                randomMessage(user = targetUser),
                randomMessage(user = targetUser),
                randomMessage(user = targetUser),
            )
            val repos: RepositoryFacade = mock {
                onBlocking { selectAllChannelUserMessages(testCid, targetUser.id) } doReturn channelMessages
                onBlocking { selectChannels(any()) } doReturn emptyList()
                onBlocking { selectMessages(any()) } doReturn emptyList()
                onBlocking { selectThreads(any()) } doReturn emptyList()
            }

            val handler = createEventHandler(scope = this, repos = repos)

            // When
            handler.handleEvents(event)

            // Then
            verify(repos).selectAllChannelUserMessages(testCid, targetUser.id)
            verify(repos, never()).selectAllUserMessages(any())

            // Verify soft delete - messages should be marked as deleted, not removed
            val expectedDeletedMessages = channelMessages.map { it.copy(deletedAt = deletedAt) }
            verify(repos).insertMessages(expectedDeletedMessages)
            verify(repos, never()).deleteChannelMessage(any())
        }

    @Test
    fun `When UserMessagesDeletedEvent is processed for repository updates with cid and hard delete, should remove messages from specific channel`() =
        runTest {
            // Given
            val event = randomUserMessagesDeletedEvent(
                cid = testCid,
                user = targetUser,
                hardDelete = true,
                createdAt = deletedAt,
            )
            val channelMessages = listOf(
                randomMessage(user = targetUser),
                randomMessage(user = targetUser),
            )
            val repos: RepositoryFacade = mock {
                onBlocking { selectAllChannelUserMessages(testCid, targetUser.id) } doReturn channelMessages
                onBlocking { selectChannels(any()) } doReturn emptyList()
                onBlocking { selectMessages(any()) } doReturn emptyList()
                onBlocking { selectThreads(any()) } doReturn emptyList()
            }

            val handler = createEventHandler(scope = this, repos = repos)

            // When
            handler.handleEvents(event)

            // Then
            verify(repos).selectAllChannelUserMessages(testCid, targetUser.id)
            verify(repos, never()).selectAllUserMessages(any())

            // Verify hard delete - messages should be removed from DB
            verify(repos).deleteMessages(channelMessages)
            verify(repos).deleteAllChannelUserMessages(testCid, targetUser.id)
        }

    @Test
    fun `When UserMessagesDeletedEvent is processed for repository updates without cid, should delete messages from all channels`() =
        runTest {
            // Given
            val event = randomUserMessagesDeletedEvent(
                cid = null,
                user = targetUser,
                hardDelete = false,
                createdAt = deletedAt,
            )
            val allUserMessages = listOf(
                randomMessage(user = targetUser),
                randomMessage(user = targetUser),
                randomMessage(user = targetUser),
                randomMessage(user = targetUser),
            )
            val repos: RepositoryFacade = mock {
                onBlocking { selectAllUserMessages(targetUser.id) } doReturn allUserMessages
                onBlocking { selectChannels(any()) } doReturn emptyList()
                onBlocking { selectMessages(any()) } doReturn emptyList()
                onBlocking { selectThreads(any()) } doReturn emptyList()
            }

            val handler = createEventHandler(scope = this, repos = repos)

            // When
            handler.handleEvents(event)

            // Then
            verify(repos).selectAllUserMessages(targetUser.id)
            verify(repos, never()).selectAllChannelUserMessages(any(), any())

            // Verify soft delete - messages should be marked as deleted
            val expectedDeletedMessages = allUserMessages.map { it.copy(deletedAt = deletedAt) }
            verify(repos).insertMessages(expectedDeletedMessages)
            verify(repos, never()).deleteChannelMessage(any())
        }

    @Test
    fun `When UserMessagesDeletedEvent is processed for repository updates without cid and hard delete, should remove messages from all channels`() =
        runTest {
            // Given
            val event = randomUserMessagesDeletedEvent(
                cid = null,
                user = targetUser,
                hardDelete = true,
                createdAt = deletedAt,
            )
            val allUserMessages = listOf(
                randomMessage(user = targetUser),
                randomMessage(user = targetUser),
                randomMessage(user = targetUser),
            )
            val repos: RepositoryFacade = mock {
                onBlocking { selectAllUserMessages(targetUser.id) } doReturn allUserMessages
                onBlocking { selectChannels(any()) } doReturn emptyList()
                onBlocking { selectMessages(any()) } doReturn emptyList()
                onBlocking { selectThreads(any()) } doReturn emptyList()
            }

            val handler = createEventHandler(scope = this, repos = repos)

            // When
            handler.handleEvents(event)

            // Then
            verify(repos).selectAllUserMessages(targetUser.id)
            verify(repos, never()).selectAllChannelUserMessages(any(), any())

            // Verify hard delete - messages should be removed from DB
            verify(repos).deleteMessages(allUserMessages)
            verify(repos).deleteAllChannelUserMessages(null, targetUser.id)
        }

    @Test
    fun `When UserMessagesDeletedEvent is processed, should handle both state and repository updates`() = runTest {
        // Given
        val event = randomUserMessagesDeletedEvent(
            cid = testCid,
            user = targetUser,
            hardDelete = false,
            createdAt = deletedAt,
        )
        val channelMessages = listOf(randomMessage(user = targetUser))
        val channelLogic: ChannelLogic = mock()
        val logicRegistry: LogicRegistry = mock {
            on { isActiveChannel(any(), any()) } doReturn true
            on { channel(any(), any()) } doReturn channelLogic
        }
        val repos: RepositoryFacade = mock {
            onBlocking { selectAllChannelUserMessages(testCid, targetUser.id) } doReturn channelMessages
            onBlocking { selectChannels(any()) } doReturn emptyList()
            onBlocking { selectMessages(any()) } doReturn emptyList()
            onBlocking { selectThreads(any()) } doReturn emptyList()
        }

        val handler = createEventHandler(scope = this, logicRegistry = logicRegistry, repos = repos)

        // When
        handler.handleEvents(event)

        // Then
        // Verify state updates
        verify(logicRegistry).isActiveChannel("messaging", "123")
        verify(logicRegistry).channel("messaging", "123")
        verify(channelLogic).handleEvent(event)

        // Verify repository updates
        verify(repos).selectAllChannelUserMessages(testCid, targetUser.id)
        val expectedDeletedMessages = channelMessages.map { it.copy(deletedAt = deletedAt) }
        verify(repos).insertMessages(expectedDeletedMessages)
    }

    private fun createEventHandler(
        scope: CoroutineScope,
        logicRegistry: LogicRegistry = mock(),
        repos: RepositoryFacade = mock(),
    ): EventHandlerSequential {
        val subscribeForEvents: (ChatEventListener<ChatEvent>) -> Disposable = { _ ->
            EventHandlerSequential.EMPTY_DISPOSABLE
        }
        val stateRegistry: StateRegistry = mock()
        val clientState: ClientState = mock {
            on { user } doReturn MutableStateFlow(currentUser)
        }
        val mutableGlobalState = MutableGlobalState(currentUser.id)
        val sideEffect: suspend () -> Unit = {}
        val syncedEvents: Flow<List<ChatEvent>> = emptyFlow()

        return EventHandlerSequential(
            currentUserId = currentUser.id,
            subscribeForEvents = subscribeForEvents,
            logicRegistry = logicRegistry,
            stateRegistry = stateRegistry,
            clientState = clientState,
            mutableGlobalState = mutableGlobalState,
            repos = repos,
            sideEffect = sideEffect,
            syncedEvents = syncedEvents,
            scope = scope,
        )
    }
}
