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

package io.getstream.chat.android.ui.common.feature.channel.info

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.state.GlobalState
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.toChannelData
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoMemberViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

internal class ChannelInfoMemberViewControllerTest {

    @Test
    fun `initial state`() = runTest {
        val sut = Fixture().get(backgroundScope)

        assertEquals(ChannelInfoMemberViewState.Loading, sut.state.value)
    }

    @Test
    fun `member content with no capabilities`() = runTest {
        val member = randomMember()
        val channel = randomChannel(
            ownCapabilities = emptySet(),
            members = listOf(member),
        )
        val fixture = Fixture().given(channel, memberId = member.getUserId())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1)

            assertEquals(
                ChannelInfoMemberViewState.Content(
                    member = member,
                    capabilities = emptySet(),
                    isMuted = false,
                    isBlocked = false,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `member content with capabilities`() = runTest {
        var member = randomMember()
        var channel = randomChannel(
            ownCapabilities = emptySet(),
            members = listOf(member),
        )
        val fixture = Fixture().given(channel, memberId = member.getUserId())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1)

            assertEquals(
                ChannelInfoMemberViewState.Content(
                    member = member,
                    capabilities = emptySet(),
                    isMuted = false,
                    isBlocked = false,
                ),
                awaitItem(),
            )

            channel = channel.copy(
                ownCapabilities = setOf(
                    ChannelCapabilities.UPDATE_CHANNEL_MEMBERS,
                    ChannelCapabilities.BAN_CHANNEL_MEMBERS,
                ),
            )
            fixture.given(channel)

            assertEquals(
                ChannelInfoMemberViewState.Content(
                    member = member,
                    capabilities = setOf(
                        ChannelCapabilities.UPDATE_CHANNEL_MEMBERS,
                        ChannelCapabilities.BAN_CHANNEL_MEMBERS,
                    ),
                    isMuted = false,
                    isBlocked = false,
                ),
                awaitItem(),
            )

            channel = channel.copy(members = listOf(member.copy(banned = true)))
            member = member.copy(banned = true)
            fixture.given(channel)

            assertEquals(
                ChannelInfoMemberViewState.Content(
                    member = member,
                    capabilities = setOf(
                        ChannelCapabilities.UPDATE_CHANNEL_MEMBERS,
                        ChannelCapabilities.BAN_CHANNEL_MEMBERS,
                    ),
                    isMuted = false,
                    isBlocked = false,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `muted state is reflected`() = runTest {
        val member = randomMember()
        val channel = randomChannel(members = listOf(member))
        val fixture = Fixture().given(channel, memberId = member.getUserId())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1)

            val initial = awaitItem() as ChannelInfoMemberViewState.Content
            assertEquals(false, initial.isMuted)

            fixture.setMuted(
                listOf(
                    Mute(
                        user = randomUser(),
                        target = member.user,
                        createdAt = Date(),
                        updatedAt = Date(),
                        expires = null,
                    ),
                ),
            )

            val muted = awaitItem() as ChannelInfoMemberViewState.Content
            assertEquals(true, muted.isMuted)
        }
    }

    @Test
    fun `blocked state is reflected`() = runTest {
        val member = randomMember()
        val channel = randomChannel(members = listOf(member))
        val fixture = Fixture().given(channel, memberId = member.getUserId())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1)

            val initial = awaitItem() as ChannelInfoMemberViewState.Content
            assertEquals(false, initial.isBlocked)

            fixture.setBlocked(listOf(member.getUserId()))

            val blocked = awaitItem() as ChannelInfoMemberViewState.Content
            assertEquals(true, blocked.isBlocked)
        }
    }

    @Test
    fun `message member click with no distinct channel`() = runTest {
        val member = randomMember()
        val fixture = Fixture().given(memberId = member.getUserId())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1)

            sut.events.test {
                sut.onViewAction(ChannelInfoMemberViewAction.MessageMemberClick)

                assertEquals(
                    ChannelInfoMemberViewEvent.MessageMember(
                        memberId = member.getUserId(),
                        distinctCid = null,
                    ),
                    awaitItem(),
                )
            }
        }
    }

    @Test
    fun `message member click with distinct channel`() = runTest {
        val member = randomMember()
        val distinctChannel = randomChannel()
        val currentUser = randomUser()
        val fixture = Fixture()
            .given(
                channel = randomChannel(members = listOf(member)),
                memberId = member.getUserId(),
                distinctChannel = distinctChannel,
                currentUser = currentUser,
            )
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2)

            sut.events.test {
                sut.onViewAction(ChannelInfoMemberViewAction.MessageMemberClick)

                assertEquals(
                    ChannelInfoMemberViewEvent.MessageMember(
                        memberId = member.getUserId(),
                        distinctCid = distinctChannel.cid,
                    ),
                    awaitItem(),
                )
            }
        }
    }

    @Test
    fun `ban member click`() = runTest {
        val member = randomMember()
        val channel = randomChannel(members = listOf(member))
        val fixture = Fixture().given(channel, memberId = member.getUserId())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2)

            sut.events.test {
                sut.onViewAction(ChannelInfoMemberViewAction.BanMemberClick)

                assertEquals(ChannelInfoMemberViewEvent.BanMember(member), awaitItem())
            }
        }
    }

    @Test
    fun `unban member click`() = runTest {
        val member = randomMember()
        val channel = randomChannel(members = listOf(member))
        val fixture = Fixture().given(channel, memberId = member.getUserId())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2)

            sut.events.test {
                sut.onViewAction(ChannelInfoMemberViewAction.UnbanMemberClick)

                assertEquals(ChannelInfoMemberViewEvent.UnbanMember(member), awaitItem())
            }
        }
    }

    @Test
    fun `remove member click`() = runTest {
        val member = randomMember()
        val channel = randomChannel(members = listOf(member))
        val fixture = Fixture().given(channel, memberId = member.getUserId())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2)

            sut.events.test {
                sut.onViewAction(ChannelInfoMemberViewAction.RemoveMemberClick)

                assertEquals(ChannelInfoMemberViewEvent.RemoveMember(member), awaitItem())
            }
        }
    }

    @Test
    fun `mute user click`() = runTest {
        val member = randomMember()
        val channel = randomChannel(members = listOf(member))
        val fixture = Fixture().given(channel, memberId = member.getUserId())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2)

            sut.events.test {
                sut.onViewAction(ChannelInfoMemberViewAction.MuteUserClick)

                assertEquals(ChannelInfoMemberViewEvent.MuteUser(member), awaitItem())
            }
        }
    }

    @Test
    fun `unmute user click`() = runTest {
        val member = randomMember()
        val channel = randomChannel(members = listOf(member))
        val fixture = Fixture().given(channel, memberId = member.getUserId())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2)

            sut.events.test {
                sut.onViewAction(ChannelInfoMemberViewAction.UnmuteUserClick)

                assertEquals(ChannelInfoMemberViewEvent.UnmuteUser(member), awaitItem())
            }
        }
    }

    @Test
    fun `block user click`() = runTest {
        val member = randomMember()
        val channel = randomChannel(members = listOf(member))
        val fixture = Fixture().given(channel, memberId = member.getUserId())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2)

            sut.events.test {
                sut.onViewAction(ChannelInfoMemberViewAction.BlockUserClick)

                assertEquals(ChannelInfoMemberViewEvent.BlockUser(member), awaitItem())
            }
        }
    }

    @Test
    fun `unblock user click`() = runTest {
        val member = randomMember()
        val channel = randomChannel(members = listOf(member))
        val fixture = Fixture().given(channel, memberId = member.getUserId())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2)

            sut.events.test {
                sut.onViewAction(ChannelInfoMemberViewAction.UnblockUserClick)

                assertEquals(ChannelInfoMemberViewEvent.UnblockUser(member), awaitItem())
            }
        }
    }

    private class Fixture {
        private val channelData = MutableStateFlow(ChannelData(type = "", id = ""))
        private val channelMembers = MutableStateFlow(emptyList<Member>())
        private val channelState: ChannelState = mock {
            on { channelData } doReturn channelData
            on { members } doReturn channelMembers
        }
        private val chatClient: ChatClient = mock {
            on { getCurrentUser() } doReturn randomUser()
            on { queryChannels(any()) } doReturn emptyList<Channel>().asCall()
        }
        private val mutedUsers = MutableStateFlow(emptyList<Mute>())
        private val blockedUserIds = MutableStateFlow(emptyList<String>())
        private val globalState: GlobalState = mock {
            on { muted } doReturn mutedUsers
            on { this.blockedUserIds } doReturn this@Fixture.blockedUserIds
        }
        private var memberId: String = randomString()

        fun given(
            channel: Channel? = null,
            memberId: String? = null,
            distinctChannel: Channel? = null,
            currentUser: User? = null,
        ) = apply {
            if (channel != null) {
                channelData.value = channel.toChannelData()
                channelMembers.value = channel.members
            }
            if (memberId != null) {
                this.memberId = memberId
            }
            if (distinctChannel != null) {
                whenever(
                    chatClient.queryChannels(
                        request = QueryChannelsRequest(
                            filter = Filters.distinct(listOfNotNull(memberId, currentUser?.id)),
                            querySort = QuerySortByField.descByName("last_updated"),
                            messageLimit = 0,
                            memberLimit = 1,
                            limit = 1,
                        ),
                    ),
                ) doReturn listOf(distinctChannel).asCall()
            }
            if (currentUser != null) {
                whenever(chatClient.getCurrentUser()) doReturn currentUser
            }
        }

        fun setMuted(mutes: List<Mute>) {
            mutedUsers.value = mutes
        }

        fun setBlocked(userIds: List<String>) {
            blockedUserIds.value = userIds
        }

        fun get(scope: CoroutineScope) = ChannelInfoMemberViewController(
            cid = randomCID(),
            memberId = memberId,
            scope = scope,
            chatClient = chatClient,
            channelState = MutableStateFlow(channelState),
            globalState = flowOf(globalState),
        )
    }
}
