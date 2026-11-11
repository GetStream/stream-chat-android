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

package io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal

import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateImplTestBase
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.test.randomMemberAddedEvent
import io.getstream.chat.android.client.test.randomMemberRemovedEvent
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.util.Date

internal class ChannelLogicImplChannelUpdateTest : ChannelStateImplTestBase() {

    private lateinit var sut: ChannelLogicImpl

    @BeforeEach
    fun setUpLogic() {
        sut = ChannelLogicImpl(
            cid = CID,
            messagesUpdateLogic = mock(),
            repository = mock(),
            state = channelState,
            mutableGlobalState = MutableGlobalState(currentUser.id),
            userPresence = false,
            coroutineScope = testCoroutines.scope,
            getCurrentUserId = { currentUser.id },
            now = { System.currentTimeMillis() },
        )
    }

    @Test
    fun `applyChannelUpdate updates the channel data and keeps the own capabilities`() {
        val capabilities = setOf(ChannelCapabilities.UPDATE_CHANNEL)
        channelState.updateChannelData {
            ChannelData(id = CHANNEL_ID, type = CHANNEL_TYPE, name = "old", ownCapabilities = capabilities)
        }

        sut.applyChannelUpdate(channel(name = "new").copy(ownCapabilities = emptySet()))

        assertEquals("new", channelState.channelData.value.name)
        assertEquals(capabilities, channelState.channelData.value.ownCapabilities)
    }

    @Test
    fun `member added event after the add response does not count the member twice`() {
        val jane = randomMember(user = randomUser(id = "jane"))
        channelState.setMemberCount(1)

        sut.addMembers(listOf(jane))
        sut.handleEvent(randomMemberAddedEvent(cid = CID, channelType = CHANNEL_TYPE, channelId = CHANNEL_ID, member = jane))

        assertEquals(2, channelState.membersCount.value)
        assertEquals(listOf("jane"), channelState.members.value.map { it.getUserId() })
    }

    @Test
    fun `an add response keeps a removal processed before it`() {
        val john = randomMember(user = randomUser(id = "john"))
        val y = randomMember(user = randomUser(id = "y"))
        val x = randomMember(user = randomUser(id = "x"))
        channelState.setMembers(listOf(john, y))
        channelState.setMemberCount(2)
        sut.handleEvent(randomMemberRemovedEvent(cid = CID, channelType = CHANNEL_TYPE, channelId = CHANNEL_ID, member = y))

        // The response predates the removal: it still lists y, with the older count
        sut.applyChannelUpdate(channel(members = listOf(john, y, x), memberCount = 3))
        sut.addMembers(listOf(x))

        assertEquals(2, channelState.membersCount.value)
        assertEquals(listOf("john", "x"), channelState.members.value.map { it.getUserId() })
    }

    @Test
    fun `an update response keeps the members and the later last message date`() {
        val john = randomMember(user = randomUser(id = "john"))
        channelState.setMembers(listOf(john))
        channelState.setMemberCount(1)
        channelState.updateChannelData {
            ChannelData(id = CHANNEL_ID, type = CHANNEL_TYPE, lastMessageAt = Date(5_000), messageCount = 10)
        }

        sut.applyChannelUpdate(channel(name = "new").copy(lastMessageAt = Date(4_000), messageCount = 9))

        assertEquals("new", channelState.channelData.value.name)
        assertEquals(Date(5_000), channelState.channelData.value.lastMessageAt)
        assertEquals(10, channelState.channelData.value.messageCount)
        assertEquals(1, channelState.membersCount.value)
        assertEquals(listOf("john"), channelState.members.value.map { it.getUserId() })
    }

    @Test
    fun `truncate removes the messages created before the truncation`() {
        val messages = createMessages(count = 3, baseTimestamp = 10_000)
        channelState.setMessages(messages)

        sut.truncate(Date(messages[1].createdAt!!.time + 1))

        assertEquals(listOf(messages[2].id), channelState.messages.value.map { it.id })
    }

    @Test
    fun `deleteChannel marks the channel as deleted`() {
        val deletedAt = Date(20_000)

        sut.deleteChannel(deletedAt)

        assertEquals(deletedAt, channelState.channelData.value.deletedAt)
    }

    private fun channel(
        name: String = "name",
        members: List<Member> = emptyList(),
        memberCount: Int = 0,
    ) = randomChannel(
        type = CHANNEL_TYPE,
        id = CHANNEL_ID,
        name = name,
        members = members,
        memberCount = memberCount,
        truncatedAt = null,
        deletedAt = null,
    )
}
