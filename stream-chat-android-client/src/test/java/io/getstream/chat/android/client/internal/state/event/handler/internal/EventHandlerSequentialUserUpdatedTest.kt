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
import io.getstream.chat.android.client.api.MessageBufferConfig
import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomNotificationMutesUpdatedEvent
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMute
import io.getstream.chat.android.randomPrivacySettings
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.Date

/**
 * Covers the [EventHandlerSequential] handling of [UserUpdatedEvent] for the current user: the event's user carries
 * no mutes, so the current user's mutes must survive it.
 */
internal class EventHandlerSequentialUserUpdatedTest {

    private val mutes = List(3) { randomMute() }
    private val currentUser = randomUser(
        name = "Old",
        updatedAt = Date(1_000),
        mutes = mutes,
        privacySettings = randomPrivacySettings(),
    )

    @Test
    fun `When the current user is updated, should keep their mutes in global state and storage`() = runTest {
        // Given
        val eventUser = currentUser.copy(
            name = "New",
            updatedAt = Date(2_000),
            mutes = emptyList(),
            privacySettings = null,
        )
        val event = UserUpdatedEvent(
            type = "user.updated",
            createdAt = Date(),
            rawCreatedAt = "",
            user = eventUser,
        )
        val repos = mockRepos()
        val mutableGlobalState = MutableGlobalState(currentUser.id).apply { setMutedUsers(mutes) }
        val handler = createEventHandler(scope = this, repos = repos, mutableGlobalState = mutableGlobalState)

        // When
        handler.handleEvents(event)

        // Then
        assertEquals(mutes, mutableGlobalState.muted.value)
        val storedUser = argumentCaptor<User>().apply { verify(repos).insertCurrentUser(capture()) }.firstValue
        assertEquals("New", storedUser.name)
        assertEquals(mutes, storedUser.mutes)
        assertEquals(currentUser.privacySettings, storedUser.privacySettings)
    }

    @Test
    fun `When the mutes change earlier in the same batch, should keep the new mutes in storage`() = runTest {
        // Given
        val newMutes = mutes + randomMute()
        val mutesUpdatedEvent = randomNotificationMutesUpdatedEvent(me = currentUser.copy(mutes = newMutes))
        val userUpdatedEvent = UserUpdatedEvent(
            type = "user.updated",
            createdAt = Date(),
            rawCreatedAt = "",
            user = currentUser.copy(name = "New", updatedAt = Date(2_000), mutes = emptyList()),
        )
        val repos = mockRepos()
        val mutableGlobalState = MutableGlobalState(currentUser.id)
        val handler = createEventHandler(scope = this, repos = repos, mutableGlobalState = mutableGlobalState)

        // When
        handler.handleEvents(mutesUpdatedEvent, userUpdatedEvent)

        // Then
        assertEquals(newMutes, mutableGlobalState.muted.value)
        val storedUser = argumentCaptor<User>().apply { verify(repos, times(2)).insertCurrentUser(capture()) }.lastValue
        assertEquals("New", storedUser.name)
        assertEquals(newMutes, storedUser.mutes)
    }

    @Test
    fun `When another user is updated, should not store the current user`() = runTest {
        // Given
        val event = UserUpdatedEvent(
            type = "user.updated",
            createdAt = Date(),
            rawCreatedAt = "",
            user = randomUser(),
        )
        val repos = mockRepos()
        val handler = createEventHandler(scope = this, repos = repos, mutableGlobalState = MutableGlobalState(currentUser.id))

        // When
        handler.handleEvents(event)

        // Then
        verify(repos, never()).insertCurrentUser(any())
    }

    private fun mockRepos(): RepositoryFacade =
        mock {
            onBlocking { selectChannels(any()) } doReturn emptyList()
            onBlocking { selectMessages(any()) } doReturn emptyList()
            onBlocking { selectThreads(any()) } doReturn emptyList()
        }

    private fun createEventHandler(
        scope: CoroutineScope,
        repos: RepositoryFacade,
        mutableGlobalState: MutableGlobalState,
    ): EventHandlerSequential {
        val subscribeForEvents: (ChatEventListener<ChatEvent>) -> Disposable = { _ ->
            EventHandlerSequential.EMPTY_DISPOSABLE
        }
        val logicRegistry: LogicRegistry = mock()
        val stateRegistry: StateRegistry = mock()
        val clientState: ClientState = mock {
            on { user } doReturn MutableStateFlow(currentUser)
        }
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
            bufferConfig = MessageBufferConfig(),
            isLocalUnreadCountEnabled = false,
            scope = scope,
        )
    }
}
