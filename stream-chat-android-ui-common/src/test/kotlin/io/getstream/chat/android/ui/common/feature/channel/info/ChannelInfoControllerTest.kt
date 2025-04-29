package io.getstream.chat.android.ui.common.feature.channel.info

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
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
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
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
            assertEquals(ChannelInfoState.Content.Loading, state.content)
        }
    }

    @Test
    fun `direct channel content`() = runTest {
        val currentUser = User(id = "1")
        val otherUser = User(id = "2")
        val channel = Channel(
            createdBy = otherUser,
            members = listOf(
                Member(user = currentUser),
                Member(user = otherUser),
            ),
        )
        val sut = Fixture()
            .givenCurrentUser(currentUser)
            .givenChannel(channel)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val actual = awaitItem()
            assertEquals(
                ChannelInfoState.Content.Success(
                    expandedMembers = listOf(
                        ChannelInfoState.Member(
                            user = otherUser,
                            role = ChannelInfoState.Role.Owner,
                        ),
                    ),
                    collapsedMembers = emptyList(),
                    areMembersExpandable = false,
                    areMembersExpanded = false,
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
            )
        )
        val sut = Fixture()
            .givenCurrentUser(currentUser)
            .givenChannel(channel)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val actual = awaitItem()
            assertEquals(
                ChannelInfoState.Content.Success(
                    expandedMembers = channel.members
                        .filter { member -> member.user.id != currentUser.id }
                        .map { member ->
                            ChannelInfoState.Member(
                                user = member.user,
                                role = ChannelInfoState.Role.Other(""),
                            )
                        },
                    collapsedMembers = emptyList(),
                    areMembersExpandable = false,
                    areMembersExpanded = false,
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
                Member(user = currentUser)
        )
        val sut = Fixture()
            .givenCurrentUser(currentUser)
            .givenChannel(channel)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val actual = awaitItem()
            assertEquals(
                ChannelInfoState.Content.Success(
                    expandedMembers = channel.members
                        .filter { it.user.id != currentUser.id }
                        .take(5)
                        .map { member ->
                            ChannelInfoState.Member(
                                user = member.user,
                                role = ChannelInfoState.Role.Other(""),
                            )
                        },
                    collapsedMembers = channel.members
                        .filter { it.user.id != currentUser.id }
                        .takeLast(4)
                        .map { member ->
                            ChannelInfoState.Member(
                                user = member.user,
                                role = ChannelInfoState.Role.Other(""),
                            )
                        },
                    areMembersExpandable = true,
                    areMembersExpanded = false,
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
                Member(user = currentUser)
        )
        val sut = Fixture()
            .givenCurrentUser(currentUser)
            .givenChannel(channel)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state
            val expectedChannelMembers = channel.members.filter { it.user.id != currentUser.id }
            val actual = awaitItem()
            assertEquals(
                ChannelInfoState.Content.Success(
                    expandedMembers = expectedChannelMembers
                        .take(5)
                        .map { member ->
                            ChannelInfoState.Member(
                                user = member.user,
                                role = ChannelInfoState.Role.Other(""),
                            )
                        },
                    collapsedMembers = expectedChannelMembers
                        .takeLast(4)
                        .map { member ->
                            ChannelInfoState.Member(
                                user = member.user,
                                role = ChannelInfoState.Role.Other(""),
                            )
                        },
                    areMembersExpandable = true,
                    areMembersExpanded = false,
                ),
                actual.content,
            )

            sut.expandMembers()
            val expandedState = awaitItem()
            assertEquals(
                ChannelInfoState.Content.Success(
                    expandedMembers = expectedChannelMembers
                        .take(5)
                        .map { member ->
                            ChannelInfoState.Member(
                                user = member.user,
                                role = ChannelInfoState.Role.Other(""),
                            )
                        },
                    collapsedMembers = expectedChannelMembers
                        .takeLast(4)
                        .map { member ->
                            ChannelInfoState.Member(
                                user = member.user,
                                role = ChannelInfoState.Role.Other(""),
                            )
                        },
                    areMembersExpandable = true,
                    areMembersExpanded = true,
                ),
                expandedState.content,
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

    private val channelState: ChannelState = mock()
    private val clientState: ClientState = mock()
    private val stateRegistry: StateRegistry = mock {
        whenever(mock.channel(channelType, channelId)) doReturn channelState
    }
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
    }

    fun givenCurrentUser(user: User) = apply {
        whenever(clientState.user) doReturn MutableStateFlow(user)
        whenever(clientState.initializationState) doReturn MutableStateFlow(InitializationState.COMPLETE)
        whenever(chatClient.getCurrentOrStoredUserId()) doReturn user.id
        whenever(chatClient.awaitInitializationState(any())) doReturn InitializationState.COMPLETE
    }

    fun givenChannel(channel: Channel) = apply {
        whenever(channelState.channelData) doReturn MutableStateFlow(channel.toChannelData())
        whenever(channelState.members) doReturn MutableStateFlow(channel.members)
        whenever(chatClient.queryChannel(eq(channelType), eq(channelId), any(), any())) doReturn channel.asCall()
    }

    fun get(scope: CoroutineScope) = ChannelInfoController(
        scope = scope,
        cid = CID,
        chatClient = chatClient
    )
}
