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
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomChannelHiddenEvent
import io.getstream.chat.android.client.test.randomChannelUpdatedEvent
import io.getstream.chat.android.client.test.randomMemberUpdatedEvent
import io.getstream.chat.android.client.test.randomNotificationMarkReadEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.plugin.config.MessageBufferConfig
import io.getstream.chat.android.state.plugin.config.MessageLimitConfig
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.state.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.chat.android.state.plugin.state.querychannels.internal.toMutableState
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should not contain`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/**
 * Reproduces the "unmatch" event batch: `channel.hidden` arrives together with `channel.updated`
 * and `member.updated` for the same channel. The later events must not re-add the just-hidden
 * channel to a grouped query.
 */
internal class EventHandlerSequentialHiddenChannelTest {

    @Test
    fun `a hidden channel is not re-added to a grouped query by later events in the same batch`() = runTest {
        val fixture = Fixture()
        val membership = randomMember(user = fixture.currentUser)
        fixture.withActiveChannel(CHANNEL_TYPE, CHANNEL_ID, membership)
        val groupedState = fixture.withGroupedQuery(groupKey = "all")
        val eventHandler = fixture.get()

        eventHandler.handleEvents(
            randomNotificationMarkReadEvent(cid = CID, channelType = CHANNEL_TYPE, channelId = CHANNEL_ID),
            randomChannelHiddenEvent(
                cid = CID,
                channelType = CHANNEL_TYPE,
                channelId = CHANNEL_ID,
                user = fixture.currentUser,
                clearHistory = false,
            ),
            randomChannelUpdatedEvent(
                cid = CID,
                channelType = CHANNEL_TYPE,
                channelId = CHANNEL_ID,
                channel = updatedChannel(),
            ),
            randomMemberUpdatedEvent(
                cid = CID,
                channelType = CHANNEL_TYPE,
                channelId = CHANNEL_ID,
                member = membership,
            ),
        )

        groupedState.rawChannels.orEmpty().keys `should not contain` CID
    }

    @Test
    fun `the same batch without the hidden event adds the channel to the grouped query`() = runTest {
        val fixture = Fixture()
        val membership = randomMember(user = fixture.currentUser)
        fixture.withActiveChannel(CHANNEL_TYPE, CHANNEL_ID, membership)
        val groupedState = fixture.withGroupedQuery(groupKey = "all")
        val eventHandler = fixture.get()

        eventHandler.handleEvents(
            randomNotificationMarkReadEvent(cid = CID, channelType = CHANNEL_TYPE, channelId = CHANNEL_ID),
            randomChannelUpdatedEvent(
                cid = CID,
                channelType = CHANNEL_TYPE,
                channelId = CHANNEL_ID,
                channel = updatedChannel(),
            ),
            randomMemberUpdatedEvent(
                cid = CID,
                channelType = CHANNEL_TYPE,
                channelId = CHANNEL_ID,
                member = membership,
            ),
        )

        groupedState.rawChannels.orEmpty().keys `should contain` CID
    }

    private fun updatedChannel(): Channel = randomChannel(
        id = CHANNEL_ID,
        type = CHANNEL_TYPE,
        extraData = mapOf("group" to "ended"),
    )

    private class Fixture {
        val currentUser = randomUser()
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

        /** Watches the channel the way an open chat screen does, seeding the current user's membership. */
        fun withActiveChannel(channelType: String, channelId: String, membership: Member) {
            logicRegistry.channel(channelType, channelId)
            logicRegistry.channelState(channelType, channelId).updateChannelData(
                randomChannel(id = channelId, type = channelType, membership = membership),
            )
        }

        /** Registers a grouped query, installing its GroupAwareChatEventHandler, and returns its state. */
        fun withGroupedQuery(groupKey: String): QueryChannelsMutableState {
            val identifier = QueryChannelsIdentifier.Grouped(groupKey)
            logicRegistry.queryChannels(identifier)
            return stateRegistry.queryChannels(identifier).toMutableState()
        }

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
    }
}
