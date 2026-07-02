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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.api2.model.dto.UnknownEventDto
import io.getstream.chat.android.client.api2.model.dto.utils.internal.ExactDate
import io.getstream.chat.android.client.events.AIIndicatorClearEvent
import io.getstream.chat.android.client.events.AIIndicatorStopEvent
import io.getstream.chat.android.client.events.AIIndicatorUpdatedEvent
import io.getstream.chat.android.client.events.AnswerCastedEvent
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectionErrorEvent
import io.getstream.chat.android.client.events.DraftMessageDeletedEvent
import io.getstream.chat.android.client.events.DraftMessageUpdatedEvent
import io.getstream.chat.android.client.events.GlobalUserBannedEvent
import io.getstream.chat.android.client.events.GlobalUserUnbannedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageDeliveredEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.events.NotificationInviteAcceptedEvent
import io.getstream.chat.android.client.events.NotificationInviteRejectedEvent
import io.getstream.chat.android.client.events.NotificationInvitedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMarkUnreadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationReminderDueEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.NotificationThreadMessageNewEvent
import io.getstream.chat.android.client.events.PollClosedEvent
import io.getstream.chat.android.client.events.PollDeletedEvent
import io.getstream.chat.android.client.events.PollUpdatedEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.ReminderCreatedEvent
import io.getstream.chat.android.client.events.ReminderDeletedEvent
import io.getstream.chat.android.client.events.ReminderUpdatedEvent
import io.getstream.chat.android.client.events.ThreadUpdatedEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.events.UserDeletedEvent
import io.getstream.chat.android.client.events.UserMessagesDeletedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.events.VoteCastedEvent
import io.getstream.chat.android.client.events.VoteChangedEvent
import io.getstream.chat.android.client.events.VoteRemovedEvent
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomString
import io.getstream.result.Error
import org.junit.jupiter.params.provider.Arguments
import java.util.Date

/**
 * Provides the arguments (ChatEventDto and corresponding ChatEvent) for the [EventMappingTest].
 */
@Suppress("LargeClass", "UNUSED")
internal object EventMappingTestArguments {

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val DATE = Date(1593411268000)
    private const val DATE_STRING = "2020-06-29T06:14:28.000Z"
    private val EXACT_DATE = ExactDate(DATE, DATE_STRING)
    private val USER = Mother.randomDownstreamUserDto()
    private val SLIM_USER = io.getstream.chat.android.network.models.UserResponseCommonFields(
        banned = false,
        createdAt = DATE,
        id = USER.id,
        language = USER.language,
        online = USER.online,
        role = USER.role,
        updatedAt = DATE,
        name = USER.name,
        image = USER.image,
        lastActive = DATE,
    )
    private val SLIM_PRIVACY_USER = io.getstream.chat.android.network.models.UserResponsePrivacyFields(
        banned = false,
        createdAt = DATE,
        id = USER.id,
        language = USER.language,
        online = USER.online,
        role = USER.role,
        updatedAt = DATE,
        name = USER.name,
        image = USER.image,
    )
    private val SLIM_USER_DOMAIN = io.getstream.chat.android.models.User(
        id = SLIM_USER.id,
        name = SLIM_USER.name ?: "",
        image = SLIM_USER.image ?: "",
        role = SLIM_USER.role,
        invisible = false,
        language = SLIM_USER.language.orEmpty(),
        banned = SLIM_USER.banned,
        online = SLIM_USER.online,
        createdAt = SLIM_USER.createdAt,
        updatedAt = SLIM_USER.updatedAt,
        lastActive = SLIM_USER.lastActive,
        extraData = mutableMapOf(),
    )
    private val OWN_USER = Mother.randomOwnUserResponse()
    private val CHANNEL_TYPE = randomString()
    private val CHANNEL_ID = randomString()
    private val CID = "$CHANNEL_TYPE:$CHANNEL_ID"
    private val CHANNEL_MEMBER_COUNT = positiveRandomInt()
    private val CHANNEL_NAME = randomString()
    private val CHANNEL_IMAGE = randomString()
    private val MESSAGE_ID = randomString()
    private val MESSAGE = Mother.randomDownstreamMessageDto()
    private val DRAFT = Mother.randomDownstreamDraftDto()
    private val CHANNEL = Mother.randomDownstreamChannelDto()
    private val CLEAR_HISTORY = randomBoolean()
    private val SHADOW_BAN = randomBoolean()
    private val CONNECTION_ID = randomString()
    private val ERROR = Mother.randomErrorDto()
    private val GENERIC_ERROR = Error.GenericError("generic error")
    private val MEMBER = Mother.randomDownstreamMemberDto()
    private val HARD_DELETE = randomBoolean()
    private val FIRST_UNREAD_MESSAGE_ID = randomString()
    private val LAST_DELIVERED_MESSAGE_ID = randomString()
    private val LAST_READ_MESSAGE_ID = randomString()
    private val UNREAD_MESSAGES = positiveRandomInt()
    private val TOTAL_UNREAD_COUNT = positiveRandomInt()
    private val UNREAD_CHANNELS = positiveRandomInt()
    private val UNREAD_THREADS = positiveRandomInt()
    private val UNREAD_THREAD_MESSAGES = positiveRandomInt()
    private val REACTION = Mother.randomDownstreamReactionDto()
    private val WATCHER_COUNT = positiveRandomInt()
    private val POLL = Mother.randomDownstreamPollDto()
    private val POLL_VOTE = Mother.randomDownstreamVoteDto()
    private val REMINDER = Mother.randomDownstreamReminderDto()
    private val THREAD_INFO = Mother.randomDownstreamThreadInfoDto()
    private val AI_MESSAGE_ID = randomString()
    private val AI_STATE = randomString()
    private val DELETED_FOR_ME = randomBoolean()

    // BEGIN: DTO Models

    private val unknownDto = UnknownEventDto(
        type = EventType.UNKNOWN,
        created_at = EXACT_DATE,
        user = USER,
        rawData = emptyMap<String, String>(),
    )

    // END: DTO Models

    // BEGIN: Domain models

    private val unknown = UnknownEvent(
        type = unknownDto.type,
        createdAt = unknownDto.created_at.date,
        rawCreatedAt = unknownDto.created_at.rawDate,
        user = with(domainMapping) { unknownDto.user?.toDomain() },
        rawData = unknownDto.rawData,
    )

    // END: Domain models

    /**
     * Provides the test arguments for the [EventMappingTest].
     */
    @JvmStatic
    @Suppress("LongMethod")
    fun arguments() = listOf(
        Arguments.of(unknownDto, unknown),
    )

    private val messageNewGenerated = io.getstream.chat.android.network.models.MessageNewEvent(
        createdAt = DATE,
        messageId = MESSAGE_ID,
        watcherCount = 0,
        message = MESSAGE,
        type = EventType.MESSAGE_NEW,
        channelId = CHANNEL_ID,
        channelMemberCount = CHANNEL_MEMBER_COUNT,
        channelType = CHANNEL_TYPE,
        cid = CID,
        channelCustom = mapOf("name" to CHANNEL_NAME, "image" to CHANNEL_IMAGE),
        user = SLIM_USER,
    )

    private val messageNewExpected = io.getstream.chat.android.client.events.NewMessageEvent(
        type = EventType.MESSAGE_NEW,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        message = with(domainMapping) {
            val channelInfo = ChannelInfo(
                cid = CID,
                id = CHANNEL_ID,
                type = CHANNEL_TYPE,
                memberCount = CHANNEL_MEMBER_COUNT,
                name = CHANNEL_NAME,
                image = CHANNEL_IMAGE,
            )
            MESSAGE.toDomain(channelInfo)
        },
        watcherCount = 0,
        totalUnreadCount = 0,
        unreadChannels = 0,
        channelMessageCount = null,
    )

    private val typingStartGenerated = io.getstream.chat.android.network.models.TypingStartEvent(
        createdAt = DATE,
        type = EventType.TYPING_START,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        parentId = "parent-1",
        user = SLIM_USER,
    )

    private val typingStartExpected = TypingStartEvent(
        type = EventType.TYPING_START,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        parentId = "parent-1",
    )

    private val typingStopGenerated = io.getstream.chat.android.network.models.TypingStopEvent(
        createdAt = DATE,
        type = EventType.TYPING_STOP,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        parentId = "parent-1",
        user = SLIM_USER,
    )

    private val typingStopExpected = TypingStopEvent(
        type = EventType.TYPING_STOP,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        parentId = "parent-1",
    )

    private val reactionDeletedGenerated = io.getstream.chat.android.network.models.ReactionDeletedEvent(
        createdAt = DATE,
        type = EventType.REACTION_DELETED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        user = SLIM_USER,
        message = MESSAGE,
        reaction = REACTION,
    )

    private val reactionDeletedExpected = ReactionDeletedEvent(
        type = EventType.REACTION_DELETED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        message = with(domainMapping) { MESSAGE.toDomain() },
        reaction = with(domainMapping) { REACTION.toDomain() },
    )

    private val reactionNewGenerated = io.getstream.chat.android.network.models.ReactionNewEvent(
        createdAt = DATE,
        type = EventType.REACTION_NEW,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        user = SLIM_USER,
        message = MESSAGE,
        reaction = REACTION,
    )

    private val reactionNewExpected = ReactionNewEvent(
        type = EventType.REACTION_NEW,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        message = with(domainMapping) { MESSAGE.toDomain() },
        reaction = with(domainMapping) { REACTION.toDomain() },
    )

    private val messageUpdatedGenerated = io.getstream.chat.android.network.models.MessageUpdatedEvent(
        createdAt = DATE,
        messageId = MESSAGE_ID,
        message = MESSAGE,
        type = EventType.MESSAGE_UPDATED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER,
    )

    private val messageUpdatedExpected = MessageUpdatedEvent(
        type = EventType.MESSAGE_UPDATED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        message = with(domainMapping) { MESSAGE.toDomain() },
    )

    private val messageDeletedGenerated = io.getstream.chat.android.network.models.MessageDeletedEvent(
        createdAt = DATE,
        hardDelete = HARD_DELETE,
        messageId = MESSAGE_ID,
        message = MESSAGE,
        type = EventType.MESSAGE_DELETED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER,
        deletedForMe = DELETED_FOR_ME,
    )

    private val messageDeletedExpected = MessageDeletedEvent(
        type = EventType.MESSAGE_DELETED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        message = with(domainMapping) { MESSAGE.toDomain() },
        hardDelete = HARD_DELETE,
        channelMessageCount = null,
        deletedForMe = DELETED_FOR_ME,
    )

    private val messageReadGenerated = io.getstream.chat.android.network.models.MessageReadEvent(
        createdAt = DATE,
        type = EventType.MESSAGE_READ,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER,
        lastReadMessageId = LAST_READ_MESSAGE_ID,
    )

    private val messageReadExpected = MessageReadEvent(
        type = EventType.MESSAGE_READ,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        lastReadMessageId = LAST_READ_MESSAGE_ID,
        team = null,
    )

    private val notificationMarkReadGenerated = io.getstream.chat.android.network.models.NotificationMarkReadEvent(
        createdAt = DATE,
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        unreadCount = TOTAL_UNREAD_COUNT,
        type = EventType.NOTIFICATION_MARK_READ,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER,
        lastReadMessageId = LAST_READ_MESSAGE_ID,
    )

    private val notificationMarkReadExpected = NotificationMarkReadEvent(
        type = EventType.NOTIFICATION_MARK_READ,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        threadId = null,
        thread = null,
        unreadThreads = null,
        unreadThreadMessages = null,
        lastReadMessageId = LAST_READ_MESSAGE_ID,
    )

    private val markAllReadGenerated = io.getstream.chat.android.network.models.NotificationMarkReadEvent(
        createdAt = DATE,
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        unreadCount = TOTAL_UNREAD_COUNT,
        type = EventType.NOTIFICATION_MARK_READ,
        // cid absent => MarkAllReadEvent
        user = SLIM_USER,
    )

    private val messageDeliveredGenerated = io.getstream.chat.android.network.models.MessageDeliveredEvent(
        createdAt = DATE,
        type = EventType.MESSAGE_DELIVERED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER,
        lastDeliveredAt = DATE_STRING,
        lastDeliveredMessageId = LAST_DELIVERED_MESSAGE_ID,
    )

    private val memberAddedGenerated = io.getstream.chat.android.network.models.MemberAddedEvent(
        createdAt = DATE,
        type = EventType.MEMBER_ADDED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        member = MEMBER,
        user = SLIM_USER,
    )

    private val memberAddedExpected = MemberAddedEvent(
        type = EventType.MEMBER_ADDED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        member = with(domainMapping) { MEMBER.toDomain() },
    )

    private val memberUpdatedGenerated = io.getstream.chat.android.network.models.MemberUpdatedEvent(
        createdAt = DATE,
        type = EventType.MEMBER_UPDATED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        member = MEMBER,
        user = SLIM_USER,
    )

    private val memberUpdatedExpected = MemberUpdatedEvent(
        type = EventType.MEMBER_UPDATED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        member = with(domainMapping) { MEMBER.toDomain() },
    )

    private val memberRemovedGenerated = io.getstream.chat.android.network.models.MemberRemovedEvent(
        createdAt = DATE,
        type = EventType.MEMBER_REMOVED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        member = MEMBER,
        user = SLIM_USER,
    )

    private val memberRemovedExpected = MemberRemovedEvent(
        type = EventType.MEMBER_REMOVED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        member = with(domainMapping) { MEMBER.toDomain() },
    )

    private val userWatchingStartGenerated = io.getstream.chat.android.network.models.UserWatchingStartEvent(
        createdAt = DATE,
        type = EventType.USER_WATCHING_START,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER,
        watcherCount = WATCHER_COUNT,
    )

    private val userWatchingStartExpected = UserStartWatchingEvent(
        type = EventType.USER_WATCHING_START,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER_DOMAIN,
        watcherCount = WATCHER_COUNT,
    )

    private val userWatchingStopGenerated = io.getstream.chat.android.network.models.UserWatchingStopEvent(
        createdAt = DATE,
        type = EventType.USER_WATCHING_STOP,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER,
        watcherCount = WATCHER_COUNT,
    )

    private val channelHiddenGenerated = io.getstream.chat.android.network.models.ChannelHiddenEvent(
        createdAt = DATE,
        type = EventType.CHANNEL_HIDDEN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        user = SLIM_USER,
        clearHistory = CLEAR_HISTORY,
    )

    private val channelHiddenExpected = ChannelHiddenEvent(
        type = EventType.CHANNEL_HIDDEN,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = with(domainMapping) { CHANNEL.toDomain() },
        clearHistory = CLEAR_HISTORY,
    )

    private val channelVisibleGenerated = io.getstream.chat.android.network.models.ChannelVisibleEvent(
        createdAt = DATE,
        type = EventType.CHANNEL_VISIBLE,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        user = SLIM_USER,
    )

    private val POLL_VOTE_NON_ANSWER = POLL_VOTE.copy(isAnswer = false)
    private val POLL_VOTE_ANSWER = POLL_VOTE.copy(isAnswer = true)

    private val voteCastedGenerated = io.getstream.chat.android.network.models.PollVoteCastedEvent(
        createdAt = DATE,
        type = EventType.POLL_VOTE_CASTED,
        cid = CID,
        messageId = MESSAGE_ID,
        poll = POLL,
        pollVote = POLL_VOTE_NON_ANSWER,
    )

    private val voteCastedExpected = VoteCastedEvent(
        type = EventType.POLL_VOTE_CASTED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CID.split(":").first(),
        channelId = CID.split(":").last(),
        messageId = MESSAGE_ID,
        poll = with(domainMapping) { POLL.toDomain() },
        newVote = with(domainMapping) { POLL_VOTE_NON_ANSWER.toDomain() },
    )

    private val voteChangedGenerated = io.getstream.chat.android.network.models.PollVoteChangedEvent(
        createdAt = DATE,
        type = EventType.POLL_VOTE_CHANGED,
        cid = CID,
        messageId = MESSAGE_ID,
        poll = POLL,
        pollVote = POLL_VOTE_NON_ANSWER,
    )

    private val voteChangedExpected = VoteChangedEvent(
        type = EventType.POLL_VOTE_CHANGED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CID.split(":").first(),
        channelId = CID.split(":").last(),
        messageId = MESSAGE_ID,
        poll = with(domainMapping) { POLL.toDomain() },
        newVote = with(domainMapping) { POLL_VOTE_NON_ANSWER.toDomain() },
    )

    private val voteRemovedGenerated = io.getstream.chat.android.network.models.PollVoteRemovedEvent(
        createdAt = DATE,
        type = EventType.POLL_VOTE_REMOVED,
        cid = CID,
        messageId = MESSAGE_ID,
        poll = POLL,
        pollVote = POLL_VOTE,
    )

    private val voteRemovedExpected = VoteRemovedEvent(
        type = EventType.POLL_VOTE_REMOVED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CID.split(":").first(),
        channelId = CID.split(":").last(),
        messageId = MESSAGE_ID,
        poll = with(domainMapping) { POLL.toDomain() },
        removedVote = with(domainMapping) { POLL_VOTE.toDomain() },
    )

    private val answerCastedGenerated = io.getstream.chat.android.network.models.PollVoteCastedEvent(
        createdAt = DATE,
        type = EventType.POLL_VOTE_CASTED,
        cid = CID,
        messageId = MESSAGE_ID,
        poll = POLL,
        pollVote = POLL_VOTE_ANSWER,
    )

    private val answerCastedExpected = AnswerCastedEvent(
        type = EventType.POLL_VOTE_CASTED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CID.split(":").first(),
        channelId = CID.split(":").last(),
        messageId = MESSAGE_ID,
        poll = with(domainMapping) { POLL.toDomain() },
        newAnswer = with(domainMapping) { POLL_VOTE_ANSWER.toAnswerDomain() },
    )

    private val pollClosedGenerated = io.getstream.chat.android.network.models.PollClosedEvent(
        createdAt = DATE,
        type = EventType.POLL_CLOSED,
        cid = CID,
        messageId = MESSAGE_ID,
        poll = POLL,
    )

    private val pollClosedExpected = PollClosedEvent(
        type = EventType.POLL_CLOSED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CID.split(":").first(),
        channelId = CID.split(":").last(),
        messageId = MESSAGE_ID,
        poll = with(domainMapping) { POLL.toDomain() },
    )

    private val pollDeletedGenerated = io.getstream.chat.android.network.models.PollDeletedEvent(
        createdAt = DATE,
        type = EventType.POLL_DELETED,
        cid = CID,
        messageId = MESSAGE_ID,
        poll = POLL,
    )

    private val pollDeletedExpected = PollDeletedEvent(
        type = EventType.POLL_DELETED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CID.split(":").first(),
        channelId = CID.split(":").last(),
        messageId = MESSAGE_ID,
        poll = with(domainMapping) { POLL.toDomain() },
    )

    private val pollUpdatedGenerated = io.getstream.chat.android.network.models.PollUpdatedEvent(
        createdAt = DATE,
        type = EventType.POLL_UPDATED,
        cid = CID,
        messageId = MESSAGE_ID,
        poll = POLL,
    )

    private val pollUpdatedExpected = PollUpdatedEvent(
        type = EventType.POLL_UPDATED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CID.split(":").first(),
        channelId = CID.split(":").last(),
        messageId = MESSAGE_ID,
        poll = with(domainMapping) { POLL.toDomain() },
    )

    private val healthGenerated = io.getstream.chat.android.network.models.HealthCheckEvent(
        createdAt = DATE,
        type = EventType.HEALTH_CHECK,
        connectionId = CONNECTION_ID,
    )

    private val healthExpected = HealthEvent(
        type = EventType.HEALTH_CHECK,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        connectionId = CONNECTION_ID,
    )

    private val connectedGenerated = io.getstream.chat.android.network.models.HealthCheckEvent(
        createdAt = DATE,
        type = EventType.HEALTH_CHECK,
        connectionId = CONNECTION_ID,
        me = OWN_USER,
    )

    private val connectedExpected = ConnectedEvent(
        type = EventType.HEALTH_CHECK,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        me = with(domainMapping) { OWN_USER.toDomain() },
        connectionId = CONNECTION_ID,
    )

    private val connectionErrorGenerated = io.getstream.chat.android.network.models.ConnectionErrorEvent(
        connectionId = CONNECTION_ID,
        createdAt = DATE,
        error = ERROR,
        type = EventType.CONNECTION_ERROR,
    )

    private val connectionErrorExpected = ConnectionErrorEvent(
        type = EventType.CONNECTION_ERROR,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        connectionId = CONNECTION_ID,
        error = ERROR.toDomain(),
    )

    private val aiIndicatorUpdateGenerated = io.getstream.chat.android.network.models.AIIndicatorUpdateEvent(
        createdAt = DATE,
        type = EventType.AI_TYPING_INDICATOR_UPDATED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        messageId = AI_MESSAGE_ID,
        aiState = AI_STATE,
        user = SLIM_USER,
    )

    private val aiIndicatorUpdatedExpected = AIIndicatorUpdatedEvent(
        type = EventType.AI_TYPING_INDICATOR_UPDATED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER_DOMAIN,
        messageId = AI_MESSAGE_ID,
        aiState = AI_STATE,
    )

    private val aiIndicatorClearGenerated = io.getstream.chat.android.network.models.AIIndicatorClearEvent(
        createdAt = DATE,
        type = EventType.AI_TYPING_INDICATOR_CLEAR,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER,
    )

    private val aiIndicatorClearExpected = AIIndicatorClearEvent(
        type = EventType.AI_TYPING_INDICATOR_CLEAR,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER_DOMAIN,
    )

    private val aiIndicatorStopGenerated = io.getstream.chat.android.network.models.AIIndicatorStopEvent(
        createdAt = DATE,
        type = EventType.AI_TYPING_INDICATOR_STOP,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER,
    )

    private val aiIndicatorStopExpected = AIIndicatorStopEvent(
        type = EventType.AI_TYPING_INDICATOR_STOP,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER_DOMAIN,
    )

    private val reminderCreatedGenerated = io.getstream.chat.android.network.models.ReminderCreatedEvent(
        createdAt = DATE,
        type = EventType.REMINDER_CREATED,
        cid = CID,
        messageId = MESSAGE.id,
        userId = USER.id,
        reminder = REMINDER,
    )

    private val reminderCreatedExpected = ReminderCreatedEvent(
        type = EventType.REMINDER_CREATED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CID.split(":").first(),
        channelId = CID.split(":").last(),
        messageId = MESSAGE.id,
        userId = USER.id,
        reminder = with(domainMapping) { REMINDER.toDomain() },
    )

    private val reminderUpdatedGenerated = io.getstream.chat.android.network.models.ReminderUpdatedEvent(
        createdAt = DATE,
        type = EventType.REMINDER_UPDATED,
        cid = CID,
        messageId = MESSAGE.id,
        userId = USER.id,
        reminder = REMINDER,
    )

    private val reminderUpdatedExpected = ReminderUpdatedEvent(
        type = EventType.REMINDER_UPDATED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CID.split(":").first(),
        channelId = CID.split(":").last(),
        messageId = MESSAGE.id,
        userId = USER.id,
        reminder = with(domainMapping) { REMINDER.toDomain() },
    )

    private val reminderDeletedGenerated = io.getstream.chat.android.network.models.ReminderDeletedEvent(
        createdAt = DATE,
        type = EventType.REMINDER_DELETED,
        cid = CID,
        messageId = MESSAGE.id,
        userId = USER.id,
        reminder = REMINDER,
    )

    private val reminderDeletedExpected = ReminderDeletedEvent(
        type = EventType.REMINDER_DELETED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CID.split(":").first(),
        channelId = CID.split(":").last(),
        messageId = MESSAGE.id,
        userId = USER.id,
        reminder = with(domainMapping) { REMINDER.toDomain() },
    )

    private val userDeletedGenerated = io.getstream.chat.android.network.models.UserDeletedEvent(
        createdAt = DATE,
        type = EventType.USER_DELETED,
        user = SLIM_USER,
        deleteConversation = "",
        deleteConversationChannels = false,
        deleteMessages = "",
        deleteUser = "",
        hardDelete = false,
        markMessagesDeleted = false,
    )

    private val userDeletedExpected = UserDeletedEvent(
        type = EventType.USER_DELETED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
    )

    private val userMessagesDeletedGenerated = io.getstream.chat.android.network.models.UserMessagesDeletedEvent(
        createdAt = DATE,
        type = EventType.USER_MESSAGES_DELETED,
        user = SLIM_USER,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        hardDelete = HARD_DELETE,
    )

    private val userMessagesDeletedExpected = UserMessagesDeletedEvent(
        type = EventType.USER_MESSAGES_DELETED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        hardDelete = HARD_DELETE,
    )

    private val channelUpdatedGenerated = io.getstream.chat.android.network.models.ChannelUpdatedEvent(
        createdAt = DATE,
        type = EventType.CHANNEL_UPDATED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        message = MESSAGE,
        user = null,
    )

    private val channelUpdatedExpected = ChannelUpdatedEvent(
        type = EventType.CHANNEL_UPDATED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        message = with(domainMapping) { MESSAGE.toDomain(CHANNEL.toChannelInfo()) },
        channel = with(domainMapping) { CHANNEL.toDomain() },
    )

    private val channelUpdatedByUserGenerated = io.getstream.chat.android.network.models.ChannelUpdatedEvent(
        createdAt = DATE,
        type = EventType.CHANNEL_UPDATED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        message = MESSAGE,
        user = SLIM_USER,
    )

    private val channelUpdatedByUserExpected = ChannelUpdatedByUserEvent(
        type = EventType.CHANNEL_UPDATED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER_DOMAIN,
        message = with(domainMapping) { MESSAGE.toDomain(CHANNEL.toChannelInfo()) },
        channel = with(domainMapping) { CHANNEL.toDomain() },
    )

    private val notificationReminderDueGenerated = io.getstream.chat.android.network.models.ReminderNotificationEvent(
        createdAt = DATE,
        type = EventType.NOTIFICATION_REMINDER_DUE,
        cid = CID,
        messageId = MESSAGE.id,
        userId = USER.id,
        reminder = REMINDER,
    )

    private val notificationReminderDueExpected = NotificationReminderDueEvent(
        type = EventType.NOTIFICATION_REMINDER_DUE,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CID.split(":").first(),
        channelId = CID.split(":").last(),
        messageId = MESSAGE.id,
        userId = USER.id,
        reminder = with(domainMapping) { REMINDER.toDomain() },
    )

    private val threadUpdatedGenerated = io.getstream.chat.android.network.models.ThreadUpdatedEvent(
        createdAt = DATE,
        type = EventType.THREAD_UPDATED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        thread = THREAD_INFO,
    )

    private val threadUpdatedExpected = ThreadUpdatedEvent(
        type = EventType.THREAD_UPDATED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        thread = with(domainMapping) { THREAD_INFO.toDomain() },
    )

    private val reactionUpdatedGenerated = io.getstream.chat.android.network.models.ReactionUpdatedEvent(
        createdAt = DATE,
        type = EventType.REACTION_UPDATED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER,
        message = MESSAGE,
        messageId = MESSAGE.id,
        reaction = REACTION,
    )

    private val reactionUpdatedExpected = ReactionUpdateEvent(
        type = EventType.REACTION_UPDATED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        message = with(domainMapping) { MESSAGE.toDomain() },
        reaction = with(domainMapping) { REACTION.toDomain() },
    )

    private val notificationMarkUnreadGenerated = io.getstream.chat.android.network.models.NotificationMarkUnreadEvent(
        createdAt = DATE,
        type = EventType.NOTIFICATION_MARK_UNREAD,
        user = SLIM_USER,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        firstUnreadMessageId = FIRST_UNREAD_MESSAGE_ID,
        lastReadMessageId = LAST_READ_MESSAGE_ID,
        lastReadAt = DATE,
        unreadMessages = UNREAD_MESSAGES,
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
    )

    private val notificationMarkUnreadExpected = NotificationMarkUnreadEvent(
        type = EventType.NOTIFICATION_MARK_UNREAD,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        firstUnreadMessageId = FIRST_UNREAD_MESSAGE_ID,
        lastReadMessageId = LAST_READ_MESSAGE_ID,
        lastReadMessageAt = DATE,
        unreadMessages = UNREAD_MESSAGES,
    )

    private val notificationMutesUpdatedGenerated = io.getstream.chat.android.network.models.NotificationMutesUpdatedEvent(
        createdAt = DATE,
        type = EventType.NOTIFICATION_MUTES_UPDATED,
        me = OWN_USER,
    )

    private val notificationMutesUpdatedExpected = NotificationMutesUpdatedEvent(
        type = EventType.NOTIFICATION_MUTES_UPDATED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        me = with(domainMapping) { OWN_USER.toDomain() },
    )

    private val notificationChannelMutesUpdatedGenerated = io.getstream.chat.android.network.models.NotificationChannelMutesUpdatedEvent(
        createdAt = DATE,
        type = EventType.NOTIFICATION_CHANNEL_MUTES_UPDATED,
        me = OWN_USER,
    )

    private val notificationChannelMutesUpdatedExpected = NotificationChannelMutesUpdatedEvent(
        type = EventType.NOTIFICATION_CHANNEL_MUTES_UPDATED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        me = with(domainMapping) { OWN_USER.toDomain() },
    )

    private val userUpdatedGenerated = io.getstream.chat.android.network.models.UserUpdatedEvent(
        createdAt = DATE,
        type = EventType.USER_UPDATED,
        user = SLIM_PRIVACY_USER,
    )

    private val userUpdatedExpected = UserUpdatedEvent(
        type = EventType.USER_UPDATED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = with(domainMapping) { SLIM_PRIVACY_USER.toDomain() },
    )

    private val channelUserBannedGenerated = io.getstream.chat.android.network.models.UserBannedEvent(
        createdAt = DATE,
        type = EventType.USER_BANNED,
        user = SLIM_USER,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        expiration = DATE,
        shadow = SHADOW_BAN,
    )

    private val channelUserBannedExpected = ChannelUserBannedEvent(
        type = EventType.USER_BANNED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER_DOMAIN,
        expiration = DATE,
        shadow = SHADOW_BAN,
    )

    private val globalUserBannedGenerated = io.getstream.chat.android.network.models.UserBannedEvent(
        createdAt = DATE,
        type = EventType.USER_BANNED,
        user = SLIM_USER,
    )

    private val globalUserBannedExpected = GlobalUserBannedEvent(
        type = EventType.USER_BANNED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
    )

    private val channelUserUnbannedGenerated = io.getstream.chat.android.network.models.UserUnbannedEvent(
        createdAt = DATE,
        type = EventType.USER_UNBANNED,
        user = SLIM_USER,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
    )

    private val channelUserUnbannedExpected = ChannelUserUnbannedEvent(
        type = EventType.USER_UNBANNED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
    )

    private val globalUserUnbannedGenerated = io.getstream.chat.android.network.models.UserUnbannedEvent(
        createdAt = DATE,
        type = EventType.USER_UNBANNED,
        user = SLIM_USER,
    )

    private val globalUserUnbannedExpected = GlobalUserUnbannedEvent(
        type = EventType.USER_UNBANNED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
    )

    private val draftUpdatedGenerated = io.getstream.chat.android.network.models.DraftUpdatedEvent(
        createdAt = DATE,
        type = EventType.DRAFT_MESSAGE_UPDATED,
        draft = DRAFT,
    )

    private val draftUpdatedExpected = DraftMessageUpdatedEvent(
        type = EventType.DRAFT_MESSAGE_UPDATED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        draftMessage = with(domainMapping) { DRAFT.toDomain() },
    )

    private val draftDeletedGenerated = io.getstream.chat.android.network.models.DraftDeletedEvent(
        createdAt = DATE,
        type = EventType.DRAFT_MESSAGE_DELETED,
        draft = DRAFT,
    )

    private val draftDeletedExpected = DraftMessageDeletedEvent(
        type = EventType.DRAFT_MESSAGE_DELETED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        draftMessage = with(domainMapping) { DRAFT.toDomain() },
    )

    private val channelDeletedGenerated = io.getstream.chat.android.network.models.ChannelDeletedEvent(
        createdAt = DATE,
        type = EventType.CHANNEL_DELETED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        user = SLIM_USER,
    )

    private val channelDeletedExpected = ChannelDeletedEvent(
        type = EventType.CHANNEL_DELETED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = with(domainMapping) { CHANNEL.toDomain() },
    )

    private val channelTruncatedGenerated = io.getstream.chat.android.network.models.ChannelTruncatedEvent(
        createdAt = DATE,
        type = EventType.CHANNEL_TRUNCATED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        user = SLIM_USER,
        message = MESSAGE,
    )

    private val channelTruncatedExpected = ChannelTruncatedEvent(
        type = EventType.CHANNEL_TRUNCATED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        message = with(domainMapping) { MESSAGE.toDomain(CHANNEL.toChannelInfo()) },
        channel = with(domainMapping) { CHANNEL.toDomain() },
    )

    private val notificationChannelDeletedGenerated = io.getstream.chat.android.network.models.NotificationChannelDeletedEvent(
        createdAt = DATE,
        type = EventType.NOTIFICATION_CHANNEL_DELETED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
    )

    private val notificationChannelDeletedExpected = NotificationChannelDeletedEvent(
        type = EventType.NOTIFICATION_CHANNEL_DELETED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = with(domainMapping) { CHANNEL.toDomain() },
    )

    private val notificationChannelTruncatedGenerated = io.getstream.chat.android.network.models.NotificationChannelTruncatedEvent(
        createdAt = DATE,
        type = EventType.NOTIFICATION_CHANNEL_TRUNCATED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
    )

    private val notificationChannelTruncatedExpected = NotificationChannelTruncatedEvent(
        type = EventType.NOTIFICATION_CHANNEL_TRUNCATED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = with(domainMapping) { CHANNEL.toDomain() },
    )

    private val notificationAddedToChannelGenerated = io.getstream.chat.android.network.models.NotificationAddedToChannelEvent(
        createdAt = DATE,
        type = EventType.NOTIFICATION_ADDED_TO_CHANNEL,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        member = MEMBER,
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        unreadCount = TOTAL_UNREAD_COUNT,
    )

    private val notificationAddedToChannelExpected = NotificationAddedToChannelEvent(
        type = EventType.NOTIFICATION_ADDED_TO_CHANNEL,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = with(domainMapping) { CHANNEL.toDomain() },
        member = with(domainMapping) { MEMBER.toDomain() },
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
    )

    private val notificationRemovedFromChannelGenerated = io.getstream.chat.android.network.models.NotificationRemovedFromChannelEvent(
        createdAt = DATE,
        type = EventType.NOTIFICATION_REMOVED_FROM_CHANNEL,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        member = MEMBER,
        user = SLIM_USER,
    )

    private val notificationRemovedFromChannelExpected = NotificationRemovedFromChannelEvent(
        type = EventType.NOTIFICATION_REMOVED_FROM_CHANNEL,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = with(domainMapping) { CHANNEL.toDomain() },
        member = with(domainMapping) { MEMBER.toDomain() },
        user = SLIM_USER_DOMAIN,
    )

    private val notificationInvitedGenerated = io.getstream.chat.android.network.models.NotificationInvitedEvent(
        createdAt = DATE,
        type = EventType.NOTIFICATION_INVITED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        member = MEMBER,
        user = SLIM_USER,
    )

    private val notificationInvitedExpected = NotificationInvitedEvent(
        type = EventType.NOTIFICATION_INVITED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER_DOMAIN,
        member = with(domainMapping) { MEMBER.toDomain() },
    )

    private val notificationInviteAcceptedGenerated = io.getstream.chat.android.network.models.NotificationInviteAcceptedEvent(
        createdAt = DATE,
        type = EventType.NOTIFICATION_INVITE_ACCEPTED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        member = MEMBER,
        user = SLIM_USER,
    )

    private val notificationInviteAcceptedExpected = NotificationInviteAcceptedEvent(
        type = EventType.NOTIFICATION_INVITE_ACCEPTED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER_DOMAIN,
        member = with(domainMapping) { MEMBER.toDomain() },
        channel = with(domainMapping) { CHANNEL.toDomain() },
    )

    private val notificationInviteRejectedGenerated = io.getstream.chat.android.network.models.NotificationInviteRejectedEvent(
        createdAt = DATE,
        type = EventType.NOTIFICATION_INVITE_REJECTED,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        member = MEMBER,
        user = SLIM_USER,
    )

    private val notificationInviteRejectedExpected = NotificationInviteRejectedEvent(
        type = EventType.NOTIFICATION_INVITE_REJECTED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER_DOMAIN,
        member = with(domainMapping) { MEMBER.toDomain() },
        channel = with(domainMapping) { CHANNEL.toDomain() },
    )

    private val notificationMessageNewGenerated = io.getstream.chat.android.network.models.NotificationNewMessageEvent(
        createdAt = DATE,
        messageId = MESSAGE_ID,
        watcherCount = WATCHER_COUNT,
        type = EventType.NOTIFICATION_MESSAGE_NEW,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        message = MESSAGE,
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        unreadCount = TOTAL_UNREAD_COUNT,
    )

    private val notificationMessageNewExpected = NotificationMessageNewEvent(
        type = EventType.NOTIFICATION_MESSAGE_NEW,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = with(domainMapping) { CHANNEL.toDomain() },
        message = with(domainMapping) { MESSAGE.toDomain(CHANNEL.toChannelInfo()) },
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
    )

    private val notificationThreadMessageNewGenerated = io.getstream.chat.android.network.models.NotificationThreadMessageNewEvent(
        createdAt = DATE,
        messageId = MESSAGE_ID,
        threadId = MESSAGE_ID,
        watcherCount = WATCHER_COUNT,
        type = EventType.NOTIFICATION_THREAD_MESSAGE_NEW,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = CHANNEL,
        message = MESSAGE,
        unreadThreads = UNREAD_THREADS,
        unreadThreadMessages = UNREAD_THREAD_MESSAGES,
    )

    private val notificationThreadMessageNewExpected = NotificationThreadMessageNewEvent(
        type = EventType.NOTIFICATION_THREAD_MESSAGE_NEW,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        message = with(domainMapping) { MESSAGE.toDomain(CHANNEL.toChannelInfo()) },
        channel = with(domainMapping) { CHANNEL.toDomain() },
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        unreadThreads = UNREAD_THREADS,
        unreadThreadMessages = UNREAD_THREAD_MESSAGES,
    )

    private val userPresenceChangedGenerated = io.getstream.chat.android.network.models.UserPresenceChangedEvent(
        createdAt = DATE,
        type = EventType.USER_PRESENCE_CHANGED,
        user = SLIM_USER,
    )

    private val userPresenceChangedExpected = UserPresenceChangedEvent(
        type = EventType.USER_PRESENCE_CHANGED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
    )

    private val channelVisibleExpected = ChannelVisibleEvent(
        type = EventType.CHANNEL_VISIBLE,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channel = with(domainMapping) { CHANNEL.toDomain() },
        channelId = CHANNEL_ID,
    )

    private val userWatchingStopExpected = UserStopWatchingEvent(
        type = EventType.USER_WATCHING_STOP,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = SLIM_USER_DOMAIN,
        watcherCount = WATCHER_COUNT,
    )

    private val messageDeliveredExpected = MessageDeliveredEvent(
        type = EventType.MESSAGE_DELIVERED,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        lastDeliveredAt = DATE,
        lastDeliveredMessageId = LAST_DELIVERED_MESSAGE_ID,
    )

    private val markAllReadExpected = MarkAllReadEvent(
        type = EventType.NOTIFICATION_MARK_READ,
        createdAt = DATE,
        rawCreatedAt = DATE_STRING,
        user = SLIM_USER_DOMAIN,
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
    )

    @JvmStatic
    fun generatedArguments() = listOf(
        Arguments.of(messageNewGenerated, DATE_STRING, messageNewExpected),
        Arguments.of(typingStartGenerated, DATE_STRING, typingStartExpected),
        Arguments.of(typingStopGenerated, DATE_STRING, typingStopExpected),
        Arguments.of(reactionDeletedGenerated, DATE_STRING, reactionDeletedExpected),
        Arguments.of(reactionNewGenerated, DATE_STRING, reactionNewExpected),
        Arguments.of(messageUpdatedGenerated, DATE_STRING, messageUpdatedExpected),
        Arguments.of(messageDeletedGenerated, DATE_STRING, messageDeletedExpected),
        Arguments.of(messageReadGenerated, DATE_STRING, messageReadExpected),
        Arguments.of(notificationMarkReadGenerated, DATE_STRING, notificationMarkReadExpected),
        Arguments.of(markAllReadGenerated, DATE_STRING, markAllReadExpected),
        Arguments.of(messageDeliveredGenerated, DATE_STRING, messageDeliveredExpected),
        Arguments.of(memberAddedGenerated, DATE_STRING, memberAddedExpected),
        Arguments.of(memberRemovedGenerated, DATE_STRING, memberRemovedExpected),
        Arguments.of(memberUpdatedGenerated, DATE_STRING, memberUpdatedExpected),
        Arguments.of(userWatchingStartGenerated, DATE_STRING, userWatchingStartExpected),
        Arguments.of(userWatchingStopGenerated, DATE_STRING, userWatchingStopExpected),
        Arguments.of(channelHiddenGenerated, DATE_STRING, channelHiddenExpected),
        Arguments.of(channelVisibleGenerated, DATE_STRING, channelVisibleExpected),
        Arguments.of(userPresenceChangedGenerated, DATE_STRING, userPresenceChangedExpected),
        Arguments.of(channelDeletedGenerated, DATE_STRING, channelDeletedExpected),
        Arguments.of(channelTruncatedGenerated, DATE_STRING, channelTruncatedExpected),
        Arguments.of(notificationChannelDeletedGenerated, DATE_STRING, notificationChannelDeletedExpected),
        Arguments.of(notificationChannelTruncatedGenerated, DATE_STRING, notificationChannelTruncatedExpected),
        Arguments.of(notificationAddedToChannelGenerated, DATE_STRING, notificationAddedToChannelExpected),
        Arguments.of(notificationRemovedFromChannelGenerated, DATE_STRING, notificationRemovedFromChannelExpected),
        Arguments.of(notificationInvitedGenerated, DATE_STRING, notificationInvitedExpected),
        Arguments.of(notificationInviteAcceptedGenerated, DATE_STRING, notificationInviteAcceptedExpected),
        Arguments.of(notificationInviteRejectedGenerated, DATE_STRING, notificationInviteRejectedExpected),
        Arguments.of(notificationMessageNewGenerated, DATE_STRING, notificationMessageNewExpected),
        Arguments.of(notificationThreadMessageNewGenerated, DATE_STRING, notificationThreadMessageNewExpected),
        Arguments.of(draftUpdatedGenerated, DATE_STRING, draftUpdatedExpected),
        Arguments.of(draftDeletedGenerated, DATE_STRING, draftDeletedExpected),
        Arguments.of(userUpdatedGenerated, DATE_STRING, userUpdatedExpected),
        Arguments.of(channelUserBannedGenerated, DATE_STRING, channelUserBannedExpected),
        Arguments.of(globalUserBannedGenerated, DATE_STRING, globalUserBannedExpected),
        Arguments.of(channelUserUnbannedGenerated, DATE_STRING, channelUserUnbannedExpected),
        Arguments.of(globalUserUnbannedGenerated, DATE_STRING, globalUserUnbannedExpected),
        Arguments.of(notificationMutesUpdatedGenerated, DATE_STRING, notificationMutesUpdatedExpected),
        Arguments.of(notificationChannelMutesUpdatedGenerated, DATE_STRING, notificationChannelMutesUpdatedExpected),
        Arguments.of(userDeletedGenerated, DATE_STRING, userDeletedExpected),
        Arguments.of(userMessagesDeletedGenerated, DATE_STRING, userMessagesDeletedExpected),
        Arguments.of(reminderCreatedGenerated, DATE_STRING, reminderCreatedExpected),
        Arguments.of(reminderUpdatedGenerated, DATE_STRING, reminderUpdatedExpected),
        Arguments.of(reminderDeletedGenerated, DATE_STRING, reminderDeletedExpected),
        Arguments.of(aiIndicatorUpdateGenerated, DATE_STRING, aiIndicatorUpdatedExpected),
        Arguments.of(aiIndicatorClearGenerated, DATE_STRING, aiIndicatorClearExpected),
        Arguments.of(aiIndicatorStopGenerated, DATE_STRING, aiIndicatorStopExpected),
        Arguments.of(healthGenerated, DATE_STRING, healthExpected),
        Arguments.of(connectedGenerated, DATE_STRING, connectedExpected),
        Arguments.of(connectionErrorGenerated, DATE_STRING, connectionErrorExpected),
        Arguments.of(pollClosedGenerated, DATE_STRING, pollClosedExpected),
        Arguments.of(pollDeletedGenerated, DATE_STRING, pollDeletedExpected),
        Arguments.of(pollUpdatedGenerated, DATE_STRING, pollUpdatedExpected),
        Arguments.of(voteCastedGenerated, DATE_STRING, voteCastedExpected),
        Arguments.of(voteChangedGenerated, DATE_STRING, voteChangedExpected),
        Arguments.of(voteRemovedGenerated, DATE_STRING, voteRemovedExpected),
        Arguments.of(answerCastedGenerated, DATE_STRING, answerCastedExpected),
        Arguments.of(notificationMarkUnreadGenerated, DATE_STRING, notificationMarkUnreadExpected),
        Arguments.of(reactionUpdatedGenerated, DATE_STRING, reactionUpdatedExpected),
        Arguments.of(channelUpdatedGenerated, DATE_STRING, channelUpdatedExpected),
        Arguments.of(channelUpdatedByUserGenerated, DATE_STRING, channelUpdatedByUserExpected),
        Arguments.of(threadUpdatedGenerated, DATE_STRING, threadUpdatedExpected),
        Arguments.of(notificationReminderDueGenerated, DATE_STRING, notificationReminderDueExpected),
    )
}
