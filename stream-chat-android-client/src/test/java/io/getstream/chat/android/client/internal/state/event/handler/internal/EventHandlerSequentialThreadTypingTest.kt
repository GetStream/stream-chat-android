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
import io.getstream.chat.android.client.internal.state.plugin.logic.channel.thread.internal.ThreadLogic
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomTypingStartEvent
import io.getstream.chat.android.client.test.randomTypingStopEvent
import io.getstream.chat.android.client.utils.observable.Disposable
import io.getstream.chat.android.randomString
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
 * Covers how [EventHandlerSequential] routes typing events to the active threads.
 */
internal class EventHandlerSequentialThreadTypingTest {

    private val currentUser = randomUser()
    private val parentId = randomString()
    private val threadLogic: ThreadLogic = mock()
    private val logicRegistry: LogicRegistry = mock {
        on { getActiveThreadsLogic() } doReturn listOf(threadLogic)
    }

    @Test
    fun `Typing events from other users reach the active threads`() = runTest {
        val start = randomTypingStartEvent(user = randomUser(), parentId = parentId)
        val stop = randomTypingStopEvent(user = randomUser(), parentId = null)

        createEventHandler(scope = this).handleEvents(start, stop)

        verify(threadLogic).handleTypingEvents(listOf(start, stop))
    }

    @Test
    fun `The current user's own typing is not routed`() = runTest {
        val event = randomTypingStartEvent(user = currentUser, parentId = parentId)

        createEventHandler(scope = this).handleEvents(event)

        verify(threadLogic, never()).handleTypingEvents(any())
    }

    private fun createEventHandler(scope: CoroutineScope): EventHandlerSequential {
        val subscribeForEvents: (ChatEventListener<ChatEvent>) -> Disposable = { _ ->
            EventHandlerSequential.EMPTY_DISPOSABLE
        }
        val repos: RepositoryFacade = mock {
            onBlocking { selectChannels(any()) } doReturn emptyList()
            onBlocking { selectMessages(any()) } doReturn emptyList()
            onBlocking { selectThreads(any()) } doReturn emptyList()
        }
        val stateRegistry: StateRegistry = mock()
        val clientState: ClientState = mock {
            on { user } doReturn MutableStateFlow(currentUser)
        }
        val syncedEvents: Flow<List<ChatEvent>> = emptyFlow()
        return EventHandlerSequential(
            currentUserId = currentUser.id,
            subscribeForEvents = subscribeForEvents,
            logicRegistry = logicRegistry,
            stateRegistry = stateRegistry,
            clientState = clientState,
            mutableGlobalState = MutableGlobalState(currentUser.id),
            repos = repos,
            sideEffect = {},
            syncedEvents = syncedEvents,
            bufferConfig = MessageBufferConfig(),
            isLocalUnreadCountEnabled = false,
            scope = scope,
        )
    }
}
