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
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

internal class ChannelStateLogicTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @BeforeEach
    fun setup() {
        clientState = mock() {
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
    }

    private val user = randomUser()
    private val userFlow = MutableStateFlow(user)
    private var _messages: Map<String, Message> = emptyMap()
    private val _unreadCount: MutableStateFlow<Int> = MutableStateFlow(0)
    private val unreadCount = randomInt()
    private val _read: MutableStateFlow<ChannelUserRead> = MutableStateFlow(
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
        val createdLocallyAt = randomDateBefore(createdAt.time)
        val updatedAt = randomDateAfter(createdAt.time)
        val oldUpdatedAt = randomDateBefore(updatedAt.time)
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

        verify(mutableState, times(0)).updateTypingEvents(any(), any())
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
            updatedAt = randomDateAfter((message.updatedAt ?: message.updatedLocallyAt ?: NEVER).time),
            updatedLocallyAt = randomDateAfter((message.updatedLocallyAt ?: message.updatedAt ?: NEVER).time),
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
}
