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

@file:Suppress("TooManyFunctions")

package io.getstream.chat.android.client.api2.mapping

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
import io.getstream.chat.android.client.api2.model.dto.ChatEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectingEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectionErrorEventDto
import io.getstream.chat.android.client.api2.model.dto.DisconnectedEventDto
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
import io.getstream.chat.android.client.api2.model.dto.NotificationRemovedFromChannelEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationThreadMessageNewEventDto
import io.getstream.chat.android.client.api2.model.dto.PollClosedEventDto
import io.getstream.chat.android.client.api2.model.dto.PollDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.PollUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ReactionDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.ReactionNewEventDto
import io.getstream.chat.android.client.api2.model.dto.ReactionUpdateEventDto
import io.getstream.chat.android.client.api2.model.dto.TypingStartEventDto
import io.getstream.chat.android.client.api2.model.dto.TypingStopEventDto
import io.getstream.chat.android.client.api2.model.dto.UnknownEventDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamConnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.UserDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.UserPresenceChangedEventDto
import io.getstream.chat.android.client.api2.model.dto.UserStartWatchingEventDto
import io.getstream.chat.android.client.api2.model.dto.UserStopWatchingEventDto
import io.getstream.chat.android.client.api2.model.dto.UserUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.VoteCastedEventDto
import io.getstream.chat.android.client.api2.model.dto.VoteChangedEventDto
import io.getstream.chat.android.client.api2.model.dto.VoteRemovedEventDto
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
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.ConnectionErrorEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
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
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.NotificationThreadMessageNewEvent
import io.getstream.chat.android.client.events.PollClosedEvent
import io.getstream.chat.android.client.events.PollDeletedEvent
import io.getstream.chat.android.client.events.PollUpdatedEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.events.UserDeletedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.events.VoteCastedEvent
import io.getstream.chat.android.client.events.VoteChangedEvent
import io.getstream.chat.android.client.events.VoteRemovedEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.models.ChannelTransformer
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.UserId

internal fun ConnectedEvent.toDto(): UpstreamConnectedEventDto {
    return UpstreamConnectedEventDto(
        type = this.type,
        created_at = createdAt,
        me = me.toDto(),
        connection_id = connectionId,
    )
}

/**
 * Transforms [ChatEventDto] to [ChatEvent].
 * This is a generic transformation method that can be used to transform any [ChatEventDto] to [ChatEvent].
 * The actual transformation is delegated to the specific transformation methods for each event type.
 * The specific transformation methods are defined below.
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
@Suppress("ComplexMethod")
internal fun ChatEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): ChatEvent {
    return when (this) {
        is NewMessageEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is ChannelDeletedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is ChannelHiddenEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is ChannelTruncatedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is ChannelUpdatedByUserEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is ChannelUpdatedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is ChannelUserBannedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is ChannelUserUnbannedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is ChannelVisibleEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is ConnectedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is ConnectionErrorEventDto -> toDomain()
        is ConnectingEventDto -> toDomain()
        is DisconnectedEventDto -> toDomain()
        is ErrorEventDto -> toDomain()
        is GlobalUserBannedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is GlobalUserUnbannedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is HealthEventDto -> toDomain()
        is MarkAllReadEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is MemberAddedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is MemberRemovedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is MemberUpdatedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is MessageDeletedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is MessageReadEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is MessageUpdatedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is NotificationAddedToChannelEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is NotificationChannelDeletedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is NotificationChannelMutesUpdatedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is NotificationChannelTruncatedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is NotificationInviteAcceptedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is NotificationInviteRejectedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is NotificationInvitedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is NotificationMarkReadEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is NotificationMarkUnreadEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is NotificationMessageNewEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is NotificationThreadMessageNewEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is NotificationMutesUpdatedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is NotificationRemovedFromChannelEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is ReactionDeletedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is ReactionNewEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is ReactionUpdateEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is TypingStartEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is TypingStopEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is UnknownEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is UserDeletedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is UserPresenceChangedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is UserStartWatchingEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is UserStopWatchingEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is UserUpdatedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is PollClosedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is PollDeletedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is PollUpdatedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is VoteCastedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is VoteChangedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is AnswerCastedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is VoteRemovedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is AIIndicatorUpdatedEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is AIIndicatorClearEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
        is AIIndicatorStopEventDto -> toDomain(currentUserId, channelTransformer, messageTransformer)
    }
}

/**
 * Transforms [AIIndicatorClearEventDto] to [AIIndicatorClearEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
private fun ChannelDeletedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): ChannelDeletedEvent {
    return ChannelDeletedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channel = channel.toDomain(
            currentUserId = currentUserId,
            eventChatLastMessageAt = channel_last_message_at,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        user = user?.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [AIIndicatorClearEventDto] to [AIIndicatorClearEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
private fun ChannelHiddenEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): ChannelHiddenEvent {
    return ChannelHiddenEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        clearHistory = clear_history,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [ChannelTruncatedEventDto] to [ChannelTruncatedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun ChannelTruncatedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): ChannelTruncatedEvent {
    return ChannelTruncatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user?.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        message = message?.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channel = channel.toDomain(
            currentUserId = currentUserId,
            eventChatLastMessageAt = channel_last_message_at,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [ChannelUpdatedEventDto] to [ChannelUpdatedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun ChannelUpdatedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): ChannelUpdatedEvent {
    return ChannelUpdatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message?.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer
        ),
        channel = channel.toDomain(
            currentUserId = currentUserId,
            eventChatLastMessageAt = channel_last_message_at,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [ChannelUserBannedEventDto] to [ChannelUserBannedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun ChannelUpdatedByUserEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): ChannelUpdatedByUserEvent {
    return ChannelUpdatedByUserEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        message = message?.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channel = channel.toDomain(
            currentUserId = currentUserId,
            eventChatLastMessageAt = channel_last_message_at,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [ChannelUserBannedEventDto] to [ChannelUserBannedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun ChannelVisibleEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): ChannelVisibleEvent {
    return ChannelVisibleEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

private fun HealthEventDto.toDomain(): HealthEvent {
    return HealthEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        connectionId = connection_id,
    )
}

/**
 * Transforms [MarkAllReadEventDto] to [MarkAllReadEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun MemberAddedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): MemberAddedEvent {
    return MemberAddedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        member = member.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [MemberRemovedEventDto] to [MemberRemovedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun MemberRemovedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): MemberRemovedEvent {
    return MemberRemovedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        member = member.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

private fun MemberUpdatedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): MemberUpdatedEvent {
    return MemberUpdatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        member = member.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [MessageDeletedEventDto] to [MessageDeletedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun MessageDeletedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): MessageDeletedEvent {
    // TODO review createdAt and deletedAt fields here
    return MessageDeletedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user?.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        hardDelete = hard_delete ?: false,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [MessageReadEventDto] to [MessageReadEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun MessageReadEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): MessageReadEvent {
    return MessageReadEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        thread = thread?.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [MessageUpdatedEventDto] to [MessageUpdatedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun MessageUpdatedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): MessageUpdatedEvent {
    return MessageUpdatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [NewMessageEventDto] to [NewMessageEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun NewMessageEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): NewMessageEvent {
    return NewMessageEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        watcherCount = watcher_count,
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [NotificationMessageNewEventDto] to [NotificationMessageNewEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun NotificationAddedToChannelEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): NotificationAddedToChannelEvent {
    return NotificationAddedToChannelEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channel = channel.toDomain(
            currentUserId = currentUserId,
            eventChatLastMessageAt = channel_last_message_at,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer
        ),
        member = member.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [NotificationChannelDeletedEventDto] to [NotificationChannelDeletedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun NotificationChannelDeletedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): NotificationChannelDeletedEvent {
    return NotificationChannelDeletedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channel = channel.toDomain(
            currentUserId = currentUserId,
            eventChatLastMessageAt = channel_last_message_at,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer
        ),
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [NotificationChannelMutesUpdatedEventDto] to [NotificationChannelMutesUpdatedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun NotificationChannelMutesUpdatedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): NotificationChannelMutesUpdatedEvent {
    return NotificationChannelMutesUpdatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        me = me.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
    )
}

/**
 * Transforms [NotificationChannelTruncatedEventDto] to [NotificationChannelTruncatedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun NotificationChannelTruncatedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): NotificationChannelTruncatedEvent {
    return NotificationChannelTruncatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channel = channel.toDomain(
            currentUserId = currentUserId,
            eventChatLastMessageAt = channel_last_message_at,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer
        ),
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [NotificationInviteAcceptedEventDto] to [NotificationInviteAcceptedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun NotificationInviteAcceptedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): NotificationInviteAcceptedEvent {
    return NotificationInviteAcceptedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        member = member.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channel = channel.toDomain(
            currentUserId = currentUserId,
            eventChatLastMessageAt = channel_last_message_at,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [NotificationInviteRejectedEventDto] to [NotificationInviteRejectedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun NotificationInviteRejectedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): NotificationInviteRejectedEvent {
    return NotificationInviteRejectedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        member = member.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channel = channel.toDomain(
            currentUserId = currentUserId,
            eventChatLastMessageAt = channel_last_message_at,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [NotificationInvitedEventDto] to [NotificationInvitedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun NotificationInvitedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): NotificationInvitedEvent {
    return NotificationInvitedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        member = member.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [NotificationMarkReadEventDto] to [NotificationMarkReadEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun NotificationMarkReadEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): NotificationMarkReadEvent {
    return NotificationMarkReadEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer
        ),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
        threadId = thread_id,
        thread = thread?.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        unreadThreads = unread_threads,
        unreadThreadMessages = unread_thread_messages,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [NotificationMarkUnreadEventDto] to [NotificationMarkUnreadEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun NotificationMarkUnreadEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): NotificationMarkUnreadEvent {
    return NotificationMarkUnreadEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
        firstUnreadMessageId = first_unread_message_id,
        lastReadMessageId = last_read_message_id,
        lastReadMessageAt = last_read_at.date,
        unreadMessages = unread_messages,
        threadId = thread_id,
        unreadThreads = unread_threads,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [MarkAllReadEventDto] to [MarkAllReadEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun MarkAllReadEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): MarkAllReadEvent {
    return MarkAllReadEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
    )
}

/**
 * Transforms [NotificationMessageNewEventDto] to [NotificationMessageNewEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun NotificationMessageNewEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): NotificationMessageNewEvent {
    return NotificationMessageNewEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channel = channel.toDomain(
            currentUserId = currentUserId,
            eventChatLastMessageAt = channel_last_message_at,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        message = message.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [NotificationThreadMessageNewEventDto] to [NotificationThreadMessageNewEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun NotificationThreadMessageNewEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): NotificationThreadMessageNewEvent {
    return NotificationThreadMessageNewEvent(
        type = type,
        cid = cid,
        channelId = channel_id,
        channelType = channel_type,
        message = message.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channel = channel.toDomain(
            currentUserId = currentUserId,
            eventChatLastMessageAt = channel_last_message_at,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        unreadThreads = unread_threads,
        unreadThreadMessages = unread_thread_messages,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [NotificationMutesUpdatedEventDto] to [NotificationMutesUpdatedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun NotificationMutesUpdatedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): NotificationMutesUpdatedEvent {
    return NotificationMutesUpdatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        me = me.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
    )
}

/**
 * Transforms [NotificationRemovedFromChannelEventDto] to [NotificationRemovedFromChannelEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun NotificationRemovedFromChannelEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): NotificationRemovedFromChannelEvent {
    return NotificationRemovedFromChannelEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user?.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channel = channel.toDomain(
            currentUserId = currentUserId,
            eventChatLastMessageAt = channel_last_message_at,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        member = member.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [PollClosedEventDto] to [PollClosedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun ReactionDeletedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): ReactionDeletedEvent {
    return ReactionDeletedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        reaction = reaction.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [ReactionNewEventDto] to [ReactionNewEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun ReactionNewEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): ReactionNewEvent {
    return ReactionNewEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        reaction = reaction.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [ReactionUpdateEventDto] to [ReactionUpdateEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun ReactionUpdateEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): ReactionUpdateEvent {
    return ReactionUpdateEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        reaction = reaction.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [TypingStartEventDto] to [TypingStartEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun TypingStartEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): TypingStartEvent {
    return TypingStartEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        parentId = parent_id,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [TypingStopEventDto] to [TypingStopEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun TypingStopEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): TypingStopEvent {
    return TypingStopEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        parentId = parent_id,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [ChannelUserBannedEventDto] to [ChannelUserBannedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
private fun ChannelUserBannedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): ChannelUserBannedEvent {
    return ChannelUserBannedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        expiration = expiration,
        shadow = shadow ?: false,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [GlobalUserBannedEventDto] to [GlobalUserBannedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
private fun GlobalUserBannedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): GlobalUserBannedEvent {
    return GlobalUserBannedEvent(
        type = type,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
    )
}

/**
 * Transforms [UserDeletedEventDto] to [UserDeletedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
private fun UserDeletedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): UserDeletedEvent {
    return UserDeletedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
    )
}

/**
 * Transforms [UserPresenceChangedEventDto] to [UserPresenceChangedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
private fun UserPresenceChangedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): UserPresenceChangedEvent {
    return UserPresenceChangedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
    )
}

/**
 * Transforms [UserStartWatchingEventDto] to [UserStartWatchingEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
private fun UserStartWatchingEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): UserStartWatchingEvent {
    return UserStartWatchingEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        watcherCount = watcher_count,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [UserStopWatchingEventDto] to [UserStopWatchingEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
private fun UserStopWatchingEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): UserStopWatchingEvent {
    return UserStopWatchingEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        watcherCount = watcher_count,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [ChannelUserUnbannedEventDto] to [ChannelUserUnbannedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
private fun ChannelUserUnbannedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): ChannelUserUnbannedEvent {
    return ChannelUserUnbannedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [GlobalUserUnbannedEventDto] to [GlobalUserUnbannedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
private fun GlobalUserUnbannedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): GlobalUserUnbannedEvent {
    return GlobalUserUnbannedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
    )
}

/**
 * Transforms [UserUpdatedEventDto] to [UserUpdatedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
private fun UserUpdatedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): UserUpdatedEvent {
    return UserUpdatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
    )
}

/**
 * Transforms [PollClosedEventDto] to [PollClosedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
private fun PollClosedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): PollClosedEvent {
    val newPoll = poll.toDomain(
        currentUserId = currentUserId,
        channelTransformer = channelTransformer,
        messageTransformer = messageTransformer,
    )
    val (channelType, channelId) = cid.cidToTypeAndId()
    return PollClosedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        poll = newPoll,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [PollCreatedEventDto] to [PollCreatedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
private fun PollDeletedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): PollDeletedEvent {
    val newPoll = poll.toDomain(
        currentUserId = currentUserId,
        channelTransformer = channelTransformer,
        messageTransformer = messageTransformer,
    )
    val (channelType, channelId) = cid.cidToTypeAndId()
    return PollDeletedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        poll = newPoll,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [PollDeletedEventDto] to [PollDeletedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
private fun PollUpdatedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): PollUpdatedEvent {
    val newPoll = poll.toDomain(
        currentUserId = currentUserId,
        channelTransformer = channelTransformer,
        messageTransformer = messageTransformer,
    )
    val (channelType, channelId) = cid.cidToTypeAndId()
    return PollUpdatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        poll = newPoll,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [VoteCastedEventDto] to [VoteCastedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun VoteCastedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): VoteCastedEvent {
    val pollVote = poll_vote.toDomain(
        currentUserId = currentUserId,
        channelTransformer = channelTransformer,
        messageTransformer = messageTransformer,
    )
    val (channelType, channelId) = cid.cidToTypeAndId()
    val newPoll = poll.toDomain(
        currentUserId = currentUserId,
        channelTransformer = channelTransformer,
        messageTransformer = messageTransformer,
    )
        .let { poll ->
            pollVote.takeIf { it.user?.id == currentUserId }
                ?.let {
                    poll.copy(
                        votes = (poll.votes.associateBy { it.id } + (it.id to it)).values.toList(),
                        ownVotes = (poll.ownVotes.associateBy { it.id } + (it.id to it)).values.toList(),
                    )
                } ?: poll
        }
    return VoteCastedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        poll = newPoll,
        newVote = pollVote,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [AnswerCastedEventDto] to [AnswerCastedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun AnswerCastedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): AnswerCastedEvent {
    val newAnswer = poll_vote.toAnswerDomain(
        currentUserId = currentUserId,
        channelTransformer = channelTransformer,
        messageTransformer = messageTransformer,
    )
    val (channelType, channelId) = cid.cidToTypeAndId()
    return AnswerCastedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        poll = poll.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        newAnswer = newAnswer,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [VoteChangedEventDto] to [VoteChangedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun VoteChangedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): VoteChangedEvent {
    val pollVote = poll_vote.toDomain(
        currentUserId = currentUserId,
        channelTransformer = channelTransformer,
        messageTransformer = messageTransformer,
    )
    val (channelType, channelId) = cid.cidToTypeAndId()
    val newPoll = poll.toDomain(
        currentUserId = currentUserId,
        channelTransformer = channelTransformer,
        messageTransformer = messageTransformer,
    )
        .let { poll ->
            pollVote.takeIf { it.user?.id == currentUserId }
                ?.let {
                    poll.copy(
                        votes = (poll.votes.associateBy { it.id } + (it.id to it)).values.toList(),
                        ownVotes = (poll.ownVotes.associateBy { it.id } + (it.id to it)).values.toList(),
                    )
                } ?: poll
        }
    return VoteChangedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        poll = newPoll,
        newVote = pollVote,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [VoteRemovedEventDto] to [VoteRemovedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun VoteRemovedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): VoteRemovedEvent {
    val removedVote = poll_vote.toDomain(
        currentUserId = currentUserId,
        channelTransformer = channelTransformer,
        messageTransformer = messageTransformer,
    )
    val (channelType, channelId) = cid.cidToTypeAndId()
    val newPoll = poll.toDomain(
        currentUserId = currentUserId,
        channelTransformer = channelTransformer,
        messageTransformer = messageTransformer,
    )
    return VoteRemovedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        poll = newPoll,
        removedVote = removedVote,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [AIIndicatorUpdatedEventDto] to [AIIndicatorUpdatedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun AIIndicatorUpdatedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): AIIndicatorUpdatedEvent {
    val (channelType, channelId) = cid.cidToTypeAndId()
    return AIIndicatorUpdatedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channelType = channelType,
        channelId = channelId,
        channelLastMessageAt = channel_last_message_at,
        aiState = ai_state,
        messageId = message_id,
    )
}

/**
 * Transforms [AIIndicatorClearEventDto] to [AIIndicatorClearEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun AIIndicatorClearEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): AIIndicatorClearEvent {
    val (channelType, channelId) = cid.cidToTypeAndId()
    return AIIndicatorClearEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [AIIndicatorStopEvent] to [AIIndicatorStopEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun AIIndicatorStopEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): AIIndicatorStopEvent {
    val (channelType, channelId) = cid.cidToTypeAndId()
    return AIIndicatorStopEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        cid = cid,
        user = user.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        channelType = channelType,
        channelId = channelId,
        channelLastMessageAt = channel_last_message_at,
    )
}

/**
 * Transforms [CconnectedEventDto] to [ConnectedEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun ConnectedEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): ConnectedEvent {
    return ConnectedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        me = me.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        connectionId = connection_id,
    )
}

private fun ConnectionErrorEventDto.toDomain(): ConnectionErrorEvent {
    return ConnectionErrorEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        connectionId = connection_id,
        error = error.toDomain(),
    )
}

private fun ConnectingEventDto.toDomain(): ConnectingEvent {
    return ConnectingEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
    )
}

private fun DisconnectedEventDto.toDomain(): DisconnectedEvent {
    return DisconnectedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
    )
}

private fun ErrorEventDto.toDomain(): ErrorEvent {
    return ErrorEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        error = error,
    )
}

/**
 * Transforms [UnknownEventDto] to [UnknownEvent].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the messages.
 */
private fun UnknownEventDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): UnknownEvent {
    return UnknownEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        user = user?.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        rawData = rawData,
    )
}
