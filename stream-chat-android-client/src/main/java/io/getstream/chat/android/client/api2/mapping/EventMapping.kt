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
import io.getstream.chat.android.client.api2.model.dto.NotificationMessageNewEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationMutesUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationRemovedFromChannelEventDto
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
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
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

internal fun ConnectedEvent.toDto(): UpstreamConnectedEventDto {
    return UpstreamConnectedEventDto(
        type = this.type,
        createdAt = createdAt,
        me = me.toDto(),
        connectionId = connectionId,
    )
}

@Suppress("ComplexMethod")
internal fun ChatEventDto.toDomain(): ChatEvent {
    return when (this) {
        is NewMessageEventDto -> toDomain()
        is ChannelDeletedEventDto -> toDomain()
        is ChannelHiddenEventDto -> toDomain()
        is ChannelTruncatedEventDto -> toDomain()
        is ChannelUpdatedByUserEventDto -> toDomain()
        is ChannelUpdatedEventDto -> toDomain()
        is ChannelUserBannedEventDto -> toDomain()
        is ChannelUserUnbannedEventDto -> toDomain()
        is ChannelVisibleEventDto -> toDomain()
        is ConnectedEventDto -> toDomain()
        is ConnectingEventDto -> toDomain()
        is DisconnectedEventDto -> toDomain()
        is ErrorEventDto -> toDomain()
        is GlobalUserBannedEventDto -> toDomain()
        is GlobalUserUnbannedEventDto -> toDomain()
        is HealthEventDto -> toDomain()
        is MarkAllReadEventDto -> toDomain()
        is MemberAddedEventDto -> toDomain()
        is MemberRemovedEventDto -> toDomain()
        is MemberUpdatedEventDto -> toDomain()
        is MessageDeletedEventDto -> toDomain()
        is MessageReadEventDto -> toDomain()
        is MessageUpdatedEventDto -> toDomain()
        is NotificationAddedToChannelEventDto -> toDomain()
        is NotificationChannelDeletedEventDto -> toDomain()
        is NotificationChannelMutesUpdatedEventDto -> toDomain()
        is NotificationChannelTruncatedEventDto -> toDomain()
        is NotificationInviteAcceptedEventDto -> toDomain()
        is NotificationInviteRejectedEventDto -> toDomain()
        is NotificationInvitedEventDto -> toDomain()
        is NotificationMarkReadEventDto -> toDomain()
        is NotificationMessageNewEventDto -> toDomain()
        is NotificationMutesUpdatedEventDto -> toDomain()
        is NotificationRemovedFromChannelEventDto -> toDomain()
        is ReactionDeletedEventDto -> toDomain()
        is ReactionNewEventDto -> toDomain()
        is ReactionUpdateEventDto -> toDomain()
        is TypingStartEventDto -> toDomain()
        is TypingStopEventDto -> toDomain()
        is UnknownEventDto -> toDomain()
        is UserDeletedEventDto -> toDomain()
        is UserPresenceChangedEventDto -> toDomain()
        is UserStartWatchingEventDto -> toDomain()
        is UserStopWatchingEventDto -> toDomain()
        is UserUpdatedEventDto -> toDomain()
    }
}

private fun ChannelDeletedEventDto.toDomain(): ChannelDeletedEvent {
    return ChannelDeletedEvent(
        type = type,
        createdAt = createdAt,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel.toDomain(),
        user = user?.toDomain(),
    )
}

private fun ChannelHiddenEventDto.toDomain(): ChannelHiddenEvent {
    return ChannelHiddenEvent(
        type = type,
        createdAt = createdAt,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        user = user.toDomain(),
        clearHistory = clearHistory,
    )
}

private fun ChannelTruncatedEventDto.toDomain(): ChannelTruncatedEvent {
    return ChannelTruncatedEvent(
        type = type,
        createdAt = createdAt,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        user = user?.toDomain(),
        message = message?.toDomain(),
        channel = channel.toDomain(),
    )
}

private fun ChannelUpdatedEventDto.toDomain(): ChannelUpdatedEvent {
    return ChannelUpdatedEvent(
        type = type,
        createdAt = createdAt,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message?.toDomain(),
        channel = channel.toDomain(),
    )
}

private fun ChannelUpdatedByUserEventDto.toDomain(): ChannelUpdatedByUserEvent {
    return ChannelUpdatedByUserEvent(
        type = type,
        createdAt = createdAt,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        user = user.toDomain(),
        message = message?.toDomain(),
        channel = channel.toDomain(),
    )
}

private fun ChannelVisibleEventDto.toDomain(): ChannelVisibleEvent {
    return ChannelVisibleEvent(
        type = type,
        createdAt = createdAt,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        user = user.toDomain(),
    )
}

private fun HealthEventDto.toDomain(): HealthEvent {
    return HealthEvent(
        type = type,
        createdAt = createdAt,
        connectionId = connectionId,
    )
}

private fun MemberAddedEventDto.toDomain(): MemberAddedEvent {
    return MemberAddedEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        member = member.toDomain(),
    )
}

private fun MemberRemovedEventDto.toDomain(): MemberRemovedEvent {
    return MemberRemovedEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        member = member.toDomain(),
    )
}

private fun MemberUpdatedEventDto.toDomain(): MemberUpdatedEvent {
    return MemberUpdatedEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        member = member.toDomain(),
    )
}

private fun MessageDeletedEventDto.toDomain(): MessageDeletedEvent {
    // TODO review createdAt and deletedAt fields here
    return MessageDeletedEvent(
        type = type,
        createdAt = createdAt,
        user = user?.toDomain(),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message.toDomain(),
        hardDelete = hardDelete ?: false,
    )
}

private fun MessageReadEventDto.toDomain(): MessageReadEvent {
    return MessageReadEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
    )
}

private fun MessageUpdatedEventDto.toDomain(): MessageUpdatedEvent {
    return MessageUpdatedEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message.toDomain(),
    )
}

private fun NewMessageEventDto.toDomain(): NewMessageEvent {
    return NewMessageEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message.toDomain(),
        watcherCount = watcherCount,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

private fun NotificationAddedToChannelEventDto.toDomain(): NotificationAddedToChannelEvent {
    return NotificationAddedToChannelEvent(
        type = type,
        createdAt = createdAt,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel.toDomain(),
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadchannels,
    )
}

private fun NotificationChannelDeletedEventDto.toDomain(): NotificationChannelDeletedEvent {
    return NotificationChannelDeletedEvent(
        type = type,
        createdAt = createdAt,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel.toDomain(),
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

private fun NotificationChannelMutesUpdatedEventDto.toDomain(): NotificationChannelMutesUpdatedEvent {
    return NotificationChannelMutesUpdatedEvent(
        type = type,
        createdAt = createdAt,
        me = me.toDomain(),
    )
}

private fun NotificationChannelTruncatedEventDto.toDomain(): NotificationChannelTruncatedEvent {
    return NotificationChannelTruncatedEvent(
        type = type,
        createdAt = createdAt,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel.toDomain(),
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

private fun NotificationInviteAcceptedEventDto.toDomain(): NotificationInviteAcceptedEvent {
    return NotificationInviteAcceptedEvent(
        type = type,
        createdAt = createdAt,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        user = user.toDomain(),
        member = member.toDomain(),
        channel = channel.toDomain(),
    )
}

private fun NotificationInviteRejectedEventDto.toDomain(): NotificationInviteRejectedEvent {
    return NotificationInviteRejectedEvent(
        type = type,
        createdAt = createdAt,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        user = user.toDomain(),
        member = member.toDomain(),
        channel = channel.toDomain(),
    )
}

private fun NotificationInvitedEventDto.toDomain(): NotificationInvitedEvent {
    return NotificationInvitedEvent(
        type = type,
        createdAt = createdAt,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        user = user.toDomain(),
        member = member.toDomain(),
    )
}

private fun NotificationMarkReadEventDto.toDomain(): NotificationMarkReadEvent {
    return NotificationMarkReadEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

private fun MarkAllReadEventDto.toDomain(): MarkAllReadEvent {
    return MarkAllReadEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

private fun NotificationMessageNewEventDto.toDomain(): NotificationMessageNewEvent {
    return NotificationMessageNewEvent(
        type = type,
        createdAt = createdAt,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel.toDomain(),
        message = message.toDomain(),
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

private fun NotificationMutesUpdatedEventDto.toDomain(): NotificationMutesUpdatedEvent {
    return NotificationMutesUpdatedEvent(
        type = type,
        createdAt = createdAt,
        me = me.toDomain(),
    )
}

private fun NotificationRemovedFromChannelEventDto.toDomain(): NotificationRemovedFromChannelEvent {
    return NotificationRemovedFromChannelEvent(
        type = type,
        createdAt = createdAt,
        user = user?.toDomain(),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel.toDomain(),
        member = member.toDomain()
    )
}

private fun ReactionDeletedEventDto.toDomain(): ReactionDeletedEvent {
    return ReactionDeletedEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message.toDomain(),
        reaction = reaction.toDomain(),
    )
}

private fun ReactionNewEventDto.toDomain(): ReactionNewEvent {
    return ReactionNewEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message.toDomain(),
        reaction = reaction.toDomain(),
    )
}

private fun ReactionUpdateEventDto.toDomain(): ReactionUpdateEvent {
    return ReactionUpdateEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message.toDomain(),
        reaction = reaction.toDomain(),
    )
}

private fun TypingStartEventDto.toDomain(): TypingStartEvent {
    return TypingStartEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        parentId = parentId,
    )
}

private fun TypingStopEventDto.toDomain(): TypingStopEvent {
    return TypingStopEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        parentId = parentId,
    )
}

private fun ChannelUserBannedEventDto.toDomain(): ChannelUserBannedEvent {
    return ChannelUserBannedEvent(
        type = type,
        createdAt = createdAt,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        user = user.toDomain(),
        expiration = expiration,
    )
}

private fun GlobalUserBannedEventDto.toDomain(): GlobalUserBannedEvent {
    return GlobalUserBannedEvent(
        type = type,
        user = user.toDomain(),
        createdAt = createdAt,
    )
}

private fun UserDeletedEventDto.toDomain(): UserDeletedEvent {
    return UserDeletedEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
    )
}

private fun UserPresenceChangedEventDto.toDomain(): UserPresenceChangedEvent {
    return UserPresenceChangedEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
    )
}

private fun UserStartWatchingEventDto.toDomain(): UserStartWatchingEvent {
    return UserStartWatchingEvent(
        type = type,
        createdAt = createdAt,
        cid = cid,
        watcherCount = watcherCount,
        channelType = channelType,
        channelId = channelId,
        user = user.toDomain(),
    )
}

private fun UserStopWatchingEventDto.toDomain(): UserStopWatchingEvent {
    return UserStopWatchingEvent(
        type = type,
        createdAt = createdAt,
        cid = cid,
        watcherCount = watcherCount,
        channelType = channelType,
        channelId = channelId,
        user = user.toDomain(),
    )
}

private fun ChannelUserUnbannedEventDto.toDomain(): ChannelUserUnbannedEvent {
    return ChannelUserUnbannedEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
    )
}

private fun GlobalUserUnbannedEventDto.toDomain(): GlobalUserUnbannedEvent {
    return GlobalUserUnbannedEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
    )
}

private fun UserUpdatedEventDto.toDomain(): UserUpdatedEvent {
    return UserUpdatedEvent(
        type = type,
        createdAt = createdAt,
        user = user.toDomain(),
    )
}

private fun ConnectedEventDto.toDomain(): ConnectedEvent {
    return ConnectedEvent(
        type = type,
        createdAt = createdAt,
        me = me.toDomain(),
        connectionId = connectionId,
    )
}

private fun ConnectingEventDto.toDomain(): ConnectingEvent {
    return ConnectingEvent(
        type = type,
        createdAt = createdAt,
    )
}

private fun DisconnectedEventDto.toDomain(): DisconnectedEvent {
    return DisconnectedEvent(
        type = type,
        createdAt = createdAt,
    )
}

private fun ErrorEventDto.toDomain(): ErrorEvent {
    return ErrorEvent(
        type = type,
        createdAt = createdAt,
        error = error,
    )
}

private fun UnknownEventDto.toDomain(): UnknownEvent {
    return UnknownEvent(
        type = type,
        createdAt = createdAt,
        user = user?.toDomain(),
        rawData = rawData,
    )
}
