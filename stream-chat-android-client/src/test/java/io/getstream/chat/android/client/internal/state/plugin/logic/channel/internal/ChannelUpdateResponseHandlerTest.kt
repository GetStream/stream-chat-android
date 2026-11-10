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
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelMute
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
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
    fun `onChannelUpdated stores the merged channel and applies it to the active channel`() = runTest {
        givenActiveChannel()
        val stored = channel(name = "old", updatedAt = Date(1_000))
        whenever(repos.selectChannel(CID)) doReturn stored
        val response = channel(name = "new", updatedAt = Date(2_000))

        handler.onChannelUpdated(response)

        val saved = argumentCaptor<Channel>().apply { verify(repos).insertChannel(capture()) }.firstValue
        saved.name shouldBeEqualTo "new"
        saved.messages shouldBeEqualTo stored.messages
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
    fun `onMembersAdded applies a response that lists every added member`() = runTest {
        givenActiveChannel()
        val added = randomMember(user = randomUser(id = "jane"))
        val response = channel(members = listOf(added), memberCount = 2)

        handler.onMembersAdded(response, listOf("jane"))

        verify(channelLogic).applyChannelUpdate(response)
    }

    @Test
    fun `onMembersAdded skips a response that does not list every added member`() = runTest {
        givenActiveChannel()
        whenever(repos.selectChannel(CID)) doReturn channel()
        val response = channel(members = listOf(randomMember(user = randomUser(id = "jane"))), memberCount = 200)

        handler.onMembersAdded(response, listOf("jane", "john"))

        verify(repos, never()).insertChannel(any<Channel>())
        verify(channelLogic, never()).applyChannelUpdate(any())
    }

    @Test
    fun `onMembersRemoved stores the channel without the removed members and leaves the state to the event`() =
        runTest {
            givenActiveChannel()
            val jane = randomMember(user = randomUser(id = "jane"))
            val john = randomMember(user = randomUser(id = "john"))
            whenever(repos.selectChannel(CID)) doReturn channel(members = listOf(jane, john), memberCount = 2)
            // A large channel's response lists no members
            val response = channel(members = emptyList(), memberCount = 1)

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
    fun `onChannelTruncated removes the truncated messages`() = runTest {
        givenActiveChannel()
        val truncatedAt = Date(5_000)

        handler.onChannelTruncated(channel().copy(truncatedAt = truncatedAt))

        verify(repos).deleteChannelMessagesBefore(CID, truncatedAt)
        verify(channelLogic).truncate(truncatedAt)
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
    fun `onChannelMuted replaces the channel's mute in the channel mutes`() = runTest {
        givenActiveChannel()
        val otherMute = randomChannelMute(channel = randomChannel(type = "messaging", id = "other"))
        val oldMute = randomChannelMute(channel = channel())
        globalState.setChannelMutes(listOf(otherMute, oldMute))
        val newMute = randomChannelMute(channel = channel())

        handler.onChannelMuted(CHANNEL_TYPE, CHANNEL_ID, listOf(newMute))

        globalState.channelMutes.value shouldBeEqualTo listOf(otherMute, newMute)
        verify(channelLogic).setMuted(true)
    }

    @Test
    fun `onChannelUnmuted removes the channel's mute from the channel mutes`() = runTest {
        givenActiveChannel()
        val otherMute = randomChannelMute(channel = randomChannel(type = "messaging", id = "other"))
        globalState.setChannelMutes(listOf(otherMute, randomChannelMute(channel = channel())))

        handler.onChannelUnmuted(CHANNEL_TYPE, CHANNEL_ID)

        globalState.channelMutes.value shouldBeEqualTo listOf(otherMute)
        verify(channelLogic).setMuted(false)
    }

    private fun givenActiveChannel() {
        whenever(logic.isActiveChannel(CHANNEL_TYPE, CHANNEL_ID)) doReturn true
    }

    private fun channel(
        name: String = "name",
        updatedAt: Date? = Date(1_000),
        deletedAt: Date? = null,
        members: List<io.getstream.chat.android.models.Member> = emptyList(),
        memberCount: Int = 0,
    ) = randomChannel(
        type = CHANNEL_TYPE,
        id = CHANNEL_ID,
        name = name,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        members = members,
        memberCount = memberCount,
    ).copy(truncatedAt = null, hidden = false)

    private companion object {
        const val CHANNEL_TYPE = "messaging"
        const val CHANNEL_ID = "123"
        const val CID = "$CHANNEL_TYPE:$CHANNEL_ID"
    }
}
