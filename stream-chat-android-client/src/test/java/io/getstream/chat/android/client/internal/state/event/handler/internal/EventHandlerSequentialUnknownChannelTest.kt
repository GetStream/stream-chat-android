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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.MessageBufferConfig
import io.getstream.chat.android.client.api.MessageLimitConfig
import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.internal.state.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.chat.android.client.internal.state.plugin.state.querychannels.internal.toMutableState
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomNewMessageEvent
import io.getstream.chat.android.client.utils.internal.ChannelId
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should not contain`
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

/**
 * A message for a channel with no stored copy and no state must not create an empty state for it.
 *
 * Each case runs against both channel state implementations, since `ChatClientConfig` selects between them and both
 * registries follow that same flag.
 */
internal class EventHandlerSequentialUnknownChannelTest {

    @ParameterizedTest(name = "legacy channel state: {0}")
    @ValueSource(booleans = [false, true])
    fun `a new message for an unknown channel does not create a state or list it`(legacy: Boolean) = runTest {
        val fixture = Fixture(legacy)

        fixture.handleNewMessage(totalUnreadCount = 7, unreadChannels = 3)

        fixture.stateRegistry.isActiveChannel(CHANNEL).`should be false`()
        fixture.groupedList.rawChannels.orEmpty().keys `should not contain` CID
        // No capability without a state, so the event's counts do not apply.
        fixture.mutableGlobalState.totalUnreadCount.value `should be equal to` 0
        fixture.mutableGlobalState.channelUnreadCount.value `should be equal to` 0
    }

    @ParameterizedTest(name = "legacy channel state: {0}")
    @ValueSource(booleans = [false, true])
    fun `a new message applies the unread counts when a loaded state has the read events capability`(
        legacy: Boolean,
    ) = runTest {
        val fixture = Fixture(legacy)
        fixture.withLoadedChannel(
            ChannelData(type = CHANNEL_TYPE, id = CHANNEL_ID, ownCapabilities = setOf(ChannelCapabilities.READ_EVENTS)),
        )

        fixture.handleNewMessage(totalUnreadCount = 7, unreadChannels = 3)

        fixture.mutableGlobalState.totalUnreadCount.value `should be equal to` 7
        fixture.mutableGlobalState.channelUnreadCount.value `should be equal to` 3
    }

    private class Fixture(private val legacyChannelState: Boolean) {
        val currentUser = randomUser()
        private val userFlow = MutableStateFlow<User?>(currentUser)
        private val clientState: ClientState = mock { on { user } doReturn userFlow }

        // Nothing is stored, as for a channel this device has not loaded.
        private val repos: RepositoryFacade = mock<RepositoryFacade>().apply {
            stub { onBlocking { selectChannels(any()) } doReturn emptyList() }
        }
        val mutableGlobalState = MutableGlobalState(currentUser.id)
        val stateRegistry = StateRegistry(
            userStateFlow = userFlow,
            latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
            activeLiveLocations = MutableStateFlow(emptyList<Location>()),
            job = Job(),
            now = { 0L },
            scope = testCoroutines.scope,
            messageLimitConfig = MessageLimitConfig(),
            useLegacyChannelState = legacyChannelState,
        )
        private val logicRegistry = LogicRegistry(
            stateRegistry = stateRegistry,
            clientState = clientState,
            mutableGlobalState = mutableGlobalState,
            userPresence = false,
            repos = repos,
            client = mock<ChatClient>(),
            coroutineScope = testCoroutines.scope,
            now = { 0L },
            useLegacyChannelLogic = legacyChannelState,
            isLocalUnreadCountEnabled = false,
        )
        val groupedList: QueryChannelsMutableState
        private val eventHandler = EventHandlerSequential(
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
            isLocalUnreadCountEnabled = false,
            scope = testCoroutines.scope,
        )

        init {
            val identifier = QueryChannelsIdentifier.Grouped("all")
            logicRegistry.queryChannels(identifier)
            groupedList = stateRegistry.queryChannels(identifier).toMutableState()
            // A loaded list, so a channel could be added to it.
            groupedList.setChannels(listOf(randomChannel(hidden = false)).associateBy { it.cid })
        }

        fun withLoadedChannel(channelData: ChannelData) {
            if (legacyChannelState) {
                stateRegistry.legacyChannelState(CHANNEL).setChannelData(channelData)
            } else {
                stateRegistry.channelState(CHANNEL).updateChannelData { channelData }
            }
        }

        suspend fun handleNewMessage(totalUnreadCount: Int, unreadChannels: Int) {
            eventHandler.handleEvents(
                randomNewMessageEvent(
                    cid = CID,
                    channelType = CHANNEL_TYPE,
                    channelId = CHANNEL_ID,
                    message = randomMessage(type = "regular", shadowed = false, parentId = null),
                    totalUnreadCount = totalUnreadCount,
                    unreadChannels = unreadChannels,
                ),
            )
        }
    }

    companion object {

        private const val CHANNEL_TYPE = "messaging"
        private const val CHANNEL_ID = "unknown"
        private const val CID = "$CHANNEL_TYPE:$CHANNEL_ID"
        private val CHANNEL = ChannelId.fromCid(CID)!!

        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }
}
