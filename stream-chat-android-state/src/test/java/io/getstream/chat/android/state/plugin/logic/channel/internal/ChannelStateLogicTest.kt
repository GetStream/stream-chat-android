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

package io.getstream.chat.android.state.plugin.logic.channel.internal

import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomChannelUserBannedEvent
import io.getstream.chat.android.client.test.randomTypingStartEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.toChannelData
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelMute
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomDateAfter
import io.getstream.chat.android.randomDateBefore
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMembers
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.message.attachments.internal.AttachmentUrlValidator
import io.getstream.chat.android.state.model.querychannels.pagination.internal.QueryChannelPaginationRequest
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Error
import kotlinx.coroutines.flow.MutableStateFlow
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@Suppress("LargeClass")
internal class ChannelStateLogicTest {

    @BeforeEach
    fun setup() {
        clientState = mock {
            on(it.user) doReturn this@ChannelStateLogicTest.userFlow
        }
        spyMutableGlobalState = spy(MutableGlobalState(user.id))
        channelStateLogic = ChannelStateLogic(
            clientState = clientState,
            mutableState = mutableState,
            globalMutableState = spyMutableGlobalState,
            searchLogic = SearchLogic(mutableState),
            attachmentUrlValidator = attachmentUrlValidator,
            coroutineScope = testCoroutines.scope,
        )
        _messages = emptyMap()
        _members.value = emptyList()
        _membersCount.value = 0
        _unreadCount.value = 0
        _read.value = ChannelUserRead(
            user = user,
            lastReceivedEventDate = Date(Long.MIN_VALUE),
            unreadMessages = unreadCount,
            lastRead = Date(Long.MIN_VALUE),
            lastReadMessageId = randomString(),
        )
        _channelData.value = randomChannel().toChannelData()
        _reads = emptyMap()
        _insideSearch.value = false
        _watcherCount.value = 0
        _watcherCount.value = 0
        _muted.value = false
    }

    private val userFlow = MutableStateFlow(user)
    private var _messages: Map<String, Message> = emptyMap()
    private val _unreadCount: MutableStateFlow<Int> = MutableStateFlow(0)
    private val unreadCount = randomInt()
    private val _read: MutableStateFlow<ChannelUserRead?> = MutableStateFlow(
        ChannelUserRead(
            user = user,
            lastReceivedEventDate = Date(Long.MIN_VALUE),
            unreadMessages = unreadCount,
            lastRead = Date(Long.MIN_VALUE),
            lastReadMessageId = randomString(),
        ),
    )
    private val _channelData: MutableStateFlow<ChannelData> =
        MutableStateFlow(randomChannel().toChannelData())
    private var _reads: Map<String, ChannelUserRead> = emptyMap()
    private val _insideSearch = MutableStateFlow(false)
    private val _watcherCount = MutableStateFlow(0)
    private val _membersCount = MutableStateFlow(0)
    private val _channelConfig = MutableStateFlow(Config())
    private val channelId = "channelId"
    private val _endOfNewerMessages = MutableStateFlow(true)
    private val _cachedMessages = MutableStateFlow<Map<String, Message>>(emptyMap())
    private val _quotedMessagesMap = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    private val _members = MutableStateFlow<List<Member>>(emptyList())
    private val _muted = MutableStateFlow(false)

    @Suppress("UNCHECKED_CAST")
    private val mutableState: ChannelMutableState = mock { mock ->
        on(mock.unreadCount) doReturn _unreadCount
        on(mock.read) doReturn _read
        on(mock.cid) doReturn randomCID()
        on(mock.channelId) doReturn channelId
        on(mock.channelData) doReturn _channelData
        on(mock.insideSearch) doReturn _insideSearch
        on(mock.watcherCount) doReturn _watcherCount
        on(mock.membersCount) doReturn _membersCount
        on(mock.channelConfig) doReturn _channelConfig
        on(mock.messageList) doReturn MutableStateFlow(emptyList())
        on(mock.endOfNewerMessages) doReturn _endOfNewerMessages
        on(mock.cachedLatestMessages) doReturn _cachedMessages
        on(mock.quotedMessagesMap) doReturn _quotedMessagesMap
        on(mock.members) doReturn _members
        on(mock.muted) doReturn _muted
    }
    private lateinit var clientState: ClientState
    private lateinit var spyMutableGlobalState: MutableGlobalState

    private val attachmentUrlValidator: AttachmentUrlValidator = mock {
        on(it.updateValidAttachmentsUrl(any(), any())) doAnswer { invocationOnMock ->
            invocationOnMock.arguments[0] as List<Message>
        }
    }

    private lateinit var channelStateLogic: ChannelStateLogic

    @Test
    fun `given a message is outdated it should not be upserted`() {
        val createdAt = randomDate()
        val createdLocallyAt = randomDateBefore(createdAt)
        val updatedAt = randomDateAfter(createdAt)
        val oldUpdatedAt = randomDateBefore(updatedAt)
        whenever(mutableState.visibleMessages) doReturn MutableStateFlow(_messages)

        val recentMessage = randomMessage(
            user = User(id = "otherUserId"),
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            updatedAt = updatedAt,
            updatedLocallyAt = updatedAt,
            deletedAt = null,
            silent = false,
            showInChannel = true,
        )
        val oldMessage = randomMessage(
            user = User(id = "otherUserId"),
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            updatedAt = oldUpdatedAt,
            updatedLocallyAt = oldUpdatedAt,
            deletedAt = null,
            silent = false,
            showInChannel = true,
        )

        channelStateLogic.upsertMessage(recentMessage)
        channelStateLogic.upsertMessage(oldMessage)

        // Only the new message is available
        _messages `should not be equal to` mapOf(recentMessage.id to recentMessage)
    }

    @Test
    fun `Given TypingStartEvent contains the currently logged in userId Should not update typing events`() {
        val typingStartEvent = randomTypingStartEvent(user = user)

        channelStateLogic.setTyping(typingStartEvent.user.id, typingStartEvent)

        verify(mutableState, times(0)).updateTypingEvent(any())
        verify(spyMutableGlobalState, times(0)).tryEmitTypingEvent(any(), any())
    }

    @Test
    fun `given loading message by id, message list should enter search state`() {
        val message = randomMessage()
        val channel = randomChannel(messages = listOf(message))
        val request = QueryChannelRequest().withMessages(direction = Pagination.AROUND_ID, messageId = message.id, 30)

        channelStateLogic.propagateChannelQuery(channel, request)

        verify(mutableState).setInsideSearch(true)
    }

    @Test
    fun `given message list refreshed, message list should exit search state`() {
        val message = randomMessage()
        val channel = randomChannel(messages = listOf(message))
        val request = QueryChannelRequest().apply {
            shouldRefresh = true
        }.withMessages(30)

        channelStateLogic.propagateChannelQuery(channel, request)

        verify(mutableState).setInsideSearch(false)
    }

    @Test
    fun `given messageLimit is 0, messages should not be upserted`() {
        val randomMessage = randomMessage()
        val channel: Channel = randomChannel(messages = listOf(randomMessage))
        val request = QueryChannelRequest().withMessages(0)
        channelStateLogic.propagateChannelQuery(channel, request)

        verify(mutableState, times(0)).updateCachedLatestMessages(any())
        verify(mutableState, times(0)).upsertMessages(any())
    }

    @Test
    fun `given inside search should not upsert messages when messages are not coming from scroll update`() {
        _insideSearch.value = true

        val message = randomMessage()

        channelStateLogic.updateDataForChannel(
            randomChannel(messages = listOf(message)),
            shouldRefreshMessages = false,
            scrollUpdate = false,
            isNotificationUpdate = false,
            messageLimit = 30,
        )

        verify(mutableState, times(0)).upsertMessages(any())
    }

    @Test
    fun `given inside search, when new page arrives, should be upserted`() {
        _insideSearch.value = true

        val randomMessage = randomMessage()
        val channel: Channel = randomChannel(messages = listOf(randomMessage))
        val filteringRequest = QueryChannelPaginationRequest(1)
            .withMessages(Pagination.GREATER_THAN, randomString(), 1)

        channelStateLogic.propagateChannelQuery(channel, filteringRequest)
        verify(mutableState).upsertMessages(any())
    }

    @Test
    fun `given inside search, when notification message arrives, should not be upserted`() {
        _insideSearch.value = true

        val randomMessage = randomMessage()
        val channel: Channel = randomChannel(messages = listOf(randomMessage))
        val request = QueryChannelRequest().apply { isNotificationUpdate = true }.withMessages(1)
        channelStateLogic.propagateChannelQuery(channel, request)

        verify(mutableState, times(0)).upsertMessages(any())
    }

    @Test
    fun `given inside search, new message should be added to cached messages`() {
        _insideSearch.value = true

        val randomMessage = randomMessage()
        val channel: Channel = randomChannel(messages = listOf(randomMessage))
        val request = QueryChannelRequest().withMessages(1)
        channelStateLogic.propagateChannelQuery(channel, request)

        verify(mutableState).updateCachedLatestMessages(any())
    }

    @Test
    fun `given refresh messages is true, new messages should replace the old ones`() {
        val messages = listOf(randomMessage(), randomMessage())
        val channel: Channel = randomChannel(messages = messages)
        val request = QueryChannelRequest().apply { shouldRefresh = true }.withMessages(1)
        channelStateLogic.propagateChannelQuery(channel, request)

        verify(mutableState).setMessages(any())
    }

    @Test
    fun `given inside search, if message update comes, should update the message`() {
        _insideSearch.value = true
        val message = randomMessage()
        whenever(mutableState.visibleMessages) doReturn MutableStateFlow(mapOf(message.id to message))
        val updatedMessage = message.copy(
            text = "new text",
            updatedAt = randomDateAfter(message.updatedAt ?: message.updatedLocallyAt ?: NEVER),
            updatedLocallyAt = randomDateAfter(message.updatedLocallyAt ?: message.updatedAt ?: NEVER),
        )

        channelStateLogic.upsertMessage(updatedMessage)

        verify(mutableState).upsertMessages(eq(listOf(updatedMessage)))
    }

    @Test
    fun `Given ChannelUserBannedEvent updates the channel state`() {
        /* Given */
        val originMembers = randomMembers(size = 2) { idx ->
            randomMember(user = randomUser(id = "user_${idx + 1}"), banned = false, banExpires = null, shadowBanned = false)
        }
        _members.value = originMembers
        _membersCount.value = originMembers.size
        val bannedEvent = randomChannelUserBannedEvent(
            cid = mutableState.cid,
            user = originMembers.first().user,
            banExpires = randomDate(),
        )
        val expectedMembers = originMembers.map {
            if (it.user.id != bannedEvent.user.id) {
                it
            } else {
                it.copy(banned = true, banExpires = bannedEvent.expiration)
            }
        }

        /* When */
        channelStateLogic.updateMemberBanned(
            memberUserId = bannedEvent.user.id,
            banned = true,
            banExpires = bannedEvent.expiration,
            shadow = false,
        )

        /* Then */
        verify(mutableState).upsertMembers(eq(expectedMembers))
    }

    @Test
    fun `Given existing poll, When calling upsertPoll for the same poll, Then the poll is updated`() {
        // given
        val originalPoll = randomPoll()
        val message = randomMessage(poll = originalPoll)
        whenever(mutableState.getMessageById(message.id)) doReturn message
        channelStateLogic.upsertMessages(listOf(message), true)
        // when
        val updatedPoll = randomPoll(id = originalPoll.id)
        channelStateLogic.upsertPoll(updatedPoll)
        // then
        channelStateLogic.getPoll(originalPoll.id) `should be equal to` updatedPoll
    }

    @Test
    fun `Given existing poll, When calling deletePoll, Then the poll is removed`() {
        // given
        val poll = randomPoll()
        val message = randomMessage(poll = poll)
        whenever(mutableState.getMessageById(message.id)) doReturn message
        channelStateLogic.upsertMessages(listOf(message), true)
        // when
        channelStateLogic.deletePoll(poll)
        // then
        channelStateLogic.getPoll(poll.id) `should be equal to` null
    }

    @Test
    fun `Given user has no messages, When calling deleteMessagesFromUser, Then no action is taken`() {
        // given
        val userId = randomString()
        val deletedAt = randomDate()
        whenever(mutableState.getMessagesFromUser(userId)) doReturn emptyList()

        // when
        channelStateLogic.deleteMessagesFromUser(userId, hard = true, deletedAt = deletedAt)

        // then
        verify(mutableState, times(0)).deleteMessages(any())
        verify(mutableState, times(0)).upsertMessages(any())
    }

    @Test
    fun `Given user has messages and hardDelete is true, When calling deleteMessagesFromUser, Then messages are removed from state`() {
        // given
        val userId = randomString()
        val user = randomUser(id = userId)
        val deletedAt = randomDate()
        val messagesFromUser = listOf(
            randomMessage(user = user),
            randomMessage(user = user),
            randomMessage(user = user),
        )
        whenever(mutableState.getMessagesFromUser(userId)) doReturn messagesFromUser

        // when
        channelStateLogic.deleteMessagesFromUser(userId, hard = true, deletedAt = deletedAt)

        // then
        verify(mutableState).deleteMessages(eq(messagesFromUser))
        verify(mutableState, times(0)).upsertMessages(any())
    }

    @Test
    fun `Given user has messages and hardDelete is false, When calling deleteMessagesFromUser, Then messages are marked as deleted`() {
        // given
        val userId = randomString()
        val user = randomUser(id = userId)
        val deletedAt = randomDate()
        val messagesFromUser = listOf(
            randomMessage(user = user, deletedAt = null),
            randomMessage(user = user, deletedAt = null),
            randomMessage(user = user, deletedAt = null),
        )
        val expectedMarkedAsDeleted = messagesFromUser.map { it.copy(deletedAt = deletedAt) }
        whenever(mutableState.getMessagesFromUser(userId)) doReturn messagesFromUser

        // when
        channelStateLogic.deleteMessagesFromUser(userId, hard = false, deletedAt = deletedAt)

        // then
        verify(mutableState).upsertMessages(eq(expectedMarkedAsDeleted))
        verify(mutableState, times(0)).deleteMessages(any())
    }

    @Test
    fun `Given message is already processed, When updateCurrentUserRead is called, Then unread count is not updated`() {
        // given
        val initialUnreadCount = 5
        val initialChannelUserRead = randomChannelUserRead(
            user = user,
            lastReceivedEventDate = Date(10L),
            unreadMessages = initialUnreadCount,
            lastRead = Date(10L),
            lastReadMessageId = randomString(),
        )
        _read.value = initialChannelUserRead

        val eventDate = Date(20L)
        val newMessage = randomMessage(
            user = randomUser(id = "anotherUserId"),
            createdAt = eventDate,
            silent = false,
            shadowed = false,
            parentId = null,
        )

        // Process the message once
        channelStateLogic.updateCurrentUserRead(eventDate, newMessage)

        // when - process the same message again
        channelStateLogic.updateCurrentUserRead(eventDate, newMessage)

        // then - should only be called once (from the first call)
        verify(mutableState, times(1)).upsertReads(any())
    }

    @Test
    fun `Given channel is muted, When updateCurrentUserRead is called, Then unread count is not updated`() {
        // given
        val initialUnreadCount = 5
        val initialChannelUserRead = randomChannelUserRead(
            user = user,
            lastReceivedEventDate = Date(10L),
            unreadMessages = initialUnreadCount,
            lastRead = Date(10L),
            lastReadMessageId = randomString(),
        )
        _read.value = initialChannelUserRead
        _muted.value = true

        val eventDate = Date(20L)
        val newMessage = randomMessage(
            user = randomUser(id = "anotherUserId"),
            createdAt = eventDate,
            silent = false,
            shadowed = false,
            parentId = null,
        )

        // when
        channelStateLogic.updateCurrentUserRead(eventDate, newMessage)

        // then
        verify(mutableState, times(0)).upsertReads(any())
    }

    @Test
    fun `Given message is a thread reply not shown in channel, When updateCurrentUserRead is called, Then unread count is not updated`() {
        // given
        val initialUnreadCount = 5
        val initialChannelUserRead = randomChannelUserRead(
            user = user,
            lastReceivedEventDate = Date(10L),
            unreadMessages = initialUnreadCount,
            lastRead = Date(10L),
            lastReadMessageId = randomString(),
        )
        _read.value = initialChannelUserRead

        val eventDate = Date(20L)
        val newMessage = randomMessage(
            user = randomUser(id = "anotherUserId"),
            createdAt = eventDate,
            parentId = randomString(), // Has parent ID
            showInChannel = false, // Not shown in channel
            silent = false,
            shadowed = false,
        )

        // when
        channelStateLogic.updateCurrentUserRead(eventDate, newMessage)

        // then
        verify(mutableState, times(0)).upsertReads(any())
    }

    @Test
    fun `Given message is from current user, When updateCurrentUserRead is called, Then unread count is not updated`() {
        // given
        val initialUnreadCount = 5
        val initialChannelUserRead = randomChannelUserRead(
            user = user,
            lastReceivedEventDate = Date(10L),
            unreadMessages = initialUnreadCount,
            lastRead = Date(10L),
            lastReadMessageId = randomString(),
        )
        _read.value = initialChannelUserRead

        val eventDate = Date(20L)
        val newMessage = randomMessage(
            user = user, // Message from current user
            createdAt = eventDate,
            silent = false,
            shadowed = false,
            parentId = null,
        )

        // when
        channelStateLogic.updateCurrentUserRead(eventDate, newMessage)

        // then
        verify(mutableState, times(0)).upsertReads(any())
    }

    @Test
    fun `Given message is from muted user, When updateCurrentUserRead is called, Then unread count is not updated`() {
        // given
        val mutedUserId = "mutedUserId"
        val mutedUser = randomUser(id = mutedUserId)
        val mute = io.getstream.chat.android.randomMute(target = mutedUser)
        spyMutableGlobalState.setMutedUsers(listOf(mute))

        val initialUnreadCount = 5
        val initialChannelUserRead = randomChannelUserRead(
            user = user,
            lastReceivedEventDate = Date(10L),
            unreadMessages = initialUnreadCount,
            lastRead = Date(10L),
            lastReadMessageId = randomString(),
        )
        _read.value = initialChannelUserRead

        val eventDate = Date(20L)
        val newMessage = randomMessage(
            user = mutedUser,
            createdAt = eventDate,
            silent = false,
            shadowed = false,
            parentId = null,
        )

        // when
        channelStateLogic.updateCurrentUserRead(eventDate, newMessage)

        // then
        verify(mutableState, times(0)).upsertReads(any())
    }

    @Test
    fun `Given message is shadowed, When updateCurrentUserRead is called, Then unread count is not updated`() {
        // given
        val initialUnreadCount = 5
        val initialChannelUserRead = randomChannelUserRead(
            user = user,
            lastReceivedEventDate = Date(10L),
            unreadMessages = initialUnreadCount,
            lastRead = Date(10L),
            lastReadMessageId = randomString(),
        )
        _read.value = initialChannelUserRead

        val eventDate = Date(20L)
        val newMessage = randomMessage(
            user = randomUser(id = "anotherUserId"),
            createdAt = eventDate,
            shadowed = true, // Shadow banned message
            silent = false,
            parentId = null,
        )

        // when
        channelStateLogic.updateCurrentUserRead(eventDate, newMessage)

        // then
        verify(mutableState, times(0)).upsertReads(any())
    }

    @Test
    fun `Given message is silent, When updateCurrentUserRead is called, Then unread count is not updated`() {
        // given
        val initialUnreadCount = 5
        val initialChannelUserRead = randomChannelUserRead(
            user = user,
            lastReceivedEventDate = Date(10L),
            unreadMessages = initialUnreadCount,
            lastRead = Date(10L),
            lastReadMessageId = randomString(),
        )
        _read.value = initialChannelUserRead

        val eventDate = Date(20L)
        val newMessage = randomMessage(
            user = randomUser(id = "anotherUserId"),
            createdAt = eventDate,
            silent = true, // Silent message
            shadowed = false,
            parentId = null,
        )

        // when
        channelStateLogic.updateCurrentUserRead(eventDate, newMessage)

        // then
        verify(mutableState, times(0)).upsertReads(any())
    }

    @Test
    fun `Given event is outdated, When updateCurrentUserRead is called, Then unread count is not updated`() {
        // given
        val initialUnreadCount = 5
        val initialChannelUserRead = randomChannelUserRead(
            user = user,
            lastReceivedEventDate = Date(100L), // Current read state is at 100L
            unreadMessages = initialUnreadCount,
            lastRead = Date(10L),
            lastReadMessageId = randomString(),
        )
        _read.value = initialChannelUserRead

        val eventDate = Date(50L) // Event is older than current read state
        val newMessage = randomMessage(
            user = randomUser(id = "anotherUserId"),
            createdAt = eventDate,
            silent = false,
            shadowed = false,
            parentId = null,
        )

        // when
        channelStateLogic.updateCurrentUserRead(eventDate, newMessage)

        // then
        verify(mutableState, times(0)).upsertReads(any())
    }

    @Test
    fun `Given regular message from another user, When updateCurrentUserRead is called, Then unread count is incremented`() {
        // given
        val initialUnreadCount = 5
        val initialChannelUserRead = randomChannelUserRead(
            user = user,
            lastReceivedEventDate = Date(10L),
            unreadMessages = initialUnreadCount,
            lastRead = Date(10L),
            lastReadMessageId = randomString(),
        )
        _read.value = initialChannelUserRead

        val eventDate = Date(20L)
        val newMessage = randomMessage(
            user = randomUser(id = "anotherUserId"),
            createdAt = eventDate,
            silent = false,
            shadowed = false,
            parentId = null,
        )

        val expectedChannelUserRead = initialChannelUserRead.copy(
            unreadMessages = initialUnreadCount + 1,
            lastReceivedEventDate = eventDate,
        )

        // when
        channelStateLogic.updateCurrentUserRead(eventDate, newMessage)

        // then
        verify(mutableState).upsertReads(eq(listOf(expectedChannelUserRead)))
    }

    @Test
    fun `Given thread reply shown in channel from another user, When updateCurrentUserRead is called, Then unread count is incremented`() {
        // given
        val initialUnreadCount = 5
        val initialChannelUserRead = randomChannelUserRead(
            user = user,
            lastReceivedEventDate = Date(10L),
            unreadMessages = initialUnreadCount,
            lastRead = Date(10L),
            lastReadMessageId = randomString(),
        )
        _read.value = initialChannelUserRead

        val eventDate = Date(20L)
        val newMessage = randomMessage(
            user = randomUser(id = "anotherUserId"),
            createdAt = eventDate,
            parentId = randomString(), // Has parent ID
            showInChannel = true, // Shown in channel
            silent = false,
            shadowed = false,
        )

        val expectedChannelUserRead = initialChannelUserRead.copy(
            unreadMessages = initialUnreadCount + 1,
            lastReceivedEventDate = eventDate,
        )

        // when
        channelStateLogic.updateCurrentUserRead(eventDate, newMessage)

        // then
        verify(mutableState).upsertReads(eq(listOf(expectedChannelUserRead)))
    }

    @Test
    fun `Given no current read state exists, When updateCurrentUserRead is called, Then unread count is not updated`() {
        // given - no current read state
        _read.value = null

        val eventDate = Date(20L)
        val newMessage = randomMessage(
            user = randomUser(id = "anotherUserId"),
            createdAt = eventDate,
            silent = false,
            shadowed = false,
            parentId = null,
        )

        // when
        channelStateLogic.updateCurrentUserRead(eventDate, newMessage)

        // then - should not crash and should not call upsertReads
        verify(mutableState, times(0)).upsertReads(any())
    }

    @Test
    fun `Given channel is in global mutes, When syncMuteState is called, Then setMuted is called with true`() {
        // given
        val cid = mutableState.cid
        val (type, id) = cid.cidToTypeAndId()
        val channelMute = randomChannelMute(
            channel = randomChannel(type = type, id = id),
        )
        spyMutableGlobalState.setChannelMutes(listOf(channelMute))

        // when
        channelStateLogic.syncMuteState()

        // then
        // Verify called twice:
        // once in the `init` block (initially)
        verify(mutableState).setMuted(false)
        // and then with true
        verify(mutableState).setMuted(true)
    }

    @Test
    fun `Given channel is not in global mutes, When syncMuteState is called, Then setMuted is called with false`() {
        // given
        val differentCid = randomCID()
        val (type, id) = differentCid.cidToTypeAndId()
        val channelMute = randomChannelMute(
            channel = randomChannel(type = type, id = id),
        )
        spyMutableGlobalState.setChannelMutes(listOf(channelMute))

        // when
        channelStateLogic.syncMuteState()

        // then
        // Verify called twice, once in the `init` block and once in the function call
        verify(mutableState, times(2)).setMuted(false)
    }

    @Test
    fun `Given updateDelivered is called, Then mutable state is upserted`() {
        val read = randomChannelUserRead()

        channelStateLogic.updateDelivered(read)

        verify(mutableState).upsertDelivered(read)
    }

    @Test
    fun `Given local read state is more recent than server, When updateDataForChannel is called, Then local unread count is preserved`() {
        // given - local state has more recent lastReceivedEventDate and higher unread count
        val localLastReceivedEventDate = Date(100L)
        val serverLastReceivedEventDate = Date(50L)
        val localUnreadCount = 5
        val serverUnreadCount = 0

        val localRead = ChannelUserRead(
            user = user,
            lastReceivedEventDate = localLastReceivedEventDate,
            unreadMessages = localUnreadCount,
            lastRead = Date(90L),
            lastReadMessageId = "local-read-id",
            lastDeliveredAt = Date(80L),
            lastDeliveredMessageId = "local-delivered-id",
        )
        _read.value = localRead

        val serverRead = ChannelUserRead(
            user = user.copy(name = "Updated Name"), // Server has updated user data
            lastReceivedEventDate = serverLastReceivedEventDate,
            unreadMessages = serverUnreadCount,
            lastRead = Date(95L), // Server has more recent lastRead
            lastReadMessageId = "server-read-id",
            lastDeliveredAt = Date(85L),
            lastDeliveredMessageId = "server-delivered-id",
        )

        val channel = randomChannel(read = listOf(serverRead))

        // when
        channelStateLogic.updateDataForChannel(
            channel = channel,
            messageLimit = 0,
        )

        // then - local unread count and lastReceivedEventDate should be preserved
        // but other fields should be merged from server
        verify(mutableState).upsertReads(
            eq(
                listOf(
                    localRead.copy(
                        user = serverRead.user, // User data from server
                        lastRead = serverRead.lastRead, // More recent lastRead from server
                        lastReadMessageId = serverRead.lastReadMessageId,
                        lastDeliveredAt = serverRead.lastDeliveredAt,
                        lastDeliveredMessageId = serverRead.lastDeliveredMessageId,
                        // lastReceivedEventDate and unreadMessages are preserved from local
                    ),
                ),
            ),
        )
    }

    @Test
    fun `Given server read state is more recent than local, When updateDataForChannel is called, Then server data is used`() {
        // given - server state has more recent lastReceivedEventDate
        val localLastReceivedEventDate = Date(50L)
        val serverLastReceivedEventDate = Date(100L)
        val localUnreadCount = 3
        val serverUnreadCount = 2

        val localRead = ChannelUserRead(
            user = user,
            lastReceivedEventDate = localLastReceivedEventDate,
            unreadMessages = localUnreadCount,
            lastRead = Date(40L),
            lastReadMessageId = "local-read-id",
        )
        _read.value = localRead

        val serverRead = ChannelUserRead(
            user = user.copy(name = "Updated Name"),
            lastReceivedEventDate = serverLastReceivedEventDate,
            unreadMessages = serverUnreadCount,
            lastRead = Date(95L),
            lastReadMessageId = "server-read-id",
        )

        val channel = randomChannel(read = listOf(serverRead))

        // when
        channelStateLogic.updateDataForChannel(
            channel = channel,
            messageLimit = 0,
        )

        // then - server data should be used entirely (it's more recent)
        verify(mutableState).upsertReads(eq(listOf(serverRead)))
    }

    @Test
    fun `Given local and server read states have same lastReceivedEventDate, When updateDataForChannel is called, Then server data is used`() {
        // given - same lastReceivedEventDate, server should win
        val sameDate = Date(100L)
        val localUnreadCount = 5
        val serverUnreadCount = 2

        val localRead = ChannelUserRead(
            user = user,
            lastReceivedEventDate = sameDate,
            unreadMessages = localUnreadCount,
            lastRead = Date(90L),
            lastReadMessageId = "local-read-id",
        )
        _read.value = localRead

        val serverRead = ChannelUserRead(
            user = user,
            lastReceivedEventDate = sameDate,
            unreadMessages = serverUnreadCount,
            lastRead = Date(95L),
            lastReadMessageId = "server-read-id",
        )

        val channel = randomChannel(read = listOf(serverRead))

        // when
        channelStateLogic.updateDataForChannel(
            channel = channel,
            messageLimit = 0,
        )

        // then - server data should be used (when dates are equal, server wins)
        verify(mutableState).upsertReads(eq(listOf(serverRead)))
    }

    @Test
    fun `Given local read state is more recent, When updateDataForChannel is called with multiple reads, Then only current user read is preserved`() {
        // given
        val otherUser = randomUser(id = "other-user-id")
        val localLastReceivedEventDate = Date(100L)
        val serverLastReceivedEventDate = Date(50L)

        val localRead = ChannelUserRead(
            user = user,
            lastReceivedEventDate = localLastReceivedEventDate,
            unreadMessages = 5,
            lastRead = Date(90L),
            lastReadMessageId = "local-read-id",
        )
        _read.value = localRead

        val serverReads = listOf(
            ChannelUserRead(
                user = user,
                lastReceivedEventDate = serverLastReceivedEventDate,
                unreadMessages = 0,
                lastRead = Date(95L),
                lastReadMessageId = "server-read-id",
            ),
            ChannelUserRead(
                user = otherUser,
                lastReceivedEventDate = Date(200L),
                unreadMessages = 10,
                lastRead = Date(190L),
                lastReadMessageId = "other-read-id",
            ),
        )

        val channel = randomChannel(read = serverReads)

        // when
        channelStateLogic.updateDataForChannel(
            channel = channel,
            messageLimit = 0,
        )

        // then - current user's read should preserve local state, other user's read should use server
        verify(mutableState).upsertReads(
            eq(
                listOf(
                    localRead.copy(
                        lastRead = serverReads[0].lastRead,
                        lastReadMessageId = serverReads[0].lastReadMessageId,
                    ),
                    serverReads[1], // Other user's read unchanged
                ),
            ),
        )
    }

    @Test
    fun `Given no local read state exists, When updateDataForChannel is called, Then server data is used`() {
        // given - no local read state
        _read.value = null

        val serverRead = ChannelUserRead(
            user = user,
            lastReceivedEventDate = Date(100L),
            unreadMessages = 3,
            lastRead = Date(95L),
            lastReadMessageId = "server-read-id",
        )

        val channel = randomChannel(read = listOf(serverRead))

        // when
        channelStateLogic.updateDataForChannel(
            channel = channel,
            messageLimit = 0,
        )

        // then - server data should be used
        verify(mutableState).upsertReads(eq(listOf(serverRead)))
    }

    @Test
    fun `Given local read state is more recent with higher unread count, When updateDataForChannel is called, Then local unread count is preserved even if server has lower count`() {
        // given - scenario: hidden channel received messages, local count incremented
        val localLastReceivedEventDate = Date(100L)
        val serverLastReceivedEventDate = Date(50L)
        val localUnreadCount = 10 // Local has higher count (messages arrived while hidden)
        val serverUnreadCount = 0 // Server has 0 (doesn't track unread for hidden channels)

        val localRead = ChannelUserRead(
            user = user,
            lastReceivedEventDate = localLastReceivedEventDate,
            unreadMessages = localUnreadCount,
            lastRead = Date(30L),
            lastReadMessageId = null,
        )
        _read.value = localRead

        val serverRead = ChannelUserRead(
            user = user,
            lastReceivedEventDate = serverLastReceivedEventDate,
            unreadMessages = serverUnreadCount,
            lastRead = Date(30L),
            lastReadMessageId = null,
        )

        val channel = randomChannel(read = listOf(serverRead))

        // when
        channelStateLogic.updateDataForChannel(
            channel = channel,
            messageLimit = 0,
        )

        // then - local unread count should be preserved
        verify(mutableState).upsertReads(
            eq(
                listOf(
                    localRead.copy(
                        lastRead = serverRead.lastRead,
                        lastReadMessageId = serverRead.lastReadMessageId,
                    ),
                ),
            ),
        )
    }

    @Test
    fun `When propagateQueryError is called for recoverable error, Then channel is marked for recovery and loading is reset`() {
        // when
        channelStateLogic.propagateQueryError(Error.GenericError("Test error"))

        // then
        verify(mutableState).recoveryNeeded = true
        verify(mutableState).setLoadingOlderMessages(false)
        verify(mutableState).setLoadingNewerMessages(false)
    }

    @Test
    fun `When propagateQueryError is called for unrecoverable error, Then channel is not marked for recovery and loading is reset`() {
        // when
        channelStateLogic.propagateQueryError(Error.NetworkError("Test network error", 500))

        // then
        verify(mutableState, never()).recoveryNeeded = any<Boolean>()
        verify(mutableState).setLoadingOlderMessages(false)
        verify(mutableState).setLoadingNewerMessages(false)
    }

    companion object {

        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private val user = randomUser()
    }
}
