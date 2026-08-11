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
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomMemberUpdatedEvent
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.models.MemberInfo
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

/**
 * Covers the [EventHandlerSequential] handling of [MemberUpdatedEvent]: the backend never emits `message.updated` when
 * a membership changes, so the member snapshot denormalized onto the stored messages has to be refreshed by the SDK.
 */
internal class EventHandlerSequentialMemberUpdatedTest {

    private val currentUser = randomUser()
    private val author = randomUser()
    private val testCid = "messaging:123"

    @Test
    fun `When MemberUpdatedEvent is processed, should refresh the member snapshot on the author's stored messages`() =
        runTest {
            // Given
            val member = randomMember(user = author, channelRole = "channel_moderator")
                .copy(notificationsMuted = true, extraData = mapOf("flair" to "gold"))
            val event = randomMemberUpdatedEvent(cid = testCid, user = author, member = member)
            val repos = mockRepos()

            val handler = createEventHandler(scope = this, repos = repos)

            // When
            handler.handleEvents(event)

            // Then - only the member column is written, so a concurrent edit elsewhere is not overwritten
            verify(repos).updateChannelUserMessagesMember(
                testCid,
                author.id,
                MemberInfo(
                    channelRole = "channel_moderator",
                    notificationsMuted = true,
                    extraData = mapOf("flair" to "gold"),
                ),
            )
            verify(repos, never()).insertMessages(argThat { isNotEmpty() })
        }

    private fun mockRepos(): RepositoryFacade =
        mock {
            onBlocking { selectChannels(any()) } doReturn emptyList()
            onBlocking { selectMessages(any()) } doReturn emptyList()
            onBlocking { selectThreads(any()) } doReturn emptyList()
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
            bufferConfig = MessageBufferConfig(),
            isLocalUnreadCountEnabled = false,
            scope = scope,
        )
    }
}
