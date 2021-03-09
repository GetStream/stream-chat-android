package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.ChannelCreatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelHiddenEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelMuteEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelTruncatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUnmuteEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUpdatedByUserEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUserBannedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUserUnbannedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelVisibleEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelsMuteEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelsUnmuteEventDto
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
import io.getstream.chat.android.client.api2.model.dto.UserDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.UserMutedEventDto
import io.getstream.chat.android.client.api2.model.dto.UserPresenceChangedEventDto
import io.getstream.chat.android.client.api2.model.dto.UserStartWatchingEventDto
import io.getstream.chat.android.client.api2.model.dto.UserStopWatchingEventDto
import io.getstream.chat.android.client.api2.model.dto.UserUnmutedEventDto
import io.getstream.chat.android.client.api2.model.dto.UserUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.UsersMutedEventDto
import io.getstream.chat.android.client.api2.model.dto.UsersUnmutedEventDto
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.events.NewMessageEvent

internal fun ChatEventDto.toDomain(): ChatEvent {
    return when (this) {
        is NewMessageEventDto -> toDomain()
        is ChannelCreatedEventDto -> TODO()
        is ChannelDeletedEventDto -> TODO()
        is ChannelHiddenEventDto -> TODO()
        is ChannelMuteEventDto -> TODO()
        is ChannelTruncatedEventDto -> TODO()
        is ChannelUnmuteEventDto -> TODO()
        is ChannelUpdatedByUserEventDto -> TODO()
        is ChannelUpdatedEventDto -> TODO()
        is ChannelUserBannedEventDto -> TODO()
        is ChannelUserUnbannedEventDto -> TODO()
        is ChannelVisibleEventDto -> TODO()
        is ChannelsMuteEventDto -> TODO()
        is ChannelsUnmuteEventDto -> TODO()
        is ConnectedEventDto -> TODO()
        is ConnectingEventDto -> TODO()
        is DisconnectedEventDto -> TODO()
        is ErrorEventDto -> TODO()
        is GlobalUserBannedEventDto -> TODO()
        is GlobalUserUnbannedEventDto -> TODO()
        is HealthEventDto -> toDomain()
        is MarkAllReadEventDto -> TODO()
        is MemberAddedEventDto -> TODO()
        is MemberRemovedEventDto -> TODO()
        is MemberUpdatedEventDto -> TODO()
        is MessageDeletedEventDto -> TODO()
        is MessageReadEventDto -> TODO()
        is MessageUpdatedEventDto -> TODO()
        is NotificationAddedToChannelEventDto -> TODO()
        is NotificationChannelDeletedEventDto -> TODO()
        is NotificationChannelMutesUpdatedEventDto -> TODO()
        is NotificationChannelTruncatedEventDto -> TODO()
        is NotificationInviteAcceptedEventDto -> TODO()
        is NotificationInvitedEventDto -> TODO()
        is NotificationMarkReadEventDto -> TODO()
        is NotificationMessageNewEventDto -> TODO()
        is NotificationMutesUpdatedEventDto -> TODO()
        is NotificationRemovedFromChannelEventDto -> TODO()
        is ReactionDeletedEventDto -> TODO()
        is ReactionNewEventDto -> TODO()
        is ReactionUpdateEventDto -> TODO()
        is TypingStartEventDto -> TODO()
        is TypingStopEventDto -> TODO()
        is UnknownEventDto -> TODO()
        is UserDeletedEventDto -> TODO()
        is UserMutedEventDto -> TODO()
        is UserPresenceChangedEventDto -> TODO()
        is UserStartWatchingEventDto -> TODO()
        is UserStopWatchingEventDto -> TODO()
        is UserUnmutedEventDto -> TODO()
        is UserUpdatedEventDto -> TODO()
        is UsersMutedEventDto -> TODO()
        is UsersUnmutedEventDto -> TODO()
    }
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

private fun HealthEventDto.toDomain(): HealthEvent {
    return HealthEvent(
        type = type,
        createdAt = created_at,
        connectionId = connection_id,
    )
}
