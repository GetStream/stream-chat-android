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
import io.getstream.chat.android.offline.event.handler.internal.EventHandlerImpl
import io.getstream.chat.android.offline.model.connection.ConnectionState
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import io.getstream.chat.android.offline.sync.internal.SyncManager
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@ExperimentalCoroutinesApi
internal class EventHandlerImplTest {

    private val chatClient: ChatClient = mock()
    private val logicRegistry: LogicRegistry = mock()
    private val stateRegistry: StateRegistry = mock()
    private val globalState = GlobalMutableState.create()
    private val repositoryFacade: RepositoryFacade = mock()
    private val syncManager: SyncManager = mock()
    private val user = randomUser()

    private val eventHandlerImpl = EventHandlerImpl(
        recoveryEnabled = true,
        client = chatClient,
        logic = logicRegistry,
        state = stateRegistry,
        mutableGlobalState = globalState,
        repos = repositoryFacade,
        syncManager = syncManager,
    )

    @BeforeEach
    fun setUp() {
        globalState._user.value = user
    }

    @Test
    fun `when connected event arrives, user should be updated and state should be propagated`() = runBlockingTest {
        whenever(repositoryFacade.selectMessages(any(), any())) doReturn listOf(randomMessage())
        whenever(repositoryFacade.selectChannels(any(), any<Boolean>())) doReturn listOf(randomChannel())

        val connectedEvent = ConnectedEvent(
            type = "type",
            createdAt = Date(),
            me = user,
            connectionId = randomString()
        )

        eventHandlerImpl.handleEvent(connectedEvent)

        globalState.connectionState.value `should be` ConnectionState.CONNECTED
        globalState.initialized.value `should be` true
    }

    @Test
    fun `when disconnected event arrives, state should be propagated`() = runBlockingTest {
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

        eventHandlerImpl.handleEvent(connectedEvent) // To make sure that we are not asserting the initial state
        eventHandlerImpl.handleEvent(disconnectedEvent)

        globalState.connectionState.value `should be` ConnectionState.OFFLINE
    }

    @Test
    fun `when connecting event arrives, state should be propagated`() = runBlockingTest {
        whenever(repositoryFacade.selectMessages(any(), any())) doReturn listOf(randomMessage())
        whenever(repositoryFacade.selectChannels(any(), any<Boolean>())) doReturn listOf(randomChannel())

        val connectingEvent = ConnectingEvent(
            type = "type",
            createdAt = Date(),
        )

        eventHandlerImpl.handleEvent(connectingEvent)

        globalState.connectionState.value `should be` ConnectionState.CONNECTING
    }

    @Test
    fun `when a health check event happens, a request to retry failed entities should happen`() = runBlockingTest {
        whenever(repositoryFacade.selectMessages(any(), any())) doReturn listOf(randomMessage())
        whenever(repositoryFacade.selectChannels(any(), any<Boolean>())) doReturn listOf(randomChannel())

        val connectingEvent = HealthEvent(
            type = "type",
            createdAt = Date(),
            connectionId = randomString()
        )

        eventHandlerImpl.handleEvent(connectingEvent)

        verify(syncManager).retryFailedEntities()
    }
}
