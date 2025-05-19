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

import android.content.ClipData
import android.content.ClipboardManager
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
import io.getstream.chat.android.models.UserId
import io.getstream.chat.android.models.toChannelData
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent.Navigation
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
import org.mockito.kotlin.verify
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
        val currentUser = User(id = "1", name = "username")
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
                    options = listOf(
                        ChannelInfoViewState.Content.Option.UserInfo(username = currentUser.name),
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `direct channel content`() = runTest {
        val currentUser = User(id = "1")
        val otherUser = User(id = "2", name = "username")
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
                    options = listOf(
                        ChannelInfoViewState.Content.Option.UserInfo(username = otherUser.name),
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
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
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
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
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Suppress("LongMethod")
    @Test
    fun `expand and collapse group channel content`() = runTest {
        val channel = Channel(members = (1..10).map { i -> Member(user = User(id = "$i")) })
        val options = listOf(
            ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
            ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
            ChannelInfoViewState.Content.Option.PinnedMessages,
            ChannelInfoViewState.Content.Option.Separator,
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
                    options = options,
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
                    options = options,
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
                    options = options,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `channel options`() = runTest {
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
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
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
                    options = listOf(
                        ChannelInfoViewState.Content.Option.AddMember,
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = false),
                        ChannelInfoViewState.Content.Option.MuteChannel(isMuted = false),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                        ChannelInfoViewState.Content.Option.LeaveChannel,
                        ChannelInfoViewState.Content.Option.DeleteChannel,
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `copy user id to clipboard`() = runTest {
        val userId = "userId"
        val fixture = Fixture()
        val sut = fixture.get(backgroundScope)

        sut.onViewAction(ChannelInfoViewAction.CopyUserHandleClick(userId))

        fixture.verifyCopiedToClipboard(userId)
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
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = channel.name, isReadOnly = false),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )

            val newName = "newName"
            fixture.givenRenameChannel(newName)

            sut.onViewAction(ChannelInfoViewAction.RenameChannelClick(newName))

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = newName, isReadOnly = false),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
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
    fun `pinned messages click`() = runTest {
        val fixture = Fixture().given(channel = Channel())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            sut.events.test {
                sut.onViewAction(ChannelInfoViewAction.PinnedMessagesClick)

                assertEquals(ChannelInfoViewEvent.NavigateToPinnedMessages, awaitItem())
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
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.MuteChannel(isMuted = false),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )

            fixture.givenMuteChannel()

            sut.onViewAction(ChannelInfoViewAction.MuteChannelClick)

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.MuteChannel(isMuted = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
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
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.MuteChannel(isMuted = false),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
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
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.MuteChannel(isMuted = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )

            fixture.givenUnmuteChannel()

            sut.onViewAction(ChannelInfoViewAction.UnmuteChannelClick)

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.MuteChannel(isMuted = false),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
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
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.MuteChannel(isMuted = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
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
    fun `hide channel click`() = runTest {
        val fixture = Fixture().given(isHidden = false)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            sut.events.test {
                sut.onViewAction(ChannelInfoViewAction.HideChannelClick)

                assertEquals(ChannelInfoViewEvent.HideChannelModal, awaitItem())
            }
        }

        launch { fixture.verifyNoMoreInteractions() }
    }

    @Test
    fun `hide channel success`() = runTest {
        val fixture = Fixture().given(isHidden = false)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )

            val clearHistory = true
            fixture.givenHideChannel(clearHistory)

            sut.onViewAction(ChannelInfoViewAction.HideChannelConfirmationClick(clearHistory = clearHistory))

            sut.events.test {
                assertEquals(
                    ChannelInfoViewEvent.NavigateUp(reason = Navigation.Reason.HideChannelSuccess),
                    awaitItem(),
                )
            }

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = true),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
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
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )

            val clearHistory = true
            fixture.givenHideChannel(
                clearHistory = clearHistory,
                error = Error.GenericError("Error hiding channel"),
            )

            sut.onViewAction(ChannelInfoViewAction.HideChannelConfirmationClick(clearHistory = clearHistory))

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
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = true),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )

            fixture.givenUnhideChannel()

            sut.onViewAction(ChannelInfoViewAction.UnhideChannelClick)

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
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
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = true),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
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
    fun `leave channel click`() = runTest {
        val fixture = Fixture().given(channel = Channel())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            sut.events.test {
                sut.onViewAction(ChannelInfoViewAction.LeaveChannelClick)

                assertEquals(ChannelInfoViewEvent.LeaveChannelModal, awaitItem())
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
                sut.onViewAction(ChannelInfoViewAction.LeaveChannelConfirmationClick(quitMessage = null))

                assertEquals(ChannelInfoViewEvent.LeaveChannelError, awaitItem())
            }
        }

        launch { fixture.verifyNoMoreInteractions() }
    }

    @Test
    fun `leave channel success`() = runTest {
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
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                        ChannelInfoViewState.Content.Option.LeaveChannel,
                    ),
                ),
                awaitItem(),
            )

            val quitMessage = Message(text = "${currentUser.id} left")
            fixture.givenLeaveChannel(quitMessage)

            sut.onViewAction(ChannelInfoViewAction.LeaveChannelConfirmationClick(quitMessage))

            sut.events.test {
                assertEquals(
                    ChannelInfoViewEvent.NavigateUp(reason = Navigation.Reason.LeaveChannelSuccess),
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
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                        ChannelInfoViewState.Content.Option.LeaveChannel,
                    ),
                ),
                awaitItem(),
            )

            val quitMessage = Message()
            fixture.givenLeaveChannel(
                quitMessage = quitMessage,
                error = Error.GenericError("Error leaving channel"),
            )

            sut.onViewAction(ChannelInfoViewAction.LeaveChannelConfirmationClick(quitMessage))

            sut.events.test {
                assertEquals(ChannelInfoViewEvent.LeaveChannelError, awaitItem())
            }
        }
    }

    @Test
    fun `delete channel click`() = runTest {
        val fixture = Fixture().given(channel = Channel())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            sut.events.test {
                sut.onViewAction(ChannelInfoViewAction.DeleteChannelClick)

                assertEquals(ChannelInfoViewEvent.DeleteChannelModal, awaitItem())
            }
        }

        launch { fixture.verifyNoMoreInteractions() }
    }

    @Test
    fun `delete channel success`() = runTest {
        val channel = Channel(ownCapabilities = setOf(ChannelCapabilities.DELETE_CHANNEL))
        val fixture = Fixture().given(channel = channel)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                        ChannelInfoViewState.Content.Option.DeleteChannel,
                    ),
                ),
                awaitItem(),
            )

            fixture.givenDeleteChannel()

            sut.onViewAction(ChannelInfoViewAction.DeleteChannelConfirmationClick)

            sut.events.test {
                assertEquals(
                    ChannelInfoViewEvent.NavigateUp(reason = Navigation.Reason.DeleteChannelSuccess),
                    awaitItem(),
                )
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
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = "", isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.Separator,
                        ChannelInfoViewState.Content.Option.DeleteChannel,
                    ),
                ),
                awaitItem(),
            )

            fixture.givenDeleteChannel(error = Error.GenericError("Error deleting channel"))

            sut.onViewAction(ChannelInfoViewAction.DeleteChannelConfirmationClick)

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
    private val clipboardManager: ClipboardManager = mock()

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

    fun verifyCopiedToClipboard(userId: UserId) = apply {
        verify(clipboardManager).setPrimaryClip(ClipData.newPlainText("User ID", userId))
    }

    fun get(scope: CoroutineScope) = ChannelInfoViewController(
        context = mock(),
        cid = CID,
        scope = scope,
        chatClient = chatClient,
        channelState = MutableStateFlow(channelState),
        channelClient = channelClient,
        clipboardManager = clipboardManager,
    )
}

private fun <T> emptyMembers() = ExpandableList<T>(
    items = emptyList(),
    minimumVisibleItems = 5,
)
