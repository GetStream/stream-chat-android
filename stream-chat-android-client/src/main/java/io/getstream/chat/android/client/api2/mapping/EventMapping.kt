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
        created_at = createdAt,
        me = me.toDto(),
        connection_id = connectionId,
    )
}

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
        createdAt = created_at,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channel = channel.toDomain(),
        user = user?.toDomain(),
    )
}

private fun ChannelHiddenEventDto.toDomain(): ChannelHiddenEvent {
    return ChannelHiddenEvent(
        type = type,
        createdAt = created_at,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
        clearHistory = clear_history,
    )
}

private fun ChannelTruncatedEventDto.toDomain(): ChannelTruncatedEvent {
    return ChannelTruncatedEvent(
        type = type,
        createdAt = created_at,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
        channel = channel.toDomain(),
    )
}

private fun ChannelUpdatedEventDto.toDomain(): ChannelUpdatedEvent {
    return ChannelUpdatedEvent(
        type = type,
        createdAt = created_at,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message?.toDomain(),
        channel = channel.toDomain(),
    )
}

private fun ChannelUpdatedByUserEventDto.toDomain(): ChannelUpdatedByUserEvent {
    return ChannelUpdatedByUserEvent(
        type = type,
        createdAt = created_at,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
        message = message?.toDomain(),
        channel = channel.toDomain(),
    )
}

private fun ChannelVisibleEventDto.toDomain(): ChannelVisibleEvent {
    return ChannelVisibleEvent(
        type = type,
        createdAt = created_at,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
    )
}

private fun HealthEventDto.toDomain(): HealthEvent {
    return HealthEvent(
        type = type,
        createdAt = created_at,
        connectionId = connection_id,
    )
}

private fun MemberAddedEventDto.toDomain(): MemberAddedEvent {
    return MemberAddedEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        member = member.toDomain(),
    )
}

private fun MemberRemovedEventDto.toDomain(): MemberRemovedEvent {
    return MemberRemovedEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
    )
}

private fun MemberUpdatedEventDto.toDomain(): MemberUpdatedEvent {
    return MemberUpdatedEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        member = member.toDomain(),
    )
}

private fun MessageDeletedEventDto.toDomain(): MessageDeletedEvent {
    // TODO review createdAt and deletedAt fields here
    return MessageDeletedEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(),
    )
}

private fun MessageReadEventDto.toDomain(): MessageReadEvent {
    return MessageReadEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
    )
}

private fun MessageUpdatedEventDto.toDomain(): MessageUpdatedEvent {
    return MessageUpdatedEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(),
    )
}

private fun NewMessageEventDto.toDomain(): NewMessageEvent {
    return NewMessageEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(),
        watcherCount = watcher_count,
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
    )
}

private fun NotificationAddedToChannelEventDto.toDomain(): NotificationAddedToChannelEvent {
    return NotificationAddedToChannelEvent(
        type = type,
        createdAt = created_at,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channel = channel.toDomain(),
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
    )
}

private fun NotificationChannelDeletedEventDto.toDomain(): NotificationChannelDeletedEvent {
    return NotificationChannelDeletedEvent(
        type = type,
        createdAt = created_at,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channel = channel.toDomain(),
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
    )
}

private fun NotificationChannelMutesUpdatedEventDto.toDomain(): NotificationChannelMutesUpdatedEvent {
    return NotificationChannelMutesUpdatedEvent(
        type = type,
        createdAt = created_at,
        me = me.toDomain(),
    )
}

private fun NotificationChannelTruncatedEventDto.toDomain(): NotificationChannelTruncatedEvent {
    return NotificationChannelTruncatedEvent(
        type = type,
        createdAt = created_at,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channel = channel.toDomain(),
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
    )
}

private fun NotificationInviteAcceptedEventDto.toDomain(): NotificationInviteAcceptedEvent {
    return NotificationInviteAcceptedEvent(
        type = type,
        createdAt = created_at,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
        member = member.toDomain(),
    )
}

private fun NotificationInviteRejectedEventDto.toDomain(): NotificationInviteRejectedEvent {
    return NotificationInviteRejectedEvent(
        type = type,
        createdAt = created_at,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
        member = member.toDomain()
    )
}

private fun NotificationInvitedEventDto.toDomain(): NotificationInvitedEvent {
    return NotificationInvitedEvent(
        type = type,
        createdAt = created_at,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
        member = member.toDomain(),
    )
}

private fun NotificationMarkReadEventDto.toDomain(): NotificationMarkReadEvent {
    return NotificationMarkReadEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
    )
}

private fun MarkAllReadEventDto.toDomain(): MarkAllReadEvent {
    return MarkAllReadEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
    )
}

private fun NotificationMessageNewEventDto.toDomain(): NotificationMessageNewEvent {
    return NotificationMessageNewEvent(
        type = type,
        createdAt = created_at,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        channel = channel.toDomain(),
        message = message.toDomain(),
        totalUnreadCount = total_unread_count,
        unreadChannels = unread_channels,
    )
}

private fun NotificationMutesUpdatedEventDto.toDomain(): NotificationMutesUpdatedEvent {
    return NotificationMutesUpdatedEvent(
        type = type,
        createdAt = created_at,
        me = me.toDomain(),
    )
}

private fun NotificationRemovedFromChannelEventDto.toDomain(): NotificationRemovedFromChannelEvent {
    return NotificationRemovedFromChannelEvent(
        type = type,
        createdAt = created_at,
        user = user?.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        member = member.toDomain()
    )
}

private fun ReactionDeletedEventDto.toDomain(): ReactionDeletedEvent {
    return ReactionDeletedEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(),
        reaction = reaction.toDomain(),
    )
}

private fun ReactionNewEventDto.toDomain(): ReactionNewEvent {
    return ReactionNewEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(),
        reaction = reaction.toDomain(),
    )
}

private fun ReactionUpdateEventDto.toDomain(): ReactionUpdateEvent {
    return ReactionUpdateEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        message = message.toDomain(),
        reaction = reaction.toDomain(),
    )
}

private fun TypingStartEventDto.toDomain(): TypingStartEvent {
    return TypingStartEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        parentId = parent_id,
    )
}

private fun TypingStopEventDto.toDomain(): TypingStopEvent {
    return TypingStopEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        parentId = parent_id,
    )
}

private fun ChannelUserBannedEventDto.toDomain(): ChannelUserBannedEvent {
    return ChannelUserBannedEvent(
        type = type,
        createdAt = created_at,
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
        expiration = expiration,
    )
}

private fun GlobalUserBannedEventDto.toDomain(): GlobalUserBannedEvent {
    return GlobalUserBannedEvent(
        type = type,
        user = user.toDomain(),
        createdAt = created_at,
    )
}

private fun UserDeletedEventDto.toDomain(): UserDeletedEvent {
    return UserDeletedEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
    )
}

private fun UserPresenceChangedEventDto.toDomain(): UserPresenceChangedEvent {
    return UserPresenceChangedEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
    )
}

private fun UserStartWatchingEventDto.toDomain(): UserStartWatchingEvent {
    return UserStartWatchingEvent(
        type = type,
        createdAt = created_at,
        cid = cid,
        watcherCount = watcher_count,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
    )
}

private fun UserStopWatchingEventDto.toDomain(): UserStopWatchingEvent {
    return UserStopWatchingEvent(
        type = type,
        createdAt = created_at,
        cid = cid,
        watcherCount = watcher_count,
        channelType = channel_type,
        channelId = channel_id,
        user = user.toDomain(),
    )
}

private fun ChannelUserUnbannedEventDto.toDomain(): ChannelUserUnbannedEvent {
    return ChannelUserUnbannedEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
        cid = cid,
        channelType = channel_type,
        channelId = channel_id,
    )
}

private fun GlobalUserUnbannedEventDto.toDomain(): GlobalUserUnbannedEvent {
    return GlobalUserUnbannedEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
    )
}

private fun UserUpdatedEventDto.toDomain(): UserUpdatedEvent {
    return UserUpdatedEvent(
        type = type,
        createdAt = created_at,
        user = user.toDomain(),
    )
}

private fun ConnectedEventDto.toDomain(): ConnectedEvent {
    return ConnectedEvent(
        type = type,
        createdAt = created_at,
        me = me.toDomain(),
        connectionId = connection_id,
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
        createdAt = created_at,
        rawData = rawData,
    )
}
