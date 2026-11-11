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

import io.getstream.chat.android.client.channel.ChannelMessagesUpdateLogic
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.logic.querychannels.internal.QueryChannelsLogic
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelMute
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

internal class ChannelUpdateResponseHandlerTest {

    private val currentUserId = "me"
    private val channelData = MutableStateFlow(ChannelData(id = CHANNEL_ID, type = CHANNEL_TYPE))
    private val channelState: ChannelState = mock { on(it.channelData) doReturn channelData }
    private val messagesUpdateLogic: ChannelMessagesUpdateLogic = mock { on(it.channelState()) doReturn channelState }
    private val channelLogic: ChannelLogic = mock { on(it.messagesUpdateLogic) doReturn messagesUpdateLogic }
    private val queryChannelsLogic: QueryChannelsLogic = mock()
    private val logic: LogicRegistry = mock {
        on(it.channel(CHANNEL_TYPE, CHANNEL_ID)) doReturn channelLogic
        on(it.getActiveQueryChannelsLogic()) doReturn listOf(queryChannelsLogic)
    }
    private val repos: RepositoryFacade = mock()
    private val globalState = MutableGlobalState(currentUserId)
    private val handler = ChannelUpdateResponseHandler(logic, repos, globalState) { currentUserId }

    @Test
    fun `onChannelUpdated stores the response merged into the stored channel`() = runTest {
        val jane = randomMember(user = randomUser(id = "jane"))
        val stored = channel(name = "old", updatedAt = Date(1_000), members = listOf(jane), memberCount = 1).copy(
            messages = listOf(randomMessage(cid = CID)),
            read = listOf(randomChannelUserRead()),
            ownCapabilities = setOf(ChannelCapabilities.UPDATE_CHANNEL),
            hidden = true,
            lastMessageAt = Date(5_000),
            messageCount = 10,
        )
        whenever(repos.selectChannel(CID)) doReturn stored
        // The response predates a message: an older last message date and message count, and no hidden value
        val response = channel(name = "new", updatedAt = Date(2_000)).copy(
            ownCapabilities = emptySet(),
            hidden = null,
            lastMessageAt = Date(4_000),
            messageCount = 9,
        )

        handler.onChannelUpdated(response)

        val saved = argumentCaptor<Channel>().apply { verify(repos).insertChannel(capture()) }.firstValue
        saved.name shouldBeEqualTo "new"
        saved.messages shouldBeEqualTo stored.messages
        saved.read shouldBeEqualTo stored.read
        saved.ownCapabilities shouldBeEqualTo stored.ownCapabilities
        saved.hidden shouldBeEqualTo true
        saved.lastMessageAt shouldBeEqualTo Date(5_000)
        saved.messageCount shouldBeEqualTo 10
        saved.members shouldBeEqualTo listOf(jane)
        saved.memberCount shouldBeEqualTo 1
    }

    @Test
    fun `onChannelUpdated applies the response to the active channel without its members`() = runTest {
        givenActiveChannel()
        val response = channel(name = "new", updatedAt = Date(2_000))

        handler.onChannelUpdated(response)

        verify(channelLogic).applyChannelUpdate(response)
        verify(queryChannelsLogic).refreshChannelState(CID)
    }

    @Test
    fun `onChannelUpdated ignores a response older than the stored channel and the state`() = runTest {
        givenActiveChannel()
        whenever(repos.selectChannel(CID)) doReturn channel(updatedAt = Date(3_000))
        channelData.value = channelData.value.copy(updatedAt = Date(3_000))

        handler.onChannelUpdated(channel(updatedAt = Date(2_000)))

        verify(repos, never()).insertChannel(any<Channel>())
        verify(channelLogic, never()).applyChannelUpdate(any())
    }

    @Test
    fun `onChannelUpdated does not store a channel that is not stored yet`() = runTest {
        whenever(repos.selectChannel(CID)) doReturn null

        handler.onChannelUpdated(channel())

        verify(repos, never()).insertChannel(any<Channel>())
    }

    @Test
    fun `onChannelUpdated does not create state for a channel without state`() = runTest {
        whenever(logic.isActiveChannel(CHANNEL_TYPE, CHANNEL_ID)) doReturn false

        handler.onChannelUpdated(channel())

        verify(logic, never()).channel(any<String>(), any())
    }

    @Test
    fun `onChannelUpdated logs a storage failure instead of throwing`() = runTest {
        givenActiveChannel()
        whenever(repos.selectChannel(CID)) doThrow IllegalStateException("database is closed")

        handler.onChannelUpdated(channel())

        verify(channelLogic, never()).applyChannelUpdate(any())
    }

    @Test
    fun `onMembersAdded adds the listed added members and counts each of them once`() = runTest {
        givenActiveChannel()
        val john = randomMember(user = randomUser(id = "john"))
        val jane = randomMember(user = randomUser(id = "jane"))
        whenever(repos.selectChannel(CID)) doReturn channel(members = listOf(john), memberCount = 1)
        val response = channel(members = listOf(john, jane), memberCount = 2)

        handler.onMembersAdded(response, listOf("jane"))

        val saved = argumentCaptor<Channel>().apply { verify(repos).insertChannel(capture()) }.firstValue
        saved.members shouldBeEqualTo listOf(john, jane)
        saved.memberCount shouldBeEqualTo 2
        verify(channelLogic).applyChannelUpdate(response)
        verify(channelLogic).addMembers(listOf(jane))
    }

    @Test
    fun `onMembersAdded keeps a removal processed before the response`() = runTest {
        givenActiveChannel()
        // Y was removed by someone else, and member.removed was processed before this response
        val john = randomMember(user = randomUser(id = "john"))
        val y = randomMember(user = randomUser(id = "y"))
        val x = randomMember(user = randomUser(id = "x"))
        whenever(repos.selectChannel(CID)) doReturn channel(members = listOf(john), memberCount = 1)
        val response = channel(members = listOf(john, y, x), memberCount = 3)

        handler.onMembersAdded(response, listOf("x"))

        val saved = argumentCaptor<Channel>().apply { verify(repos).insertChannel(capture()) }.firstValue
        saved.members.map { it.getUserId() } shouldBeEqualTo listOf("john", "x")
        saved.memberCount shouldBeEqualTo 2
        verify(channelLogic).addMembers(listOf(x))
    }

    @Test
    fun `onMembersAdded leaves an added member the response does not list to the event`() = runTest {
        givenActiveChannel()
        whenever(repos.selectChannel(CID)) doReturn channel(members = emptyList(), memberCount = 150)
        // A large channel's response lists a limited number of members
        val response = channel(members = emptyList(), memberCount = 151)

        handler.onMembersAdded(response, listOf("jane"))

        val saved = argumentCaptor<Channel>().apply { verify(repos).insertChannel(capture()) }.firstValue
        saved.memberCount shouldBeEqualTo 150
        verify(channelLogic).addMembers(emptyList())
    }

    @Test
    fun `onMembersRemoved stores the channel without the removed members and leaves the state to the event`() =
        runTest {
            givenActiveChannel()
            val jane = randomMember(user = randomUser(id = "jane"))
            val john = randomMember(user = randomUser(id = "john"))
            whenever(repos.selectChannel(CID)) doReturn channel(members = listOf(jane, john), memberCount = 2)
            // The response predates another removal: it still lists jane, with the older count
            val response = channel(members = listOf(jane), memberCount = 5)

            handler.onMembersRemoved(response, listOf("john"))

            val saved = argumentCaptor<Channel>().apply { verify(repos).insertChannel(capture()) }.firstValue
            saved.members.map { it.getUserId() } shouldBeEqualTo listOf("jane")
            saved.memberCount shouldBeEqualTo 1
            verify(channelLogic, never()).applyChannelUpdate(any())
        }

    @Test
    fun `onMembersRemoved skips the channel the current user left`() = runTest {
        whenever(repos.selectChannel(CID)) doReturn channel()

        handler.onMembersRemoved(channel(), listOf(currentUserId))

        verify(repos, never()).insertChannel(any<Channel>())
    }

    @Test
    fun `onChannelTruncated removes the truncated messages and applies the channel`() = runTest {
        givenActiveChannel()
        val truncatedAt = Date(5_000)
        val response = channel().copy(truncatedAt = truncatedAt)

        handler.onChannelTruncated(response)

        verify(repos).deleteChannelMessagesBefore(CID, truncatedAt)
        verify(channelLogic).truncate(truncatedAt)
        verify(channelLogic).applyChannelUpdate(response)
    }

    @Test
    fun `onChannelTruncated without a truncation date removes no messages`() = runTest {
        givenActiveChannel()

        handler.onChannelTruncated(channel())

        verify(repos, never()).deleteChannelMessagesBefore(any(), any())
        verify(channelLogic, never()).truncate(any())
    }

    @Test
    fun `onChannelDeleted marks the channel as deleted`() = runTest {
        givenActiveChannel()
        val deletedAt = Date(6_000)

        handler.onChannelDeleted(channel(deletedAt = deletedAt))

        verify(repos).deleteChannelMessagesBefore(CID, deletedAt)
        verify(repos).setChannelDeletedAt(CID, deletedAt)
        verify(channelLogic).deleteChannel(deletedAt)
    }

    @Test
    fun `onChannelDeleted without a deletion date changes nothing`() = runTest {
        givenActiveChannel()

        handler.onChannelDeleted(channel(deletedAt = null))

        verify(repos, never()).deleteChannelMessagesBefore(any(), any())
        verify(channelLogic, never()).deleteChannel(any())
    }

    @Test
    fun `onChannelMuteChanged sets the channel mutes and the channel's muted state`() = runTest {
        givenActiveChannel()
        val mutes = listOf(randomChannelMute(channel = channel()))

        handler.onChannelMuteChanged(CHANNEL_TYPE, CHANNEL_ID, muted = true, channelMutes = mutes)

        globalState.channelMutes.value shouldBeEqualTo mutes
        verify(channelLogic).setMuted(true)
        verify(queryChannelsLogic).refreshChannelState(CID)
    }

    @Test
    fun `onMarkReadFailed restores the active channel's cleared unread count and refreshes the lists`() = runTest {
        givenActiveChannel()
        whenever(channelLogic.onMarkReadFailed()) doReturn true

        handler.onMarkReadFailed(CHANNEL_TYPE, CHANNEL_ID)

        verify(channelLogic).onMarkReadFailed()
        verify(queryChannelsLogic).refreshChannelState(CID)
    }

    @Test
    fun `onMarkReadFailed does not refresh the lists when nothing was restored`() = runTest {
        givenActiveChannel()
        // A request the precondition skipped: nothing was cleared, so nothing comes back
        whenever(channelLogic.onMarkReadFailed()) doReturn false

        handler.onMarkReadFailed(CHANNEL_TYPE, CHANNEL_ID)

        verify(queryChannelsLogic, never()).refreshChannelState(any())
    }

    @Test
    fun `onMarkReadFailed does not create state for a channel without state`() = runTest {
        whenever(logic.isActiveChannel(CHANNEL_TYPE, CHANNEL_ID)) doReturn false

        handler.onMarkReadFailed(CHANNEL_TYPE, CHANNEL_ID)

        verify(logic, never()).channel(any<String>(), any())
    }

    private fun givenActiveChannel() {
        whenever(logic.isActiveChannel(CHANNEL_TYPE, CHANNEL_ID)) doReturn true
    }

    private fun channel(
        name: String = "name",
        updatedAt: Date? = Date(1_000),
        deletedAt: Date? = null,
        members: List<Member> = emptyList(),
        memberCount: Int = 0,
    ) = randomChannel(
        type = CHANNEL_TYPE,
        id = CHANNEL_ID,
        name = name,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        members = members,
        memberCount = memberCount,
        truncatedAt = null,
        hidden = false,
    )

    private companion object {
        const val CHANNEL_TYPE = "messaging"
        const val CHANNEL_ID = "123"
        const val CID = "$CHANNEL_TYPE:$CHANNEL_ID"
    }
}
