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
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.toChannelData
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomString
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoMemberViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class ChannelInfoMemberViewControllerTest {

    @Test
    fun `initial state`() = runTest {
        val sut = Fixture().get(backgroundScope)

        assertEquals(ChannelInfoMemberViewState.Loading, sut.state.value)
    }

    @Test
    fun `member options`() = runTest {
        var member = randomMember()
        var channel = randomChannel(
            ownCapabilities = emptySet(),
            members = listOf(member),
        )
        val fixture = Fixture().given(channel = channel, memberId = member.getUserId())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                ChannelInfoMemberViewState.Content(
                    member = member,
                    options = emptyList(),
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
                    options = listOf(
                        ChannelInfoMemberViewState.Content.Option.BanMember(member),
                        ChannelInfoMemberViewState.Content.Option.RemoveMember(member),
                    ),
                ),
                awaitItem(),
            )

            channel = channel.copy(members = listOf(member.copy(banned = true)))
            member = member.copy(banned = true)
            fixture.given(channel)

            assertEquals(
                ChannelInfoMemberViewState.Content(
                    member = member,
                    options = listOf(
                        ChannelInfoMemberViewState.Content.Option.UnbanMember(member),
                        ChannelInfoMemberViewState.Content.Option.RemoveMember(member),
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `member message click`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            sut.events.test {
                sut.onViewAction(ChannelInfoMemberViewAction.MessageMemberClick)

                // https://linear.app/stream/issue/AND-567/compose-navigate-to-messages-from-the-member-modal-sheet-of-channel
                assertEquals(ChannelInfoMemberViewEvent.MessageMember(channelId = ""), awaitItem())
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
            skipItems(2) // Skip initial states

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
            skipItems(2) // Skip initial states

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
            skipItems(2) // Skip initial states

            sut.events.test {
                sut.onViewAction(ChannelInfoMemberViewAction.RemoveMemberClick)

                assertEquals(ChannelInfoMemberViewEvent.RemoveMember(member), awaitItem())
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
        private val chatClient: ChatClient = mock()
        private var memberId: String = randomString()

        fun given(channel: Channel, memberId: String? = null) = apply {
            if (memberId != null) {
                this.memberId = memberId
            }
            channelData.value = channel.toChannelData()
            channelMembers.value = channel.members
        }

        fun get(scope: CoroutineScope) = ChannelInfoMemberViewController(
            cid = randomCID(),
            memberId = memberId,
            scope = scope,
            chatClient = chatClient,
            channelState = MutableStateFlow(channelState),
        )
    }
}
