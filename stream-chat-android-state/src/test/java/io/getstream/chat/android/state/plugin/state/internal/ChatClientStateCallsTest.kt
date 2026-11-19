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
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.logic.querychannels.internal.QueryChannelsLogic
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.state.plugin.state.channel.thread.ThreadState
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.test.asCall
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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
    private val initializationStateFlow = MutableStateFlow(InitializationState.COMPLETE)
    private val identifier = QueryChannelsIdentifier.Grouped("test-group")

    @BeforeEach
    fun setUp() {
        clientState = mock {
            on(it.user) doReturn userFlow
            on(it.initializationState) doReturn initializationStateFlow
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

    @Test
    fun `watchChannel marks the channel as loading before querying it`() = runTest {
        // Given
        userFlow.value = User(id = "test-user")
        val channelState: ChannelMutableState = mock()
        whenever(stateRegistry.mutableChannel("messaging", "123")) doReturn channelState
        whenever(chatClient.queryChannel(any(), any(), any(), any())) doReturn randomChannel().asCall()

        // When
        val result = chatClientStateCalls.watchChannel("messaging:123", messageLimit = 30, userPresence = true)

        // Then
        assertEquals(channelState, result)
        inOrder(channelState, chatClient) {
            verify(channelState).setLoadingIfEmpty()
            verify(chatClient).queryChannel(eq("messaging"), eq("123"), any(), any())
        }
    }

    @Test
    fun `awaitReplies is cancelled when the user disconnects before the state is resolved`() = runTest {
        // Given - a disconnect marks the client uninitialized while the state is being resolved
        userFlow.value = User(id = "test-user")
        givenDisconnectOnResolution(number = 1)
        whenever(chatClient.getReplies("parent-id", 30)) doReturn emptyList<Message>().asCall()

        // When
        val error = runCatching {
            chatClientStateCalls.awaitReplies("parent-id", messageLimit = 30, olderToNewer = false)
        }.exceptionOrNull()

        // Then - the call ends as a cancellation and the shared scope survives
        assertInstanceOf(CancellationException::class.java, error)
        assertTrue(testCoroutines.scope.isActive)
    }

    @Test
    fun `awaitReplies rethrows a state resolution failure while the client is initialized`() = runTest {
        // Given - a misconfigured client without the state plugin
        userFlow.value = User(id = "test-user")
        whenever(chatClient.plugins) doReturn emptyList()
        whenever(chatClient.getReplies("parent-id", 30)) doReturn emptyList<Message>().asCall()

        // When
        val error = runCatching {
            chatClientStateCalls.awaitReplies("parent-id", messageLimit = 30, olderToNewer = false)
        }.exceptionOrNull()

        // Then - the exact class, since a CancellationException is also an IllegalStateException
        assertEquals(IllegalStateException::class.java, error?.javaClass)
    }

    @Test
    fun `initGroupedQueryChannelsState is cancelled when the user disconnects before the logic is resolved`() =
        runTest {
            // Given - the state resolves, then a disconnect starts before the logic lookup
            userFlow.value = User(id = "test-user")
            givenDisconnectOnResolution(number = 2)

            // When
            val error = runCatching {
                chatClientStateCalls.initGroupedQueryChannelsState(identifier, ChatEventHandlerFactory(clientState))
            }.exceptionOrNull()

            // Then
            assertInstanceOf(CancellationException::class.java, error)
            verify(queryChannelsLogic, never()).loadOfflineGroupedChannels()
        }

    @Test
    fun `awaitReplies made in a session is cancelled when the session ends before the state lookup`() = runTest {
        // Given - calls made while connected, and a logout that completes while the replies load
        userFlow.value = User(id = "test-user")
        val sessionCalls = ChatClientStateCalls(chatClient, testCoroutines.scope)
        whenever(chatClient.getReplies("parent-id", 30)) doAnswer {
            initializationStateFlow.value = InitializationState.NOT_INITIALIZED
            userFlow.value = null
            emptyList<Message>().asCall()
        }

        // When
        val error = runCatching {
            sessionCalls.awaitReplies("parent-id", messageLimit = 30, olderToNewer = false)
        }.exceptionOrNull()

        // Then - the call ends instead of waiting for a later session, and no lookup runs
        assertInstanceOf(CancellationException::class.java, error)
        verify(chatClient, never()).awaitInitializationState(any())
    }

    @Test
    fun `awaitReplies made in a session is cancelled when another user connects before the state lookup`() =
        runTest {
            // Given - calls made while connected, and a switch to another user while the replies load
            userFlow.value = User(id = "test-user")
            val sessionCalls = ChatClientStateCalls(chatClient, testCoroutines.scope)
            whenever(chatClient.getReplies("parent-id", 30)) doAnswer {
                userFlow.value = User(id = "other-user")
                emptyList<Message>().asCall()
            }

            // When
            val error = runCatching {
                sessionCalls.awaitReplies("parent-id", messageLimit = 30, olderToNewer = false)
            }.exceptionOrNull()

            // Then - the other user's state is never returned
            assertInstanceOf(CancellationException::class.java, error)
            verify(stateRegistry, never()).thread(any())
        }

    @Test
    fun `awaitReplies made while the user disconnects is cancelled`() = runTest {
        // Given - a disconnect has marked the client uninitialized but not cleared the user yet
        userFlow.value = User(id = "test-user")
        initializationStateFlow.value = InitializationState.NOT_INITIALIZED
        val teardownCalls = ChatClientStateCalls(chatClient, testCoroutines.scope)
        whenever(chatClient.getReplies("parent-id", 30)) doReturn emptyList<Message>().asCall()

        // When - another user connects before the state is needed
        userFlow.value = User(id = "other-user")
        initializationStateFlow.value = InitializationState.COMPLETE
        val error = runCatching {
            teardownCalls.awaitReplies("parent-id", messageLimit = 30, olderToNewer = false)
        }.exceptionOrNull()

        // Then
        assertInstanceOf(CancellationException::class.java, error)
        verify(stateRegistry, never()).thread(any())
    }

    @Test
    fun `awaitReplies made before a session waits for it and resolves its state`() = runTest {
        // Given - calls made before connectUser
        initializationStateFlow.value = InitializationState.NOT_INITIALIZED
        val preSessionCalls = ChatClientStateCalls(chatClient, testCoroutines.scope)
        val threadState: ThreadState = mock()
        whenever(stateRegistry.thread("parent-id")) doReturn threadState
        whenever(chatClient.getReplies("parent-id", 30)) doReturn emptyList<Message>().asCall()
        var result: ThreadState? = null

        // When
        val job = launch { result = preSessionCalls.awaitReplies("parent-id", messageLimit = 30, olderToNewer = false) }
        advanceUntilIdle()

        // Then - it waits until the user connects
        assertEquals(null, result)
        userFlow.value = User(id = "test-user")
        initializationStateFlow.value = InitializationState.COMPLETE
        advanceUntilIdle()
        assertEquals(threadState, result)
        job.cancel()
    }

    private fun givenDisconnectOnResolution(number: Int) {
        var resolutions = 0
        whenever(chatClient.awaitInitializationState(any())) doAnswer {
            resolutions++
            if (resolutions == number) initializationStateFlow.value = InitializationState.NOT_INITIALIZED
            initializationStateFlow.value
        }
    }
}
