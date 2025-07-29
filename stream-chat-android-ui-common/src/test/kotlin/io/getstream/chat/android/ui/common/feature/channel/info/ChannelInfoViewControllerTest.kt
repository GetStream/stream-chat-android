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
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.toChannelData
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomGenericError
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMembers
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent.Navigation
import io.getstream.chat.android.ui.common.helper.CopyToClipboardHandler
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
        val currentMember = randomMember()
        val channel = randomChannel(
            createdBy = currentMember.user,
            members = listOf(currentMember),
            ownCapabilities = emptySet(),
        )
        val sut = Fixture()
            .given(
                currentUser = currentMember.user,
                channel = channel,
            )
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = currentMember.user,
                    members = ExpandableList(
                        items = channel.members,
                        minimumVisibleItems = 5,
                    ),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.UserInfo(user = currentMember.user),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `direct channel content`() = runTest {
        val currentMember = randomMember()
        val otherMember = randomMember()
        val channel = randomChannel(
            id = "!members-${currentMember.getUserId()},${otherMember.getUserId()}",
            createdBy = otherMember.user,
            members = listOf(currentMember, otherMember),
            memberCount = 2,
            ownCapabilities = emptySet(),
        )
        val sut = Fixture()
            .given(
                currentUser = currentMember.user,
                channel = channel,
            )
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = otherMember.user,
                    members = ExpandableList(
                        items = listOf(otherMember),
                        minimumVisibleItems = 5,
                    ),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.UserInfo(user = otherMember.user),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `group channel content`() = runTest {
        val owner = randomMember()
        val user2 = randomMember(channelRole = "channel_moderator")
        val user3 = randomMember(channelRole = "channel_member")
        val user4 = randomMember()
        val channel = randomChannel(
            createdBy = owner.user,
            members = listOf(owner, user2, user3, user4),
            ownCapabilities = emptySet(),
        )
        val sut = Fixture()
            .given(channel = channel)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = owner.user,
                    members = ExpandableList(
                        items = channel.members,
                        minimumVisibleItems = 5,
                    ),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = channel.name, isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `expandable group channel content`() = runTest {
        val channel = randomChannel(members = randomMembers(10), ownCapabilities = emptySet())
        val sut = Fixture()
            .given(channel = channel)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = channel.createdBy,
                    members = ExpandableList(
                        items = channel.members,
                        minimumVisibleItems = 5,
                    ),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = channel.name, isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
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
        val channel = randomChannel(members = randomMembers(10), ownCapabilities = emptySet())
        val options = listOf(
            ChannelInfoViewState.Content.Option.RenameChannel(name = channel.name, isReadOnly = true),
            ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
            ChannelInfoViewState.Content.Option.PinnedMessages,
            ChannelInfoViewState.Content.Option.MediaAttachments,
            ChannelInfoViewState.Content.Option.FilesAttachments,
            ChannelInfoViewState.Content.Option.Separator,
        )
        val sut = Fixture()
            .given(channel = channel)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = channel.createdBy,
                    members = ExpandableList(
                        items = channel.members,
                        minimumVisibleItems = 5,
                    ),
                    options = options,
                ),
                awaitItem(),
            )

            sut.onViewAction(ChannelInfoViewAction.ExpandMembersClick)

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = channel.createdBy,
                    members = ExpandableList(
                        items = channel.members,
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
                    owner = channel.createdBy,
                    members = ExpandableList(
                        items = channel.members,
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
        val channel = randomChannel(ownCapabilities = emptySet())
        val fixture = Fixture().given(channel = channel)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = channel.createdBy,
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = channel.name, isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )

            val updatedChannel = channel.copy(
                ownCapabilities = setOf(
                    ChannelCapabilities.UPDATE_CHANNEL_MEMBERS,
                    ChannelCapabilities.UPDATE_CHANNEL,
                    ChannelCapabilities.BAN_CHANNEL_MEMBERS,
                    ChannelCapabilities.MUTE_CHANNEL,
                    ChannelCapabilities.LEAVE_CHANNEL,
                    ChannelCapabilities.DELETE_CHANNEL,
                ),
            )
            fixture.given(channel = updatedChannel)

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = updatedChannel.createdBy,
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.AddMember,
                        ChannelInfoViewState.Content.Option.RenameChannel(
                            name = updatedChannel.name,
                            isReadOnly = false,
                        ),
                        ChannelInfoViewState.Content.Option.MuteChannel(isMuted = false),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
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
    fun `user info click`() = runTest {
        val user = randomUser()
        val fixture = Fixture()
        val sut = fixture.get(backgroundScope)

        sut.onViewAction(ChannelInfoViewAction.UserInfoClick(user))

        fixture.verifyCopiedUserHandleToClipboard(text = "@${user.name}")
    }

    @Test
    fun `rename channel`() = runTest {
        val channel = randomChannel(ownCapabilities = setOf(ChannelCapabilities.UPDATE_CHANNEL))

        val fixture = Fixture().given(channel = channel)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = channel.createdBy,
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = channel.name, isReadOnly = false),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
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
                    owner = channel.createdBy,
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = newName, isReadOnly = false),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `rename channel error`() = runTest {
        val channel = randomChannel(ownCapabilities = setOf(ChannelCapabilities.UPDATE_CHANNEL))
        val fixture = Fixture().given(channel = channel)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial and loaded state

            val newName = "newName"
            fixture.givenRenameChannel(
                name = newName,
                error = randomGenericError(),
            )

            sut.onViewAction(ChannelInfoViewAction.RenameChannelClick(newName))

            sut.events.test {
                assertEquals(ChannelInfoViewEvent.RenameChannelError, awaitItem())
            }
        }
    }

    @Test
    fun `pinned messages click`() = runTest {
        val fixture = Fixture().given(channel = randomChannel())
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
    fun `media attachments click`() = runTest {
        val fixture = Fixture().given(channel = randomChannel())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            sut.events.test {
                sut.onViewAction(ChannelInfoViewAction.MediaAttachmentsClick)

                assertEquals(ChannelInfoViewEvent.NavigateToMediaAttachments, awaitItem())
            }
        }

        launch { fixture.verifyNoMoreInteractions() }
    }

    @Test
    fun `files attachments click`() = runTest {
        val fixture = Fixture().given(channel = randomChannel())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            sut.events.test {
                sut.onViewAction(ChannelInfoViewAction.FilesAttachmentsClick)

                assertEquals(ChannelInfoViewEvent.NavigateToFilesAttachments, awaitItem())
            }
        }

        launch { fixture.verifyNoMoreInteractions() }
    }

    @Test
    fun `mute channel`() = runTest {
        val channel = randomChannel(ownCapabilities = setOf(ChannelCapabilities.MUTE_CHANNEL))
        val fixture = Fixture().given(channel = channel, isMuted = false)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = channel.createdBy,
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = channel.name, isReadOnly = true),
                        ChannelInfoViewState.Content.Option.MuteChannel(isMuted = false),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )

            fixture.givenMuteChannel()

            sut.onViewAction(ChannelInfoViewAction.MuteChannelClick)

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = channel.createdBy,
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = channel.name, isReadOnly = true),
                        ChannelInfoViewState.Content.Option.MuteChannel(isMuted = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `mute channel error`() = runTest {
        val channel = randomChannel(ownCapabilities = setOf(ChannelCapabilities.MUTE_CHANNEL))
        val fixture = Fixture().given(channel = channel, isMuted = false)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = channel.createdBy,
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = channel.name, isReadOnly = true),
                        ChannelInfoViewState.Content.Option.MuteChannel(isMuted = false),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )

            fixture.givenMuteChannel(error = randomGenericError())

            sut.onViewAction(ChannelInfoViewAction.MuteChannelClick)

            sut.events.test {
                assertEquals(ChannelInfoViewEvent.MuteChannelError, awaitItem())
            }
        }
    }

    @Test
    fun `unmute channel`() = runTest {
        val channel = randomChannel(ownCapabilities = setOf(ChannelCapabilities.MUTE_CHANNEL))
        val fixture = Fixture().given(channel = channel, isMuted = true)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = channel.createdBy,
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = channel.name, isReadOnly = true),
                        ChannelInfoViewState.Content.Option.MuteChannel(isMuted = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )

            fixture.givenUnmuteChannel()

            sut.onViewAction(ChannelInfoViewAction.UnmuteChannelClick)

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = channel.createdBy,
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = channel.name, isReadOnly = true),
                        ChannelInfoViewState.Content.Option.MuteChannel(isMuted = false),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `unmute channel error`() = runTest {
        val channel = randomChannel(ownCapabilities = setOf(ChannelCapabilities.MUTE_CHANNEL))
        val fixture = Fixture().given(channel = channel, isMuted = true)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = channel.createdBy,
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = channel.name, isReadOnly = true),
                        ChannelInfoViewState.Content.Option.MuteChannel(isMuted = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )

            fixture.givenUnmuteChannel(error = randomGenericError())

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
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
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
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
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
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )

            val clearHistory = true
            fixture.givenHideChannel(
                clearHistory = clearHistory,
                error = randomGenericError(),
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
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
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
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
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
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
                        ChannelInfoViewState.Content.Option.Separator,
                    ),
                ),
                awaitItem(),
            )

            fixture.givenUnhideChannel(error = randomGenericError())

            sut.onViewAction(ChannelInfoViewAction.UnhideChannelClick)

            sut.events.test {
                assertEquals(ChannelInfoViewEvent.UnhideChannelError, awaitItem())
            }
        }
    }

    @Test
    fun `leave channel click`() = runTest {
        val fixture = Fixture()
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
        val fixture = Fixture()
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
        val currentUser = randomUser()
        val channel = randomChannel(ownCapabilities = setOf(ChannelCapabilities.LEAVE_CHANNEL))
        val fixture = Fixture()
            .given(
                currentUser = currentUser,
                channel = channel,
            )
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = channel.createdBy,
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = channel.name, isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
                        ChannelInfoViewState.Content.Option.Separator,
                        ChannelInfoViewState.Content.Option.LeaveChannel,
                    ),
                ),
                awaitItem(),
            )

            val quitMessage = randomMessage()
            fixture.givenRemoveMember(memberId = currentUser.id, systemMessage = quitMessage)

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
        val currentUser = randomUser()
        val channel = randomChannel(ownCapabilities = setOf(ChannelCapabilities.LEAVE_CHANNEL))
        val fixture = Fixture()
            .given(
                currentUser = currentUser,
                channel = channel,
            )
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = channel.createdBy,
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = channel.name, isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
                        ChannelInfoViewState.Content.Option.Separator,
                        ChannelInfoViewState.Content.Option.LeaveChannel,
                    ),
                ),
                awaitItem(),
            )

            val quitMessage = randomMessage()
            fixture.givenRemoveMember(
                memberId = currentUser.id,
                systemMessage = quitMessage,
                error = randomGenericError(),
            )

            sut.onViewAction(ChannelInfoViewAction.LeaveChannelConfirmationClick(quitMessage))

            sut.events.test {
                assertEquals(ChannelInfoViewEvent.LeaveChannelError, awaitItem())
            }
        }
    }

    @Test
    fun `delete channel click`() = runTest {
        val fixture = Fixture()
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
        val channel = randomChannel(ownCapabilities = setOf(ChannelCapabilities.DELETE_CHANNEL))
        val fixture = Fixture().given(channel = channel)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = channel.createdBy,
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = channel.name, isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
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
        val channel = randomChannel(ownCapabilities = setOf(ChannelCapabilities.DELETE_CHANNEL))
        val fixture = Fixture().given(channel = channel)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoViewState.Content(
                    owner = channel.createdBy,
                    members = emptyMembers(),
                    options = listOf(
                        ChannelInfoViewState.Content.Option.RenameChannel(name = channel.name, isReadOnly = true),
                        ChannelInfoViewState.Content.Option.HideChannel(isHidden = false),
                        ChannelInfoViewState.Content.Option.PinnedMessages,
                        ChannelInfoViewState.Content.Option.MediaAttachments,
                        ChannelInfoViewState.Content.Option.FilesAttachments,
                        ChannelInfoViewState.Content.Option.Separator,
                        ChannelInfoViewState.Content.Option.DeleteChannel,
                    ),
                ),
                awaitItem(),
            )

            fixture.givenDeleteChannel(error = randomGenericError())

            sut.onViewAction(ChannelInfoViewAction.DeleteChannelConfirmationClick)

            sut.events.test {
                assertEquals(ChannelInfoViewEvent.DeleteChannelError, awaitItem())
            }
        }
    }

    @Test
    fun `message member with distinct channel`() = runTest {
        val memberId = randomString()
        val cid = randomCID()
        val fixture = Fixture()
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            sut.events.test {
                sut.onMemberViewEvent(ChannelInfoMemberViewEvent.MessageMember(memberId, cid))

                assertEquals(
                    ChannelInfoViewEvent.NavigateToChannel(cid),
                    awaitItem(),
                )
            }
        }

        launch { fixture.verifyNoMoreInteractions() }
    }

    @Test
    fun `message member with no distinct channel`() = runTest {
        val memberId = randomString()
        val fixture = Fixture()
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            sut.events.test {
                sut.onMemberViewEvent(ChannelInfoMemberViewEvent.MessageMember(memberId, distinctCid = null))

                assertEquals(
                    ChannelInfoViewEvent.NavigateToDraftChannel(memberId),
                    awaitItem(),
                )
            }
        }

        launch { fixture.verifyNoMoreInteractions() }
    }

    @Test
    fun `ban member modal`() = runTest {
        val member = randomMember()
        val fixture = Fixture()
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            sut.events.test {
                sut.onMemberViewEvent(ChannelInfoMemberViewEvent.BanMember(member))

                assertEquals(ChannelInfoViewEvent.BanMemberModal(member), awaitItem())
            }
        }

        launch { fixture.verifyNoMoreInteractions() }
    }

    @Test
    fun `ban member success`() = runTest {
        val member = randomMember()
        val timeoutInMinutes = 60
        val fixture = Fixture()
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            fixture.givenBanMember(member, timeoutInMinutes)

            sut.onViewAction(
                ChannelInfoViewAction.BanMemberConfirmationClick(
                    memberId = member.getUserId(),
                    timeoutInMinutes = timeoutInMinutes,
                ),
            )
        }

        launch {
            fixture.verifyMemberBanned(member, timeoutInMinutes)
            fixture.verifyNoMoreInteractions()
        }
    }

    @Test
    fun `ban member error`() = runTest {
        val member = randomMember()
        val timeoutInMinutes = 60
        val fixture = Fixture()
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            fixture.givenBanMember(
                member = member,
                timeout = timeoutInMinutes,
                error = randomGenericError(),
            )

            sut.onViewAction(
                ChannelInfoViewAction.BanMemberConfirmationClick(
                    memberId = member.getUserId(),
                    timeoutInMinutes = timeoutInMinutes,
                ),
            )

            sut.events.test {
                assertEquals(ChannelInfoViewEvent.BanMemberError, awaitItem())
            }
        }
    }

    @Test
    fun `unban member success`() = runTest {
        val member = randomMember()
        val fixture = Fixture()
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            fixture.givenUnbanMember(member)

            sut.onMemberViewEvent(ChannelInfoMemberViewEvent.UnbanMember(member))
        }

        launch {
            fixture.verifyMemberNotBanned(member)
            fixture.verifyNoMoreInteractions()
        }
    }

    @Test
    fun `unban member error`() = runTest {
        val member = randomMember()
        val fixture = Fixture()
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            fixture.givenUnbanMember(
                member = member,
                error = randomGenericError(),
            )

            sut.onMemberViewEvent(ChannelInfoMemberViewEvent.UnbanMember(member))

            sut.events.test {
                assertEquals(ChannelInfoViewEvent.UnbanMemberError, awaitItem())
            }
        }
    }

    @Test
    fun `remove member modal`() = runTest {
        val member = randomMember()
        val fixture = Fixture()
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            sut.events.test {
                sut.onMemberViewEvent(ChannelInfoMemberViewEvent.RemoveMember(member))

                assertEquals(ChannelInfoViewEvent.RemoveMemberModal(member), awaitItem())
            }
        }

        launch { fixture.verifyNoMoreInteractions() }
    }

    @Test
    fun `remove member success`() = runTest {
        val member = randomMember()
        val fixture = Fixture()
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            fixture.givenRemoveMember(memberId = member.getUserId())

            sut.onViewAction(
                ChannelInfoViewAction.RemoveMemberConfirmationClick(
                    memberId = member.getUserId(),
                ),
            )
        }

        launch {
            fixture.verifyMemberRemoved(member)
            fixture.verifyNoMoreInteractions()
        }
    }

    @Test
    fun `remove member error`() = runTest {
        val member = randomMember()
        val fixture = Fixture()
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            fixture.givenRemoveMember(
                memberId = member.getUserId(),
                error = randomGenericError(),
            )

            sut.onViewAction(
                ChannelInfoViewAction.RemoveMemberConfirmationClick(
                    memberId = member.getUserId(),
                ),
            )

            sut.events.test {
                assertEquals(ChannelInfoViewEvent.RemoveMemberError, awaitItem())
            }
        }
    }
}

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
    private val copyToClipboardHandler: CopyToClipboardHandler = mock()

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

    fun givenRemoveMember(memberId: String, systemMessage: Message? = null, error: Error? = null) = apply {
        whenever(
            channelClient.removeMembers(
                memberIds = listOf(memberId),
                systemMessage = systemMessage,
            ),
        ) doAnswer {
            error?.asCall() ?: mock<Channel>().asCall()
        }
    }

    fun givenDeleteChannel(error: Error? = null) = apply {
        whenever(channelClient.delete()) doAnswer {
            error?.asCall() ?: mock<Channel>().asCall()
        }
    }

    fun givenBanMember(member: Member, timeout: Int? = null, error: Error? = null) = apply {
        whenever(
            channelClient.banUser(
                targetId = member.getUserId(),
                reason = null,
                timeout = timeout,
            ),
        ) doAnswer {
            error?.asCall() ?: Unit.asCall()
        }
    }

    fun givenUnbanMember(member: Member, error: Error? = null) = apply {
        whenever(channelClient.unbanUser(targetId = member.getUserId())) doAnswer {
            error?.asCall()
                ?: Unit.asCall()
        }
    }

    fun verifyNoMoreInteractions() = apply {
        verifyNoMoreInteractions(channelClient)
    }

    fun verifyCopiedUserHandleToClipboard(text: String) = apply {
        verify(copyToClipboardHandler).copy(text = text)
    }

    fun verifyMemberBanned(member: Member, timeout: Int?) = apply {
        verify(channelClient).banUser(
            targetId = member.getUserId(),
            reason = null,
            timeout = timeout,
        )
    }

    fun verifyMemberNotBanned(member: Member) = apply {
        verify(channelClient).unbanUser(targetId = member.getUserId())
    }

    fun verifyMemberRemoved(member: Member) = apply {
        verify(channelClient).removeMembers(
            memberIds = listOf(member.getUserId()),
            systemMessage = null,
            skipPush = null,
        )
    }

    fun get(scope: CoroutineScope) = ChannelInfoViewController(
        cid = randomCID(),
        scope = scope,
        chatClient = chatClient,
        channelState = MutableStateFlow(channelState),
        channelClient = channelClient,
        copyToClipboardHandler = copyToClipboardHandler,
    )
}

private fun <T> emptyMembers() = ExpandableList<T>(
    items = emptyList(),
    minimumVisibleItems = 5,
)
