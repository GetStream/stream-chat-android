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

package io.getstream.chat.android.state.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.plugin.config.MessageLimitConfig
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.state.plugin.state.querychannels.internal.toMutableState
import io.getstream.chat.android.state.utils.internal.ChannelId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/**
 * An event refresh copies the channel states into the list. One that lands while [QueryChannelsStateLogic.addChannelsState]
 * is still updating them must not leave the list with an outdated member list.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class QueryChannelsStateLogicRefreshRaceTest {

    private val currentUser = randomUser(id = "current")
    private val otherUser = randomUser(id = "other")
    private val thirdUser = randomUser(id = "third")
    private val userFlow = MutableStateFlow<User?>(currentUser)

    /** Runs the channel state updates of addChannelsState only when advanced, so a refresh can land before them. */
    private val channelUpdatesScope = TestScope(StandardTestDispatcher())

    private val stateRegistry = StateRegistry(
        userStateFlow = userFlow,
        latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser, otherUser.id to otherUser)),
        activeLiveLocations = MutableStateFlow(emptyList<Location>()),
        job = Job(),
        now = { 0L },
        scope = channelUpdatesScope,
        messageLimitConfig = MessageLimitConfig(),
    )
    private val clientState: ClientState = mock { on { user } doReturn userFlow }
    private val logicRegistry = LogicRegistry(
        stateRegistry = stateRegistry,
        clientState = clientState,
        mutableGlobalState = MutableGlobalState(currentUser.id),
        userPresence = false,
        repos = mock<RepositoryFacade>(),
        client = mock<ChatClient>(),
        coroutineScope = channelUpdatesScope,
        now = { 0L },
    )
    private val listState = stateRegistry.queryChannels(QueryChannelsIdentifier.Grouped("all")).toMutableState()
    private val stateLogic = QueryChannelsStateLogic(listState, stateRegistry, logicRegistry, channelUpdatesScope)

    private val channel = randomChannel(
        type = "messaging",
        id = "dm",
        members = listOf(randomMember(user = currentUser), randomMember(user = otherUser)),
        memberCount = 2,
    )
    private val channelWithOnlyCurrentUser = channel.copy(members = listOf(randomMember(user = currentUser)))

    @AfterEach
    fun tearDown() {
        channelUpdatesScope.cancel()
    }

    @Test
    fun `a refresh during addChannelsState keeps the members when the channel state was empty`() = runTest {
        stateRegistry.mutableChannel(ChannelId.fromCid(channel.cid)!!)

        addChannelsWithRefreshInBetween(channel)

        assertEquals(listOf("current", "other"), listedMemberIds())
    }

    @Test
    fun `a refresh during addChannelsState keeps the members when the channel state had only the current user`() =
        runTest {
            fillChannelState(channelWithOnlyCurrentUser)

            addChannelsWithRefreshInBetween(channel)

            assertEquals(listOf("current", "other"), listedMemberIds())
        }

    @Test
    fun `a refresh during addChannelsState keeps the members when the list already held a partial copy`() = runTest {
        fillChannelState(channelWithOnlyCurrentUser)
        addChannels(channelWithOnlyCurrentUser)

        addChannelsWithRefreshInBetween(channel)

        assertEquals(listOf("current", "other"), listedMemberIds())
    }

    @Test
    fun `an event during addChannelsState reaches the list`() = runTest {
        val newMessage = randomMessage(id = "new", cid = channel.cid, user = otherUser, parentId = null)
        launchAddChannels(channel)
        // A message.new handled while the page's channel states are still being updated.
        logicRegistry.channel("messaging", "dm").stateLogic.upsertMessage(newMessage)
        stateLogic.refreshChannels(listOf(channel.cid))
        channelUpdatesScope.testScheduler.advanceUntilIdle()

        assertTrue(listState.rawChannels?.get(channel.cid)?.messages.orEmpty().any { it.id == "new" })
    }

    @Test
    fun `a channel removed during addChannelsState stays out of the list`() = runTest {
        launchAddChannels(channel)
        stateLogic.removeChannels(setOf(channel.cid))
        channelUpdatesScope.testScheduler.advanceUntilIdle()

        assertFalse(listState.rawChannels.orEmpty().containsKey(channel.cid))
    }

    @Test
    fun `a member who left before the page loaded is not listed`() = runTest {
        // Cached copy from before the third user left.
        val cached = channel.copy(members = channel.members + randomMember(user = thirdUser), memberCount = 3)
        fillChannelState(cached)
        addChannels(cached)

        addChannels(channel)

        assertEquals(listOf("current", "other"), listedMemberIds())
    }

    @Test
    fun `a refresh after a cancelled addChannelsState copies the updated channel state`() = runTest {
        launchAddChannels(channel).cancel()
        channelUpdatesScope.testScheduler.advanceUntilIdle()
        logicRegistry.channel("messaging", "dm").stateLogic.upsertMember(randomMember(user = thirdUser))

        stateLogic.refreshChannels(listOf(channel.cid))

        assertEquals(listOf("current", "other", "third"), listedMemberIds())
    }

    @Test
    fun `a refresh during overlapping addChannelsState calls is applied once both finish`() = runTest {
        stateRegistry.mutableChannel(ChannelId.fromCid(channel.cid)!!)
        launchAddChannels(channel)
        launchAddChannels(channel)

        stateLogic.refreshChannels(listOf(channel.cid))
        channelUpdatesScope.testScheduler.advanceUntilIdle()

        assertEquals(listOf("current", "other"), listedMemberIds())
    }

    @Test
    fun `a refresh outside addChannelsState is applied straight away`() = runTest {
        addChannels(channel)
        logicRegistry.channel("messaging", "dm").stateLogic.upsertMember(randomMember(user = thirdUser))

        stateLogic.refreshChannels(listOf(channel.cid))

        assertEquals(listOf("current", "other", "third"), listedMemberIds())
    }

    @Test
    fun `a member added during addChannelsState stays listed`() = runTest {
        launchAddChannels(channel)
        // A member.added handled while the page's channel states are still being updated.
        logicRegistry.channel("messaging", "dm").stateLogic.upsertMember(randomMember(user = thirdUser))
        stateLogic.refreshChannels(listOf(channel.cid))
        channelUpdatesScope.testScheduler.advanceUntilIdle()

        assertEquals(listOf("current", "other", "third"), listedMemberIds())
    }

    @Test
    fun `reloading an unchanged page keeps the listed copy`() = runTest {
        val page = channel.copy(hidden = false)
        addChannels(page)
        // The channel state now differs from the page in a field the page does not set on it.
        logicRegistry.channel("messaging", "dm").stateLogic.setHidden(true)

        addChannels(page)

        assertEquals(false, listState.rawChannels?.get(page.cid)?.hidden)
    }

    private fun fillChannelState(channel: Channel) {
        logicRegistry.channel("messaging", "dm").stateLogic.updateDataForChannel(channel = channel, messageLimit = 0)
        channelUpdatesScope.testScheduler.advanceUntilIdle()
    }

    /** Starts addChannelsState and leaves it waiting on the channel state updates. */
    private fun TestScope.launchAddChannels(channel: Channel): Job =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { stateLogic.addChannelsState(listOf(channel)) }

    private fun TestScope.addChannels(channel: Channel) {
        launchAddChannels(channel)
        channelUpdatesScope.testScheduler.advanceUntilIdle()
    }

    private fun TestScope.addChannelsWithRefreshInBetween(channel: Channel) {
        launchAddChannels(channel)
        stateLogic.refreshChannels(listOf(channel.cid))
        channelUpdatesScope.testScheduler.advanceUntilIdle()
    }

    private fun listedMemberIds() = listState.rawChannels?.get(channel.cid)?.members?.map { it.user.id }?.sorted()
}
