/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.feature.channel.info

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.toChannelData
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
import io.getstream.result.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ChannelInfoControllerTest {

    @Test
    fun `initial state`() = runTest {
        val sut = Fixture()
            .get(backgroundScope)

        sut.state.value.let { state ->
            assertEquals(ChannelInfoViewState.Content.Loading, state.content)
        }
    }

    @Test
    fun `direct channel content`() = runTest {
        val currentUser = User(id = "1")
        val otherUser = User(id = "2")
        val channel = Channel(
            name = "name",
            createdBy = otherUser,
            members = listOf(
                Member(user = currentUser),
                Member(user = otherUser),
            ),
        )
        val isMuted = false
        val sut = Fixture()
            .givenCurrentUser(currentUser)
            .givenChannel(channel, isMuted)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    expandedMembers = listOf(
                        ChannelInfoViewState.Member(
                            user = otherUser,
                            role = ChannelInfoViewState.Role.Owner,
                        ),
                    ),
                    collapsedMembers = emptyList(),
                    areMembersExpandable = false,
                    areMembersExpanded = false,
                    name = channel.name,
                    isMuted = false,
                ),
                actual.content,
            )
        }
    }

    @Test
    fun `group channel content`() = runTest {
        val currentUser = User(id = "1")
        val channel = Channel(
            members = listOf(
                Member(user = currentUser),
                Member(user = User(id = "2")),
                Member(user = User(id = "3")),
                Member(user = User(id = "4")),
            ),
        )
        val isMuted = false
        val sut = Fixture()
            .givenCurrentUser(currentUser)
            .givenChannel(channel, isMuted)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    expandedMembers = channel.members
                        .filter { member -> member.user.id != currentUser.id }
                        .map { member ->
                            ChannelInfoViewState.Member(
                                user = member.user,
                                role = ChannelInfoViewState.Role.Other(""),
                            )
                        },
                    collapsedMembers = emptyList(),
                    areMembersExpandable = false,
                    areMembersExpanded = false,
                    name = channel.name,
                    isMuted = false,
                ),
                actual.content,
            )
        }
    }

    @Test
    fun `expandable group channel content`() = runTest {
        val currentUser = User(id = "1")
        val channel = Channel(
            members = (2..10).map { i -> Member(user = User(id = "$i")) } +
                Member(user = currentUser),
        )
        val isMuted = false
        val sut = Fixture()
            .givenCurrentUser(currentUser)
            .givenChannel(channel, isMuted)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    expandedMembers = channel.members
                        .filter { it.user.id != currentUser.id }
                        .take(5)
                        .map { member ->
                            ChannelInfoViewState.Member(
                                user = member.user,
                                role = ChannelInfoViewState.Role.Other(""),
                            )
                        },
                    collapsedMembers = channel.members
                        .filter { it.user.id != currentUser.id }
                        .takeLast(4)
                        .map { member ->
                            ChannelInfoViewState.Member(
                                user = member.user,
                                role = ChannelInfoViewState.Role.Other(""),
                            )
                        },
                    areMembersExpandable = true,
                    areMembersExpanded = false,
                    name = channel.name,
                    isMuted = isMuted,
                ),
                actual.content,
            )
        }
    }

    @Test
    fun `expand group channel content`() = runTest {
        val currentUser = User(id = "1")
        val channel = Channel(
            members = (2..10).map { i -> Member(user = User(id = "$i")) } +
                Member(user = currentUser),
        )
        val isMuted = false
        val sut = Fixture()
            .givenCurrentUser(currentUser)
            .givenChannel(channel, isMuted)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val expectedChannelMembers = channel.members.filter { it.user.id != currentUser.id }
            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    expandedMembers = expectedChannelMembers
                        .take(5)
                        .map { member ->
                            ChannelInfoViewState.Member(
                                user = member.user,
                                role = ChannelInfoViewState.Role.Other(""),
                            )
                        },
                    collapsedMembers = expectedChannelMembers
                        .takeLast(4)
                        .map { member ->
                            ChannelInfoViewState.Member(
                                user = member.user,
                                role = ChannelInfoViewState.Role.Other(""),
                            )
                        },
                    areMembersExpandable = true,
                    areMembersExpanded = false,
                    name = channel.name,
                    isMuted = isMuted,
                ),
                actual.content,
            )

            sut.expandMembers()

            val expandedState = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    expandedMembers = expectedChannelMembers
                        .take(5)
                        .map { member ->
                            ChannelInfoViewState.Member(
                                user = member.user,
                                role = ChannelInfoViewState.Role.Other(""),
                            )
                        },
                    collapsedMembers = expectedChannelMembers
                        .takeLast(4)
                        .map { member ->
                            ChannelInfoViewState.Member(
                                user = member.user,
                                role = ChannelInfoViewState.Role.Other(""),
                            )
                        },
                    areMembersExpandable = true,
                    areMembersExpanded = true,
                    name = channel.name,
                    isMuted = isMuted,
                ),
                expandedState.content,
            )
        }
    }

    @Test
    fun `update name`() = runTest {
        val currentUser = User(id = "1")
        val channel = Channel(
            name = "name",
            members = listOf(
                Member(user = currentUser),
            ),
        )
        val isMuted = false
        val fixture = Fixture()
        val sut = fixture
            .givenCurrentUser(currentUser)
            .givenChannel(channel, isMuted)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    expandedMembers = emptyList(),
                    collapsedMembers = emptyList(),
                    areMembersExpandable = false,
                    areMembersExpanded = false,
                    name = channel.name,
                    isMuted = isMuted,
                ),
                actual.content,
            )

            fixture.givenNewName("newName", channel)

            sut.updateName("newName")

            val updatedState = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    expandedMembers = emptyList(),
                    collapsedMembers = emptyList(),
                    areMembersExpandable = false,
                    areMembersExpanded = false,
                    name = "newName",
                    isMuted = isMuted,
                ),
                updatedState.content,
            )
        }
    }

    @Test
    fun `update name error`() = runTest {
        val currentUser = User(id = "1")
        val channel = Channel(
            name = "name",
            members = listOf(
                Member(user = currentUser),
            ),
        )
        val error = Error.GenericError("Error updating channel name")
        val isMuted = false
        val fixture = Fixture()
        val sut = fixture
            .givenCurrentUser(currentUser)
            .givenChannel(channel, isMuted)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    expandedMembers = emptyList(),
                    collapsedMembers = emptyList(),
                    areMembersExpandable = false,
                    areMembersExpanded = false,
                    name = channel.name,
                    isMuted = isMuted,
                ),
                actual.content,
            )

            fixture.givenNewNameError("newName", error)

            sut.updateName("newName")
        }

        sut.events.test {
            val actual = awaitItem()
            assertEquals(
                ChannelInfoEvent.UpdateNameError(message = error.message),
                actual,
            )
        }
    }
}

private const val CHANNEL_TYPE = "messaging"
private const val CHANNEL_ID = "1"
private const val CID = "$CHANNEL_TYPE:$CHANNEL_ID"

private class Fixture {
    private val channelType: String = CHANNEL_TYPE
    private val channelId: String = CHANNEL_ID

    private val channelFlow = MutableStateFlow(Channel().toChannelData())
    private val channelState: ChannelState = mock {
        whenever(mock.channelData) doReturn channelFlow
    }
    private val clientState: ClientState = mock()
    private val stateRegistry: StateRegistry = mock {
        whenever(mock.channel(channelType, channelId)) doReturn channelState
    }
    private val channelClient: ChannelClient = mock()
    private val chatClient: ChatClient = mock {
        val statePlugin: StatePlugin = mock {
            whenever(mock.resolveDependency(StateRegistry::class)) doReturn stateRegistry
        }
        whenever(mock.clientState) doReturn clientState
        whenever(mock.plugins) doReturn listOf(statePlugin)
        val statePluginFactory: StreamStatePluginFactory = mock {
            whenever(mock.resolveDependency(StatePluginConfig::class)) doReturn StatePluginConfig()
        }
        whenever(mock.pluginFactories) doReturn listOf(statePluginFactory)
        whenever(mock.channel(CID)) doReturn channelClient
    }

    fun givenCurrentUser(user: User) = apply {
        whenever(clientState.user) doReturn MutableStateFlow(user)
        whenever(clientState.initializationState) doReturn MutableStateFlow(InitializationState.COMPLETE)
        whenever(chatClient.getCurrentOrStoredUserId()) doReturn user.id
        whenever(chatClient.awaitInitializationState(any())) doReturn InitializationState.COMPLETE
    }

    fun givenChannel(channel: Channel, isMuted: Boolean) = apply {
        channelFlow.value = channel.toChannelData()
        whenever(channelState.members) doReturn MutableStateFlow(channel.members)
        whenever(channelState.muted) doReturn MutableStateFlow(isMuted)
        whenever(chatClient.queryChannel(eq(channelType), eq(channelId), any(), any())) doReturn channel.asCall()
    }

    fun givenNewName(name: String, channel: Channel) = apply {
        val updatedChannel = channel.copy(name = name)
        whenever(channelClient.updatePartial(mapOf("name" to name))) doAnswer {
            channelFlow.value = updatedChannel.toChannelData()
            updatedChannel.asCall()
        }
    }

    fun givenNewNameError(name: String, error: Error) = apply {
        whenever(channelClient.updatePartial(mapOf("name" to name))) doReturn error.asCall()
    }

    fun get(scope: CoroutineScope) = ChannelInfoController(
        cid = CID,
        scope = scope,
        chatClient = chatClient,
    )
}
