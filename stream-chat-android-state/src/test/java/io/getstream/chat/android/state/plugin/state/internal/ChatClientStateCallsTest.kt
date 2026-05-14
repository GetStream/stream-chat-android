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

package io.getstream.chat.android.state.plugin.state.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.logic.querychannels.internal.QueryChannelsLogic
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
internal class ChatClientStateCallsTest {

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    private lateinit var chatClient: ChatClient
    private lateinit var clientState: ClientState
    private lateinit var stateRegistry: StateRegistry
    private lateinit var logicRegistry: LogicRegistry
    private lateinit var queryChannelsLogic: QueryChannelsLogic
    private lateinit var queryChannelsState: QueryChannelsState
    private lateinit var chatClientStateCalls: ChatClientStateCalls

    private val userFlow = MutableStateFlow<User?>(null)
    private val identifier = QueryChannelsIdentifier.Grouped("test-group")

    @BeforeEach
    fun setUp() {
        clientState = mock {
            on(it.user) doReturn userFlow
        }
        queryChannelsState = mock()
        stateRegistry = mock {
            on(it.queryChannels(any(), any())) doReturn queryChannelsState
            on(it.queryChannels(any<QueryChannelsIdentifier>())) doReturn queryChannelsState
        }
        queryChannelsLogic = mock()
        logicRegistry = mock {
            on(it.queryChannels(any<QueryChannelsIdentifier>())) doReturn queryChannelsLogic
        }

        val statePlugin: StatePlugin = mock {
            on(it.resolveDependency(eq(StateRegistry::class))) doReturn stateRegistry
            on(it.resolveDependency(eq(LogicRegistry::class))) doReturn logicRegistry
        }

        chatClient = mock {
            on(it.plugins) doReturn listOf(statePlugin)
            on(it.clientState) doReturn clientState
            on(it.awaitInitializationState(any())) doReturn InitializationState.COMPLETE
        }

        chatClientStateCalls = ChatClientStateCalls(chatClient, testCoroutines.scope)
    }

    @Test
    fun `initGroupedQueryChannelsState creates state without API call`() = runTest {
        // Given - user is connected
        userFlow.value = User(id = "test-user")
        val factory = ChatEventHandlerFactory(clientState)

        // When
        val result = chatClientStateCalls.initGroupedQueryChannelsState(identifier, factory)

        // Then — no remote queryChannels API call; the offline grouped load runs locally.
        verify(chatClient, never()).queryChannels(any())
        assertNotNull(result)
    }

    @Test
    fun `initGroupedQueryChannelsState waits for user before proceeding`() = runTest {
        // Given - user is NOT connected yet
        val factory = ChatEventHandlerFactory(clientState)
        var completed = false

        // When - launch initGroupedQueryChannelsState (it should suspend waiting for user)
        val job = launch {
            chatClientStateCalls.initGroupedQueryChannelsState(identifier, factory)
            completed = true
        }
        advanceUntilIdle()

        // Then - should not have completed yet
        assertEquals(false, completed)

        // When - user connects
        userFlow.value = User(id = "test-user")
        advanceUntilIdle()

        // Then - should complete now
        assertEquals(true, completed)
        job.cancel()
    }

    @Test
    fun `initGroupedQueryChannelsState returns state matching the identifier`() = runTest {
        // Given
        userFlow.value = User(id = "test-user")
        val factory = ChatEventHandlerFactory(clientState)

        // When
        chatClientStateCalls.initGroupedQueryChannelsState(identifier, factory)

        // Then - stateRegistry.queryChannels should be called with the identifier
        verify(stateRegistry).queryChannels(identifier)
    }
}
