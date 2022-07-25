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
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
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
import io.getstream.chat.android.offline.sync.internal.SyncManager
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.randomString
import io.getstream.logging.StreamLog
import io.getstream.logging.kotlin.KotlinStreamLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    private lateinit var clientState: ClientState
    private lateinit var repositoryFacade: RepositoryFacade
    private lateinit var syncManager: SyncManager
    private lateinit var user: User

    private val connectionState = MutableStateFlow(ConnectionState.OFFLINE)

    @BeforeAll
    fun beforeAll() {
        StreamLog.setValidator { _, _ -> true }
        StreamLog.setLogger(KotlinStreamLogger())
    }

    @BeforeEach
    fun setUp() {
        user = randomUser()
        chatClient = mock()
        logicRegistry = mock()
        stateRegistry = mock()
        globalState = mock()
        clientState = mock {
            on(it.user) doReturn MutableStateFlow(user)
            on(it.connectionState) doReturn connectionState
        }
        repositoryFacade = mock()
        syncManager = mock()
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
