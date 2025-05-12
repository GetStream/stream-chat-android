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

@file:OptIn(ExperimentalStreamChatApi::class)

package io.getstream.chat.android.ui.common.feature.channel.info

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.toChannelData
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
import io.getstream.chat.android.ui.common.utils.ExpandableList
import io.getstream.result.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@Suppress("LargeClass")
internal class ChannelInfoViewControllerTest {

    @Test
    fun `initial state`() = runTest {
        val sut = Fixture().get(backgroundScope)

        assertEquals(ChannelInfoViewState.Loading, sut.state.value)
    }

    @Test
    fun `single-member channel content`() = runTest {
        val currentUser = User(id = "1")
        val channel = Channel(
            id = "!members-1",
            createdBy = currentUser,
            members = listOf(Member(user = currentUser)),
        )
        val sut = Fixture()
            .given(
                currentUser = currentUser,
                channel = channel,
            )
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = ExpandableList(
                        items = listOf(
                            ChannelInfoViewState.Content.Member(
                                user = currentUser,
                                role = ChannelInfoViewState.Content.Role.Owner,
                            ),
                        ),
                        minimumVisibleItems = 5,
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `direct channel content`() = runTest {
        val currentUser = User(id = "1")
        val otherUser = User(id = "2")
        val channel = Channel(
            id = "!members-1,2",
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

            assertEquals(
                ChannelInfoViewState.Content(
                    members = ExpandableList(
                        items = listOf(
                            ChannelInfoViewState.Content.Member(
                                user = otherUser,
                                role = ChannelInfoViewState.Content.Role.Owner,
                            ),
                        ),
                        minimumVisibleItems = 5,
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `group channel content`() = runTest {
        val owner = User(id = "1")
        val user2 = User(id = "2")
        val user3 = User(id = "3")
        val user4 = User(id = "4")
        val channel = Channel(
            createdBy = owner,
            members = listOf(
                Member(user = owner),
                Member(user = user2, channelRole = "channel_moderator"),
                Member(user = user3, channelRole = "channel_member"),
                Member(user = user4, channelRole = "admin"),
            ),
        )
        val sut = Fixture()
            .given(channel = channel)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = ExpandableList(
                        items = listOf(
                            ChannelInfoViewState.Content.Member(
                                user = owner,
                                role = ChannelInfoViewState.Content.Role.Owner,
                            ),
                            ChannelInfoViewState.Content.Member(
                                user = user2,
                                role = ChannelInfoViewState.Content.Role.Moderator,
                            ),
                            ChannelInfoViewState.Content.Member(
                                user = user3,
                                role = ChannelInfoViewState.Content.Role.Member,
                            ),
                            ChannelInfoViewState.Content.Member(
                                user = user4,
                                role = ChannelInfoViewState.Content.Role.Other("admin"),
                            ),
                        ),
                        minimumVisibleItems = 5,
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `expandable group channel content`() = runTest {
        val channel = Channel(
            members = (1..10).map { i -> Member(user = User(id = "$i")) },
        )
        val sut = Fixture()
            .given(channel = channel)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = ExpandableList(
                        items = channel.members
                            .map { member ->
                                ChannelInfoViewState.Content.Member(
                                    user = member.user,
                                    role = ChannelInfoViewState.Content.Role.Other(""),
                                )
                            },
                        minimumVisibleItems = 5,
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `expand and collapse group channel content`() = runTest {
        val channel = Channel(members = (1..10).map { i -> Member(user = User(id = "$i")) })
        val sut = Fixture()
            .given(channel = channel)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = ExpandableList(
                        items = channel.members
                            .map { member ->
                                ChannelInfoViewState.Content.Member(
                                    user = member.user,
                                    role = ChannelInfoViewState.Content.Role.Other(""),
                                )
                            },
                        minimumVisibleItems = 5,
                    ),
                ),
                awaitItem(),
            )

            sut.onViewAction(ChannelInfoViewAction.ExpandMembersClick)

            assertEquals(
                ChannelInfoViewState.Content(
                    members = ExpandableList(
                        items = channel.members
                            .map { member ->
                                ChannelInfoViewState.Content.Member(
                                    user = member.user,
                                    role = ChannelInfoViewState.Content.Role.Other(""),
                                )
                            },
                        minimumVisibleItems = 5,
                        isCollapsed = false,
                    ),
                ),
                awaitItem(),
            )

            sut.onViewAction(ChannelInfoViewAction.CollapseMembersClick)

            assertEquals(
                ChannelInfoViewState.Content(
                    members = ExpandableList(
                        items = channel.members
                            .map { member ->
                                ChannelInfoViewState.Content.Member(
                                    user = member.user,
                                    role = ChannelInfoViewState.Content.Role.Other(""),
                                )
                            },
                        minimumVisibleItems = 5,
                        isCollapsed = true,
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun capabilities() = runTest {
        val channel = Channel(
            ownCapabilities = emptySet(),
        )
        val fixture = Fixture().given(channel = channel)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    capability = ChannelInfoViewState.Content.Capability(
                        canAddMembers = false,
                        canRemoveMembers = false,
                        canBanMembers = false,
                        canRenameChannel = false,
                        canMuteChannel = false,
                        canLeaveChannel = false,
                        canDeleteChannel = false,
                    ),
                ),
                awaitItem(),
            )

            fixture.given(
                channel = Channel(
                    ownCapabilities = setOf(
                        ChannelCapabilities.UPDATE_CHANNEL_MEMBERS,
                        ChannelCapabilities.UPDATE_CHANNEL,
                        ChannelCapabilities.BAN_CHANNEL_MEMBERS,
                        ChannelCapabilities.MUTE_CHANNEL,
                        ChannelCapabilities.LEAVE_CHANNEL,
                        ChannelCapabilities.DELETE_CHANNEL,
                    ),
                ),
            )

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    capability = ChannelInfoViewState.Content.Capability(
                        canAddMembers = true,
                        canRemoveMembers = true,
                        canBanMembers = true,
                        canRenameChannel = true,
                        canMuteChannel = true,
                        canLeaveChannel = true,
                        canDeleteChannel = true,
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `rename channel permission error`() = runTest {
        val fixture = Fixture().given(channel = Channel())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            sut.events.test {
                sut.onViewAction(ChannelInfoViewAction.RenameChannelClick(name = "newName"))

                assertEquals(ChannelInfoViewEvent.RenameChannelError, awaitItem())
            }
        }

        launch { fixture.verifyNoMoreInteractions() }
    }

    @Test
    fun `rename channel`() = runTest {
        val channel = Channel(
            name = "name",
            ownCapabilities = setOf(ChannelCapabilities.UPDATE_CHANNEL),
        )

        val fixture = Fixture().given(channel = channel)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    name = channel.name,
                    capability = ChannelInfoViewState.Content.Capability(canRenameChannel = true),
                ),
                awaitItem(),
            )

            val newName = "newName"
            fixture.givenRenameChannel(newName)

            sut.onViewAction(ChannelInfoViewAction.RenameChannelClick(newName))

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    name = newName,
                    capability = ChannelInfoViewState.Content.Capability(canRenameChannel = true),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `rename channel error`() = runTest {
        val channel = Channel(ownCapabilities = setOf(ChannelCapabilities.UPDATE_CHANNEL))
        val fixture = Fixture().given(channel = channel)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial and loaded state

            val newName = "newName"
            fixture.givenRenameChannel(
                name = newName,
                error = Error.GenericError("Error updating channel name"),
            )

            sut.onViewAction(ChannelInfoViewAction.RenameChannelClick(newName))

            sut.events.test {
                assertEquals(ChannelInfoViewEvent.RenameChannelError, awaitItem())
            }
        }
    }

    @Test
    fun `mute channel permission error`() = runTest {
        val fixture = Fixture().given(channel = Channel())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            sut.events.test {
                sut.onViewAction(ChannelInfoViewAction.MuteChannelClick)

                assertEquals(ChannelInfoViewEvent.MuteChannelError, awaitItem())
            }
        }

        launch { fixture.verifyNoMoreInteractions() }
    }

    @Test
    fun `mute channel`() = runTest {
        val channel = Channel(ownCapabilities = setOf(ChannelCapabilities.MUTE_CHANNEL))
        val fixture = Fixture().given(channel = channel, isMuted = false)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    isMuted = false,
                    capability = ChannelInfoViewState.Content.Capability(canMuteChannel = true),
                ),
                awaitItem(),
            )

            fixture.givenMuteChannel()

            sut.onViewAction(ChannelInfoViewAction.MuteChannelClick)

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    isMuted = true,
                    capability = ChannelInfoViewState.Content.Capability(canMuteChannel = true),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `mute channel error`() = runTest {
        val channel = Channel(ownCapabilities = setOf(ChannelCapabilities.MUTE_CHANNEL))
        val fixture = Fixture().given(channel = channel, isMuted = false)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    isMuted = false,
                    capability = ChannelInfoViewState.Content.Capability(canMuteChannel = true),
                ),
                awaitItem(),
            )

            fixture.givenMuteChannel(error = Error.GenericError("Error muting channel"))

            sut.onViewAction(ChannelInfoViewAction.MuteChannelClick)

            sut.events.test {
                assertEquals(ChannelInfoViewEvent.MuteChannelError, awaitItem())
            }
        }
    }

    @Test
    fun `unmute channel`() = runTest {
        val channel = Channel(ownCapabilities = setOf(ChannelCapabilities.MUTE_CHANNEL))
        val fixture = Fixture().given(channel = channel, isMuted = true)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    isMuted = true,
                    capability = ChannelInfoViewState.Content.Capability(canMuteChannel = true),
                ),
                awaitItem(),
            )

            fixture.givenUnmuteChannel()

            sut.onViewAction(ChannelInfoViewAction.UnmuteChannelClick)

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    isMuted = false,
                    capability = ChannelInfoViewState.Content.Capability(canMuteChannel = true),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `unmute channel error`() = runTest {
        val channel = Channel(ownCapabilities = setOf(ChannelCapabilities.MUTE_CHANNEL))
        val fixture = Fixture().given(channel = channel, isMuted = true)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    isMuted = true,
                    capability = ChannelInfoViewState.Content.Capability(canMuteChannel = true),
                ),
                awaitItem(),
            )

            fixture.givenUnmuteChannel(error = Error.GenericError("Error unmuting channel"))

            sut.onViewAction(ChannelInfoViewAction.UnmuteChannelClick)

            sut.events.test {
                assertEquals(ChannelInfoViewEvent.UnmuteChannelError, awaitItem())
            }
        }
    }

    @Test
    fun `hide channel`() = runTest {
        val fixture = Fixture().given(isHidden = false)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    isHidden = false,
                ),
                awaitItem(),
            )

            val clearHistory = true
            fixture.givenHideChannel(clearHistory)

            sut.onViewAction(ChannelInfoViewAction.HideChannelClick(clearHistory = clearHistory))

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    isHidden = true,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `hide channel error`() = runTest {
        val fixture = Fixture().given(isHidden = false)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    isHidden = false,
                ),
                awaitItem(),
            )

            val clearHistory = true
            fixture.givenHideChannel(
                clearHistory = clearHistory,
                error = Error.GenericError("Error hiding channel"),
            )

            sut.onViewAction(ChannelInfoViewAction.HideChannelClick(clearHistory = clearHistory))

            sut.events.test {
                assertEquals(ChannelInfoViewEvent.HideChannelError, awaitItem())
            }
        }
    }

    @Test
    fun `unhide channel`() = runTest {
        val fixture = Fixture().given(isHidden = true)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    isHidden = true,
                ),
                awaitItem(),
            )

            fixture.givenUnhideChannel()

            sut.onViewAction(ChannelInfoViewAction.UnhideChannelClick)

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    isHidden = false,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `unhide channel error`() = runTest {
        val fixture = Fixture().given(isHidden = true)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    isHidden = true,
                ),
                awaitItem(),
            )

            fixture.givenUnhideChannel(error = Error.GenericError("Error unhiding channel"))

            sut.onViewAction(ChannelInfoViewAction.UnhideChannelClick)

            sut.events.test {
                assertEquals(ChannelInfoViewEvent.UnhideChannelError, awaitItem())
            }
        }
    }

    @Test
    fun `leave channel permission error`() = runTest {
        val fixture = Fixture().given(channel = Channel())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            sut.events.test {
                sut.onViewAction(ChannelInfoViewAction.LeaveChannelClick(quitMessage = null))

                assertEquals(ChannelInfoViewEvent.LeaveChannelError, awaitItem())
            }
        }

        launch { fixture.verifyNoMoreInteractions() }
    }

    @Test
    fun `leave channel not connected user error`() = runTest {
        val fixture = Fixture().given(channel = Channel(ownCapabilities = setOf(ChannelCapabilities.LEAVE_CHANNEL)))
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            sut.events.test {
                sut.onViewAction(ChannelInfoViewAction.LeaveChannelClick(quitMessage = null))

                assertEquals(ChannelInfoViewEvent.LeaveChannelError, awaitItem())
            }
        }

        launch { fixture.verifyNoMoreInteractions() }
    }

    @Test
    fun `leave channel`() = runTest {
        val currentUser = User(id = "1")
        val fixture = Fixture()
            .given(
                currentUser = currentUser,
                channel = Channel(
                    id = "!members-1,2",
                    ownCapabilities = setOf(ChannelCapabilities.LEAVE_CHANNEL),
                ),
            )
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    capability = ChannelInfoViewState.Content.Capability(canLeaveChannel = true),
                ),
                awaitItem(),
            )

            val quitMessage = Message(text = "${currentUser.id} left")
            fixture.givenLeaveChannel(quitMessage)

            sut.onViewAction(ChannelInfoViewAction.LeaveChannelClick(quitMessage))

            sut.events.test {
                assertEquals(
                    ChannelInfoViewEvent.LeaveChannelSuccess,
                    awaitItem(),
                )
            }
        }
    }

    @Test
    fun `leave channel error`() = runTest {
        val currentUser = User(id = "1")
        val fixture = Fixture()
            .given(
                currentUser = currentUser,
                channel = Channel(
                    id = "!members-1,2",
                    ownCapabilities = setOf(ChannelCapabilities.LEAVE_CHANNEL),
                ),
            )
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    capability = ChannelInfoViewState.Content.Capability(canLeaveChannel = true),
                ),
                awaitItem(),
            )

            val quitMessage = Message()
            fixture.givenLeaveChannel(
                quitMessage = quitMessage,
                error = Error.GenericError("Error leaving channel"),
            )

            sut.onViewAction(ChannelInfoViewAction.LeaveChannelClick(quitMessage))

            sut.events.test {
                assertEquals(ChannelInfoViewEvent.LeaveChannelError, awaitItem())
            }
        }
    }

    @Test
    fun `delete channel permission error`() = runTest {
        val fixture = Fixture().given(channel = Channel())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            sut.events.test {
                sut.onViewAction(ChannelInfoViewAction.DeleteChannelClick)

                assertEquals(ChannelInfoViewEvent.DeleteChannelError, awaitItem())
            }
        }

        launch { fixture.verifyNoMoreInteractions() }
    }

    @Test
    fun `delete channel`() = runTest {
        val channel = Channel(ownCapabilities = setOf(ChannelCapabilities.DELETE_CHANNEL))
        val fixture = Fixture().given(channel = channel)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    capability = ChannelInfoViewState.Content.Capability(canDeleteChannel = true),
                ),
                awaitItem(),
            )

            fixture.givenDeleteChannel()

            sut.onViewAction(ChannelInfoViewAction.DeleteChannelClick)

            sut.events.test {
                assertEquals(ChannelInfoViewEvent.DeleteChannelSuccess, awaitItem())
            }
        }
    }

    @Test
    fun `delete channel error`() = runTest {
        val channel = Channel(ownCapabilities = setOf(ChannelCapabilities.DELETE_CHANNEL))
        val fixture = Fixture().given(channel = channel)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    capability = ChannelInfoViewState.Content.Capability(canDeleteChannel = true),
                ),
                awaitItem(),
            )

            fixture.givenDeleteChannel(error = Error.GenericError("Error deleting channel"))

            sut.onViewAction(ChannelInfoViewAction.DeleteChannelClick)

            sut.events.test {
                assertEquals(ChannelInfoViewEvent.DeleteChannelError, awaitItem())
            }
        }
    }
}

private const val CID = "messaging:1"

private class Fixture {
    private val channelData = MutableStateFlow(ChannelData(type = "", id = ""))
    private val channelMembers = MutableStateFlow(emptyList<Member>())
    private val channelMuted = MutableStateFlow(false)
    private val channelHidden = MutableStateFlow(false)
    private val channelState: ChannelState = mock {
        on { channelData } doReturn channelData
        on { members } doReturn channelMembers
        on { muted } doReturn channelMuted
        on { hidden } doReturn channelHidden
    }
    private val channelClient: ChannelClient = mock()
    private val chatClient: ChatClient = mock()

    fun given(
        currentUser: User? = null,
        channel: Channel? = null,
        isMuted: Boolean? = null,
        isHidden: Boolean? = null,
    ) = apply {
        if (currentUser != null) {
            whenever(chatClient.getCurrentUser()) doReturn currentUser
        }
        if (channel != null) {
            channelData.value = channel.toChannelData()
            channelMembers.value = channel.members
        }
        if (isMuted != null) {
            channelMuted.value = isMuted
        }
        if (isHidden != null) {
            channelHidden.value = isHidden
        }
    }

    fun givenRenameChannel(name: String, error: Error? = null) = apply {
        whenever(channelClient.updatePartial(mapOf("name" to name))) doAnswer {
            error?.asCall()
                ?: mock<Channel>().asCall().also {
                    channelData.update { channelData -> channelData.copy(name = name) }
                }
        }
    }

    fun givenMuteChannel(error: Error? = null) = apply {
        whenever(channelClient.mute()) doAnswer {
            error?.asCall()
                ?: Unit.asCall().also {
                    channelMuted.value = true
                }
        }
    }

    fun givenUnmuteChannel(error: Error? = null) = apply {
        whenever(channelClient.unmute()) doAnswer {
            error?.asCall()
                ?: Unit.asCall().also {
                    channelMuted.value = false
                }
        }
    }

    fun givenHideChannel(clearHistory: Boolean, error: Error? = null) = apply {
        whenever(channelClient.hide(clearHistory)) doAnswer {
            error?.asCall()
                ?: Unit.asCall().also {
                    channelHidden.value = true
                }
        }
    }

    fun givenUnhideChannel(error: Error? = null) = apply {
        whenever(channelClient.show()) doAnswer {
            error?.asCall()
                ?: Unit.asCall().also {
                    channelHidden.value = false
                }
        }
    }

    fun givenLeaveChannel(quitMessage: Message, error: Error? = null) = apply {
        whenever(
            channelClient.removeMembers(
                memberIds = listOf(requireNotNull(chatClient.getCurrentUser()?.id)),
                systemMessage = quitMessage,
            ),
        ) doAnswer {
            error?.asCall()
                ?: mock<Channel>().asCall()
        }
    }

    fun givenDeleteChannel(error: Error? = null) = apply {
        whenever(channelClient.delete()) doAnswer {
            error?.asCall()
                ?: mock<Channel>().asCall()
        }
    }

    fun verifyNoMoreInteractions() = apply {
        verifyNoMoreInteractions(channelClient)
    }

    fun get(scope: CoroutineScope) = ChannelInfoViewController(
        cid = CID,
        scope = scope,
        chatClient = chatClient,
        channelState = MutableStateFlow(channelState),
        channelClient = channelClient,
    )
}

private fun <T> emptyMembers() = ExpandableList<T>(
    items = emptyList(),
    minimumVisibleItems = 5,
)
