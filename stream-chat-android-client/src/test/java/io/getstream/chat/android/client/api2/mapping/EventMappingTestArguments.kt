/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.client.api2.model.dto.AnswerCastedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelHiddenEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelTruncatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUpdatedByUserEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUserBannedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUserUnbannedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelVisibleEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectingEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectionErrorEventDto
import io.getstream.chat.android.client.api2.model.dto.DisconnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.DraftMessageDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.DraftMessageUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ErrorEventDto
import io.getstream.chat.android.client.api2.model.dto.GlobalUserBannedEventDto
import io.getstream.chat.android.client.api2.model.dto.GlobalUserUnbannedEventDto
import io.getstream.chat.android.client.api2.model.dto.HealthEventDto
import io.getstream.chat.android.client.api2.model.dto.MarkAllReadEventDto
import io.getstream.chat.android.client.api2.model.dto.MemberAddedEventDto
import io.getstream.chat.android.client.api2.model.dto.MemberRemovedEventDto
import io.getstream.chat.android.client.api2.model.dto.MemberUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.MessageDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.MessageReadEventDto
import io.getstream.chat.android.client.api2.model.dto.MessageUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.NewMessageEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationAddedToChannelEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationChannelDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationChannelMutesUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationChannelTruncatedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationInviteAcceptedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationInviteRejectedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationInvitedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationMarkReadEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationMarkUnreadEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationMessageNewEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationMutesUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationReminderDueEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationRemovedFromChannelEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationThreadMessageNewEventDto
import io.getstream.chat.android.client.api2.model.dto.PollClosedEventDto
import io.getstream.chat.android.client.api2.model.dto.PollDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.PollUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ReactionDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.ReactionNewEventDto
import io.getstream.chat.android.client.api2.model.dto.ReactionUpdateEventDto
import io.getstream.chat.android.client.api2.model.dto.ReminderCreatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ReminderDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.ReminderUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.TypingStartEventDto
import io.getstream.chat.android.client.api2.model.dto.TypingStopEventDto
import io.getstream.chat.android.client.api2.model.dto.UnknownEventDto
import io.getstream.chat.android.client.api2.model.dto.UserDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.UserMessagesDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.UserPresenceChangedEventDto
import io.getstream.chat.android.client.api2.model.dto.UserStartWatchingEventDto
import io.getstream.chat.android.client.api2.model.dto.UserStopWatchingEventDto
import io.getstream.chat.android.client.api2.model.dto.UserUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.VoteCastedEventDto
import io.getstream.chat.android.client.api2.model.dto.VoteChangedEventDto
import io.getstream.chat.android.client.api2.model.dto.VoteRemovedEventDto
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
    private val CHANNEL_TYPE = randomString()
    private val CHANNEL_ID = randomString()
    private val CID = "$CHANNEL_TYPE:$CHANNEL_ID"
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
    private val SOFT_DELETE = randomBoolean()
    private val FIRST_UNREAD_MESSAGE_ID = randomString()
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
    private val AI_MESSAGE_ID = randomString()
    private val AI_STATE = randomString()

    // BEGIN: DTO Models

    private val newMessageDto = NewMessageEventDto(
        type = EventType.MESSAGE_NEW,
        created_at = EXACT_DATE,
        user = USER,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        message = MESSAGE,
    )

    private val draftMessageUpdatedDto = DraftMessageUpdatedEventDto(
        type = EventType.DRAFT_MESSAGE_UPDATED,
        created_at = EXACT_DATE,
        draft = DRAFT,
    )

    private val draftMessageDeletedDto = DraftMessageDeletedEventDto(
        type = EventType.DRAFT_MESSAGE_DELETED,
        created_at = EXACT_DATE,
        draft = DRAFT,
    )

    private val channelDeletedDto = ChannelDeletedEventDto(
        type = EventType.CHANNEL_DELETED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        channel = CHANNEL,
        user = USER,
    )

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

    private val channelTruncatedDto = ChannelTruncatedEventDto(
        type = EventType.CHANNEL_TRUNCATED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        message = MESSAGE,
        channel = CHANNEL,
    )

    private val channelUpdatedByUserDto = ChannelUpdatedByUserEventDto(
        type = EventType.CHANNEL_UPDATED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        message = MESSAGE,
        channel = CHANNEL,
    )

    private val channelUpdatedDto = ChannelUpdatedEventDto(
        type = EventType.CHANNEL_UPDATED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        message = MESSAGE,
        channel = CHANNEL,
    )

    private val channelUserBannedDto = ChannelUserBannedEventDto(
        type = EventType.USER_BANNED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        expiration = DATE,
        shadow = SHADOW_BAN,
    )

    private val channelUserUnbannedDto = ChannelUserUnbannedEventDto(
        type = EventType.USER_UNBANNED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
    )

    private val channelVisibleDto = ChannelVisibleEventDto(
        type = EventType.CHANNEL_VISIBLE,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        channel = CHANNEL,
        user = USER,
    )

    private val connectedDto = ConnectedEventDto(
        type = EventType.CONNECTION_CONNECTING,
        created_at = EXACT_DATE,
        me = USER,
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

    private val globalUserBannedDto = GlobalUserBannedEventDto(
        type = EventType.USER_BANNED,
        created_at = EXACT_DATE,
        user = USER,
    )

    private val globalUserUnbannedDto = GlobalUserUnbannedEventDto(
        type = EventType.USER_UNBANNED,
        created_at = EXACT_DATE,
        user = USER,
    )

    private val healthDto = HealthEventDto(
        type = EventType.HEALTH_CHECK,
        created_at = EXACT_DATE,
        connection_id = CONNECTION_ID,
    )

    private val markAllReadDto = MarkAllReadEventDto(
        type = EventType.NOTIFICATION_MARK_READ,
        created_at = EXACT_DATE,
        user = USER,
    )

    private val memberAddedDto = MemberAddedEventDto(
        type = EventType.MEMBER_ADDED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        member = MEMBER,
    )

    private val memberRemovedDto = MemberRemovedEventDto(
        type = EventType.MEMBER_REMOVED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        member = MEMBER,
    )

    private val memberUpdatedDto = MemberUpdatedEventDto(
        type = EventType.MEMBER_UPDATED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        member = MEMBER,
    )

    private val messageDeletedDto = MessageDeletedEventDto(
        type = EventType.MESSAGE_DELETED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        message = MESSAGE,
        hard_delete = HARD_DELETE,
    )

    private val messageReadDto = MessageReadEventDto(
        type = EventType.MESSAGE_READ,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        last_read_message_id = LAST_READ_MESSAGE_ID,
    )

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
        channel = CHANNEL,
        member = MEMBER,
    )

    private val notificationChannelDeletedDto = NotificationChannelDeletedEventDto(
        type = EventType.NOTIFICATION_CHANNEL_DELETED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        channel = CHANNEL,
    )

    private val notificationChannelMutesUpdatesDto = NotificationChannelMutesUpdatedEventDto(
        type = EventType.NOTIFICATION_CHANNEL_MUTES_UPDATED,
        created_at = EXACT_DATE,
        me = USER,
    )

    private val notificationChannelTruncatedDto = NotificationChannelTruncatedEventDto(
        type = EventType.NOTIFICATION_CHANNEL_TRUNCATED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        channel = CHANNEL,
    )

    private val notificationInviteAcceptedDto = NotificationInviteAcceptedEventDto(
        type = EventType.NOTIFICATION_INVITE_ACCEPTED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        member = MEMBER,
        channel = CHANNEL,
    )

    private val notificationInviteRejectedDto = NotificationInviteRejectedEventDto(
        type = EventType.NOTIFICATION_INVITE_REJECTED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        member = MEMBER,
        channel = CHANNEL,
    )

    private val notificationInvitedDto = NotificationInvitedEventDto(
        type = EventType.NOTIFICATION_INVITED,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        member = MEMBER,
    )

    private val notificationMarkReadDto = NotificationMarkReadEventDto(
        type = EventType.NOTIFICATION_MARK_READ,
        created_at = EXACT_DATE,
        user = USER,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        last_read_message_id = LAST_READ_MESSAGE_ID,
    )

    private val notificationMarkUnreadDto = NotificationMarkUnreadEventDto(
        type = EventType.NOTIFICATION_MARK_UNREAD,
        created_at = EXACT_DATE,
        user = USER,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        first_unread_message_id = FIRST_UNREAD_MESSAGE_ID,
        last_read_message_id = LAST_READ_MESSAGE_ID,
        last_read_at = EXACT_DATE,
        unread_messages = UNREAD_MESSAGES,
        total_unread_count = TOTAL_UNREAD_COUNT,
        unread_channels = UNREAD_CHANNELS,
    )

    private val notificationMessageNewDto = NotificationMessageNewEventDto(
        type = EventType.NOTIFICATION_MESSAGE_NEW,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        message = MESSAGE,
        channel = CHANNEL,
    )

    private val notificationThreadMessageNewDto = NotificationThreadMessageNewEventDto(
        type = EventType.NOTIFICATION_THREAD_MESSAGE_NEW,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        message = MESSAGE,
        channel = CHANNEL,
        unread_threads = UNREAD_THREADS,
        unread_thread_messages = UNREAD_THREAD_MESSAGES,
    )

    private val notificationMutesUpdatedDto = NotificationMutesUpdatedEventDto(
        type = EventType.NOTIFICATION_MUTES_UPDATED,
        created_at = EXACT_DATE,
        me = USER,
    )

    private val notificationRemovedFromChannelDto = NotificationRemovedFromChannelEventDto(
        type = EventType.NOTIFICATION_REMOVED_FROM_CHANNEL,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        channel = CHANNEL,
        member = MEMBER,
        user = USER,
    )

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

    private val typingStartDto = TypingStartEventDto(
        type = EventType.TYPING_START,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        parent_id = randomString(),
    )

    private val typingStopDto = TypingStopEventDto(
        type = EventType.TYPING_STOP,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        parent_id = randomString(),
    )

    private val unknownDto = UnknownEventDto(
        type = EventType.UNKNOWN,
        created_at = EXACT_DATE,
        user = USER,
        rawData = emptyMap<String, String>(),
    )

    private val userDeletedDto = UserDeletedEventDto(
        type = EventType.USER_DELETED,
        created_at = EXACT_DATE,
        user = USER,
    )

    private val userPresenceChangedDto = UserPresenceChangedEventDto(
        type = EventType.USER_PRESENCE_CHANGED,
        created_at = EXACT_DATE,
        user = USER,
    )

    private val userStartWatchingDto = UserStartWatchingEventDto(
        type = EventType.USER_WATCHING_START,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        watcher_count = WATCHER_COUNT,
    )

    private val userStopWatchingDto = UserStopWatchingEventDto(
        type = EventType.USER_WATCHING_STOP,
        created_at = EXACT_DATE,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        user = USER,
        watcher_count = WATCHER_COUNT,
    )

    private val userUpdatedDto = UserUpdatedEventDto(
        type = EventType.USER_UPDATED,
        created_at = EXACT_DATE,
        user = USER,
    )

    private val pollClosedDto = PollClosedEventDto(
        type = EventType.POLL_CLOSED,
        created_at = EXACT_DATE,
        cid = CID,
        poll = POLL,
    )

    private val pollDeletedDto = PollDeletedEventDto(
        type = EventType.POLL_DELETED,
        created_at = EXACT_DATE,
        cid = CID,
        poll = POLL,
    )

    private val pollUpdatedDto = PollUpdatedEventDto(
        type = EventType.POLL_UPDATED,
        created_at = EXACT_DATE,
        cid = CID,
        poll = POLL,
    )

    private val voteCastedDto = VoteCastedEventDto(
        type = EventType.POLL_VOTE_CASTED,
        created_at = EXACT_DATE,
        cid = CID,
        poll = POLL,
        poll_vote = POLL_VOTE,
    )

    private val voteChangedDto = VoteChangedEventDto(
        type = EventType.POLL_VOTE_CHANGED,
        created_at = EXACT_DATE,
        cid = CID,
        poll = POLL,
        poll_vote = POLL_VOTE,
    )

    private val voteRemovedDto = VoteRemovedEventDto(
        type = EventType.POLL_VOTE_REMOVED,
        created_at = EXACT_DATE,
        cid = CID,
        poll = POLL,
        poll_vote = POLL_VOTE,
    )

    private val answerCastedDto = AnswerCastedEventDto(
        type = EventType.POLL_VOTE_CASTED,
        created_at = EXACT_DATE,
        cid = CID,
        poll = POLL,
        poll_vote = POLL_VOTE,
    )

    private val reminderCreatedDto = ReminderCreatedEventDto(
        type = EventType.REMINDER_CREATED,
        created_at = EXACT_DATE,
        cid = CID,
        message_id = MESSAGE.id,
        user_id = USER.id,
        reminder = REMINDER,
    )

    private val reminderUpdatedDto = ReminderUpdatedEventDto(
        type = EventType.REMINDER_UPDATED,
        created_at = EXACT_DATE,
        cid = CID,
        message_id = MESSAGE.id,
        user_id = USER.id,
        reminder = REMINDER,
    )

    private val reminderDeletedDto = ReminderDeletedEventDto(
        type = EventType.REMINDER_DELETED,
        created_at = EXACT_DATE,
        cid = CID,
        message_id = MESSAGE.id,
        user_id = USER.id,
        reminder = REMINDER,
    )

    private val notificationReminderDueDto = NotificationReminderDueEventDto(
        type = EventType.NOTIFICATION_REMINDER_DUE,
        created_at = EXACT_DATE,
        cid = CID,
        message_id = MESSAGE.id,
        user_id = USER.id,
        reminder = REMINDER,
    )

    private val userMessagesDeletedEventDto = UserMessagesDeletedEventDto(
        type = EventType.USER_MESSAGES_DELETED,
        created_at = EXACT_DATE,
        user = USER,
        cid = CID,
        channel_type = CHANNEL_TYPE,
        channel_id = CHANNEL_ID,
        hard_delete = HARD_DELETE,
    )

    private val aiIndicatorUpdatedDto = AIIndicatorUpdatedEventDto(
        type = EventType.AI_TYPING_INDICATOR_UPDATED,
        created_at = EXACT_DATE,
        cid = CID,
        user = USER,
        message_id = AI_MESSAGE_ID,
        ai_state = AI_STATE,
    )

    private val aiIndicatorStopDto = AIIndicatorStopEventDto(
        type = EventType.AI_TYPING_INDICATOR_STOP,
        created_at = EXACT_DATE,
        cid = CID,
        user = USER,
    )

    private val ioIndicatorClearDto = AIIndicatorClearEventDto(
        type = EventType.AI_TYPING_INDICATOR_CLEAR,
        created_at = EXACT_DATE,
        cid = CID,
        user = USER,
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
        message = with(domainMapping) { newMessageDto.message.toDomain() },
        watcherCount = newMessageDto.watcher_count,
        totalUnreadCount = newMessageDto.total_unread_count,
        unreadChannels = newMessageDto.unread_channels,
        channelMessageCount = newMessageDto.channel_message_count,
    )

    private val draftMessageUpdatedEvent = DraftMessageUpdatedEvent(
        type = draftMessageUpdatedDto.type,
        createdAt = draftMessageUpdatedDto.created_at.date,
        rawCreatedAt = draftMessageUpdatedDto.created_at.rawDate,
        draftMessage = with(domainMapping) { draftMessageUpdatedDto.draft.toDomain() },
    )

    private val draftMessageDeletedEvent = DraftMessageDeletedEvent(
        type = draftMessageDeletedDto.type,
        createdAt = draftMessageDeletedDto.created_at.date,
        rawCreatedAt = draftMessageDeletedDto.created_at.rawDate,
        draftMessage = with(domainMapping) { draftMessageDeletedDto.draft.toDomain() },
    )

    private val channelDeleted = ChannelDeletedEvent(
        type = channelDeletedDto.type,
        createdAt = channelDeletedDto.created_at.date,
        rawCreatedAt = channelDeletedDto.created_at.rawDate,
        user = with(domainMapping) { channelDeletedDto.user?.toDomain() },
        cid = channelDeletedDto.cid,
        channelType = channelDeletedDto.channel_type,
        channelId = channelDeletedDto.channel_id,
        channel = with(domainMapping) { channelDeletedDto.channel.toDomain() },
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
        type = channelTruncatedDto.type,
        createdAt = channelTruncatedDto.created_at.date,
        rawCreatedAt = channelTruncatedDto.created_at.rawDate,
        user = with(domainMapping) { channelTruncatedDto.user?.toDomain() },
        cid = channelTruncatedDto.cid,
        channelType = channelTruncatedDto.channel_type,
        channelId = channelTruncatedDto.channel_id,
        message = with(domainMapping) { channelTruncatedDto.message?.toDomain() },
        channel = with(domainMapping) {
            channelTruncatedDto.channel.toDomain()
        },
    )

    private val channelUpdatedByUser = ChannelUpdatedByUserEvent(
        type = channelUpdatedByUserDto.type,
        createdAt = channelUpdatedByUserDto.created_at.date,
        rawCreatedAt = channelUpdatedByUserDto.created_at.rawDate,
        user = with(domainMapping) { channelUpdatedByUserDto.user.toDomain() },
        cid = channelUpdatedByUserDto.cid,
        channelType = channelUpdatedByUserDto.channel_type,
        channelId = channelUpdatedByUserDto.channel_id,
        message = with(domainMapping) { channelUpdatedByUserDto.message?.toDomain() },
        channel = with(domainMapping) {
            channelUpdatedByUserDto.channel.toDomain()
        },
    )

    private val channelUpdated = ChannelUpdatedEvent(
        type = channelUpdatedDto.type,
        createdAt = channelUpdatedDto.created_at.date,
        rawCreatedAt = channelUpdatedDto.created_at.rawDate,
        cid = channelUpdatedDto.cid,
        channelType = channelUpdatedDto.channel_type,
        channelId = channelUpdatedDto.channel_id,
        message = with(domainMapping) { channelUpdatedDto.message?.toDomain() },
        channel = with(domainMapping) {
            channelUpdatedDto.channel.toDomain()
        },
    )

    private val channelUserBanned = ChannelUserBannedEvent(
        type = channelUserBannedDto.type,
        createdAt = channelUserBannedDto.created_at.date,
        rawCreatedAt = channelUserBannedDto.created_at.rawDate,
        user = with(domainMapping) { channelUserBannedDto.user.toDomain() },
        cid = channelUserBannedDto.cid,
        channelType = channelUserBannedDto.channel_type,
        channelId = channelUserBannedDto.channel_id,
        expiration = channelUserBannedDto.expiration,
        shadow = channelUserBannedDto.shadow ?: false,
    )

    private val channelUserUnbanned = ChannelUserUnbannedEvent(
        type = channelUserUnbannedDto.type,
        createdAt = channelUserUnbannedDto.created_at.date,
        rawCreatedAt = channelUserUnbannedDto.created_at.rawDate,
        user = with(domainMapping) { channelUserUnbannedDto.user.toDomain() },
        cid = channelUserUnbannedDto.cid,
        channelType = channelUserUnbannedDto.channel_type,
        channelId = channelUserUnbannedDto.channel_id,
    )

    private val channelVisible = ChannelVisibleEvent(
        type = channelVisibleDto.type,
        createdAt = channelVisibleDto.created_at.date,
        rawCreatedAt = channelVisibleDto.created_at.rawDate,
        user = with(domainMapping) { channelVisibleDto.user.toDomain() },
        cid = channelVisibleDto.cid,
        channelType = channelVisibleDto.channel_type,
        channel = with(domainMapping) { channelVisibleDto.channel.toDomain() },
        channelId = channelVisibleDto.channel_id,
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
        type = globalUserBannedDto.type,
        createdAt = globalUserBannedDto.created_at.date,
        rawCreatedAt = globalUserBannedDto.created_at.rawDate,
        user = with(domainMapping) { globalUserBannedDto.user.toDomain() },
    )

    private val globalUserUnbanned = GlobalUserUnbannedEvent(
        type = globalUserUnbannedDto.type,
        createdAt = globalUserUnbannedDto.created_at.date,
        rawCreatedAt = globalUserUnbannedDto.created_at.rawDate,
        user = with(domainMapping) { globalUserUnbannedDto.user.toDomain() },
    )

    private val health = HealthEvent(
        type = healthDto.type,
        createdAt = healthDto.created_at.date,
        rawCreatedAt = healthDto.created_at.rawDate,
        connectionId = healthDto.connection_id,
    )

    private val markAllRead = MarkAllReadEvent(
        type = markAllReadDto.type,
        createdAt = markAllReadDto.created_at.date,
        rawCreatedAt = markAllReadDto.created_at.rawDate,
        user = with(domainMapping) { markAllReadDto.user.toDomain() },
    )

    private val memberAdded = MemberAddedEvent(
        type = memberAddedDto.type,
        createdAt = memberAddedDto.created_at.date,
        rawCreatedAt = memberAddedDto.created_at.rawDate,
        cid = memberAddedDto.cid,
        channelType = memberAddedDto.channel_type,
        channelId = memberAddedDto.channel_id,
        user = with(domainMapping) { memberAddedDto.user.toDomain() },
        member = with(domainMapping) { memberAddedDto.member.toDomain() },
    )

    private val memberRemoved = MemberRemovedEvent(
        type = memberRemovedDto.type,
        createdAt = memberRemovedDto.created_at.date,
        rawCreatedAt = memberRemovedDto.created_at.rawDate,
        cid = memberRemovedDto.cid,
        channelType = memberRemovedDto.channel_type,
        channelId = memberRemovedDto.channel_id,
        user = with(domainMapping) { memberRemovedDto.user.toDomain() },
        member = with(domainMapping) { memberRemovedDto.member.toDomain() },
    )

    private val memberUpdated = MemberUpdatedEvent(
        type = memberUpdatedDto.type,
        createdAt = memberUpdatedDto.created_at.date,
        rawCreatedAt = memberUpdatedDto.created_at.rawDate,
        cid = memberUpdatedDto.cid,
        channelType = memberUpdatedDto.channel_type,
        channelId = memberUpdatedDto.channel_id,
        user = with(domainMapping) { memberUpdatedDto.user.toDomain() },
        member = with(domainMapping) { memberUpdatedDto.member.toDomain() },
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
    )

    private val messageRead = MessageReadEvent(
        type = messageReadDto.type,
        createdAt = messageReadDto.created_at.date,
        rawCreatedAt = messageReadDto.created_at.rawDate,
        cid = messageReadDto.cid,
        channelType = messageReadDto.channel_type,
        channelId = messageReadDto.channel_id,
        user = with(domainMapping) { messageReadDto.user.toDomain() },
        lastReadMessageId = messageReadDto.last_read_message_id,
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
        type = notificationChannelDeletedDto.type,
        createdAt = notificationChannelDeletedDto.created_at.date,
        rawCreatedAt = notificationChannelDeletedDto.created_at.rawDate,
        cid = notificationChannelDeletedDto.cid,
        channelType = notificationChannelDeletedDto.channel_type,
        channelId = notificationChannelDeletedDto.channel_id,
        channel = with(domainMapping) {
            notificationChannelDeletedDto.channel.toDomain()
        },
    )

    private val notificationChannelMutesUpdates = NotificationChannelMutesUpdatedEvent(
        type = notificationChannelMutesUpdatesDto.type,
        createdAt = notificationChannelMutesUpdatesDto.created_at.date,
        rawCreatedAt = notificationChannelMutesUpdatesDto.created_at.rawDate,
        me = with(domainMapping) { notificationChannelMutesUpdatesDto.me.toDomain() },
    )

    private val notificationChannelTruncated = NotificationChannelTruncatedEvent(
        type = notificationChannelTruncatedDto.type,
        createdAt = notificationChannelTruncatedDto.created_at.date,
        rawCreatedAt = notificationChannelTruncatedDto.created_at.rawDate,
        cid = notificationChannelTruncatedDto.cid,
        channelType = notificationChannelTruncatedDto.channel_type,
        channelId = notificationChannelTruncatedDto.channel_id,
        channel = with(domainMapping) {
            notificationChannelTruncatedDto.channel.toDomain()
        },
    )

    private val notificationInviteAccepted = NotificationInviteAcceptedEvent(
        type = notificationInviteAcceptedDto.type,
        createdAt = notificationInviteAcceptedDto.created_at.date,
        rawCreatedAt = notificationInviteAcceptedDto.created_at.rawDate,
        cid = notificationInviteAcceptedDto.cid,
        channelType = notificationInviteAcceptedDto.channel_type,
        channelId = notificationInviteAcceptedDto.channel_id,
        user = with(domainMapping) { notificationInviteAcceptedDto.user.toDomain() },
        member = with(domainMapping) { notificationInviteAcceptedDto.member.toDomain() },
        channel = with(domainMapping) {
            notificationInviteAcceptedDto.channel.toDomain()
        },
    )

    private val notificationInviteRejected = NotificationInviteRejectedEvent(
        type = notificationInviteRejectedDto.type,
        createdAt = notificationInviteRejectedDto.created_at.date,
        rawCreatedAt = notificationInviteRejectedDto.created_at.rawDate,
        cid = notificationInviteRejectedDto.cid,
        channelType = notificationInviteRejectedDto.channel_type,
        channelId = notificationInviteRejectedDto.channel_id,
        user = with(domainMapping) { notificationInviteRejectedDto.user.toDomain() },
        member = with(domainMapping) { notificationInviteRejectedDto.member.toDomain() },
        channel = with(domainMapping) {
            notificationInviteRejectedDto.channel.toDomain()
        },
    )

    private val notificationInvited = NotificationInvitedEvent(
        type = notificationInvitedDto.type,
        createdAt = notificationInvitedDto.created_at.date,
        rawCreatedAt = notificationInvitedDto.created_at.rawDate,
        cid = notificationInvitedDto.cid,
        channelType = notificationInvitedDto.channel_type,
        channelId = notificationInvitedDto.channel_id,
        user = with(domainMapping) { notificationInvitedDto.user.toDomain() },
        member = with(domainMapping) { notificationInvitedDto.member.toDomain() },
    )

    private val notificationMarkRead = NotificationMarkReadEvent(
        type = notificationMarkReadDto.type,
        createdAt = notificationMarkReadDto.created_at.date,
        rawCreatedAt = notificationMarkReadDto.created_at.rawDate,
        user = with(domainMapping) { notificationMarkReadDto.user.toDomain() },
        cid = notificationMarkReadDto.cid,
        channelType = notificationMarkReadDto.channel_type,
        channelId = notificationMarkReadDto.channel_id,
        lastReadMessageId = notificationMarkReadDto.last_read_message_id,
    )

    private val notificationMarkUnread = NotificationMarkUnreadEvent(
        type = notificationMarkUnreadDto.type,
        createdAt = notificationMarkUnreadDto.created_at.date,
        rawCreatedAt = notificationMarkUnreadDto.created_at.rawDate,
        user = with(domainMapping) { notificationMarkUnreadDto.user.toDomain() },
        cid = notificationMarkUnreadDto.cid,
        channelType = notificationMarkUnreadDto.channel_type,
        channelId = notificationMarkUnreadDto.channel_id,
        firstUnreadMessageId = notificationMarkUnreadDto.first_unread_message_id,
        lastReadMessageId = notificationMarkUnreadDto.last_read_message_id,
        lastReadMessageAt = notificationMarkUnreadDto.last_read_at.date,
        unreadMessages = notificationMarkUnreadDto.unread_messages,
        totalUnreadCount = notificationMarkUnreadDto.total_unread_count,
        unreadChannels = notificationMarkUnreadDto.unread_channels,
    )

    private val notificationMessageNew = NotificationMessageNewEvent(
        type = notificationMessageNewDto.type,
        createdAt = notificationMessageNewDto.created_at.date,
        rawCreatedAt = notificationMessageNewDto.created_at.rawDate,
        cid = notificationMessageNewDto.cid,
        channelType = notificationMessageNewDto.channel_type,
        channelId = notificationMessageNewDto.channel_id,
        message = with(domainMapping) { notificationMessageNewDto.message.toDomain() },
        channel = with(domainMapping) {
            notificationMessageNewDto.channel.toDomain()
        },
    )

    private val notificationThreadMessageNew = NotificationThreadMessageNewEvent(
        type = notificationThreadMessageNewDto.type,
        createdAt = notificationThreadMessageNewDto.created_at.date,
        rawCreatedAt = notificationThreadMessageNewDto.created_at.rawDate,
        cid = notificationThreadMessageNewDto.cid,
        channelType = notificationThreadMessageNewDto.channel_type,
        channelId = notificationThreadMessageNewDto.channel_id,
        message = with(domainMapping) { notificationThreadMessageNewDto.message.toDomain() },
        channel = with(domainMapping) {
            notificationThreadMessageNewDto.channel.toDomain()
        },
        unreadThreads = notificationThreadMessageNewDto.unread_threads,
        unreadThreadMessages = notificationThreadMessageNewDto.unread_thread_messages,
    )

    private val notificationMutesUpdated = NotificationMutesUpdatedEvent(
        type = notificationMutesUpdatedDto.type,
        createdAt = notificationMutesUpdatedDto.created_at.date,
        rawCreatedAt = notificationMutesUpdatedDto.created_at.rawDate,
        me = with(domainMapping) { notificationMutesUpdatedDto.me.toDomain() },
    )

    private val notificationRemovedFromChannel = NotificationRemovedFromChannelEvent(
        type = notificationRemovedFromChannelDto.type,
        createdAt = notificationRemovedFromChannelDto.created_at.date,
        rawCreatedAt = notificationRemovedFromChannelDto.created_at.rawDate,
        cid = notificationRemovedFromChannelDto.cid,
        channelType = notificationRemovedFromChannelDto.channel_type,
        channelId = notificationRemovedFromChannelDto.channel_id,
        channel = with(domainMapping) {
            notificationRemovedFromChannelDto.channel.toDomain()
        },
        member = with(domainMapping) { notificationRemovedFromChannelDto.member.toDomain() },
        user = with(domainMapping) { notificationRemovedFromChannelDto.user?.toDomain() },
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
        type = typingStartDto.type,
        createdAt = typingStartDto.created_at.date,
        rawCreatedAt = typingStartDto.created_at.rawDate,
        cid = typingStartDto.cid,
        channelType = typingStartDto.channel_type,
        channelId = typingStartDto.channel_id,
        user = with(domainMapping) { typingStartDto.user.toDomain() },
        parentId = typingStartDto.parent_id,
    )

    private val typingStop = TypingStopEvent(
        type = typingStopDto.type,
        createdAt = typingStopDto.created_at.date,
        rawCreatedAt = typingStopDto.created_at.rawDate,
        cid = typingStopDto.cid,
        channelType = typingStopDto.channel_type,
        channelId = typingStopDto.channel_id,
        user = with(domainMapping) { typingStopDto.user.toDomain() },
        parentId = typingStopDto.parent_id,
    )

    private val unknown = UnknownEvent(
        type = unknownDto.type,
        createdAt = unknownDto.created_at.date,
        rawCreatedAt = unknownDto.created_at.rawDate,
        user = with(domainMapping) { unknownDto.user?.toDomain() },
        rawData = unknownDto.rawData,
    )

    private val userDeleted = UserDeletedEvent(
        type = userDeletedDto.type,
        createdAt = userDeletedDto.created_at.date,
        rawCreatedAt = userDeletedDto.created_at.rawDate,
        user = with(domainMapping) { userDeletedDto.user.toDomain() },
    )

    private val userPresenceChanged = UserPresenceChangedEvent(
        type = userPresenceChangedDto.type,
        createdAt = userPresenceChangedDto.created_at.date,
        rawCreatedAt = userPresenceChangedDto.created_at.rawDate,
        user = with(domainMapping) { userPresenceChangedDto.user.toDomain() },
    )

    private val userStartWatching = UserStartWatchingEvent(
        type = userStartWatchingDto.type,
        createdAt = userStartWatchingDto.created_at.date,
        rawCreatedAt = userStartWatchingDto.created_at.rawDate,
        cid = userStartWatchingDto.cid,
        channelType = userStartWatchingDto.channel_type,
        channelId = userStartWatchingDto.channel_id,
        user = with(domainMapping) { userStartWatchingDto.user.toDomain() },
        watcherCount = userStartWatchingDto.watcher_count,
    )

    private val userStopWatching = UserStopWatchingEvent(
        type = userStopWatchingDto.type,
        createdAt = userStopWatchingDto.created_at.date,
        rawCreatedAt = userStopWatchingDto.created_at.rawDate,
        cid = userStopWatchingDto.cid,
        channelType = userStopWatchingDto.channel_type,
        channelId = userStopWatchingDto.channel_id,
        user = with(domainMapping) { userStopWatchingDto.user.toDomain() },
        watcherCount = userStopWatchingDto.watcher_count,
    )

    private val userUpdated = UserUpdatedEvent(
        type = userUpdatedDto.type,
        createdAt = userUpdatedDto.created_at.date,
        rawCreatedAt = userUpdatedDto.created_at.rawDate,
        user = with(domainMapping) { userUpdatedDto.user.toDomain() },
    )

    private val pollClosed = PollClosedEvent(
        type = pollClosedDto.type,
        createdAt = pollClosedDto.created_at.date,
        rawCreatedAt = pollClosedDto.created_at.rawDate,
        cid = pollClosedDto.cid,
        channelType = pollClosedDto.cid.split(":").first(),
        channelId = pollClosedDto.cid.split(":").last(),
        poll = with(domainMapping) { pollClosedDto.poll.toDomain() },
    )

    private val pollDeleted = PollDeletedEvent(
        type = pollDeletedDto.type,
        createdAt = pollDeletedDto.created_at.date,
        rawCreatedAt = pollDeletedDto.created_at.rawDate,
        cid = pollDeletedDto.cid,
        channelType = pollDeletedDto.cid.split(":").first(),
        channelId = pollDeletedDto.cid.split(":").last(),
        poll = with(domainMapping) { pollDeletedDto.poll.toDomain() },
    )

    private val pollUpdated = PollUpdatedEvent(
        type = pollUpdatedDto.type,
        createdAt = pollUpdatedDto.created_at.date,
        rawCreatedAt = pollUpdatedDto.created_at.rawDate,
        cid = pollUpdatedDto.cid,
        channelType = pollUpdatedDto.cid.split(":").first(),
        channelId = pollUpdatedDto.cid.split(":").last(),
        poll = with(domainMapping) { pollUpdatedDto.poll.toDomain() },
    )

    private val voteCasted = VoteCastedEvent(
        type = voteCastedDto.type,
        createdAt = voteCastedDto.created_at.date,
        rawCreatedAt = voteCastedDto.created_at.rawDate,
        cid = voteCastedDto.cid,
        channelType = voteCastedDto.cid.split(":").first(),
        channelId = voteCastedDto.cid.split(":").last(),
        poll = with(domainMapping) { voteCastedDto.poll.toDomain() },
        newVote = with(domainMapping) { voteCastedDto.poll_vote.toDomain() },
    )

    private val voteChanged = VoteChangedEvent(
        type = voteChangedDto.type,
        createdAt = voteChangedDto.created_at.date,
        rawCreatedAt = voteChangedDto.created_at.rawDate,
        cid = voteChangedDto.cid,
        channelType = voteChangedDto.cid.split(":").first(),
        channelId = voteChangedDto.cid.split(":").last(),
        poll = with(domainMapping) { voteChangedDto.poll.toDomain() },
        newVote = with(domainMapping) { voteChangedDto.poll_vote.toDomain() },
    )

    private val voteRemoved = VoteRemovedEvent(
        type = voteRemovedDto.type,
        createdAt = voteRemovedDto.created_at.date,
        rawCreatedAt = voteRemovedDto.created_at.rawDate,
        cid = voteRemovedDto.cid,
        channelType = voteRemovedDto.cid.split(":").first(),
        channelId = voteRemovedDto.cid.split(":").last(),
        poll = with(domainMapping) { voteRemovedDto.poll.toDomain() },
        removedVote = with(domainMapping) { voteRemovedDto.poll_vote.toDomain() },
    )

    private val answerCasted = AnswerCastedEvent(
        type = answerCastedDto.type,
        createdAt = answerCastedDto.created_at.date,
        rawCreatedAt = answerCastedDto.created_at.rawDate,
        cid = answerCastedDto.cid,
        channelType = answerCastedDto.cid.split(":").first(),
        channelId = answerCastedDto.cid.split(":").last(),
        poll = with(domainMapping) { answerCastedDto.poll.toDomain() },
        newAnswer = with(domainMapping) { answerCastedDto.poll_vote.toAnswerDomain() },
    )

    private val reminderCreatedEvent = ReminderCreatedEvent(
        type = reminderCreatedDto.type,
        createdAt = reminderCreatedDto.created_at.date,
        rawCreatedAt = reminderCreatedDto.created_at.rawDate,
        cid = reminderCreatedDto.cid,
        channelType = reminderCreatedDto.cid.split(":").first(),
        channelId = reminderCreatedDto.cid.split(":").last(),
        messageId = reminderCreatedDto.message_id,
        userId = reminderCreatedDto.user_id,
        reminder = with(domainMapping) { reminderCreatedDto.reminder.toDomain() },
    )

    private val reminderDeletedEvent = ReminderDeletedEvent(
        type = reminderDeletedDto.type,
        createdAt = reminderDeletedDto.created_at.date,
        rawCreatedAt = reminderDeletedDto.created_at.rawDate,
        cid = reminderCreatedDto.cid,
        channelType = reminderCreatedDto.cid.split(":").first(),
        channelId = reminderCreatedDto.cid.split(":").last(),
        messageId = reminderCreatedDto.message_id,
        userId = reminderCreatedDto.user_id,
        reminder = with(domainMapping) { reminderDeletedDto.reminder.toDomain() },
    )

    private val reminderUpdatedEvent = ReminderUpdatedEvent(
        type = reminderUpdatedDto.type,
        createdAt = reminderUpdatedDto.created_at.date,
        rawCreatedAt = reminderUpdatedDto.created_at.rawDate,
        cid = reminderCreatedDto.cid,
        channelType = reminderCreatedDto.cid.split(":").first(),
        channelId = reminderCreatedDto.cid.split(":").last(),
        messageId = reminderCreatedDto.message_id,
        userId = reminderCreatedDto.user_id,
        reminder = with(domainMapping) { reminderUpdatedDto.reminder.toDomain() },
    )

    private val notificationReminderDueEvent = NotificationReminderDueEvent(
        type = notificationReminderDueDto.type,
        createdAt = notificationReminderDueDto.created_at.date,
        rawCreatedAt = notificationReminderDueDto.created_at.rawDate,
        cid = reminderCreatedDto.cid,
        channelType = reminderCreatedDto.cid.split(":").first(),
        channelId = reminderCreatedDto.cid.split(":").last(),
        messageId = reminderCreatedDto.message_id,
        userId = reminderCreatedDto.user_id,
        reminder = with(domainMapping) { notificationReminderDueDto.reminder.toDomain() },
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
        type = userMessagesDeletedEventDto.type,
        createdAt = userMessagesDeletedEventDto.created_at.date,
        rawCreatedAt = userMessagesDeletedEventDto.created_at.rawDate,
        user = with(domainMapping) { userMessagesDeletedEventDto.user.toDomain() },
        cid = userMessagesDeletedEventDto.cid,
        channelType = userMessagesDeletedEventDto.channel_type,
        channelId = userMessagesDeletedEventDto.channel_id,
        hardDelete = userMessagesDeletedEventDto.hard_delete == true,
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
