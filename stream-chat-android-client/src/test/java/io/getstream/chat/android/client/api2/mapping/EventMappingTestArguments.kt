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
import io.getstream.chat.android.client.api2.model.dto.ChannelUserBannedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUserUnbannedEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectingEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectionErrorEventDto
import io.getstream.chat.android.client.api2.model.dto.DisconnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.ErrorEventDto
import io.getstream.chat.android.client.api2.model.dto.GlobalUserBannedEventDto
import io.getstream.chat.android.client.api2.model.dto.GlobalUserUnbannedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationAddedToChannelEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationChannelDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationChannelTruncatedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationInviteAcceptedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationInviteRejectedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationInvitedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationMessageNewEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationReminderDueEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationRemovedFromChannelEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationThreadMessageNewEventDto
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

    private val unknownDto = UnknownEventDto(
        type = EventType.UNKNOWN,
        created_at = EXACT_DATE,
        user = USER,
        rawData = emptyMap<String, String>(),
    )

    private val notificationReminderDueDto = NotificationReminderDueEventDto(
        type = EventType.NOTIFICATION_REMINDER_DUE,
        created_at = EXACT_DATE,
        cid = CID,
        message_id = MESSAGE.id,
        user_id = USER.id,
        reminder = REMINDER,
    )

    // END: DTO Models

    // BEGIN: Domain models


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


    private val notificationMessageNew = NotificationMessageNewEvent(
        type = notificationMessageNewDto.type,
        createdAt = notificationMessageNewDto.created_at.date,
        rawCreatedAt = notificationMessageNewDto.created_at.rawDate,
        cid = notificationMessageNewDto.cid,
        channelType = notificationMessageNewDto.channel_type,
        channelId = notificationMessageNewDto.channel_id,
        message = with(domainMapping) {
            notificationMessageNewDto.message.toDomain(notificationMessageNewDto.channel.toChannelInfo())
        },
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
        message = with(domainMapping) {
            notificationThreadMessageNewDto.message.toDomain(notificationThreadMessageNewDto.channel.toChannelInfo())
        },
        channel = with(domainMapping) {
            notificationThreadMessageNewDto.channel.toDomain()
        },
        unreadThreads = notificationThreadMessageNewDto.unread_threads,
        unreadThreadMessages = notificationThreadMessageNewDto.unread_thread_messages,
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

    private val unknown = UnknownEvent(
        type = unknownDto.type,
        createdAt = unknownDto.created_at.date,
        rawCreatedAt = unknownDto.created_at.rawDate,
        user = with(domainMapping) { unknownDto.user?.toDomain() },
        rawData = unknownDto.rawData,
    )

    private val notificationReminderDueEvent = NotificationReminderDueEvent(
        type = notificationReminderDueDto.type,
        createdAt = notificationReminderDueDto.created_at.date,
        rawCreatedAt = notificationReminderDueDto.created_at.rawDate,
        cid = CID,
        channelType = CID.split(":").first(),
        channelId = CID.split(":").last(),
        messageId = MESSAGE.id,
        userId = USER.id,
        reminder = with(domainMapping) { notificationReminderDueDto.reminder.toDomain() },
    )

    // END: Domain models

    /**
     * Provides the test arguments for the [EventMappingTest].
     */
    @JvmStatic
    @Suppress("LongMethod")
    fun arguments() = listOf(
        Arguments.of(channelUserBannedDto, channelUserBanned),
        Arguments.of(channelUserUnbannedDto, channelUserUnbanned),
        Arguments.of(connectedDto, connected),
        Arguments.of(connectionErrorDto, connectionError),
        Arguments.of(connectingDto, connecting),
        Arguments.of(disconnectedDto, disconnected),
        Arguments.of(errorDto, error),
        Arguments.of(globalUserBannedDto, globalUserBanned),
        Arguments.of(globalUserUnbannedDto, globalUserUnbanned),
        Arguments.of(notificationAddedToChannelDto, notificationAddedToChannel),
        Arguments.of(notificationChannelDeletedDto, notificationChannelDeleted),
        Arguments.of(notificationChannelTruncatedDto, notificationChannelTruncated),
        Arguments.of(notificationInviteAcceptedDto, notificationInviteAccepted),
        Arguments.of(notificationInviteRejectedDto, notificationInviteRejected),
        Arguments.of(notificationInvitedDto, notificationInvited),
        Arguments.of(notificationMessageNewDto, notificationMessageNew),
        Arguments.of(notificationThreadMessageNewDto, notificationThreadMessageNew),
        Arguments.of(notificationRemovedFromChannelDto, notificationRemovedFromChannel),
        Arguments.of(unknownDto, unknown),
        Arguments.of(notificationReminderDueDto, notificationReminderDueEvent),
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
        Arguments.of(draftUpdatedGenerated, DATE_STRING, draftUpdatedExpected),
        Arguments.of(draftDeletedGenerated, DATE_STRING, draftDeletedExpected),
        Arguments.of(userUpdatedGenerated, DATE_STRING, userUpdatedExpected),
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
    )
}
