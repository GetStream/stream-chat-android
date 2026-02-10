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

import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.NotificationInviteAcceptedEvent
import io.getstream.chat.android.client.events.NotificationInviteRejectedEvent
import io.getstream.chat.android.client.events.NotificationThreadMessageNewEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.processPoll
import io.getstream.chat.android.client.extensions.internal.toMessageReminderInfo
import io.getstream.chat.android.client.internal.state.event.handler.internal.utils.toChannelUserRead
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateImpl
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.test.randomAnswerCastedEvent
import io.getstream.chat.android.client.test.randomChannelDeletedEvent
import io.getstream.chat.android.client.test.randomChannelHiddenEvent
import io.getstream.chat.android.client.test.randomChannelUpdatedByUserEvent
import io.getstream.chat.android.client.test.randomChannelUpdatedEvent
import io.getstream.chat.android.client.test.randomChannelUserBannedEvent
import io.getstream.chat.android.client.test.randomChannelVisibleEvent
import io.getstream.chat.android.client.test.randomMarkAllReadEvent
import io.getstream.chat.android.client.test.randomMemberAddedEvent
import io.getstream.chat.android.client.test.randomMemberRemovedEvent
import io.getstream.chat.android.client.test.randomMessageDeliveredEvent
import io.getstream.chat.android.client.test.randomMessageReadEvent
import io.getstream.chat.android.client.test.randomMessageUpdateEvent
import io.getstream.chat.android.client.test.randomNewMessageEvent
import io.getstream.chat.android.client.test.randomNotificationAddedToChannelEvent
import io.getstream.chat.android.client.test.randomNotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.test.randomNotificationChannelTruncatedEvent
import io.getstream.chat.android.client.test.randomNotificationMarkReadEvent
import io.getstream.chat.android.client.test.randomNotificationMarkUnreadEvent
import io.getstream.chat.android.client.test.randomNotificationMessageNewEvent
import io.getstream.chat.android.client.test.randomNotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.test.randomPollClosedEvent
import io.getstream.chat.android.client.test.randomPollDeletedEvent
import io.getstream.chat.android.client.test.randomPollUpdatedEvent
import io.getstream.chat.android.client.test.randomReactionNewEvent
import io.getstream.chat.android.client.test.randomReminderCreatedEvent
import io.getstream.chat.android.client.test.randomReminderDeletedEvent
import io.getstream.chat.android.client.test.randomReminderUpdatedEvent
import io.getstream.chat.android.client.test.randomTypingStartEvent
import io.getstream.chat.android.client.test.randomTypingStopEvent
import io.getstream.chat.android.client.test.randomUserMessagesDeletedEvent
import io.getstream.chat.android.client.test.randomUserStartWatchingEvent
import io.getstream.chat.android.client.test.randomVoteCastedEvent
import io.getstream.chat.android.client.test.randomVoteChangedEvent
import io.getstream.chat.android.client.test.randomVoteRemovedEvent
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelMute
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomMessageReminder
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomThreadInfo
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@Suppress("LargeClass")
@OptIn(ExperimentalCoroutinesApi::class)
internal class ChannelEventHandlerImplTest {

    private val cid = randomCID()
    private val currentUserId = randomString()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var state: ChannelStateImpl
    private lateinit var globalState: MutableGlobalState
    private lateinit var getCurrentUserId: () -> String?
    private lateinit var handler: ChannelEventHandlerImpl

    @BeforeEach
    fun setUp() {
        state = mock {
            on(it.cid) doReturn cid
            on(it.insideSearch) doReturn MutableStateFlow(false)
        }
        globalState = mock()
        getCurrentUserId = { currentUserId }
        handler = ChannelEventHandlerImpl(
            cid = cid,
            state = state,
            globalState = globalState,
            coroutineScope = testScope,
            getCurrentUserId = getCurrentUserId,
        )
    }

    // region NewMessageEvent tests

    @Test
    fun `When NewMessageEvent from current user is handled, Then message is upserted preserving createdLocallyAt`() {
        val createdLocallyAt = Date(1000L)
        val message = randomMessage(id = randomString(), user = randomUser(id = currentUserId))
        val existingMessage = message.copy(createdLocallyAt = createdLocallyAt)
        whenever(state.getMessageById(message.id)).thenReturn(existingMessage)
        val event = randomNewMessageEvent(cid = cid, message = message, user = randomUser(id = currentUserId))

        handler.handle(event)

        verify(state).upsertMessage(existingMessage)
        verify(state).updateCurrentUserRead(event.createdAt, existingMessage)
    }

    @Test
    fun `When NewMessageEvent from other user is handled, Then message is upserted without preserving createdLocallyAt`() {
        val message = randomMessage(user = randomUser(id = randomString()))
        val event = randomNewMessageEvent(cid = cid, message = message, user = randomUser())

        handler.handle(event)

        verify(state).upsertMessage(message)
        verify(state).updateCurrentUserRead(event.createdAt, message)
    }

    @Test
    fun `When NewMessageEvent with non-shadowed message is handled, Then channel is unhidden`() {
        val message = randomMessage(shadowed = false)
        val event = randomNewMessageEvent(cid = cid, message = message)

        handler.handle(event)

        verify(state).setHidden(false)
    }

    @Test
    fun `When NewMessageEvent with shadowed message is handled, Then channel is not unhidden`() {
        val message = randomMessage(shadowed = true)
        val event = randomNewMessageEvent(cid = cid, message = message)

        handler.handle(event)

        verify(state, never()).setHidden(false)
    }

    @Test
    fun `When NewMessageEvent with channel message count is handled, Then message count is updated`() {
        val messageCount = positiveRandomInt()
        val event = randomNewMessageEvent(cid = cid, channelMessageCount = messageCount)

        handler.handle(event)

        verify(state).setMessageCount(messageCount)
    }

    @Test
    fun `When NewMessageEvent is handled and inside search, Then message is upserted to cached messages`() {
        val message = randomMessage()
        whenever(state.insideSearch).thenReturn(MutableStateFlow(true))
        val event = randomNewMessageEvent(cid = cid, message = message)

        handler.handle(event)

        verify(state).upsertCachedMessage(message)
        verify(state, never()).upsertMessage(any())
    }

    @Test
    fun `When NewMessageEvent with pinned message is handled, Then message is added to pinned messages`() {
        val pinnedAt = Date()
        val message = randomMessage(pinned = true, pinnedAt = pinnedAt, deletedAt = null, deletedForMe = false)
        val event = randomNewMessageEvent(cid = cid, message = message)
        val now = pinnedAt.time + 1000

        handler = ChannelEventHandlerImpl(
            cid = cid,
            state = state,
            globalState = globalState,
            coroutineScope = testScope,
            getCurrentUserId = getCurrentUserId,
            now = { now },
        )

        handler.handle(event)

        verify(state).addPinnedMessage(message)
    }

    @Test
    fun `When NewMessageEvent is handled, Then lastMessageAt is updated`() {
        val message = randomMessage()
        val event = randomNewMessageEvent(cid = cid, message = message)

        handler.handle(event)

        verify(state).updateLastMessageAt(message)
    }

    // endregion

    // region NotificationMessageNewEvent tests

    @Test
    fun `When NotificationMessageNewEvent is handled and not inside search, Then message is upserted`() {
        val message = randomMessage()
        whenever(state.insideSearch).thenReturn(MutableStateFlow(false))
        val event = randomNotificationMessageNewEvent(cid = cid, message = message)

        handler.handle(event)

        verify(state).upsertMessage(message)
        verify(state).updateCurrentUserRead(event.createdAt, message)
        verify(state).setHidden(false)
    }

    @Test
    fun `When NotificationMessageNewEvent is handled and inside search, Then message is upserted to cached messages`() {
        val message = randomMessage()
        whenever(state.insideSearch).thenReturn(MutableStateFlow(true))
        val event = randomNotificationMessageNewEvent(cid = cid, message = message)

        handler.handle(event)

        verify(state).upsertCachedMessage(message)
        verify(state, never()).upsertMessage(any())
        verify(state).updateCurrentUserRead(event.createdAt, message)
        verify(state).setHidden(false)
    }

    @Test
    fun `When NotificationMessageNewEvent with shadowed message is handled, Then channel is not unhidden`() {
        val message = randomMessage(shadowed = true)
        val event = randomNotificationMessageNewEvent(cid = cid, message = message)

        handler.handle(event)

        verify(state, never()).setHidden(false)
    }

    @Test
    fun `When NotificationMessageNewEvent with pinned message is handled, Then message is added to pinned messages`() {
        val pinnedAt = Date()
        val message = randomMessage(pinned = true, pinnedAt = pinnedAt, deletedAt = null, deletedForMe = false)
        val event = randomNotificationMessageNewEvent(cid = cid, message = message)
        val now = pinnedAt.time + 1000

        handler = ChannelEventHandlerImpl(
            cid = cid,
            state = state,
            globalState = globalState,
            coroutineScope = testScope,
            getCurrentUserId = getCurrentUserId,
            now = { now },
        )

        handler.handle(event)

        verify(state).addPinnedMessage(message)
    }

    @Test
    fun `When NotificationMessageNewEvent is handled, Then lastMessageAt is updated`() {
        val message = randomMessage()
        val event = randomNotificationMessageNewEvent(cid = cid, message = message)

        handler.handle(event)

        verify(state).updateLastMessageAt(message)
    }

    // endregion

    // region MessageUpdatedEvent tests

    @Test
    fun `When MessageUpdatedEvent is handled, Then message is updated with enriched poll and ownReactions`() {
        val poll = randomPoll()
        val ownReactions = listOf(randomReaction())
        val originalMessage = randomMessage(poll = poll, ownReactions = ownReactions)
        val updatedMessage = originalMessage.copy(text = "Updated", poll = null)
        whenever(state.getMessageById(originalMessage.id)).thenReturn(originalMessage)
        val event = randomMessageUpdateEvent(cid = cid, message = updatedMessage)

        handler.handle(event)

        val expectedMessage = updatedMessage.copy(poll = poll, ownReactions = ownReactions)
        verify(state).updateMessage(expectedMessage)
        verify(state).updateQuotedMessageReferences(expectedMessage)
    }

    @Test
    fun `When MessageUpdatedEvent is handled with missing poll, Then original poll is preserved`() {
        val originalPoll = randomPoll()
        val ownReactions = listOf(randomReaction())
        val originalMessage = randomMessage(poll = originalPoll, ownReactions = ownReactions)
        val updatedMessage = originalMessage.copy(poll = null, text = "Updated")
        whenever(state.getMessageById(originalMessage.id)).thenReturn(originalMessage)
        val event = randomMessageUpdateEvent(cid = cid, message = updatedMessage)

        handler.handle(event)

        val expectedMessage = updatedMessage.copy(poll = originalPoll, ownReactions = ownReactions)
        verify(state).updateMessage(expectedMessage)
    }

    @Test
    fun `When MessageUpdatedEvent is handled for non-existing message, Then message is updated`() {
        val message = randomMessage()
        whenever(state.getMessageById(message.id)).thenReturn(null)
        val event = randomMessageUpdateEvent(cid = cid, message = message)

        handler.handle(event)

        verify(state).updateMessage(message)
    }

    @Test
    fun `When MessageUpdatedEvent with pinned message is handled, Then message is added to pinned messages`() {
        val pinnedAt = Date()
        val message = randomMessage(pinned = true, pinnedAt = pinnedAt, deletedAt = null, deletedForMe = false)
        whenever(state.getMessageById(message.id)).thenReturn(message)
        val event = randomMessageUpdateEvent(cid = cid, message = message)
        val now = pinnedAt.time + 1000

        handler = ChannelEventHandlerImpl(
            cid = cid,
            state = state,
            globalState = globalState,
            coroutineScope = testScope,
            getCurrentUserId = getCurrentUserId,
            now = { now },
        )

        handler.handle(event)

        verify(state).addPinnedMessage(message)
    }

    @Test
    fun `When MessageUpdatedEvent with unpinned message is handled, Then message is removed from pinned messages`() {
        val message = randomMessage(pinned = false, pinnedAt = null)
        whenever(state.getMessageById(message.id)).thenReturn(message)
        val event = randomMessageUpdateEvent(cid = cid, message = message)

        handler.handle(event)

        verify(state).deletePinnedMessage(message.id)
    }

    // endregion

    // region MessageDeletedEvent tests

    @Test
    fun `When MessageDeletedEvent with hard delete is handled, Then message is deleted`() {
        val message = randomMessage()
        val event = MessageDeletedEvent(
            type = EventType.MESSAGE_DELETED,
            createdAt = randomDate(),
            rawCreatedAt = "",
            user = randomUser(),
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            message = message,
            hardDelete = true,
            deletedForMe = false,
            channelMessageCount = null,
        )

        handler.handle(event)

        verify(state).deleteMessage(message.id)
        verify(state).deleteQuotedMessageReferences(message.id)
        verify(state, never()).updateMessage(any())
    }

    @Test
    fun `When MessageDeletedEvent with soft delete is handled, Then message is updated`() {
        val message = randomMessage()
        whenever(state.getMessageById(message.id)).thenReturn(message)
        val event = MessageDeletedEvent(
            type = EventType.MESSAGE_DELETED,
            createdAt = randomDate(),
            rawCreatedAt = "",
            user = randomUser(),
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            message = message,
            hardDelete = false,
            deletedForMe = false,
            channelMessageCount = null,
        )

        handler.handle(event)

        val expectedMessage = message.copy(ownReactions = message.ownReactions)
        verify(state).updateMessage(expectedMessage)
        verify(state).updateQuotedMessageReferences(expectedMessage)
        verify(state, never()).deleteMessage(any())
    }

    @Test
    fun `When MessageDeletedEvent with channel message count is handled, Then message count is updated`() {
        val messageCount = positiveRandomInt()
        val event = MessageDeletedEvent(
            type = EventType.MESSAGE_DELETED,
            createdAt = randomDate(),
            rawCreatedAt = "",
            user = randomUser(),
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            message = randomMessage(),
            hardDelete = false,
            channelMessageCount = messageCount,
            deletedForMe = false,
        )

        handler.handle(event)

        verify(state).setMessageCount(messageCount)
    }

    @Test
    fun `When MessageDeletedEvent with pinned message is handled, Then pinned message is deleted`() {
        val message = randomMessage(pinned = true)
        val event = MessageDeletedEvent(
            type = EventType.MESSAGE_DELETED,
            createdAt = randomDate(),
            rawCreatedAt = "",
            user = randomUser(),
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            message = message,
            hardDelete = false,
            deletedForMe = false,
            channelMessageCount = null,
        )

        handler.handle(event)

        verify(state).deletePinnedMessage(message.id)
    }

    // endregion

    // region NotificationThreadMessageNewEvent tests

    @Test
    fun `When NotificationThreadMessageNewEvent is handled with showInChannel, Then message is upserted`() {
        val message = randomMessage(showInChannel = true, shadowed = false)
        val event = NotificationThreadMessageNewEvent(
            type = EventType.NOTIFICATION_THREAD_MESSAGE_NEW,
            createdAt = randomDate(),
            rawCreatedAt = "",
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            channel = randomChannel(),
            message = message,
            unreadThreads = positiveRandomInt(),
            unreadThreadMessages = positiveRandomInt(),
        )

        handler.handle(event)

        verify(state).upsertMessage(message)
        verify(state).setHidden(false)
    }

    @Test
    fun `When NotificationThreadMessageNewEvent is handled without showInChannel, Then message is not upserted`() {
        val message = randomMessage(showInChannel = false)
        val event = NotificationThreadMessageNewEvent(
            type = EventType.NOTIFICATION_THREAD_MESSAGE_NEW,
            createdAt = randomDate(),
            rawCreatedAt = "",
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            channel = randomChannel(),
            message = message,
            unreadThreads = positiveRandomInt(),
            unreadThreadMessages = positiveRandomInt(),
        )

        handler.handle(event)

        verify(state, never()).upsertMessage(any())
        verify(state, never()).setHidden(any())
    }

    @Test
    fun `When NotificationThreadMessageNewEvent is handled and inside search, Then message is upserted to cached`() {
        val message = randomMessage(showInChannel = true, shadowed = false)
        whenever(state.insideSearch).thenReturn(MutableStateFlow(true))
        val event = NotificationThreadMessageNewEvent(
            type = EventType.NOTIFICATION_THREAD_MESSAGE_NEW,
            createdAt = randomDate(),
            rawCreatedAt = "",
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            channel = randomChannel(),
            message = message,
            unreadThreads = positiveRandomInt(),
            unreadThreadMessages = positiveRandomInt(),
        )

        handler.handle(event)

        verify(state).upsertCachedMessage(message)
        verify(state, never()).upsertMessage(any())
        verify(state).setHidden(false)
    }

    @Test
    fun `When NotificationThreadMessageNewEvent with shadowed message is handled, Then channel is not unhidden`() {
        val message = randomMessage(showInChannel = true, shadowed = true)
        val event = NotificationThreadMessageNewEvent(
            type = EventType.NOTIFICATION_THREAD_MESSAGE_NEW,
            createdAt = randomDate(),
            rawCreatedAt = "",
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            channel = randomChannel(),
            message = message,
            unreadThreads = positiveRandomInt(),
            unreadThreadMessages = positiveRandomInt(),
        )

        handler.handle(event)

        verify(state).upsertMessage(message)
        verify(state, never()).setHidden(any())
    }

    // endregion

    // region Reaction events tests

    @Test
    fun `When ReactionNewEvent is handled, Then message is updated with preserved ownReactions`() {
        val ownReactions = listOf(randomReaction())
        val message = randomMessage(ownReactions = ownReactions)
        whenever(state.getMessageById(message.id)).thenReturn(message)
        val event = randomReactionNewEvent(cid = cid, message = message)

        handler.handle(event)

        verify(state).updateMessage(message.copy(ownReactions = ownReactions))
    }

    @Test
    fun `When ReactionUpdateEvent is handled, Then message is updated`() {
        val ownReactions = listOf(randomReaction())
        val message = randomMessage(latestReactions = listOf(randomReaction()), ownReactions = ownReactions)
        whenever(state.getMessageById(message.id)).thenReturn(message)
        val event = ReactionUpdateEvent(
            type = EventType.REACTION_UPDATED,
            createdAt = randomDate(),
            rawCreatedAt = "",
            user = randomUser(),
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            message = message,
            reaction = message.latestReactions.first(),
        )

        handler.handle(event)

        verify(state).updateMessage(message.copy(ownReactions = ownReactions))
    }

    @Test
    fun `When ReactionDeletedEvent is handled, Then message is updated`() {
        val ownReactions = listOf(randomReaction())
        val message = randomMessage(latestReactions = listOf(randomReaction()), ownReactions = ownReactions)
        whenever(state.getMessageById(message.id)).thenReturn(message)
        val event = ReactionDeletedEvent(
            type = EventType.REACTION_DELETED,
            createdAt = randomDate(),
            rawCreatedAt = "",
            user = randomUser(),
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            message = message,
            reaction = message.latestReactions.first(),
        )

        handler.handle(event)

        verify(state).updateMessage(message.copy(ownReactions = ownReactions))
    }

    @Test
    fun `When ReactionNewEvent is handled for non-existing message, Then message is not updated`() {
        val message = randomMessage()
        whenever(state.getMessageById(message.id)).thenReturn(null)
        val event = randomReactionNewEvent(cid = cid, message = message)

        handler.handle(event)

        verify(state, never()).updateMessage(any())
    }

    // endregion

    // region Member events tests

    @Test
    fun `When MemberAddedEvent for current user is handled, Then member is added and membership is set`() {
        val member = randomMember(user = randomUser(id = currentUserId))
        val event = randomMemberAddedEvent(cid = cid, member = member)

        handler.handle(event)

        verify(state).addMember(member)
        verify(state).setMembership(member)
    }

    @Test
    fun `When MemberAddedEvent for other user is handled, Then only member is added`() {
        val member = randomMember(user = randomUser(id = randomString()))
        val event = randomMemberAddedEvent(cid = cid, member = member)

        handler.handle(event)

        verify(state).addMember(member)
        verify(state, never()).setMembership(any())
    }

    @Test
    fun `When MemberRemovedEvent for current user is handled, Then member is deleted and membership is removed`() {
        val member = randomMember(user = randomUser(id = currentUserId))
        val event = randomMemberRemovedEvent(cid = cid, member = member)

        handler.handle(event)

        verify(state).deleteMember(member.getUserId())
        verify(state).deleteMembership()
    }

    @Test
    fun `When MemberRemovedEvent for other user is handled, Then only member is deleted`() {
        val member = randomMember(user = randomUser(id = randomString()))
        val event = randomMemberRemovedEvent(cid = cid, member = member)

        handler.handle(event)

        verify(state).deleteMember(member.getUserId())
        verify(state, never()).deleteMembership()
    }

    @Test
    fun `When MemberUpdatedEvent for current user is handled, Then member and membership are updated`() {
        val member = randomMember(user = randomUser(id = currentUserId))
        val event = MemberUpdatedEvent(
            type = EventType.MEMBER_UPDATED,
            createdAt = randomDate(),
            rawCreatedAt = "",
            user = randomUser(),
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            member = member,
        )

        handler.handle(event)

        verify(state).upsertMember(member)
        verify(state).setMembership(member)
    }

    @Test
    fun `When MemberUpdatedEvent for other user is handled, Then only member is updated`() {
        val member = randomMember(user = randomUser(id = randomString()))
        val event = MemberUpdatedEvent(
            type = EventType.MEMBER_UPDATED,
            createdAt = randomDate(),
            rawCreatedAt = "",
            user = randomUser(),
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            member = member,
        )

        handler.handle(event)

        verify(state).upsertMember(member)
        verify(state, never()).setMembership(any())
    }

    @Test
    fun `When NotificationAddedToChannelEvent is handled, Then members are upserted`() {
        val members = listOf(randomMember(), randomMember())
        val event = randomNotificationAddedToChannelEvent(
            cid = cid,
            channel = randomChannel(members = members),
        )

        handler.handle(event)

        verify(state).upsertMembers(members)
    }

    @Test
    fun `When NotificationRemovedFromChannelEvent is handled, Then members and watchers are set`() {
        val members = listOf(randomMember(), randomMember())
        val watchers = listOf(randomUser(), randomUser())
        val watcherCount = watchers.size
        val event = randomNotificationRemovedFromChannelEvent(
            cid = cid,
            channel = randomChannel(
                members = members,
                memberCount = members.size,
                watchers = watchers,
                watcherCount = watcherCount,
            ),
        )

        handler.handle(event)

        verify(state).setMemberCount(members.size)
        verify(state).setMembers(members)
        verify(state).setWatchers(watchers, watcherCount)
    }

    // endregion

    // region Watcher events tests

    @Test
    fun `When UserStartWatchingEvent is handled, Then watcher is upserted`() {
        val event = randomUserStartWatchingEvent(cid = cid)

        handler.handle(event)

        verify(state).upsertWatcher(event)
    }

    @Test
    fun `When UserStopWatchingEvent is handled, Then watcher is deleted`() {
        val event = UserStopWatchingEvent(
            type = EventType.USER_WATCHING_STOP,
            createdAt = randomDate(),
            rawCreatedAt = "",
            cid = cid,
            watcherCount = randomInt(),
            channelType = randomString(),
            channelId = randomString(),
            user = randomUser(),
        )

        handler.handle(event)

        verify(state).deleteWatcher(event)
    }

    // endregion

    // region Channel update events tests

    @Test
    fun `When ChannelUpdatedEvent is handled, Then channel data is updated`() {
        val event = randomChannelUpdatedEvent(cid = cid)

        handler.handle(event)

        verify(state).updateChannelData(event)
    }

    @Test
    fun `When ChannelUpdatedByUserEvent is handled, Then channel data is updated`() {
        val event = randomChannelUpdatedByUserEvent(cid = cid)

        handler.handle(event)

        verify(state).updateChannelData(event)
    }

    @Test
    fun `When ChannelHiddenEvent with clear history is handled, Then channel is hidden and messages are removed`() {
        val event = randomChannelHiddenEvent(cid = cid, clearHistory = true)

        handler.handle(event)

        verify(state).setHidden(true)
        verify(state).removeMessagesBefore(event.createdAt)
    }

    @Test
    fun `When ChannelHiddenEvent without clear history is handled, Then only channel is hidden`() {
        val event = randomChannelHiddenEvent(cid = cid, clearHistory = false)

        handler.handle(event)

        verify(state).setHidden(true)
        verify(state, never()).removeMessagesBefore(any(), any())
    }

    @Test
    fun `When ChannelVisibleEvent is handled, Then channel is unhidden`() {
        val event = randomChannelVisibleEvent(cid = cid)

        handler.handle(event)

        verify(state).setHidden(false)
    }

    @Test
    fun `When ChannelDeletedEvent is handled, Then messages are removed and channel is deleted`() {
        val event = randomChannelDeletedEvent(cid = cid)

        handler.handle(event)

        verify(state).removeMessagesBefore(event.createdAt)
        verify(state).deleteChannel(event.createdAt)
    }

    @Test
    fun `When ChannelTruncatedEvent is handled, Then messages are removed with system message`() {
        val systemMessage = randomMessage()
        val event = ChannelTruncatedEvent(
            type = EventType.CHANNEL_TRUNCATED,
            createdAt = randomDate(),
            rawCreatedAt = "",
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            user = randomUser(),
            message = systemMessage,
            channel = randomChannel(),
        )

        handler.handle(event)

        verify(state).removeMessagesBefore(event.createdAt, systemMessage)
    }

    @Test
    fun `When NotificationChannelTruncatedEvent is handled, Then messages are removed`() {
        val event = randomNotificationChannelTruncatedEvent(cid = cid)

        handler.handle(event)

        verify(state).removeMessagesBefore(event.createdAt)
    }

    // endregion

    // region Typing events tests

    @Test
    fun `When TypingStartEvent from other user is handled, Then typing is set via pruner`() {
        val otherUserId = randomString()
        val user = randomUser(id = otherUserId)
        val event = randomTypingStartEvent(cid = cid, user = user)

        handler.handle(event)

        val expected = TypingEvent(cid.cidToTypeAndId().second, listOf(user))
        verify(state).setTyping(expected)
    }

    @Test
    fun `When TypingStartEvent from current user is handled, Then typing is not set`() {
        val user = randomUser(id = currentUserId)
        val event = randomTypingStartEvent(cid = cid, user = user)

        handler.handle(event)

        verify(state, never()).setTyping(any())
    }

    @Test
    fun `When TypingStopEvent from other user is handled, Then typing is cleared via pruner`() {
        val otherUserId = randomString()
        val user = randomUser(id = otherUserId)
        // First send a start event to populate the typing state
        val startEvent = randomTypingStartEvent(cid = cid, user = user)
        handler.handle(startEvent)

        // Then send a stop event
        val stopEvent = randomTypingStopEvent(cid = cid, user = user)
        handler.handle(stopEvent)

        val expected = TypingEvent(cid.cidToTypeAndId().second, emptyList())
        verify(state).setTyping(expected)
    }

    @Test
    fun `When TypingStopEvent from current user is handled, Then typing is not cleared`() {
        val user = randomUser(id = currentUserId)
        val event = randomTypingStopEvent(cid = cid, user = user)

        handler.handle(event)

        verify(state, never()).setTyping(any())
    }

    // endregion

    // region Read/delivery receipt events tests

    @Test
    fun `When MessageReadEvent without thread is handled, Then read is updated`() {
        val event = randomMessageReadEvent(cid = cid)

        handler.handle(event)

        verify(state).updateRead(event.toChannelUserRead())
    }

    @Test
    fun `When MessageReadEvent with thread is handled, Then read is not updated`() {
        val event = MessageReadEvent(
            type = EventType.MESSAGE_READ,
            createdAt = randomDate(),
            rawCreatedAt = "",
            user = randomUser(),
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            lastReadMessageId = randomString(),
            thread = randomThreadInfo(),
        )

        handler.handle(event)

        verify(state, never()).updateRead(any())
    }

    @Test
    fun `When NotificationMarkReadEvent without thread is handled, Then read is updated`() {
        val event = randomNotificationMarkReadEvent(cid = cid, threadId = null, thread = null)

        handler.handle(event)

        verify(state).updateRead(event.toChannelUserRead())
    }

    @Test
    fun `When NotificationMarkReadEvent with thread is handled, Then read is not updated`() {
        val threadId = randomString()
        val event = randomNotificationMarkReadEvent(
            cid = cid,
            threadId = threadId,
            thread = randomThreadInfo(parentMessageId = threadId),
        )

        handler.handle(event)

        verify(state, never()).updateRead(any())
    }

    @Test
    fun `When NotificationMarkUnreadEvent is handled, Then read is updated`() {
        val event = randomNotificationMarkUnreadEvent(cid = cid)

        handler.handle(event)

        verify(state).updateRead(event.toChannelUserRead())
    }

    @Test
    fun `When MessageDeliveredEvent is handled, Then delivered is updated`() {
        val event = randomMessageDeliveredEvent(cid = cid)

        handler.handle(event)

        verify(state).updateDelivered(event.toChannelUserRead())
    }

    // endregion

    // region Invitation events tests

    @Test
    fun `When NotificationInviteAcceptedEvent is handled, Then member is added and channel data is updated`() {
        val member = randomMember()
        val event = NotificationInviteAcceptedEvent(
            type = EventType.NOTIFICATION_INVITE_ACCEPTED,
            createdAt = randomDate(),
            rawCreatedAt = "",
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            user = randomUser(),
            member = member,
            channel = randomChannel(),
        )

        handler.handle(event)

        verify(state).addMember(member)
        verify(state).updateChannelData(event)
    }

    @Test
    fun `When NotificationInviteRejectedEvent is handled, Then member is deleted and channel data is updated`() {
        val member = randomMember()
        val event = NotificationInviteRejectedEvent(
            type = EventType.NOTIFICATION_INVITE_REJECTED,
            createdAt = randomDate(),
            rawCreatedAt = "",
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            user = randomUser(),
            member = member,
            channel = randomChannel(),
        )

        handler.handle(event)

        verify(state).deleteMember(member.getUserId())
        verify(state).updateChannelData(event)
    }

    // endregion

    // region Ban events tests

    @Test
    fun `When ChannelUserBannedEvent is handled, Then member ban is updated`() {
        val user = randomUser()
        val expiration = randomDate()
        val event = randomChannelUserBannedEvent(cid = cid, user = user, banExpires = expiration, shadow = false)

        handler.handle(event)

        verify(state).updateMemberBan(
            memberId = user.id,
            banned = true,
            expiry = expiration,
            shadow = false,
        )
    }

    @Test
    fun `When ChannelUserBannedEvent with shadow ban is handled, Then member is shadow banned`() {
        val user = randomUser()
        val event = randomChannelUserBannedEvent(cid = cid, user = user, shadow = true)

        handler.handle(event)

        verify(state).updateMemberBan(
            memberId = user.id,
            banned = true,
            expiry = event.expiration,
            shadow = true,
        )
    }

    @Test
    fun `When ChannelUserUnbannedEvent is handled, Then member is unbanned`() {
        val user = randomUser()
        val event = ChannelUserUnbannedEvent(
            type = EventType.USER_UNBANNED,
            createdAt = randomDate(),
            rawCreatedAt = "",
            user = user,
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
        )

        handler.handle(event)

        verify(state).updateMemberBan(
            memberId = user.id,
            banned = false,
            expiry = null,
            shadow = false,
        )
    }

    // endregion

    // region Poll events tests

    @Test
    fun `When PollClosedEvent is handled, Then poll is upserted with closed status`() {
        val poll = randomPoll()
        val event = randomPollClosedEvent(cid = cid, poll = poll)
        whenever(state.getPoll(poll.id)).thenReturn(poll)

        handler.handle(event)

        val expectedPoll = event.processPoll(state::getPoll)
        verify(state).upsertPoll(expectedPoll)
    }

    @Test
    fun `When PollUpdatedEvent is handled, Then poll is upserted`() {
        val poll = randomPoll()
        val event = randomPollUpdatedEvent(cid = cid, poll = poll)
        whenever(state.getPoll(poll.id)).thenReturn(poll)

        handler.handle(event)

        val expectedPoll = event.processPoll(state::getPoll)
        verify(state).upsertPoll(expectedPoll)
    }

    @Test
    fun `When PollDeletedEvent is handled, Then poll is deleted`() {
        val poll = randomPoll()
        val event = randomPollDeletedEvent(cid = cid, poll = poll)

        handler.handle(event)

        verify(state).deletePoll(poll)
    }

    @Test
    fun `When VoteCastedEvent is handled, Then poll is upserted`() {
        val poll = randomPoll()
        val event = randomVoteCastedEvent(cid = cid, poll = poll)
        whenever(state.getPoll(poll.id)).thenReturn(poll)

        handler.handle(event)

        val expectedPoll = event.processPoll(getCurrentUserId(), state::getPoll)
        verify(state).upsertPoll(expectedPoll)
    }

    @Test
    fun `When VoteChangedEvent is handled, Then poll is upserted`() {
        val poll = randomPoll()
        val event = randomVoteChangedEvent(cid = cid, poll = poll)
        whenever(state.getPoll(poll.id)).thenReturn(poll)

        handler.handle(event)

        val expectedPoll = event.processPoll(getCurrentUserId(), state::getPoll)
        verify(state).upsertPoll(expectedPoll)
    }

    @Test
    fun `When VoteRemovedEvent is handled, Then poll is upserted`() {
        val poll = randomPoll()
        val event = randomVoteRemovedEvent(cid = cid, poll = poll)
        whenever(state.getPoll(poll.id)).thenReturn(poll)

        handler.handle(event)

        val expectedPoll = event.processPoll(state::getPoll)
        verify(state).upsertPoll(expectedPoll)
    }

    @Test
    fun `When AnswerCastedEvent is handled, Then poll is upserted`() {
        val poll = randomPoll()
        val event = randomAnswerCastedEvent(cid = cid, poll = poll)
        whenever(state.getPoll(poll.id)).thenReturn(poll)

        handler.handle(event)

        val expectedPoll = event.processPoll(state::getPoll)
        verify(state).upsertPoll(expectedPoll)
    }

    // endregion

    // region Reminder events tests

    @Test
    fun `When ReminderCreatedEvent is handled, Then message reminder is updated`() {
        val message = randomMessage()
        val reminder = randomMessageReminder(message = message)
        val event = randomReminderCreatedEvent(cid = cid, messageId = message.id, reminder = reminder)
        whenever(state.getMessageById(message.id)).thenReturn(message)

        handler.handle(event)

        verify(state).updateMessage(message.copy(reminder = reminder.toMessageReminderInfo()))
    }

    @Test
    fun `When ReminderCreatedEvent is handled for non-existing message, Then message is not updated`() {
        val message = randomMessage()
        val reminder = randomMessageReminder(message = message)
        val event = randomReminderCreatedEvent(cid = cid, messageId = message.id, reminder = reminder)
        whenever(state.getMessageById(message.id)).thenReturn(null)

        handler.handle(event)

        verify(state, never()).updateMessage(any())
    }

    @Test
    fun `When ReminderUpdatedEvent is handled, Then message reminder is updated`() {
        val message = randomMessage()
        val reminder = randomMessageReminder(message = message)
        val event = randomReminderUpdatedEvent(cid = cid, messageId = message.id, reminder = reminder)
        whenever(state.getMessageById(message.id)).thenReturn(message)

        handler.handle(event)

        verify(state).updateMessage(message.copy(reminder = reminder.toMessageReminderInfo()))
    }

    @Test
    fun `When ReminderDeletedEvent is handled, Then message reminder is removed`() {
        val message = randomMessage()
        val event = randomReminderDeletedEvent(cid = cid, messageId = message.id)
        whenever(state.getMessageById(message.id)).thenReturn(message)

        handler.handle(event)

        verify(state).updateMessage(message.copy(reminder = null))
    }

    @Test
    fun `When ReminderDeletedEvent is handled for non-existing message, Then message is not updated`() {
        val message = randomMessage()
        val event = randomReminderDeletedEvent(cid = cid, messageId = message.id)
        whenever(state.getMessageById(message.id)).thenReturn(null)

        handler.handle(event)

        verify(state, never()).updateMessage(any())
    }

    // endregion

    // region User presence events tests

    @Test
    fun `When UserPresenceChangedEvent is handled, Then user presence is upserted`() {
        val user = randomUser()
        val event = UserPresenceChangedEvent(
            type = EventType.USER_PRESENCE_CHANGED,
            createdAt = randomDate(),
            rawCreatedAt = "",
            user = user,
        )

        handler.handle(event)

        verify(state).upsertUserPresence(user)
    }

    @Test
    fun `When UserUpdatedEvent is handled, Then user presence is upserted`() {
        val user = randomUser()
        val event = UserUpdatedEvent(
            type = EventType.USER_UPDATED,
            createdAt = randomDate(),
            rawCreatedAt = "",
            user = user,
        )

        handler.handle(event)

        verify(state).upsertUserPresence(user)
    }

    // endregion

    // region MarkAllReadEvent tests

    @Test
    fun `When MarkAllReadEvent is handled, Then read is updated`() {
        val event = randomMarkAllReadEvent()

        handler.handle(event)

        verify(state).updateRead(event.toChannelUserRead())
    }

    // endregion

    // region NotificationChannelMutesUpdatedEvent tests

    @Test
    fun `When NotificationChannelMutesUpdatedEvent with muted channel is handled, Then mute is updated to true`() {
        val (type, id) = cid.cidToTypeAndId()
        val channelMute = randomChannelMute(channel = randomChannel(type = type, id = id))
        val me = randomUser(channelMutes = listOf(channelMute))
        val event = randomNotificationChannelMutesUpdatedEvent(me = me)

        handler.handle(event)

        verify(state).setMuted(true)
    }

    @Test
    fun `When NotificationChannelMutesUpdatedEvent without muted channel is handled, Then mute is updated to false`() {
        val me = randomUser(channelMutes = emptyList())
        val event = randomNotificationChannelMutesUpdatedEvent(me = me)

        handler.handle(event)

        verify(state).setMuted(false)
    }

    // endregion

    // region UserMessagesDeletedEvent tests

    @Test
    fun `When UserMessagesDeletedEvent with hard delete is handled, Then messages are deleted`() {
        val user = randomUser()
        val createdAt = randomDate()
        val event = randomUserMessagesDeletedEvent(cid = cid, user = user, hardDelete = true, createdAt = createdAt)

        handler.handle(event)

        verify(state).deleteMessagesFromUser(
            userId = user.id,
            hard = true,
            deletedAt = createdAt,
        )
    }

    @Test
    fun `When UserMessagesDeletedEvent with soft delete is handled, Then messages are soft deleted`() {
        val user = randomUser()
        val createdAt = randomDate()
        val event = randomUserMessagesDeletedEvent(cid = cid, user = user, hardDelete = false, createdAt = createdAt)

        handler.handle(event)

        verify(state).deleteMessagesFromUser(
            userId = user.id,
            hard = false,
            deletedAt = createdAt,
        )
    }

    // endregion
}
