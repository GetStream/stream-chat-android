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

package io.getstream.chat.android.state.event.handler.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomChannelDeletedEvent
import io.getstream.chat.android.client.test.randomNotificationChannelDeletedEvent
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.plugin.config.MessageBufferConfig
import io.getstream.chat.android.state.plugin.config.MessageLimitConfig
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class EventHandlerSequentialChannelDeletedTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("channelDeletedEvents")
    fun `channel deletion is reflected on the channel state before the channel is discarded`(
        eventType: String,
        event: CidEvent,
    ) = runTest {
        val fixture = Fixture()
        val channelState = fixture.withActiveChannel(CHANNEL_TYPE, CHANNEL_ID)
        val eventHandler = fixture.get()

        eventHandler.handleEvents(event)

        channelState.channelData.value.deletedAt `should be equal to` event.createdAt
        fixture.isActiveChannel(CHANNEL_TYPE, CHANNEL_ID) `should be equal to` false
    }

    private class Fixture {
        private val currentUser = randomUser()
        private val userFlow = MutableStateFlow<User?>(currentUser)
        private val clientState: ClientState = mock { on { user } doReturn userFlow }
        private val repos: RepositoryFacade = mock()
        private val client: ChatClient = mock()
        private val mutableGlobalState = MutableGlobalState(currentUser.id)
        private val stateRegistry = StateRegistry(
            userStateFlow = userFlow,
            latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
            activeLiveLocations = MutableStateFlow(emptyList<Location>()),
            job = Job(),
            now = { 0L },
            scope = testCoroutines.scope,
            messageLimitConfig = MessageLimitConfig(),
        )
        private val logicRegistry = LogicRegistry(
            stateRegistry = stateRegistry,
            clientState = clientState,
            mutableGlobalState = mutableGlobalState,
            userPresence = false,
            repos = repos,
            client = client,
            coroutineScope = testCoroutines.scope,
            now = { 0L },
        )

        /** Watches the channel the way an open chat screen does, and returns the state it observes. */
        fun withActiveChannel(channelType: String, channelId: String): ChannelState {
            logicRegistry.channel(channelType, channelId)
            return stateRegistry.channel(channelType, channelId)
        }

        fun isActiveChannel(channelType: String, channelId: String) =
            logicRegistry.isActiveChannel(channelType, channelId)

        fun get() = EventHandlerSequential(
            currentUserId = currentUser.id,
            subscribeForEvents = { EventHandlerSequential.EMPTY_DISPOSABLE },
            logicRegistry = logicRegistry,
            stateRegistry = stateRegistry,
            clientState = clientState,
            mutableGlobalState = mutableGlobalState,
            repos = repos,
            sideEffect = {},
            syncedEvents = emptyFlow(),
            bufferConfig = MessageBufferConfig(),
            scope = testCoroutines.scope,
        )
    }

    companion object {

        private const val CHANNEL_TYPE = "messaging"
        private const val CHANNEL_ID = "channelId"
        private const val CID = "$CHANNEL_TYPE:$CHANNEL_ID"

        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        @JvmStatic
        fun channelDeletedEvents() = listOf(
            Arguments.of(
                EventType.CHANNEL_DELETED,
                randomChannelDeletedEvent(cid = CID, channelType = CHANNEL_TYPE, channelId = CHANNEL_ID),
            ),
            Arguments.of(
                EventType.NOTIFICATION_CHANNEL_DELETED,
                randomNotificationChannelDeletedEvent(
                    cid = CID,
                    channelType = CHANNEL_TYPE,
                    channelId = CHANNEL_ID,
                ),
            ),
        )
    }
}
