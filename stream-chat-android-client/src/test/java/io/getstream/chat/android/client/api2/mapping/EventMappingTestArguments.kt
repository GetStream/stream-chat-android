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
import io.getstream.chat.android.client.api2.model.dto.AIIndicatorClearEventDto
import io.getstream.chat.android.client.api2.model.dto.AIIndicatorStopEventDto
import io.getstream.chat.android.client.api2.model.dto.AIIndicatorUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelHiddenEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelVisibleEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectingEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectionErrorEventDto
import io.getstream.chat.android.client.api2.model.dto.DisconnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelCustomDto
import io.getstream.chat.android.client.api2.model.dto.ErrorEventDto
import io.getstream.chat.android.client.api2.model.dto.GeneratedEventDto
import io.getstream.chat.android.client.api2.model.dto.HealthEventDto
import io.getstream.chat.android.client.api2.model.dto.MessageDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.MessageUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.NewMessageEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationAddedToChannelEventDto
import io.getstream.chat.android.client.api2.model.dto.ReactionDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.ReactionNewEventDto
import io.getstream.chat.android.client.api2.model.dto.ReactionUpdateEventDto
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
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.ConnectionErrorEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.DraftMessageDeletedEvent
import io.getstream.chat.android.client.events.DraftMessageUpdatedEvent
import io.getstream.chat.android.client.events.ErrorEvent
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
import io.getstream.chat.android.client.events.NewMessageEvent
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
import io.getstream.chat.android.models.Channel
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
import io.getstream.chat.android.network.infrastructure.ExactDate as GeneratedExactDate
import io.getstream.chat.android.network.models.ChannelDeletedEvent as GeneratedChannelDeletedEvent
import io.getstream.chat.android.network.models.ChannelTruncatedEvent as GeneratedChannelTruncatedEvent
import io.getstream.chat.android.network.models.ChannelUpdatedEvent as GeneratedChannelUpdatedEvent
import io.getstream.chat.android.network.models.DraftDeletedEvent as GeneratedDraftDeletedEvent
import io.getstream.chat.android.network.models.DraftUpdatedEvent as GeneratedDraftUpdatedEvent
import io.getstream.chat.android.network.models.MemberAddedEvent as GeneratedMemberAddedEvent
import io.getstream.chat.android.network.models.MemberRemovedEvent as GeneratedMemberRemovedEvent
import io.getstream.chat.android.network.models.MemberUpdatedEvent as GeneratedMemberUpdatedEvent
import io.getstream.chat.android.network.models.MessageDeliveredEvent as GeneratedMessageDeliveredEvent
import io.getstream.chat.android.network.models.MessageReadEvent as GeneratedMessageReadEvent
import io.getstream.chat.android.network.models.NotificationChannelDeletedEvent as GeneratedNotificationChannelDeletedEvent
import io.getstream.chat.android.network.models.NotificationChannelMutesUpdatedEvent as GeneratedNotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.network.models.NotificationChannelTruncatedEvent as GeneratedNotificationChannelTruncatedEvent
import io.getstream.chat.android.network.models.NotificationInviteAcceptedEvent as GeneratedNotificationInviteAcceptedEvent
import io.getstream.chat.android.network.models.NotificationInviteRejectedEvent as GeneratedNotificationInviteRejectedEvent
import io.getstream.chat.android.network.models.NotificationInvitedEvent as GeneratedNotificationInvitedEvent
import io.getstream.chat.android.network.models.NotificationMarkReadEvent as GeneratedNotificationMarkReadEvent
import io.getstream.chat.android.network.models.NotificationMarkUnreadEvent as GeneratedNotificationMarkUnreadEvent
import io.getstream.chat.android.network.models.NotificationMutesUpdatedEvent as GeneratedNotificationMutesUpdatedEvent
import io.getstream.chat.android.network.models.NotificationNewMessageEvent as GeneratedNotificationNewMessageEvent
import io.getstream.chat.android.network.models.NotificationRemovedFromChannelEvent as GeneratedNotificationRemovedFromChannelEvent
import io.getstream.chat.android.network.models.NotificationThreadMessageNewEvent as GeneratedNotificationThreadMessageNewEvent
import io.getstream.chat.android.network.models.PollClosedEvent as GeneratedPollClosedEvent
import io.getstream.chat.android.network.models.PollDeletedEvent as GeneratedPollDeletedEvent
import io.getstream.chat.android.network.models.PollUpdatedEvent as GeneratedPollUpdatedEvent
import io.getstream.chat.android.network.models.PollVoteCastedEvent as GeneratedPollVoteCastedEvent
import io.getstream.chat.android.network.models.PollVoteChangedEvent as GeneratedPollVoteChangedEvent
import io.getstream.chat.android.network.models.PollVoteRemovedEvent as GeneratedPollVoteRemovedEvent
import io.getstream.chat.android.network.models.ReminderCreatedEvent as GeneratedReminderCreatedEvent
import io.getstream.chat.android.network.models.ReminderDeletedEvent as GeneratedReminderDeletedEvent
import io.getstream.chat.android.network.models.ReminderNotificationEvent as GeneratedReminderNotificationEvent
import io.getstream.chat.android.network.models.ReminderUpdatedEvent as GeneratedReminderUpdatedEvent
import io.getstream.chat.android.network.models.ThreadUpdatedEvent as GeneratedThreadUpdatedEvent
import io.getstream.chat.android.network.models.TypingStartEvent as GeneratedTypingStartEvent
import io.getstream.chat.android.network.models.TypingStopEvent as GeneratedTypingStopEvent
import io.getstream.chat.android.network.models.UserBannedEvent as GeneratedUserBannedEvent
import io.getstream.chat.android.network.models.UserDeletedEvent as GeneratedUserDeletedEvent
import io.getstream.chat.android.network.models.UserMessagesDeletedEvent as GeneratedUserMessagesDeletedEvent
import io.getstream.chat.android.network.models.UserPresenceChangedEvent as GeneratedUserPresenceChangedEvent
import io.getstream.chat.android.network.models.UserUnbannedEvent as GeneratedUserUnbannedEvent
import io.getstream.chat.android.network.models.UserUpdatedEvent as GeneratedUserUpdatedEvent
import io.getstream.chat.android.network.models.UserWatchingStartEvent as GeneratedUserWatchingStartEvent
import io.getstream.chat.android.network.models.UserWatchingStopEvent as GeneratedUserWatchingStopEvent

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
    private val GENERATED_EXACT_DATE = GeneratedExactDate.parseOrNull(DATE_STRING)!!
    private val USER = Mother.randomDownstreamUserDto()
    private val OWN_USER = Mother.randomOwnUserResponse()
    private val COMMON_USER = Mother.randomUserResponseCommonFields()
    private val PRIVACY_USER = Mother.randomUserResponsePrivacyFields()
    private val CHANNEL_TYPE = randomString()
    private val CHANNEL_ID = randomString()
    private val CID = "$CHANNEL_TYPE:$CHANNEL_ID"
    private val CHANNEL_MEMBER_COUNT = positiveRandomInt()
    private val CHANNEL_NAME = randomString()
    private val CHANNEL_IMAGE = randomString()
    private val MESSAGE_ID = randomString()
    private val MESSAGE = Mother.randomDownstreamMessageDto()
    private val MESSAGE_WITHOUT_CHANNEL_INFO = MESSAGE.copy(channel = null)
    private val DRAFT = Mother.randomDraftResponse()
    private val CHANNEL = Mother.randomDownstreamChannelDto()
    private val THREAD_ID = randomString()
    private val CLEAR_HISTORY = randomBoolean()
    private val SHADOW_BAN = randomBoolean()
    private val CONNECTION_ID = randomString()
    private val ERROR = Mother.randomErrorDto()
    private val GENERIC_ERROR = Error.GenericError("generic error")
    private val MEMBER = Mother.randomChannelMemberResponse()
    private val HARD_DELETE = randomBoolean()
    private val FIRST_UNREAD_MESSAGE_ID = randomString()
    private val LAST_DELIVERED_MESSAGE_ID = randomString()
    private val LAST_READ_MESSAGE_ID = randomString()
    private val UNREAD_MESSAGES = positiveRandomInt()
    private val TOTAL_UNREAD_COUNT = positiveRandomInt()
    private val UNREAD_CHANNELS = positiveRandomInt()
    private val GROUPED_UNREAD_CHANNELS = mapOf("direct" to positiveRandomInt(), "support" to positiveRandomInt())
    private val UNREAD_THREADS = positiveRandomInt()
    private val UNREAD_THREAD_MESSAGES = positiveRandomInt()
    private val REACTION = Mother.randomReactionResponse()
    private val GENERATED_CHANNEL = Mother.randomChannelResponse(id = CHANNEL_ID, type = CHANNEL_TYPE)
    private val PARTIAL_MEMBER = Mother.randomChannelMemberPartialResponse()
    private val PARENT_ID = randomString()
    private val TEAM = randomString()
    private val WATCHER_COUNT = positiveRandomInt()
    private val GENERATED_MESSAGE = Mother.randomMessageResponse(cid = CID)
    private val POLL = Mother.randomPollResponseData()
    private val POLL_VOTE = Mother.randomPollVoteResponseData(isAnswer = false)
    private val ANSWER_VOTE = Mother.randomPollVoteResponseData(isAnswer = true, answerText = "answer")
    private val REMINDER = Mother.randomReminderResponseData()
    private val THREAD_INFO = Mother.randomThreadResponse()
    private val AI_MESSAGE_ID = randomString()
    private val AI_STATE = randomString()
    private val DELETED_FOR_ME = randomBoolean()

    // BEGIN: DTO Models

    private val newMessageDto = NewMessageEventDto(
        type = EventType.MESSAGE_NEW,
        created_at = EXACT_DATE,
        user = USER,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        channel_member_count = CHANNEL_MEMBER_COUNT,
        channel_custom = DownstreamChannelCustomDto(
            name = CHANNEL_NAME,
            image = CHANNEL_IMAGE,
        ),
        message = MESSAGE_WITHOUT_CHANNEL_INFO,
        grouped_unread_channels = GROUPED_UNREAD_CHANNELS,
    )

    private val draftMessageUpdatedDto = GeneratedEventDto(
        GeneratedDraftUpdatedEvent(
            type = EventType.DRAFT_MESSAGE_UPDATED,
            createdAt = GENERATED_EXACT_DATE,
            draft = DRAFT,
        ),
    )

    private val draftMessageDeletedDto = GeneratedEventDto(
        GeneratedDraftDeletedEvent(
            type = EventType.DRAFT_MESSAGE_DELETED,
            createdAt = GENERATED_EXACT_DATE,
            draft = DRAFT,
        ),
    )

    private val channelDeletedEvent = GeneratedChannelDeletedEvent(
        type = EventType.CHANNEL_DELETED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        channel = GENERATED_CHANNEL,
        user = COMMON_USER,
    )

    private val channelDeletedDto = GeneratedEventDto(channelDeletedEvent)

    private val channelHiddenDto = ChannelHiddenEventDto(
        type = EventType.CHANNEL_HIDDEN,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        channel = CHANNEL,
        clear_history = CLEAR_HISTORY,
    )

    private val channelTruncatedEvent = GeneratedChannelTruncatedEvent(
        type = EventType.CHANNEL_TRUNCATED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        user = COMMON_USER,
        message = GENERATED_MESSAGE,
        channel = GENERATED_CHANNEL,
    )

    private val channelTruncatedDto = GeneratedEventDto(channelTruncatedEvent)

    private val channelUpdatedByUserEvent = GeneratedChannelUpdatedEvent(
        type = EventType.CHANNEL_UPDATED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        user = COMMON_USER,
        message = GENERATED_MESSAGE,
        channel = GENERATED_CHANNEL,
    )

    private val channelUpdatedByUserDto = GeneratedEventDto(channelUpdatedByUserEvent)

    private val channelUpdatedEvent = GeneratedChannelUpdatedEvent(
        type = EventType.CHANNEL_UPDATED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        message = GENERATED_MESSAGE,
        channel = GENERATED_CHANNEL,
    )

    private val channelUpdatedDto = GeneratedEventDto(channelUpdatedEvent)

    private val channelUserBannedEvent = GeneratedUserBannedEvent(
        type = EventType.USER_BANNED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        user = COMMON_USER,
        expiration = DATE,
        shadow = SHADOW_BAN,
    )

    private val channelUserBannedDto = GeneratedEventDto(channelUserBannedEvent)

    private val channelUserUnbannedEvent = GeneratedUserUnbannedEvent(
        type = EventType.USER_UNBANNED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        user = COMMON_USER,
    )

    private val channelUserUnbannedDto = GeneratedEventDto(channelUserUnbannedEvent)

    private val channelVisibleDto = ChannelVisibleEventDto(
        type = EventType.CHANNEL_VISIBLE,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        channel = CHANNEL,
        user = USER,
    )

    private val channelVisibleWithoutChannelDto = channelVisibleDto.copy(channel = null)

    private val connectedDto = ConnectedEventDto(
        type = EventType.CONNECTION_CONNECTING,
        created_at = EXACT_DATE,
        me = OWN_USER,
        connection_id = CONNECTION_ID,
    )

    private val connectionErrorDto = ConnectionErrorEventDto(
        type = EventType.CONNECTION_ERROR,
        created_at = EXACT_DATE,
        connection_id = CONNECTION_ID,
        error = ERROR,
    )

    private val connectingDto = ConnectingEventDto(
        type = EventType.CONNECTION_CONNECTING,
        created_at = EXACT_DATE,
    )

    private val disconnectedDto = DisconnectedEventDto(
        type = EventType.CONNECTION_DISCONNECTED,
        created_at = EXACT_DATE,
    )

    private val errorDto = ErrorEventDto(
        type = EventType.CONNECTION_ERROR,
        created_at = EXACT_DATE,
        error = GENERIC_ERROR,
    )

    private val globalUserBannedEvent = GeneratedUserBannedEvent(
        type = EventType.USER_BANNED,
        createdAt = GENERATED_EXACT_DATE,
        user = COMMON_USER,
    )

    private val globalUserBannedDto = GeneratedEventDto(globalUserBannedEvent)

    private val globalUserUnbannedEvent = GeneratedUserUnbannedEvent(
        type = EventType.USER_UNBANNED,
        createdAt = GENERATED_EXACT_DATE,
        user = COMMON_USER,
    )

    private val globalUserUnbannedDto = GeneratedEventDto(globalUserUnbannedEvent)

    private val healthDto = HealthEventDto(
        type = EventType.HEALTH_CHECK,
        created_at = EXACT_DATE,
        connection_id = CONNECTION_ID,
    )

    private val markAllReadEvent = GeneratedNotificationMarkReadEvent(
        type = EventType.NOTIFICATION_MARK_READ,
        createdAt = GENERATED_EXACT_DATE,
        user = COMMON_USER,
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        unreadCount = TOTAL_UNREAD_COUNT,
        groupedUnreadChannels = GROUPED_UNREAD_CHANNELS,
    )

    private val markAllReadDto = GeneratedEventDto(markAllReadEvent)

    private val memberAddedEvent = GeneratedMemberAddedEvent(
        type = EventType.MEMBER_ADDED,
        createdAt = GENERATED_EXACT_DATE,
        channel = GENERATED_CHANNEL,
        cid = CID,
        user = COMMON_USER,
        member = MEMBER,
    )

    private val memberAddedDto = GeneratedEventDto(memberAddedEvent)

    private val memberRemovedEvent = GeneratedMemberRemovedEvent(
        type = EventType.MEMBER_REMOVED,
        createdAt = GENERATED_EXACT_DATE,
        channel = GENERATED_CHANNEL,
        cid = CID,
        user = COMMON_USER,
        member = MEMBER,
    )

    private val memberRemovedDto = GeneratedEventDto(memberRemovedEvent)

    private val memberUpdatedEvent = GeneratedMemberUpdatedEvent(
        type = EventType.MEMBER_UPDATED,
        createdAt = GENERATED_EXACT_DATE,
        channel = GENERATED_CHANNEL,
        cid = CID,
        user = COMMON_USER,
        member = MEMBER,
    )

    private val memberUpdatedDto = GeneratedEventDto(memberUpdatedEvent)

    private val messageDeletedDto = MessageDeletedEventDto(
        type = EventType.MESSAGE_DELETED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        message = MESSAGE,
        hard_delete = HARD_DELETE,
        deleted_for_me = DELETED_FOR_ME,
    )

    private val messageDeliveredEvent = GeneratedMessageDeliveredEvent(
        type = EventType.MESSAGE_DELIVERED,
        createdAt = GENERATED_EXACT_DATE,
        user = COMMON_USER,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        lastDeliveredAt = DATE_STRING,
        lastDeliveredMessageId = LAST_DELIVERED_MESSAGE_ID,
    )

    private val messageDeliveredDto = GeneratedEventDto(messageDeliveredEvent)

    private val messageReadEvent = GeneratedMessageReadEvent(
        type = EventType.MESSAGE_READ,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = COMMON_USER,
        thread = THREAD_INFO,
        lastReadMessageId = LAST_READ_MESSAGE_ID,
        team = TEAM,
    )

    private val messageReadDto = GeneratedEventDto(messageReadEvent)

    private val messageUpdatedDto = MessageUpdatedEventDto(
        type = EventType.MESSAGE_UPDATED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        message = MESSAGE,
    )

    private val notificationAddedToChannelDto = NotificationAddedToChannelEventDto(
        type = EventType.NOTIFICATION_ADDED_TO_CHANNEL,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        channel = GENERATED_CHANNEL,
        member = MEMBER,
    )

    private val notificationChannelDeletedEvent = GeneratedNotificationChannelDeletedEvent(
        type = EventType.NOTIFICATION_CHANNEL_DELETED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        channel = GENERATED_CHANNEL,
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        groupedUnreadChannels = GROUPED_UNREAD_CHANNELS,
    )

    private val notificationChannelDeletedDto = GeneratedEventDto(notificationChannelDeletedEvent)

    private val notificationChannelMutesUpdatesEvent = GeneratedNotificationChannelMutesUpdatedEvent(
        type = EventType.NOTIFICATION_CHANNEL_MUTES_UPDATED,
        createdAt = GENERATED_EXACT_DATE,
        me = OWN_USER,
    )

    private val notificationChannelMutesUpdatesDto = GeneratedEventDto(notificationChannelMutesUpdatesEvent)

    private val notificationChannelTruncatedEvent = GeneratedNotificationChannelTruncatedEvent(
        type = EventType.NOTIFICATION_CHANNEL_TRUNCATED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        channel = GENERATED_CHANNEL,
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        groupedUnreadChannels = GROUPED_UNREAD_CHANNELS,
    )

    private val notificationChannelTruncatedDto = GeneratedEventDto(notificationChannelTruncatedEvent)

    private val notificationInviteAcceptedEvent = GeneratedNotificationInviteAcceptedEvent(
        type = EventType.NOTIFICATION_INVITE_ACCEPTED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        channel = GENERATED_CHANNEL,
        user = COMMON_USER,
        member = MEMBER,
    )

    private val notificationInviteAcceptedDto = GeneratedEventDto(notificationInviteAcceptedEvent)

    private val notificationInviteRejectedEvent = GeneratedNotificationInviteRejectedEvent(
        type = EventType.NOTIFICATION_INVITE_REJECTED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        channel = GENERATED_CHANNEL,
        user = COMMON_USER,
        member = MEMBER,
    )

    private val notificationInviteRejectedDto = GeneratedEventDto(notificationInviteRejectedEvent)

    private val notificationInvitedEvent = GeneratedNotificationInvitedEvent(
        type = EventType.NOTIFICATION_INVITED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        channel = GENERATED_CHANNEL,
        user = COMMON_USER,
        member = MEMBER,
    )

    private val notificationInvitedDto = GeneratedEventDto(notificationInvitedEvent)

    private val notificationMarkReadEvent = GeneratedNotificationMarkReadEvent(
        type = EventType.NOTIFICATION_MARK_READ,
        createdAt = GENERATED_EXACT_DATE,
        user = COMMON_USER,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        unreadCount = TOTAL_UNREAD_COUNT,
        threadId = PARENT_ID,
        thread = THREAD_INFO,
        unreadThreads = UNREAD_THREADS,
        unreadThreadMessages = UNREAD_THREAD_MESSAGES,
        lastReadMessageId = LAST_READ_MESSAGE_ID,
        groupedUnreadChannels = GROUPED_UNREAD_CHANNELS,
    )

    private val notificationMarkReadDto = GeneratedEventDto(notificationMarkReadEvent)

    private val notificationMarkUnreadEvent = GeneratedNotificationMarkUnreadEvent(
        type = EventType.NOTIFICATION_MARK_UNREAD,
        createdAt = GENERATED_EXACT_DATE,
        user = COMMON_USER,
        cid = CID,
        firstUnreadMessageId = FIRST_UNREAD_MESSAGE_ID,
        lastReadMessageId = LAST_READ_MESSAGE_ID,
        lastReadAt = DATE,
        unreadMessages = UNREAD_MESSAGES,
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        threadId = THREAD_ID,
        unreadThreads = UNREAD_THREADS,
        groupedUnreadChannels = GROUPED_UNREAD_CHANNELS,
    )

    private val notificationMarkUnreadDto = GeneratedEventDto(notificationMarkUnreadEvent)

    private val notificationMessageNewEvent = GeneratedNotificationNewMessageEvent(
        type = EventType.NOTIFICATION_MESSAGE_NEW,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        messageId = GENERATED_MESSAGE.id,
        watcherCount = WATCHER_COUNT,
        message = GENERATED_MESSAGE,
        channel = GENERATED_CHANNEL,
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        groupedUnreadChannels = GROUPED_UNREAD_CHANNELS,
    )

    private val notificationMessageNewDto = GeneratedEventDto(notificationMessageNewEvent)

    private val notificationThreadMessageNewEvent = GeneratedNotificationThreadMessageNewEvent(
        type = EventType.NOTIFICATION_THREAD_MESSAGE_NEW,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        messageId = GENERATED_MESSAGE.id,
        threadId = THREAD_ID,
        watcherCount = WATCHER_COUNT,
        channel = GENERATED_CHANNEL,
        message = GENERATED_MESSAGE,
        unreadThreads = UNREAD_THREADS,
        unreadThreadMessages = UNREAD_THREAD_MESSAGES,
    )

    private val notificationThreadMessageNewDto = GeneratedEventDto(notificationThreadMessageNewEvent)

    private val threadUpdatedEvent = GeneratedThreadUpdatedEvent(
        type = EventType.THREAD_UPDATED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        thread = THREAD_INFO,
    )

    private val threadUpdatedDto = GeneratedEventDto(threadUpdatedEvent)

    private val notificationMutesUpdatedEvent = GeneratedNotificationMutesUpdatedEvent(
        type = EventType.NOTIFICATION_MUTES_UPDATED,
        createdAt = GENERATED_EXACT_DATE,
        me = OWN_USER,
    )

    private val notificationMutesUpdatedDto = GeneratedEventDto(notificationMutesUpdatedEvent)

    private val notificationRemovedFromChannelEvent = GeneratedNotificationRemovedFromChannelEvent(
        type = EventType.NOTIFICATION_REMOVED_FROM_CHANNEL,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        channel = GENERATED_CHANNEL,
        member = MEMBER,
        user = COMMON_USER,
    )

    private val notificationRemovedFromChannelDto = GeneratedEventDto(notificationRemovedFromChannelEvent)

    private val reactionDeletedDto = ReactionDeletedEventDto(
        type = EventType.REACTION_DELETED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        reaction = REACTION,
        message = MESSAGE,
    )

    private val reactionNewDto = ReactionNewEventDto(
        type = EventType.REACTION_NEW,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        reaction = REACTION,
        message = MESSAGE,
    )

    private val reactionUpdateDto = ReactionUpdateEventDto(
        type = EventType.REACTION_UPDATED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        reaction = REACTION,
        message = MESSAGE,
    )

    private val typingStartEvent = GeneratedTypingStartEvent(
        type = EventType.TYPING_START,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        user = COMMON_USER,
        parentId = PARENT_ID,
        member = PARTIAL_MEMBER,
    )

    private val typingStartDto = GeneratedEventDto(typingStartEvent)

    private val typingStopEvent = GeneratedTypingStopEvent(
        type = EventType.TYPING_STOP,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        user = COMMON_USER,
        parentId = PARENT_ID,
        member = PARTIAL_MEMBER,
    )

    private val typingStopDto = GeneratedEventDto(typingStopEvent)

    private val unknownDto = UnknownEventDto(
        type = EventType.UNKNOWN,
        created_at = EXACT_DATE,
        user = USER,
        rawData = emptyMap<String, String>(),
    )

    private val userDeletedEvent = GeneratedUserDeletedEvent(
        type = EventType.USER_DELETED,
        createdAt = GENERATED_EXACT_DATE,
        deleteConversation = "",
        deleteConversationChannels = false,
        deleteMessages = "soft",
        deleteUser = "soft",
        hardDelete = false,
        markMessagesDeleted = true,
        user = COMMON_USER,
    )

    private val userDeletedDto = GeneratedEventDto(userDeletedEvent)

    private val userPresenceChangedEvent = GeneratedUserPresenceChangedEvent(
        type = EventType.USER_PRESENCE_CHANGED,
        createdAt = GENERATED_EXACT_DATE,
        user = COMMON_USER,
    )

    private val userPresenceChangedDto = GeneratedEventDto(userPresenceChangedEvent)

    private val userStartWatchingEvent = GeneratedUserWatchingStartEvent(
        type = EventType.USER_WATCHING_START,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        user = COMMON_USER,
        watcherCount = WATCHER_COUNT,
    )

    private val userStartWatchingDto = GeneratedEventDto(userStartWatchingEvent)

    private val userStopWatchingEvent = GeneratedUserWatchingStopEvent(
        type = EventType.USER_WATCHING_STOP,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        user = COMMON_USER,
        watcherCount = WATCHER_COUNT,
    )

    private val userStopWatchingDto = GeneratedEventDto(userStopWatchingEvent)

    private val userUpdatedEvent = GeneratedUserUpdatedEvent(
        type = EventType.USER_UPDATED,
        createdAt = GENERATED_EXACT_DATE,
        user = PRIVACY_USER,
    )

    private val userUpdatedDto = GeneratedEventDto(userUpdatedEvent)

    private val pollClosedEvent = GeneratedPollClosedEvent(
        type = EventType.POLL_CLOSED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        messageId = MESSAGE_ID,
        poll = POLL,
    )

    private val pollClosedDto = GeneratedEventDto(pollClosedEvent)

    private val pollDeletedEvent = GeneratedPollDeletedEvent(
        type = EventType.POLL_DELETED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        messageId = MESSAGE_ID,
        poll = POLL,
    )

    private val pollDeletedDto = GeneratedEventDto(pollDeletedEvent)

    private val pollUpdatedEvent = GeneratedPollUpdatedEvent(
        type = EventType.POLL_UPDATED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        messageId = MESSAGE_ID,
        poll = POLL,
    )

    private val pollUpdatedDto = GeneratedEventDto(pollUpdatedEvent)

    private val voteCastedEvent = GeneratedPollVoteCastedEvent(
        type = EventType.POLL_VOTE_CASTED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        messageId = MESSAGE_ID,
        poll = POLL,
        pollVote = POLL_VOTE,
    )

    private val voteCastedDto = GeneratedEventDto(voteCastedEvent)

    private val voteChangedEvent = GeneratedPollVoteChangedEvent(
        type = EventType.POLL_VOTE_CHANGED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        messageId = MESSAGE_ID,
        poll = POLL,
        pollVote = POLL_VOTE,
    )

    private val voteChangedDto = GeneratedEventDto(voteChangedEvent)

    private val voteRemovedEvent = GeneratedPollVoteRemovedEvent(
        type = EventType.POLL_VOTE_REMOVED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        messageId = MESSAGE_ID,
        poll = POLL,
        pollVote = POLL_VOTE,
    )

    private val voteRemovedDto = GeneratedEventDto(voteRemovedEvent)

    private val answerCastedEvent = GeneratedPollVoteCastedEvent(
        type = EventType.POLL_VOTE_CASTED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        messageId = MESSAGE_ID,
        poll = POLL,
        pollVote = ANSWER_VOTE,
    )

    private val answerCastedDto = GeneratedEventDto(answerCastedEvent)

    private val reminderCreatedGeneratedEvent = GeneratedReminderCreatedEvent(
        type = EventType.REMINDER_CREATED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        messageId = MESSAGE.id,
        userId = USER.id,
        reminder = REMINDER,
    )

    private val reminderCreatedDto = GeneratedEventDto(reminderCreatedGeneratedEvent)

    private val reminderUpdatedGeneratedEvent = GeneratedReminderUpdatedEvent(
        type = EventType.REMINDER_UPDATED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        messageId = MESSAGE.id,
        userId = USER.id,
        reminder = REMINDER,
    )

    private val reminderUpdatedDto = GeneratedEventDto(reminderUpdatedGeneratedEvent)

    private val reminderDeletedGeneratedEvent = GeneratedReminderDeletedEvent(
        type = EventType.REMINDER_DELETED,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        messageId = MESSAGE.id,
        userId = USER.id,
        reminder = REMINDER,
    )

    private val reminderDeletedDto = GeneratedEventDto(reminderDeletedGeneratedEvent)

    private val notificationReminderDueGeneratedEvent = GeneratedReminderNotificationEvent(
        type = EventType.NOTIFICATION_REMINDER_DUE,
        createdAt = GENERATED_EXACT_DATE,
        cid = CID,
        messageId = MESSAGE.id,
        userId = USER.id,
        reminder = REMINDER,
    )

    private val notificationReminderDueDto = GeneratedEventDto(notificationReminderDueGeneratedEvent)

    private val userMessagesDeletedGeneratedEvent = GeneratedUserMessagesDeletedEvent(
        type = EventType.USER_MESSAGES_DELETED,
        createdAt = GENERATED_EXACT_DATE,
        user = COMMON_USER,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        hardDelete = true,
    )

    private val userMessagesDeletedEventDto = GeneratedEventDto(userMessagesDeletedGeneratedEvent)

    private val aiIndicatorUpdatedDto = AIIndicatorUpdatedEventDto(
        type = EventType.AI_TYPING_INDICATOR_UPDATED,
        created_at = EXACT_DATE,
        cid = CID,
        user = COMMON_USER,
        message_id = AI_MESSAGE_ID,
        ai_state = AI_STATE,
    )

    private val aiIndicatorStopDto = AIIndicatorStopEventDto(
        type = EventType.AI_TYPING_INDICATOR_STOP,
        created_at = EXACT_DATE,
        cid = CID,
        user = COMMON_USER,
    )

    private val ioIndicatorClearDto = AIIndicatorClearEventDto(
        type = EventType.AI_TYPING_INDICATOR_CLEAR,
        created_at = EXACT_DATE,
        cid = CID,
        user = COMMON_USER,
    )

    // END: DTO Models

    // BEGIN: Domain models

    private val newMessage = NewMessageEvent(
        type = newMessageDto.type,
        createdAt = newMessageDto.created_at.date,
        rawCreatedAt = newMessageDto.created_at.rawDate,
        user = with(domainMapping) { newMessageDto.user.toDomain() },
        cid = newMessageDto.cid,
        channelType = newMessageDto.channel_type,
        channelId = newMessageDto.channel_id,
        message = with(domainMapping) {
            val channelInfo = ChannelInfo(
                cid = newMessageDto.cid,
                id = newMessageDto.channel_id,
                type = newMessageDto.channel_type,
                memberCount = newMessageDto.channel_member_count ?: 0,
                name = newMessageDto.channel_custom?.name,
                image = newMessageDto.channel_custom?.image,
            )
            newMessageDto.message.toDomain(channelInfo)
        },
        watcherCount = newMessageDto.watcher_count,
        totalUnreadCount = newMessageDto.total_unread_count,
        unreadChannels = newMessageDto.unread_channels,
        channelMessageCount = newMessageDto.channel_message_count,
        groupedUnreadChannels = newMessageDto.grouped_unread_channels,
    )

    private val draftMessageUpdatedEvent = DraftMessageUpdatedEvent(
        type = EventType.DRAFT_MESSAGE_UPDATED,
        createdAt = GENERATED_EXACT_DATE.date,
        rawCreatedAt = GENERATED_EXACT_DATE.raw,
        draftMessage = with(domainMapping) { DRAFT.toDomain() },
    )

    private val draftMessageDeletedEvent = DraftMessageDeletedEvent(
        type = EventType.DRAFT_MESSAGE_DELETED,
        createdAt = GENERATED_EXACT_DATE.date,
        rawCreatedAt = GENERATED_EXACT_DATE.raw,
        draftMessage = with(domainMapping) { DRAFT.toDomain() },
    )

    private val channelDeleted = ChannelDeletedEvent(
        type = channelDeletedEvent.type,
        createdAt = channelDeletedEvent.createdAt.date,
        rawCreatedAt = channelDeletedEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        channel = with(domainMapping) { GENERATED_CHANNEL.toDomain() },
    )

    private val channelHidden = ChannelHiddenEvent(
        type = channelHiddenDto.type,
        createdAt = channelHiddenDto.created_at.date,
        rawCreatedAt = channelHiddenDto.created_at.rawDate,
        user = with(domainMapping) { channelHiddenDto.user.toDomain() },
        cid = channelHiddenDto.cid,
        channelType = channelHiddenDto.channel_type,
        channelId = channelHiddenDto.channel_id,
        channel = with(domainMapping) { channelHiddenDto.channel.toDomain() },
        clearHistory = channelHiddenDto.clear_history,
    )

    private val channelTruncated = ChannelTruncatedEvent(
        type = channelTruncatedEvent.type,
        createdAt = channelTruncatedEvent.createdAt.date,
        rawCreatedAt = channelTruncatedEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        message = with(domainMapping) { GENERATED_MESSAGE.toDomain(GENERATED_CHANNEL.toChannelInfo()) },
        channel = with(domainMapping) { GENERATED_CHANNEL.toDomain() },
    )

    private val channelUpdatedByUser = ChannelUpdatedByUserEvent(
        type = channelUpdatedByUserEvent.type,
        createdAt = channelUpdatedByUserEvent.createdAt.date,
        rawCreatedAt = channelUpdatedByUserEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        message = with(domainMapping) { GENERATED_MESSAGE.toDomain(GENERATED_CHANNEL.toChannelInfo()) },
        channel = with(domainMapping) { GENERATED_CHANNEL.toDomain() },
    )

    private val channelUpdated = ChannelUpdatedEvent(
        type = channelUpdatedEvent.type,
        createdAt = channelUpdatedEvent.createdAt.date,
        rawCreatedAt = channelUpdatedEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        message = with(domainMapping) { GENERATED_MESSAGE.toDomain(GENERATED_CHANNEL.toChannelInfo()) },
        channel = with(domainMapping) { GENERATED_CHANNEL.toDomain() },
    )

    private val channelUserBanned = ChannelUserBannedEvent(
        type = channelUserBannedEvent.type,
        createdAt = channelUserBannedEvent.createdAt.date,
        rawCreatedAt = channelUserBannedEvent.createdAt.raw,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        expiration = DATE,
        shadow = SHADOW_BAN,
    )

    private val channelUserUnbanned = ChannelUserUnbannedEvent(
        type = channelUserUnbannedEvent.type,
        createdAt = channelUserUnbannedEvent.createdAt.date,
        rawCreatedAt = channelUserUnbannedEvent.createdAt.raw,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
    )

    private val channelVisible = ChannelVisibleEvent(
        type = channelVisibleDto.type,
        createdAt = channelVisibleDto.created_at.date,
        rawCreatedAt = channelVisibleDto.created_at.rawDate,
        user = with(domainMapping) { channelVisibleDto.user.toDomain() },
        cid = channelVisibleDto.cid,
        channelType = channelVisibleDto.channel_type,
        channel = with(domainMapping) { CHANNEL.toDomain() },
        channelId = channelVisibleDto.channel_id,
    )

    private val channelVisibleWithoutChannel = channelVisible.copy(
        channel = Channel(id = CHANNEL_ID, type = CHANNEL_TYPE),
    )

    private val connected = ConnectedEvent(
        type = connectedDto.type,
        createdAt = connectedDto.created_at.date,
        rawCreatedAt = connectedDto.created_at.rawDate,
        me = with(domainMapping) { connectedDto.me.toDomain() },
        connectionId = connectedDto.connection_id,
    )

    private val connectionError = ConnectionErrorEvent(
        type = connectionErrorDto.type,
        createdAt = connectionErrorDto.created_at.date,
        rawCreatedAt = connectionErrorDto.created_at.rawDate,
        connectionId = connectionErrorDto.connection_id,
        error = connectionErrorDto.error.toDomain(),
    )

    private val connecting = ConnectingEvent(
        type = connectingDto.type,
        createdAt = connectingDto.created_at.date,
        rawCreatedAt = connectingDto.created_at.rawDate,
    )

    private val disconnected = DisconnectedEvent(
        type = disconnectedDto.type,
        createdAt = disconnectedDto.created_at.date,
        rawCreatedAt = disconnectedDto.created_at.rawDate,
    )

    val error = ErrorEvent(
        type = errorDto.type,
        createdAt = errorDto.created_at.date,
        rawCreatedAt = errorDto.created_at.rawDate,
        error = errorDto.error,
    )

    private val globalUserBanned = GlobalUserBannedEvent(
        type = globalUserBannedEvent.type,
        createdAt = globalUserBannedEvent.createdAt.date,
        rawCreatedAt = globalUserBannedEvent.createdAt.raw,
        user = with(domainMapping) { COMMON_USER.toDomain() },
    )

    private val globalUserUnbanned = GlobalUserUnbannedEvent(
        type = globalUserUnbannedEvent.type,
        createdAt = globalUserUnbannedEvent.createdAt.date,
        rawCreatedAt = globalUserUnbannedEvent.createdAt.raw,
        user = with(domainMapping) { COMMON_USER.toDomain() },
    )

    private val health = HealthEvent(
        type = healthDto.type,
        createdAt = healthDto.created_at.date,
        rawCreatedAt = healthDto.created_at.rawDate,
        connectionId = healthDto.connection_id,
    )

    private val markAllRead = MarkAllReadEvent(
        type = EventType.NOTIFICATION_MARK_READ,
        createdAt = GENERATED_EXACT_DATE.date,
        rawCreatedAt = GENERATED_EXACT_DATE.raw,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        groupedUnreadChannels = GROUPED_UNREAD_CHANNELS,
    )

    private val memberAdded = MemberAddedEvent(
        type = memberAddedEvent.type,
        createdAt = memberAddedEvent.createdAt.date,
        rawCreatedAt = memberAddedEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        member = with(domainMapping) { MEMBER.toDomain() },
    )

    private val memberRemoved = MemberRemovedEvent(
        type = memberRemovedEvent.type,
        createdAt = memberRemovedEvent.createdAt.date,
        rawCreatedAt = memberRemovedEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        member = with(domainMapping) { MEMBER.toDomain() },
    )

    private val memberUpdated = MemberUpdatedEvent(
        type = memberUpdatedEvent.type,
        createdAt = memberUpdatedEvent.createdAt.date,
        rawCreatedAt = memberUpdatedEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        member = with(domainMapping) { MEMBER.toDomain() },
    )

    private val messageDeleted = MessageDeletedEvent(
        type = messageDeletedDto.type,
        createdAt = messageDeletedDto.created_at.date,
        rawCreatedAt = messageDeletedDto.created_at.rawDate,
        cid = messageDeletedDto.cid,
        channelType = messageDeletedDto.channel_type,
        channelId = messageDeletedDto.channel_id,
        user = with(domainMapping) { messageDeletedDto.user?.toDomain() },
        message = with(domainMapping) { messageDeletedDto.message.toDomain() },
        hardDelete = messageDeletedDto.hard_delete ?: false,
        channelMessageCount = messageDeletedDto.channel_message_count,
        deletedForMe = messageDeletedDto.deleted_for_me ?: false,
    )

    private val messageDelivered = MessageDeliveredEvent(
        type = EventType.MESSAGE_DELIVERED,
        createdAt = GENERATED_EXACT_DATE.date,
        rawCreatedAt = GENERATED_EXACT_DATE.raw,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        lastDeliveredAt = GENERATED_EXACT_DATE.date,
        lastDeliveredMessageId = LAST_DELIVERED_MESSAGE_ID,
    )

    private val messageRead = MessageReadEvent(
        type = EventType.MESSAGE_READ,
        createdAt = GENERATED_EXACT_DATE.date,
        rawCreatedAt = GENERATED_EXACT_DATE.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        thread = with(domainMapping) { THREAD_INFO.toDomain() },
        lastReadMessageId = LAST_READ_MESSAGE_ID,
        team = TEAM,
    )

    private val messageUpdated = MessageUpdatedEvent(
        type = messageUpdatedDto.type,
        createdAt = messageUpdatedDto.created_at.date,
        rawCreatedAt = messageUpdatedDto.created_at.rawDate,
        cid = messageUpdatedDto.cid,
        channelType = messageUpdatedDto.channel_type,
        channelId = messageUpdatedDto.channel_id,
        user = with(domainMapping) { messageUpdatedDto.user.toDomain() },
        message = with(domainMapping) { messageUpdatedDto.message.toDomain() },
    )

    private val notificationAddedToChannel = NotificationAddedToChannelEvent(
        type = notificationAddedToChannelDto.type,
        createdAt = notificationAddedToChannelDto.created_at.date,
        rawCreatedAt = notificationAddedToChannelDto.created_at.rawDate,
        cid = notificationAddedToChannelDto.cid,
        channelType = notificationAddedToChannelDto.channel_type,
        channelId = notificationAddedToChannelDto.channel_id,
        channel = with(domainMapping) {
            notificationAddedToChannelDto.channel.toDomain()
        },
        member = with(domainMapping) { notificationAddedToChannelDto.member.toDomain() },
    )

    private val notificationChannelDeleted = NotificationChannelDeletedEvent(
        type = notificationChannelDeletedEvent.type,
        createdAt = notificationChannelDeletedEvent.createdAt.date,
        rawCreatedAt = notificationChannelDeletedEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = with(domainMapping) { GENERATED_CHANNEL.toDomain() },
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        groupedUnreadChannels = GROUPED_UNREAD_CHANNELS,
    )

    private val notificationChannelMutesUpdates = NotificationChannelMutesUpdatedEvent(
        type = notificationChannelMutesUpdatesEvent.type,
        createdAt = notificationChannelMutesUpdatesEvent.createdAt.date,
        rawCreatedAt = notificationChannelMutesUpdatesEvent.createdAt.raw,
        me = with(domainMapping) { OWN_USER.toDomain() },
    )

    private val notificationChannelTruncated = NotificationChannelTruncatedEvent(
        type = notificationChannelTruncatedEvent.type,
        createdAt = notificationChannelTruncatedEvent.createdAt.date,
        rawCreatedAt = notificationChannelTruncatedEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = with(domainMapping) { GENERATED_CHANNEL.toDomain() },
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        groupedUnreadChannels = GROUPED_UNREAD_CHANNELS,
    )

    private val notificationInviteAccepted = NotificationInviteAcceptedEvent(
        type = notificationInviteAcceptedEvent.type,
        createdAt = notificationInviteAcceptedEvent.createdAt.date,
        rawCreatedAt = notificationInviteAcceptedEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        member = with(domainMapping) { MEMBER.toDomain() },
        channel = with(domainMapping) { GENERATED_CHANNEL.toDomain() },
    )

    private val notificationInviteRejected = NotificationInviteRejectedEvent(
        type = notificationInviteRejectedEvent.type,
        createdAt = notificationInviteRejectedEvent.createdAt.date,
        rawCreatedAt = notificationInviteRejectedEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        member = with(domainMapping) { MEMBER.toDomain() },
        channel = with(domainMapping) { GENERATED_CHANNEL.toDomain() },
    )

    private val notificationInvited = NotificationInvitedEvent(
        type = notificationInvitedEvent.type,
        createdAt = notificationInvitedEvent.createdAt.date,
        rawCreatedAt = notificationInvitedEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        member = with(domainMapping) { MEMBER.toDomain() },
    )

    private val notificationMarkRead = NotificationMarkReadEvent(
        type = EventType.NOTIFICATION_MARK_READ,
        createdAt = GENERATED_EXACT_DATE.date,
        rawCreatedAt = GENERATED_EXACT_DATE.raw,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        threadId = PARENT_ID,
        thread = with(domainMapping) { THREAD_INFO.toDomain() },
        unreadThreads = UNREAD_THREADS,
        unreadThreadMessages = UNREAD_THREAD_MESSAGES,
        lastReadMessageId = LAST_READ_MESSAGE_ID,
        groupedUnreadChannels = GROUPED_UNREAD_CHANNELS,
    )

    private val notificationMarkUnread = NotificationMarkUnreadEvent(
        type = notificationMarkUnreadEvent.type,
        createdAt = notificationMarkUnreadEvent.createdAt.date,
        rawCreatedAt = notificationMarkUnreadEvent.createdAt.raw,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        firstUnreadMessageId = FIRST_UNREAD_MESSAGE_ID,
        lastReadMessageId = LAST_READ_MESSAGE_ID,
        lastReadMessageAt = DATE,
        unreadMessages = UNREAD_MESSAGES,
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        threadId = THREAD_ID,
        unreadThreads = UNREAD_THREADS,
        groupedUnreadChannels = GROUPED_UNREAD_CHANNELS,
    )

    private val notificationMessageNew = NotificationMessageNewEvent(
        type = notificationMessageNewEvent.type,
        createdAt = notificationMessageNewEvent.createdAt.date,
        rawCreatedAt = notificationMessageNewEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        message = with(domainMapping) { GENERATED_MESSAGE.toDomain(GENERATED_CHANNEL.toChannelInfo()) },
        channel = with(domainMapping) { GENERATED_CHANNEL.toDomain() },
        totalUnreadCount = TOTAL_UNREAD_COUNT,
        unreadChannels = UNREAD_CHANNELS,
        groupedUnreadChannels = GROUPED_UNREAD_CHANNELS,
    )

    private val notificationThreadMessageNew = NotificationThreadMessageNewEvent(
        type = notificationThreadMessageNewEvent.type,
        createdAt = notificationThreadMessageNewEvent.createdAt.date,
        rawCreatedAt = notificationThreadMessageNewEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        message = with(domainMapping) { GENERATED_MESSAGE.toDomain(GENERATED_CHANNEL.toChannelInfo()) },
        channel = with(domainMapping) { GENERATED_CHANNEL.toDomain() },
        unreadThreads = UNREAD_THREADS,
        unreadThreadMessages = UNREAD_THREAD_MESSAGES,
    )

    private val threadUpdated = ThreadUpdatedEvent(
        type = threadUpdatedEvent.type,
        createdAt = threadUpdatedEvent.createdAt.date,
        rawCreatedAt = threadUpdatedEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        thread = with(domainMapping) { THREAD_INFO.toDomain() },
    )

    private val notificationMutesUpdated = NotificationMutesUpdatedEvent(
        type = notificationMutesUpdatedEvent.type,
        createdAt = notificationMutesUpdatedEvent.createdAt.date,
        rawCreatedAt = notificationMutesUpdatedEvent.createdAt.raw,
        me = with(domainMapping) { OWN_USER.toDomain() },
    )

    private val notificationRemovedFromChannel = NotificationRemovedFromChannelEvent(
        type = notificationRemovedFromChannelEvent.type,
        createdAt = notificationRemovedFromChannelEvent.createdAt.date,
        rawCreatedAt = notificationRemovedFromChannelEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        channel = with(domainMapping) { GENERATED_CHANNEL.toDomain() },
        member = with(domainMapping) { MEMBER.toDomain() },
        user = with(domainMapping) { COMMON_USER.toDomain() },
    )

    private val reactionDeleted = ReactionDeletedEvent(
        type = reactionDeletedDto.type,
        createdAt = reactionDeletedDto.created_at.date,
        rawCreatedAt = reactionDeletedDto.created_at.rawDate,
        cid = reactionDeletedDto.cid,
        channelType = reactionDeletedDto.channel_type,
        channelId = reactionDeletedDto.channel_id,
        user = with(domainMapping) { reactionDeletedDto.user.toDomain() },
        reaction = with(domainMapping) { reactionDeletedDto.reaction.toDomain() },
        message = with(domainMapping) { reactionDeletedDto.message.toDomain() },
    )

    private val reactionNew = ReactionNewEvent(
        type = reactionNewDto.type,
        createdAt = reactionNewDto.created_at.date,
        rawCreatedAt = reactionNewDto.created_at.rawDate,
        cid = reactionNewDto.cid,
        channelType = reactionNewDto.channel_type,
        channelId = reactionNewDto.channel_id,
        user = with(domainMapping) { reactionNewDto.user.toDomain() },
        reaction = with(domainMapping) { reactionNewDto.reaction.toDomain() },
        message = with(domainMapping) { reactionNewDto.message.toDomain() },
    )

    private val reactionUpdate = ReactionUpdateEvent(
        type = reactionUpdateDto.type,
        createdAt = reactionUpdateDto.created_at.date,
        rawCreatedAt = reactionUpdateDto.created_at.rawDate,
        cid = reactionUpdateDto.cid,
        channelType = reactionUpdateDto.channel_type,
        channelId = reactionUpdateDto.channel_id,
        user = with(domainMapping) { reactionUpdateDto.user.toDomain() },
        reaction = with(domainMapping) { reactionUpdateDto.reaction.toDomain() },
        message = with(domainMapping) { reactionUpdateDto.message.toDomain() },
    )

    private val typingStart = TypingStartEvent(
        type = typingStartEvent.type,
        createdAt = typingStartEvent.createdAt.date,
        rawCreatedAt = typingStartEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        parentId = PARENT_ID,
        member = with(domainMapping) { PARTIAL_MEMBER.toDomain() },
    )

    private val typingStop = TypingStopEvent(
        type = typingStopEvent.type,
        createdAt = typingStopEvent.createdAt.date,
        rawCreatedAt = typingStopEvent.createdAt.raw,
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        parentId = PARENT_ID,
        member = with(domainMapping) { PARTIAL_MEMBER.toDomain() },
    )

    private val unknown = UnknownEvent(
        type = unknownDto.type,
        createdAt = unknownDto.created_at.date,
        rawCreatedAt = unknownDto.created_at.rawDate,
        user = with(domainMapping) { unknownDto.user?.toDomain() },
        rawData = unknownDto.rawData,
    )

    private val userDeleted = UserDeletedEvent(
        type = userDeletedEvent.type,
        createdAt = userDeletedEvent.createdAt.date,
        rawCreatedAt = userDeletedEvent.createdAt.raw,
        user = with(domainMapping) { COMMON_USER.toDomain() },
    )

    private val userPresenceChanged = UserPresenceChangedEvent(
        type = userPresenceChangedEvent.type,
        createdAt = userPresenceChangedEvent.createdAt.date,
        rawCreatedAt = userPresenceChangedEvent.createdAt.raw,
        user = with(domainMapping) { COMMON_USER.toDomain() },
    )

    private val userStartWatching = UserStartWatchingEvent(
        type = userStartWatchingEvent.type,
        createdAt = userStartWatchingEvent.createdAt.date,
        rawCreatedAt = userStartWatchingEvent.createdAt.raw,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        watcherCount = WATCHER_COUNT,
    )

    private val userStopWatching = UserStopWatchingEvent(
        type = userStopWatchingEvent.type,
        createdAt = userStopWatchingEvent.createdAt.date,
        rawCreatedAt = userStopWatchingEvent.createdAt.raw,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        watcherCount = WATCHER_COUNT,
    )

    private val userUpdated = UserUpdatedEvent(
        type = EventType.USER_UPDATED,
        createdAt = GENERATED_EXACT_DATE.date,
        rawCreatedAt = GENERATED_EXACT_DATE.raw,
        user = with(domainMapping) { PRIVACY_USER.toDomain() },
    )

    private val pollClosed = PollClosedEvent(
        type = pollClosedEvent.type,
        createdAt = pollClosedEvent.createdAt.date,
        rawCreatedAt = pollClosedEvent.createdAt.raw,
        cid = pollClosedEvent.cid!!,
        channelType = pollClosedEvent.cid!!.split(":").first(),
        channelId = pollClosedEvent.cid!!.split(":").last(),
        messageId = pollClosedEvent.messageId,
        poll = with(domainMapping) { pollClosedEvent.poll.toDomain() },
    )

    private val pollDeleted = PollDeletedEvent(
        type = pollDeletedEvent.type,
        createdAt = pollDeletedEvent.createdAt.date,
        rawCreatedAt = pollDeletedEvent.createdAt.raw,
        cid = pollDeletedEvent.cid!!,
        channelType = pollDeletedEvent.cid!!.split(":").first(),
        channelId = pollDeletedEvent.cid!!.split(":").last(),
        messageId = pollDeletedEvent.messageId,
        poll = with(domainMapping) { pollDeletedEvent.poll.toDomain() },
    )

    private val pollUpdated = PollUpdatedEvent(
        type = pollUpdatedEvent.type,
        createdAt = pollUpdatedEvent.createdAt.date,
        rawCreatedAt = pollUpdatedEvent.createdAt.raw,
        cid = pollUpdatedEvent.cid!!,
        channelType = pollUpdatedEvent.cid!!.split(":").first(),
        channelId = pollUpdatedEvent.cid!!.split(":").last(),
        messageId = pollUpdatedEvent.messageId,
        poll = with(domainMapping) { pollUpdatedEvent.poll.toDomain() },
    )

    private val voteCasted = VoteCastedEvent(
        type = voteCastedEvent.type,
        createdAt = voteCastedEvent.createdAt.date,
        rawCreatedAt = voteCastedEvent.createdAt.raw,
        cid = voteCastedEvent.cid!!,
        channelType = voteCastedEvent.cid!!.split(":").first(),
        channelId = voteCastedEvent.cid!!.split(":").last(),
        messageId = voteCastedEvent.messageId,
        poll = with(domainMapping) { voteCastedEvent.poll.toDomain() },
        newVote = with(domainMapping) { voteCastedEvent.pollVote.toDomain() },
    )

    private val voteChanged = VoteChangedEvent(
        type = voteChangedEvent.type,
        createdAt = voteChangedEvent.createdAt.date,
        rawCreatedAt = voteChangedEvent.createdAt.raw,
        cid = voteChangedEvent.cid!!,
        channelType = voteChangedEvent.cid!!.split(":").first(),
        channelId = voteChangedEvent.cid!!.split(":").last(),
        messageId = voteChangedEvent.messageId,
        poll = with(domainMapping) { voteChangedEvent.poll.toDomain() },
        newVote = with(domainMapping) { voteChangedEvent.pollVote.toDomain() },
    )

    private val voteRemoved = VoteRemovedEvent(
        type = voteRemovedEvent.type,
        createdAt = voteRemovedEvent.createdAt.date,
        rawCreatedAt = voteRemovedEvent.createdAt.raw,
        cid = voteRemovedEvent.cid!!,
        channelType = voteRemovedEvent.cid!!.split(":").first(),
        channelId = voteRemovedEvent.cid!!.split(":").last(),
        messageId = voteRemovedEvent.messageId,
        poll = with(domainMapping) { voteRemovedEvent.poll.toDomain() },
        removedVote = with(domainMapping) { voteRemovedEvent.pollVote.toDomain() },
    )

    private val answerCasted = AnswerCastedEvent(
        type = answerCastedEvent.type,
        createdAt = answerCastedEvent.createdAt.date,
        rawCreatedAt = answerCastedEvent.createdAt.raw,
        cid = answerCastedEvent.cid!!,
        channelType = answerCastedEvent.cid!!.split(":").first(),
        channelId = answerCastedEvent.cid!!.split(":").last(),
        messageId = answerCastedEvent.messageId,
        poll = with(domainMapping) { answerCastedEvent.poll.toDomain() },
        newAnswer = with(domainMapping) { answerCastedEvent.pollVote.toAnswerDomain() },
    )

    private val reminderCreatedEvent = ReminderCreatedEvent(
        type = reminderCreatedGeneratedEvent.type,
        createdAt = reminderCreatedGeneratedEvent.createdAt.date,
        rawCreatedAt = reminderCreatedGeneratedEvent.createdAt.raw,
        cid = reminderCreatedGeneratedEvent.cid,
        channelType = reminderCreatedGeneratedEvent.cid.split(":").first(),
        channelId = reminderCreatedGeneratedEvent.cid.split(":").last(),
        messageId = reminderCreatedGeneratedEvent.messageId,
        userId = reminderCreatedGeneratedEvent.userId,
        reminder = with(domainMapping) { reminderCreatedGeneratedEvent.reminder.toDomain() },
    )

    private val reminderDeletedEvent = ReminderDeletedEvent(
        type = reminderDeletedGeneratedEvent.type,
        createdAt = reminderDeletedGeneratedEvent.createdAt.date,
        rawCreatedAt = reminderDeletedGeneratedEvent.createdAt.raw,
        cid = reminderDeletedGeneratedEvent.cid,
        channelType = reminderDeletedGeneratedEvent.cid.split(":").first(),
        channelId = reminderDeletedGeneratedEvent.cid.split(":").last(),
        messageId = reminderDeletedGeneratedEvent.messageId,
        userId = reminderDeletedGeneratedEvent.userId,
        reminder = with(domainMapping) { reminderDeletedGeneratedEvent.reminder.toDomain() },
    )

    private val reminderUpdatedEvent = ReminderUpdatedEvent(
        type = reminderUpdatedGeneratedEvent.type,
        createdAt = reminderUpdatedGeneratedEvent.createdAt.date,
        rawCreatedAt = reminderUpdatedGeneratedEvent.createdAt.raw,
        cid = reminderUpdatedGeneratedEvent.cid,
        channelType = reminderUpdatedGeneratedEvent.cid.split(":").first(),
        channelId = reminderUpdatedGeneratedEvent.cid.split(":").last(),
        messageId = reminderUpdatedGeneratedEvent.messageId,
        userId = reminderUpdatedGeneratedEvent.userId,
        reminder = with(domainMapping) { reminderUpdatedGeneratedEvent.reminder.toDomain() },
    )

    private val notificationReminderDueEvent = NotificationReminderDueEvent(
        type = notificationReminderDueGeneratedEvent.type,
        createdAt = notificationReminderDueGeneratedEvent.createdAt.date,
        rawCreatedAt = notificationReminderDueGeneratedEvent.createdAt.raw,
        cid = notificationReminderDueGeneratedEvent.cid,
        channelType = notificationReminderDueGeneratedEvent.cid.split(":").first(),
        channelId = notificationReminderDueGeneratedEvent.cid.split(":").last(),
        messageId = notificationReminderDueGeneratedEvent.messageId,
        userId = notificationReminderDueGeneratedEvent.userId,
        reminder = with(domainMapping) { notificationReminderDueGeneratedEvent.reminder.toDomain() },
    )

    private val aiIndicatorUpdated = AIIndicatorUpdatedEvent(
        type = aiIndicatorUpdatedDto.type,
        createdAt = aiIndicatorUpdatedDto.created_at.date,
        rawCreatedAt = aiIndicatorUpdatedDto.created_at.rawDate,
        cid = aiIndicatorUpdatedDto.cid,
        channelType = aiIndicatorUpdatedDto.cid.split(":").first(),
        channelId = aiIndicatorUpdatedDto.cid.split(":").last(),
        user = with(domainMapping) { aiIndicatorUpdatedDto.user.toDomain() },
        messageId = aiIndicatorUpdatedDto.message_id,
        aiState = aiIndicatorUpdatedDto.ai_state,
    )

    private val aiIndicatorStop = AIIndicatorStopEvent(
        type = aiIndicatorStopDto.type,
        createdAt = aiIndicatorStopDto.created_at.date,
        rawCreatedAt = aiIndicatorStopDto.created_at.rawDate,
        cid = aiIndicatorStopDto.cid,
        channelType = aiIndicatorStopDto.cid.split(":").first(),
        channelId = aiIndicatorStopDto.cid.split(":").last(),
        user = with(domainMapping) { aiIndicatorStopDto.user.toDomain() },
    )

    private val aiIndicatorClear = AIIndicatorClearEvent(
        type = ioIndicatorClearDto.type,
        createdAt = ioIndicatorClearDto.created_at.date,
        rawCreatedAt = ioIndicatorClearDto.created_at.rawDate,
        cid = ioIndicatorClearDto.cid,
        channelType = ioIndicatorClearDto.cid.split(":").first(),
        channelId = ioIndicatorClearDto.cid.split(":").last(),
        user = with(domainMapping) { ioIndicatorClearDto.user.toDomain() },
    )

    private val userMessagesDeletedEvent = UserMessagesDeletedEvent(
        type = EventType.USER_MESSAGES_DELETED,
        createdAt = GENERATED_EXACT_DATE.date,
        rawCreatedAt = GENERATED_EXACT_DATE.raw,
        user = with(domainMapping) { COMMON_USER.toDomain() },
        cid = CID,
        channelType = CHANNEL_TYPE,
        channelId = CHANNEL_ID,
        hardDelete = true,
    )

    // END: Domain models

    /**
     * Provides the test arguments for the [EventMappingTest].
     */
    @JvmStatic
    @Suppress("LongMethod")
    fun arguments() = listOf(
        Arguments.of(newMessageDto, newMessage),
        Arguments.of(draftMessageUpdatedDto, draftMessageUpdatedEvent),
        Arguments.of(draftMessageDeletedDto, draftMessageDeletedEvent),
        Arguments.of(channelDeletedDto, channelDeleted),
        Arguments.of(channelHiddenDto, channelHidden),
        Arguments.of(channelTruncatedDto, channelTruncated),
        Arguments.of(channelUpdatedByUserDto, channelUpdatedByUser),
        Arguments.of(channelUpdatedDto, channelUpdated),
        Arguments.of(channelUserBannedDto, channelUserBanned),
        Arguments.of(channelUserUnbannedDto, channelUserUnbanned),
        Arguments.of(channelVisibleDto, channelVisible),
        Arguments.of(channelVisibleWithoutChannelDto, channelVisibleWithoutChannel),
        Arguments.of(connectedDto, connected),
        Arguments.of(connectionErrorDto, connectionError),
        Arguments.of(connectingDto, connecting),
        Arguments.of(disconnectedDto, disconnected),
        Arguments.of(errorDto, error),
        Arguments.of(globalUserBannedDto, globalUserBanned),
        Arguments.of(globalUserUnbannedDto, globalUserUnbanned),
        Arguments.of(healthDto, health),
        Arguments.of(markAllReadDto, markAllRead),
        Arguments.of(memberAddedDto, memberAdded),
        Arguments.of(memberRemovedDto, memberRemoved),
        Arguments.of(memberUpdatedDto, memberUpdated),
        Arguments.of(messageDeletedDto, messageDeleted),
        Arguments.of(messageDeliveredDto, messageDelivered),
        Arguments.of(messageReadDto, messageRead),
        Arguments.of(messageUpdatedDto, messageUpdated),
        Arguments.of(notificationAddedToChannelDto, notificationAddedToChannel),
        Arguments.of(notificationChannelDeletedDto, notificationChannelDeleted),
        Arguments.of(notificationChannelMutesUpdatesDto, notificationChannelMutesUpdates),
        Arguments.of(notificationChannelTruncatedDto, notificationChannelTruncated),
        Arguments.of(notificationInviteAcceptedDto, notificationInviteAccepted),
        Arguments.of(notificationInviteRejectedDto, notificationInviteRejected),
        Arguments.of(notificationInvitedDto, notificationInvited),
        Arguments.of(notificationMarkReadDto, notificationMarkRead),
        Arguments.of(notificationMarkUnreadDto, notificationMarkUnread),
        Arguments.of(notificationMessageNewDto, notificationMessageNew),
        Arguments.of(notificationThreadMessageNewDto, notificationThreadMessageNew),
        Arguments.of(threadUpdatedDto, threadUpdated),
        Arguments.of(notificationMutesUpdatedDto, notificationMutesUpdated),
        Arguments.of(notificationRemovedFromChannelDto, notificationRemovedFromChannel),
        Arguments.of(reactionDeletedDto, reactionDeleted),
        Arguments.of(reactionNewDto, reactionNew),
        Arguments.of(reactionUpdateDto, reactionUpdate),
        Arguments.of(typingStartDto, typingStart),
        Arguments.of(typingStopDto, typingStop),
        Arguments.of(unknownDto, unknown),
        Arguments.of(userDeletedDto, userDeleted),
        Arguments.of(userPresenceChangedDto, userPresenceChanged),
        Arguments.of(userStartWatchingDto, userStartWatching),
        Arguments.of(userStopWatchingDto, userStopWatching),
        Arguments.of(userUpdatedDto, userUpdated),
        Arguments.of(pollClosedDto, pollClosed),
        Arguments.of(pollDeletedDto, pollDeleted),
        Arguments.of(pollUpdatedDto, pollUpdated),
        Arguments.of(voteCastedDto, voteCasted),
        Arguments.of(voteChangedDto, voteChanged),
        Arguments.of(voteRemovedDto, voteRemoved),
        Arguments.of(answerCastedDto, answerCasted),
        Arguments.of(reminderCreatedDto, reminderCreatedEvent),
        Arguments.of(reminderUpdatedDto, reminderUpdatedEvent),
        Arguments.of(reminderDeletedDto, reminderDeletedEvent),
        Arguments.of(notificationReminderDueDto, notificationReminderDueEvent),
        Arguments.of(aiIndicatorUpdatedDto, aiIndicatorUpdated),
        Arguments.of(aiIndicatorStopDto, aiIndicatorStop),
        Arguments.of(ioIndicatorClearDto, aiIndicatorClear),
        Arguments.of(userMessagesDeletedEventDto, userMessagesDeletedEvent),
    )
}
