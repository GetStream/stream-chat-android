/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.plugin.logic.channel.internal

import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomChannel
import io.getstream.chat.android.client.test.randomMessage
import io.getstream.chat.android.client.test.randomUser
import io.getstream.chat.android.offline.message.attachments.internal.AttachmentUrlValidator
import io.getstream.chat.android.offline.model.channel.ChannelData
import io.getstream.chat.android.offline.model.querychannels.pagination.internal.QueryChannelPaginationRequest
import io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.offline.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomDateAfter
import io.getstream.chat.android.test.randomDateBefore
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.flow.MutableStateFlow
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

internal class ChannelStateLogicImplTest {

    private val user = randomUser()
    private var _messages: Map<String, Message> = emptyMap()
    private val _unreadCount: MutableStateFlow<Int> = MutableStateFlow(0)
    private var _lastMessageAt: Date? = null
    private val unreadCount = randomInt()
    private val _read: MutableStateFlow<ChannelUserRead> = MutableStateFlow(
        ChannelUserRead(user, lastMessageSeenDate = Date(Long.MIN_VALUE), unreadMessages = unreadCount)
    )
    private val _channelData: MutableStateFlow<ChannelData> =
        MutableStateFlow(ChannelData(randomChannel(), emptySet()))
    private var _reads: Map<String, ChannelUserRead> = emptyMap()
    private val _insideSearch = MutableStateFlow(false)
    private var _watchers: Map<String, User> = emptyMap()
    private val _watcherCount = MutableStateFlow(0)
    private val _membersCount = MutableStateFlow(0)
    private var _members: Map<String, Member> = emptyMap()
    private val _channelConfig = MutableStateFlow(Config())
    private val _endOfNewerMessages = MutableStateFlow(true)

    @Suppress("UNCHECKED_CAST")
    private val mutableState: ChannelMutableState = mock { mock ->
        on(mock::rawMessages.get()) doAnswer { _messages }
        on(mock::rawMessages.set(any())) doAnswer { _messages = it.arguments[0] as Map<String, Message> }
        on(mock.unreadCount) doReturn _unreadCount
        on(mock::lastMessageAt.get()) doAnswer { _lastMessageAt }
        on(mock::lastMessageAt.set(any())) doAnswer { _lastMessageAt = it.arguments[0] as Date }
        on(mock.read) doReturn _read
        on(mock.cid) doReturn randomCID()
        on(mock.channelData) doReturn _channelData
        on(mock::rawReads.get()) doAnswer { _reads }
        on(mock::rawReads.set(any())) doAnswer { _reads = it.arguments[0] as Map<String, ChannelUserRead> }
        on(mock.insideSearch) doReturn _insideSearch
        on(mock::rawWatchers.get()) doAnswer { _watchers }
        on(mock::rawWatchers.set(any())) doAnswer { _watchers = it.arguments[0] as Map<String, User> }
        on(mock.watcherCount) doReturn _watcherCount
        on(mock::rawMembers.get()) doAnswer { _members }
        on(mock::rawMembers.set(any())) doAnswer { _members = it.arguments[0] as Map<String, Member> }
        on(mock.membersCount) doReturn _membersCount
        on(mock.channelConfig) doReturn _channelConfig
        on(mock.endOfNewerMessages) doReturn _endOfNewerMessages
    }
    private val globalMutableState: MutableGlobalState = mock {
        on(it.user) doReturn MutableStateFlow(user)
    }
    private val clientState: ClientState = mock {
        on(it.user) doReturn MutableStateFlow(user)
    }
    private val attachmentUrlValidator: AttachmentUrlValidator = mock {
        on(it.updateValidAttachmentsUrl(any(), any())) doAnswer { invocationOnMock ->
            invocationOnMock.arguments[0] as List<Message>
        }
    }

    @BeforeEach
    fun setUp() {
        _messages = emptyMap()
        _unreadCount.value = 0
        _lastMessageAt = null
        _read.value = ChannelUserRead(user, lastMessageSeenDate = Date(Long.MIN_VALUE), unreadMessages = unreadCount)
        _channelData.value = ChannelData(randomChannel(), emptySet())
        _reads = emptyMap()
        _insideSearch.value = false
        _watcherCount.value = 0
        _watcherCount.value = 0
    }

    private val channelStateLogicImpl =
        ChannelStateLogicImpl(mutableState, globalMutableState, clientState, mock(), attachmentUrlValidator)

    @Test
    fun `given a message is outdated it should not be upserted`() {
        val createdAt = randomDate()
        val createdLocallyAt = randomDateBefore(createdAt.time)
        val updatedAt = randomDateAfter(createdAt.time)
        val oldUpdatedAt = randomDateBefore(updatedAt.time)

        val recentMessage = randomMessage(
            user = User(id = "otherUserId"),
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            updatedAt = updatedAt,
            updatedLocallyAt = updatedAt,
            deletedAt = null,
            silent = false,
            showInChannel = true
        )
        val oldMessage = randomMessage(
            user = User(id = "otherUserId"),
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            updatedAt = oldUpdatedAt,
            updatedLocallyAt = oldUpdatedAt,
            deletedAt = null,
            silent = false,
            showInChannel = true
        )

        channelStateLogicImpl.upsertMessage(recentMessage)
        channelStateLogicImpl.upsertMessage(oldMessage)

        // Only the new message is available
        _messages `should not be equal to` mapOf(recentMessage.id to recentMessage)
    }

    @Test
    fun `new messages should increment the unread count`() {
        val createdAt = randomDate()
        val oldCreatedAt = randomDateBefore(createdAt.time)

        val newUnreadCount = randomInt()
        whenever(mutableState.read) doReturn MutableStateFlow(
            ChannelUserRead(
                user = user,
                lastMessageSeenDate = Date(Long.MIN_VALUE),
                unreadMessages = newUnreadCount
            )
        )

        val oldMessage = randomMessage(
            user = User(id = "otherUserId"),
            createdAt = oldCreatedAt,
            createdLocallyAt = oldCreatedAt,
            deletedAt = null,
            silent = false,
            showInChannel = true
        )

        channelStateLogicImpl.incrementUnreadCountIfNecessary(oldMessage)
        verify(mutableState).setUnreadCount(newUnreadCount + 1)
    }

    @Test
    fun `old messages should NOT increment the unread count`() {
        // The last message is really new.
        whenever(mutableState.read) doReturn MutableStateFlow(
            ChannelUserRead(user, lastMessageSeenDate = Date(Long.MAX_VALUE))
        )

        repeat(3) {
            channelStateLogicImpl.incrementUnreadCountIfNecessary(randomMessage())
        }

        _unreadCount.value `should be equal to` 0
    }

    @Test
    fun `given new messages were upserted the newest date one should be become the new lastMessageAt - older and newer message`() {
        val createdAt = randomDate()
        val oldCreatedAt = randomDateBefore(createdAt.time)
        val updatedAt = randomDateAfter(createdAt.time)

        val recentMessage = randomMessage(
            user = User(id = "otherUserId"),
            createdAt = createdAt,
            createdLocallyAt = createdAt,
            updatedAt = updatedAt,
            updatedLocallyAt = updatedAt,
            deletedAt = null,
            silent = false,
            showInChannel = true
        )
        val oldMessage = randomMessage(
            user = User(id = "otherUserId"),
            createdAt = oldCreatedAt,
            createdLocallyAt = oldCreatedAt,
            updatedAt = updatedAt,
            updatedLocallyAt = updatedAt,
            deletedAt = null,
            silent = false,
            showInChannel = true
        )

        channelStateLogicImpl.upsertMessage(oldMessage)
        _lastMessageAt `should be equal to` oldCreatedAt

        channelStateLogicImpl.upsertMessage(recentMessage)
        _lastMessageAt `should be equal to` createdAt
    }

    @Test
    fun `given new messages were upserted the newest date one should be become the new lastMessageAt - newer and old message`() {
        val createdAt = randomDate()
        val oldCreatedAt = randomDateBefore(createdAt.time)
        val updatedAt = randomDateAfter(createdAt.time)

        val recentMessage = randomMessage(
            user = User(id = "otherUserId"),
            createdAt = createdAt,
            createdLocallyAt = createdAt,
            updatedAt = updatedAt,
            updatedLocallyAt = updatedAt,
            deletedAt = null,
            silent = false,
            showInChannel = true
        )
        val oldMessage = randomMessage(
            user = User(id = "otherUserId"),
            createdAt = oldCreatedAt,
            createdLocallyAt = oldCreatedAt,
            updatedAt = updatedAt,
            updatedLocallyAt = updatedAt,
            deletedAt = null,
            silent = false,
            showInChannel = true
        )

        channelStateLogicImpl.upsertMessage(recentMessage)
        _lastMessageAt `should be equal to` createdAt

        channelStateLogicImpl.upsertMessage(oldMessage)
        _lastMessageAt `should be equal to` createdAt
    }

    @Test
    fun `given inside search should not upsert messages when messages are not coming from scroll update`() {
        _insideSearch.value = true

        val message = randomMessage()

        channelStateLogicImpl.updateDataFromChannel(
            randomChannel(messages = listOf(message)),
            shouldRefreshMessages = false,
            scrollUpdate = false
        )

        _messages `should be equal to` emptyMap()
    }

    @Test
    fun `given inside search should upsert messages when messages are coming from scroll update`() {
        _insideSearch.value = true

        val message = randomMessage()

        channelStateLogicImpl.updateDataFromChannel(
            randomChannel(messages = listOf(message)),
            shouldRefreshMessages = false,
            scrollUpdate = true
        )

        _messages `should be equal to` mapOf(message.id to message)
    }

    @Test
    fun `given message should be refreshed, old messages should be clean`() {
        val message = randomMessage()
        val message2 = randomMessage()

        channelStateLogicImpl.updateDataFromChannel(
            randomChannel(messages = listOf(message)),
            shouldRefreshMessages = false,
            scrollUpdate = true
        )

        channelStateLogicImpl.updateDataFromChannel(
            randomChannel(messages = listOf(message2)),
            shouldRefreshMessages = true,
            scrollUpdate = true
        )

        _messages `should be equal to` mapOf(message2.id to message2)
    }

    @Test
    fun `given a scroll messages come while inside search, messages should be added`() {
        _insideSearch.value = true

        val randomMessage = randomMessage()
        val channel: Channel = randomChannel(messages = listOf(randomMessage))
        val filteringRequest = QueryChannelPaginationRequest(1)
            .withMessages(Pagination.GREATER_THAN, randomString(), 1)

        channelStateLogicImpl.propagateChannelQuery(channel, filteringRequest)
        verify(mutableState).rawMessages = any()
    }
}
