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

package io.getstream.chat.android.offline.event

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.setup.state.ClientMutableState
import io.getstream.chat.android.client.test.randomChannel
import io.getstream.chat.android.client.test.randomMessage
import io.getstream.chat.android.client.test.randomUser
import io.getstream.chat.android.offline.event.handler.internal.EventHandler
import io.getstream.chat.android.offline.event.handler.internal.EventHandlerImpl
import io.getstream.chat.android.offline.event.handler.internal.EventHandlerSequential
import io.getstream.chat.android.offline.event.model.EventHandlerType
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import io.getstream.chat.android.offline.sync.internal.SyncManager
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.randomString
import io.getstream.logging.StreamLog
import io.getstream.logging.kotlin.KotlinStreamLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@ExperimentalCoroutinesApi
internal class EventHandlerImplTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var chatClient: ChatClient
    private lateinit var logicRegistry: LogicRegistry
    private lateinit var stateRegistry: StateRegistry
    private lateinit var globalState: GlobalMutableState
    private lateinit var clientState: ClientMutableState
    private lateinit var repositoryFacade: RepositoryFacade
    private lateinit var syncManager: SyncManager
    private lateinit var user: User

    @BeforeAll
    fun beforeAll() {
        StreamLog.setValidator { _, _ -> true }
        StreamLog.setLogger(KotlinStreamLogger())
    }

    @BeforeEach
    fun setUp() {
        chatClient = mock()
        logicRegistry = mock()
        stateRegistry = mock()
        globalState = GlobalMutableState.create()
        clientState = ClientMutableState.create()
        repositoryFacade = mock()
        syncManager = mock()
        user = randomUser()

        globalState.setUser(user)
    }

    @ParameterizedTest
    @EnumSource(EventHandlerType::class)
    fun `when connected event arrives, user should be updated and state should be propagated`(
        type: EventHandlerType,
    ) = runTest {
        val eventHandler = buildEventHandler(type)
        whenever(repositoryFacade.selectMessages(any(), any())) doReturn listOf(randomMessage())
        whenever(repositoryFacade.selectChannels(any(), any<Boolean>())) doReturn listOf(randomChannel())

        val connectedEvent = ConnectedEvent(
            type = "type",
            createdAt = Date(),
            me = user,
            connectionId = randomString()
        )

        eventHandler.handleEvents(connectedEvent)

        globalState.connectionState.value `should be` ConnectionState.CONNECTED
        globalState.initialized.value `should be` true
    }

    @ParameterizedTest
    @EnumSource(EventHandlerType::class)
    fun `when disconnected event arrives, state should be propagated`(
        type: EventHandlerType,
    ) = runTest {
        val eventHandler = buildEventHandler(type)
        whenever(repositoryFacade.selectMessages(any(), any())) doReturn listOf(randomMessage())
        whenever(repositoryFacade.selectChannels(any(), any<Boolean>())) doReturn listOf(randomChannel())

        val connectedEvent = ConnectedEvent(
            type = "type",
            createdAt = Date(),
            me = user,
            connectionId = randomString()
        )

        val disconnectedEvent = DisconnectedEvent(
            type = "type",
            createdAt = Date(),
        )

        eventHandler.handleEvents(connectedEvent) // To make sure that we are not asserting the initial state
        eventHandler.handleEvents(disconnectedEvent)

        globalState.connectionState.value `should be` ConnectionState.OFFLINE
    }

    @ParameterizedTest
    @EnumSource(EventHandlerType::class)
    fun `when connecting event arrives, state should be propagated`(
        type: EventHandlerType,
    ) = runTest {
        val eventHandler = buildEventHandler(type)
        whenever(repositoryFacade.selectMessages(any(), any())) doReturn listOf(randomMessage())
        whenever(repositoryFacade.selectChannels(any(), any<Boolean>())) doReturn listOf(randomChannel())

        val connectingEvent = ConnectingEvent(
            type = "type",
            createdAt = Date(),
        )

        eventHandler.handleEvents(connectingEvent)

        globalState.connectionState.value `should be` ConnectionState.CONNECTING
    }

    @ParameterizedTest
    @EnumSource(EventHandlerType::class)
    fun `when a health check event happens, a request to retry failed entities should happen`(
        type: EventHandlerType,
    ) = runTest {
        val eventHandler = buildEventHandler(type)
        whenever(repositoryFacade.selectMessages(any(), any())) doReturn listOf(randomMessage())
        whenever(repositoryFacade.selectChannels(any(), any<Boolean>())) doReturn listOf(randomChannel())

        val connectingEvent = HealthEvent(
            type = "type",
            createdAt = Date(),
            connectionId = randomString()
        )

        eventHandler.handleEvents(connectingEvent)

        verify(syncManager).retryFailedEntities()
    }

    private fun buildEventHandler(type: EventHandlerType): EventHandler {
        return when (type) {
            EventHandlerType.SEQUENTIAL -> EventHandlerSequential(
                scope = testCoroutines.scope,
                recoveryEnabled = true,
                subscribeForEvents = { listener -> chatClient.subscribe(listener) },
                logicRegistry = logicRegistry,
                stateRegistry = stateRegistry,
                mutableGlobalState = globalState,
                clientMutableState = clientState,
                repos = repositoryFacade,
                syncManager = syncManager,
                currentUserId = user.id
            )
            EventHandlerType.DEFAULT -> EventHandlerImpl(
                scope = testCoroutines.scope,
                recoveryEnabled = true,
                client = chatClient,
                logic = logicRegistry,
                state = stateRegistry,
                mutableGlobalState = globalState,
                clientMutableState = clientState,
                repos = repositoryFacade,
                syncManager = syncManager,
            )
        }
    }
}
