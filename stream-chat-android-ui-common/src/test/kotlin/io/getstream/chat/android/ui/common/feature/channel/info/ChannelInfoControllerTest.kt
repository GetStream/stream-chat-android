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
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.toChannelData
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
import io.getstream.result.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ChannelInfoControllerTest {

    @Test
    fun `initial state`() = runTest {
        val sut = Fixture().get(backgroundScope)

        sut.state.value.let { state ->
            assertEquals(ChannelInfoViewState.Content.Loading, state.content)
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
            .given(
                currentUser = currentUser,
                channel = channel,
            )
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
        val sut = Fixture()
            .given(
                currentUser = currentUser,
                channel = channel,
            )
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
        val sut = Fixture()
            .given(
                currentUser = currentUser,
                channel = channel,
            )
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
        val sut = Fixture()
            .given(
                currentUser = currentUser,
                channel = channel,
            )
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
                ),
                expandedState.content,
            )
        }
    }

    @Test
    fun `update name`() = runTest {
        val channel = Channel(name = "name")
        val fixture = Fixture().given(channel = channel)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(name = channel.name),
                actual.content,
            )

            val newName = "newName"
            fixture.givenUpdateName(newName)

            sut.updateName(newName)

            val updatedState = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(name = newName),
                updatedState.content,
            )
        }
    }

    @Test
    fun `update name error`() = runTest {
        val channel = Channel(name = "name")
        val error = Error.GenericError("Error updating channel name")
        val fixture = Fixture().given(channel = channel)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial and loaded state

            val newName = "newName"
            fixture.givenUpdateName(newName, error)

            sut.updateName(newName)
        }

        sut.events.test {
            val actual = awaitItem()
            assertEquals(
                ChannelInfoEvent.UpdateNameError(message = error.message),
                actual,
            )
        }
    }

    @Test
    fun `mute channel`() = runTest {
        val fixture = Fixture().given(isMuted = false)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(isMuted = false),
                actual.content,
            )

            fixture.givenMuteSuccess()

            sut.mute()

            val updatedState = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(isMuted = true),
                updatedState.content,
            )
        }
    }

    @Test
    fun `unmute channel`() = runTest {
        val fixture = Fixture().given(isMuted = true)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(isMuted = true),
                actual.content,
            )

            fixture.givenUnmuteSuccess()

            sut.unmute()

            val updatedState = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(isMuted = false),
                updatedState.content,
            )
        }
    }
}

private const val CID = "messaging:1"

private class Fixture {
    private val channelData = MutableStateFlow(ChannelData(type = "", id = ""))
    private val channelMembers = MutableStateFlow(emptyList<Member>())
    private val channelMuted = MutableStateFlow(false)
    private val channelState: ChannelState = mock {
        on { channelData } doReturn channelData
        on { members } doReturn channelMembers
        on { muted } doReturn channelMuted
    }
    private val channelClient: ChannelClient = mock()
    private val chatClient: ChatClient = mock()

    fun given(
        currentUser: User? = null,
        channel: Channel? = null,
        isMuted: Boolean? = null,
    ) = apply {
        if (currentUser != null) {
            whenever(chatClient.getCurrentOrStoredUserId()) doReturn currentUser.id
        }
        if (channel != null) {
            channelData.value = channel.toChannelData()
            channelMembers.value = channel.members
        }
        if (isMuted != null) {
            channelMuted.value = isMuted
        }
    }

    fun givenUpdateName(name: String, error: Error? = null) = apply {
        whenever(channelClient.updatePartial(mapOf("name" to name))) doAnswer {
            error?.asCall()
                ?: mock<Channel>().asCall().also {
                    channelData.update { channelData -> channelData.copy(name = name) }
                }
        }
    }

    fun givenMuteSuccess() = apply {
        whenever(channelClient.mute()) doAnswer {
            Unit.asCall().also {
                channelMuted.value = true
            }
        }
    }

    fun givenUnmuteSuccess() = apply {
        whenever(channelClient.unmute()) doAnswer {
            Unit.asCall().also {
                channelMuted.value = false
            }
        }
    }

    fun get(scope: CoroutineScope) = ChannelInfoController(
        cid = CID,
        scope = scope,
        chatClient = chatClient,
        channelState = MutableStateFlow(channelState),
        channelClient = channelClient,
    )
}
