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

import io.getstream.chat.android.client.attachments.AttachmentUrlValidator
import io.getstream.chat.android.client.channel.state.ChannelMutableState
import io.getstream.chat.android.client.models.ChannelData
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomDateAfter
import io.getstream.chat.android.test.randomDateBefore
import kotlinx.coroutines.flow.MutableStateFlow
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

internal class ChannelStateLogicImplTest {

    private val user = randomUser()
    private val _messages: MutableStateFlow<Map<String, Message>> = MutableStateFlow(emptyMap())
    private val _unreadCount: MutableStateFlow<Int> = MutableStateFlow(0)
    private val lastMessageAt: MutableStateFlow<Date?> = MutableStateFlow(null)
    private val _read: MutableStateFlow<ChannelUserRead?> = MutableStateFlow(ChannelUserRead(user))
    private val _channelData: MutableStateFlow<ChannelData?> =
        MutableStateFlow(ChannelData(randomChannel(), emptySet()))
    private val _reads: MutableStateFlow<Map<String, ChannelUserRead>> = MutableStateFlow(emptyMap())
    private val _insideSearch = MutableStateFlow(false)
    private val _watchers: MutableStateFlow<Map<String, User>> = MutableStateFlow(emptyMap())
    private val _watcherCount = MutableStateFlow(0)
    private val _membersCount = MutableStateFlow(0)
    private val _members = MutableStateFlow<Map<String, Member>>(emptyMap())
    private val _channelConfig = MutableStateFlow(Config())

    private val mutableState: ChannelMutableState = mock {
        on(it._messages) doReturn _messages
        on(it._unreadCount) doReturn _unreadCount
        on(it.lastMessageAt) doReturn lastMessageAt
        on(it._read) doReturn _read
        on(it.cid) doReturn randomCID()
        on(it._channelData) doReturn _channelData
        on(it._reads) doReturn _reads
        on(it._insideSearch) doReturn _insideSearch
        on(it.insideSearch) doReturn _insideSearch
        on(it._watchers) doReturn _watchers
        on(it._watcherCount) doReturn _watcherCount
        on(it._members) doReturn _members
        on(it._membersCount) doReturn _membersCount
        on(it._channelConfig) doReturn _channelConfig
    }
    private val globalMutableState: MutableGlobalState = mock {
        on(it.user) doReturn MutableStateFlow(user)
    }
    private val attachmentUrlValidator: AttachmentUrlValidator = mock {
        on(it.updateValidAttachmentsUrl(any(), any())) doAnswer { invocationOnMock ->
            invocationOnMock.arguments[0] as List<Message>
        }
    }

    @BeforeEach
    fun setUp() {
        _messages.value = emptyMap()
        _unreadCount.value = 0
        lastMessageAt.value = null
        _read.value = ChannelUserRead(user)
        _channelData.value = ChannelData(randomChannel(), emptySet())
        _reads.value = emptyMap()
        _insideSearch.value = false
        _watcherCount.value = 0
        _watcherCount.value = 0
    }

    private val channelStateLogicImpl = ChannelStateLogicImpl(mutableState, globalMutableState, attachmentUrlValidator)

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
        _messages.value `should not be equal to` mapOf(recentMessage.id to recentMessage)
    }

    @Test
    fun `new messages should increment the unread count`() {
        val createdAt = randomDate()
        val createdLocallyAt = randomDateBefore(createdAt.time)
        val updatedAt = randomDateAfter(createdAt.time)
        val oldUpdatedAt = randomDateBefore(updatedAt.time)

        whenever(mutableState._read) doReturn MutableStateFlow(
            ChannelUserRead(user, lastMessageSeenDate = Date(Long.MIN_VALUE))
        )

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

        channelStateLogicImpl.incrementUnreadCountIfNecessary(oldMessage)
        channelStateLogicImpl.incrementUnreadCountIfNecessary(recentMessage)

        _unreadCount.value `should be equal to` 2
    }

    @Test
    fun `old messages should NOT increment the unread count`() {
        // The last message is really new.
        whenever(mutableState._read) doReturn MutableStateFlow(
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
        lastMessageAt.value `should be equal to` oldCreatedAt

        channelStateLogicImpl.upsertMessage(recentMessage)
        lastMessageAt.value `should be equal to` createdAt
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
        lastMessageAt.value `should be equal to` createdAt

        channelStateLogicImpl.upsertMessage(oldMessage)
        lastMessageAt.value `should be equal to` createdAt
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

        _messages.value `should be equal to` emptyMap()
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

        _messages.value `should be equal to` mapOf(message.id to message)
    }
}
