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
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.models.ChannelInfo

@Suppress("LargeClass")
internal class EventMapping(
    private val domainMapping: DomainMapping,
) {

    /**
     * Transforms [ChatEventDto] to [ChatEvent].
     * This is a generic transformation method that can be used to transform any [ChatEventDto] to [ChatEvent].
     * The actual transformation is delegated to the specific transformation methods for each event type.
     * The specific transformation methods are defined below.
     */
    @Suppress("LongMethod")
    internal fun ChatEventDto.toDomain(): ChatEvent = when (this) {
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
        is ConnectionErrorEventDto -> toDomain()
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
        is NotificationMarkUnreadEventDto -> toDomain()
        is NotificationMessageNewEventDto -> toDomain()
        is NotificationThreadMessageNewEventDto -> toDomain()
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
        is PollClosedEventDto -> toDomain()
        is PollDeletedEventDto -> toDomain()
        is PollUpdatedEventDto -> toDomain()
        is VoteCastedEventDto -> toDomain()
        is VoteChangedEventDto -> toDomain()
        is AnswerCastedEventDto -> toDomain()
        is VoteRemovedEventDto -> toDomain()
        is DraftMessageDeletedEventDto -> toDomain()
        is DraftMessageUpdatedEventDto -> toDomain()
        is ReminderCreatedEventDto -> toDomain()
        is ReminderUpdatedEventDto -> toDomain()
        is ReminderDeletedEventDto -> toDomain()
        is NotificationReminderDueEventDto -> toDomain()
        is UserMessagesDeletedEventDto -> toDomain()
        is AIIndicatorUpdatedEventDto -> toDomain()
        is AIIndicatorClearEventDto -> toDomain()
        is AIIndicatorStopEventDto -> toDomain()
    }

    /**
     * Transforms [ChannelDeletedEventDto] to [ChannelDeletedEvent].
     */
    private fun ChannelDeletedEventDto.toDomain(): ChannelDeletedEvent = with(domainMapping) {
        ChannelDeletedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            channel = channel.toDomain(),
            user = user?.toDomain(),
        )
    }

    /**
     * Transforms [ChannelHiddenEventDto] to [ChannelHiddenEvent].
     */
    private fun ChannelHiddenEventDto.toDomain(): ChannelHiddenEvent = with(domainMapping) {
        ChannelHiddenEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            user = user.toDomain(),
            channel = channel.toDomain(),
            clearHistory = clear_history,
        )
    }

    /**
     * Transforms [ChannelTruncatedEventDto] to [ChannelTruncatedEvent].
     */
    private fun ChannelTruncatedEventDto.toDomain(): ChannelTruncatedEvent = with(domainMapping) {
        ChannelTruncatedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            user = user?.toDomain(),
            message = message?.toDomain(channel.toChannelInfo()),
            channel = channel.toDomain(),
        )
    }

    /**
     * Transforms [ChannelUpdatedEventDto] to [ChannelUpdatedEvent].
     */
    private fun ChannelUpdatedEventDto.toDomain(): ChannelUpdatedEvent = with(domainMapping) {
        ChannelUpdatedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            message = message?.toDomain(channel.toChannelInfo()),
            channel = channel.toDomain(),
        )
    }

    /**
     * Transforms [ChannelUpdatedByUserEventDto] to [ChannelUpdatedByUserEvent].
     */
    private fun ChannelUpdatedByUserEventDto.toDomain(): ChannelUpdatedByUserEvent = with(domainMapping) {
        ChannelUpdatedByUserEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            user = user.toDomain(),
            message = message?.toDomain(channel.toChannelInfo()),
            channel = channel.toDomain(),
        )
    }

    /**
     * Transforms [ChannelVisibleEventDto] to [ChannelVisibleEvent].
     */
    private fun ChannelVisibleEventDto.toDomain(): ChannelVisibleEvent = with(domainMapping) {
        ChannelVisibleEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            user = user.toDomain(),
            channel = channel.toDomain(),
        )
    }

    private fun HealthEventDto.toDomain(): HealthEvent = HealthEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        connectionId = connection_id,
    )

    /**
     * Transforms [MemberAddedEventDto] to [MemberAddedEvent].
     */
    private fun MemberAddedEventDto.toDomain(): MemberAddedEvent = with(domainMapping) {
        MemberAddedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            member = member.toDomain(),
        )
    }

    /**
     * Transforms [MemberRemovedEventDto] to [MemberRemovedEvent].
     */
    private fun MemberRemovedEventDto.toDomain(): MemberRemovedEvent = with(domainMapping) {
        MemberRemovedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            member = member.toDomain(),
        )
    }

    private fun MemberUpdatedEventDto.toDomain(): MemberUpdatedEvent = with(domainMapping) {
        MemberUpdatedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            member = member.toDomain(),
        )
    }

    /**
     * Transforms [MessageDeletedEventDto] to [MessageDeletedEvent].
     */
    private fun MessageDeletedEventDto.toDomain(): MessageDeletedEvent = with(domainMapping) {
        MessageDeletedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user?.toDomain(),
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            message = message.toDomain(),
            hardDelete = hard_delete ?: false,
            channelMessageCount = channel_message_count,
            deletedForMe = deleted_for_me ?: false,
        )
    }

    /**
     * Transforms [MessageReadEventDto] to [MessageReadEvent].
     */
    private fun MessageReadEventDto.toDomain(): MessageReadEvent = with(domainMapping) {
        MessageReadEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            thread = thread?.toDomain(),
            lastReadMessageId = last_read_message_id,
        )
    }

    /**
     * Transforms [MessageUpdatedEventDto] to [MessageUpdatedEvent].
     */
    private fun MessageUpdatedEventDto.toDomain(): MessageUpdatedEvent = with(domainMapping) {
        MessageUpdatedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            message = message.toDomain(),
        )
    }

    /**
     * Transforms [NewMessageEventDto] to [NewMessageEvent].
     */
    private fun NewMessageEventDto.toDomain(): NewMessageEvent = with(domainMapping) {
        // build ChannelInfo from the event data, as it is not delivered within the `message` field
        val channelInfo = ChannelInfo(
            cid = cid,
            id = channel_id,
            type = channel_type,
            memberCount = channel_member_count ?: 0,
            name = channel_custom?.name,
            image = channel_custom?.image,
        )
        NewMessageEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            message = message.toDomain(channelInfo),
            watcherCount = watcher_count,
            totalUnreadCount = total_unread_count,
            unreadChannels = unread_channels,
            channelMessageCount = channel_message_count,
        )
    }

    /**
     * Transforms [NotificationAddedToChannelEventDto] to [NotificationAddedToChannelEvent].
     */
    private fun NotificationAddedToChannelEventDto.toDomain(): NotificationAddedToChannelEvent = with(domainMapping) {
        NotificationAddedToChannelEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            channel = channel.toDomain(),
            member = member.toDomain(),
            totalUnreadCount = total_unread_count,
            unreadChannels = unread_channels,
        )
    }

    /**
     * Transforms [NotificationChannelDeletedEventDto] to [NotificationChannelDeletedEvent].
     */
    private fun NotificationChannelDeletedEventDto.toDomain(): NotificationChannelDeletedEvent = with(domainMapping) {
        NotificationChannelDeletedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            channel = channel.toDomain(),
            totalUnreadCount = total_unread_count,
            unreadChannels = unread_channels,
        )
    }

    /**
     * Transforms [NotificationChannelMutesUpdatedEventDto] to [NotificationChannelMutesUpdatedEvent].
     */
    private fun NotificationChannelMutesUpdatedEventDto.toDomain(): NotificationChannelMutesUpdatedEvent = with(domainMapping) {
        NotificationChannelMutesUpdatedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            me = me.toDomain(),
        )
    }

    /**
     * Transforms [NotificationChannelTruncatedEventDto] to [NotificationChannelTruncatedEvent].
     */
    private fun NotificationChannelTruncatedEventDto.toDomain(): NotificationChannelTruncatedEvent = with(domainMapping) {
        NotificationChannelTruncatedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            channel = channel.toDomain(),
            totalUnreadCount = total_unread_count,
            unreadChannels = unread_channels,
        )
    }

    /**
     * Transforms [NotificationInviteAcceptedEventDto] to [NotificationInviteAcceptedEvent].
     */
    private fun NotificationInviteAcceptedEventDto.toDomain(): NotificationInviteAcceptedEvent = with(domainMapping) {
        NotificationInviteAcceptedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            user = user.toDomain(),
            member = member.toDomain(),
            channel = channel.toDomain(),
        )
    }

    /**
     * Transforms [NotificationInviteRejectedEventDto] to [NotificationInviteRejectedEvent].
     */
    private fun NotificationInviteRejectedEventDto.toDomain(): NotificationInviteRejectedEvent = with(domainMapping) {
        NotificationInviteRejectedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            user = user.toDomain(),
            member = member.toDomain(),
            channel = channel.toDomain(),
        )
    }

    /**
     * Transforms [NotificationInvitedEventDto] to [NotificationInvitedEvent].
     */
    private fun NotificationInvitedEventDto.toDomain(): NotificationInvitedEvent = with(domainMapping) {
        NotificationInvitedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            user = user.toDomain(),
            member = member.toDomain(),
        )
    }

    /**
     * Transforms [NotificationMarkReadEventDto] to [NotificationMarkReadEvent].
     */
    private fun NotificationMarkReadEventDto.toDomain(): NotificationMarkReadEvent = with(domainMapping) {
        NotificationMarkReadEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            totalUnreadCount = total_unread_count,
            unreadChannels = unread_channels,
            threadId = thread_id,
            thread = thread?.toDomain(),
            unreadThreads = unread_threads,
            unreadThreadMessages = unread_thread_messages,
            lastReadMessageId = last_read_message_id,
        )
    }

    /**
     * Transforms [NotificationMarkUnreadEventDto] to [NotificationMarkUnreadEvent].
     */
    private fun NotificationMarkUnreadEventDto.toDomain(): NotificationMarkUnreadEvent = with(domainMapping) {
        NotificationMarkUnreadEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
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
        )
    }

    /**
     * Transforms [MarkAllReadEventDto] to [MarkAllReadEvent].
     */
    private fun MarkAllReadEventDto.toDomain(): MarkAllReadEvent = with(domainMapping) {
        MarkAllReadEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
            totalUnreadCount = total_unread_count,
            unreadChannels = unread_channels,
        )
    }

    /**
     * Transforms [NotificationMessageNewEventDto] to [NotificationMessageNewEvent].
     */
    private fun NotificationMessageNewEventDto.toDomain(): NotificationMessageNewEvent = with(domainMapping) {
        NotificationMessageNewEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            channel = channel.toDomain(),
            message = message.toDomain(channel.toChannelInfo()),
            totalUnreadCount = total_unread_count,
            unreadChannels = unread_channels,
        )
    }

    /**
     * Transforms [NotificationThreadMessageNewEventDto] to [NotificationThreadMessageNewEvent].
     */
    private fun NotificationThreadMessageNewEventDto.toDomain(): NotificationThreadMessageNewEvent = with(domainMapping) {
        NotificationThreadMessageNewEvent(
            type = type,
            cid = cid,
            channelId = channel_id,
            channelType = channel_type,
            message = message.toDomain(channel.toChannelInfo()),
            channel = channel.toDomain(),
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            unreadThreads = unread_threads,
            unreadThreadMessages = unread_thread_messages,
        )
    }

    /**
     * Transforms [NotificationMutesUpdatedEventDto] to [NotificationMutesUpdatedEvent].
     */
    private fun NotificationMutesUpdatedEventDto.toDomain(): NotificationMutesUpdatedEvent = with(domainMapping) {
        NotificationMutesUpdatedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            me = me.toDomain(),
        )
    }

    /**
     * Transforms [NotificationRemovedFromChannelEventDto] to [NotificationRemovedFromChannelEvent].
     */
    private fun NotificationRemovedFromChannelEventDto.toDomain(): NotificationRemovedFromChannelEvent = with(domainMapping) {
        NotificationRemovedFromChannelEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user?.toDomain(),
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            channel = channel.toDomain(),
            member = member.toDomain(),
        )
    }

    /**
     * Transforms [ReactionDeletedEventDto] to [ReactionDeletedEvent].
     */
    private fun ReactionDeletedEventDto.toDomain(): ReactionDeletedEvent = with(domainMapping) {
        ReactionDeletedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            message = message.toDomain(),
            reaction = reaction.toDomain(),
        )
    }

    /**
     * Transforms [ReactionNewEventDto] to [ReactionNewEvent].
     */
    private fun ReactionNewEventDto.toDomain(): ReactionNewEvent = with(domainMapping) {
        ReactionNewEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            message = message.toDomain(),
            reaction = reaction.toDomain(),
        )
    }

    /**
     * Transforms [ReactionUpdateEventDto] to [ReactionUpdateEvent].
     */
    private fun ReactionUpdateEventDto.toDomain(): ReactionUpdateEvent = with(domainMapping) {
        ReactionUpdateEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            message = message.toDomain(),
            reaction = reaction.toDomain(),
        )
    }

    /**
     * Transforms [TypingStartEventDto] to [TypingStartEvent].
     */
    private fun TypingStartEventDto.toDomain(): TypingStartEvent = with(domainMapping) {
        TypingStartEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            parentId = parent_id,
        )
    }

    /**
     * Transforms [TypingStopEventDto] to [TypingStopEvent].
     */
    private fun TypingStopEventDto.toDomain(): TypingStopEvent = with(domainMapping) {
        TypingStopEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            parentId = parent_id,
        )
    }

    /**
     * Transforms [ChannelUserBannedEventDto] to [ChannelUserBannedEvent].
     */
    private fun ChannelUserBannedEventDto.toDomain(): ChannelUserBannedEvent = with(domainMapping) {
        ChannelUserBannedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            user = user.toDomain(),
            expiration = expiration,
            shadow = shadow ?: false,
        )
    }

    /**
     * Transforms [GlobalUserBannedEventDto] to [GlobalUserBannedEvent].
     */
    private fun GlobalUserBannedEventDto.toDomain(): GlobalUserBannedEvent = with(domainMapping) {
        GlobalUserBannedEvent(
            type = type,
            user = user.toDomain(),
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
        )
    }

    /**
     * Transforms [UserDeletedEventDto] to [UserDeletedEvent].
     */
    private fun UserDeletedEventDto.toDomain(): UserDeletedEvent = with(domainMapping) {
        UserDeletedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
        )
    }

    /**
     * Transforms [UserPresenceChangedEventDto] to [UserPresenceChangedEvent].
     */
    private fun UserPresenceChangedEventDto.toDomain(): UserPresenceChangedEvent = with(domainMapping) {
        UserPresenceChangedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
        )
    }

    /**
     * Transforms [UserStartWatchingEventDto] to [UserStartWatchingEvent].
     */
    private fun UserStartWatchingEventDto.toDomain(): UserStartWatchingEvent = with(domainMapping) {
        UserStartWatchingEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            watcherCount = watcher_count,
            channelType = channel_type,
            channelId = channel_id,
            user = user.toDomain(),
        )
    }

    /**
     * Transforms [UserStopWatchingEventDto] to [UserStopWatchingEvent].
     */
    private fun UserStopWatchingEventDto.toDomain(): UserStopWatchingEvent = with(domainMapping) {
        UserStopWatchingEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            watcherCount = watcher_count,
            channelType = channel_type,
            channelId = channel_id,
            user = user.toDomain(),
        )
    }

    /**
     * Transforms [ChannelUserUnbannedEventDto] to [ChannelUserUnbannedEvent].
     */
    private fun ChannelUserUnbannedEventDto.toDomain(): ChannelUserUnbannedEvent = with(domainMapping) {
        ChannelUserUnbannedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
        )
    }

    /**
     * Transforms [GlobalUserUnbannedEventDto] to [GlobalUserUnbannedEvent].
     */
    private fun GlobalUserUnbannedEventDto.toDomain(): GlobalUserUnbannedEvent = with(domainMapping) {
        GlobalUserUnbannedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
        )
    }

    /**
     * Transforms [UserUpdatedEventDto] to [UserUpdatedEvent].
     */
    private fun UserUpdatedEventDto.toDomain(): UserUpdatedEvent = with(domainMapping) {
        UserUpdatedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
        )
    }

    /**
     * Transforms [PollClosedEventDto] to [PollClosedEvent].
     */
    private fun PollClosedEventDto.toDomain(): PollClosedEvent = with(domainMapping) {
        val newPoll = poll.toDomain()
        val (channelType, channelId) = cid.cidToTypeAndId()
        return PollClosedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = message_id,
            poll = newPoll,
        )
    }

    /**
     * Transforms [PollDeletedEventDto] to [PollDeletedEvent].
     */
    private fun PollDeletedEventDto.toDomain(): PollDeletedEvent = with(domainMapping) {
        val newPoll = poll.toDomain()
        val (channelType, channelId) = cid.cidToTypeAndId()
        return PollDeletedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = message_id,
            poll = newPoll,
        )
    }

    /**
     * Transforms [PollUpdatedEventDto] to [PollUpdatedEvent].
     */
    private fun PollUpdatedEventDto.toDomain(): PollUpdatedEvent = with(domainMapping) {
        val newPoll = poll.toDomain()
        val (channelType, channelId) = cid.cidToTypeAndId()
        return PollUpdatedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = message_id,
            poll = newPoll,
        )
    }

    /**
     * Transforms [VoteCastedEventDto] to [VoteCastedEvent].
     */
    private fun VoteCastedEventDto.toDomain(): VoteCastedEvent = with(domainMapping) {
        val pollVote = poll_vote.toDomain()
        val (channelType, channelId) = cid.cidToTypeAndId()
        val newPoll = poll.toDomain()
            .let { poll ->
                pollVote.takeIf { it.user?.id == currentUserIdProvider() }
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
            messageId = message_id,
            poll = newPoll,
            newVote = pollVote,
        )
    }

    /**
     * Transforms [AnswerCastedEventDto] to [AnswerCastedEvent].
     */
    private fun AnswerCastedEventDto.toDomain(): AnswerCastedEvent = with(domainMapping) {
        val newAnswer = poll_vote.toAnswerDomain()
        val (channelType, channelId) = cid.cidToTypeAndId()
        return AnswerCastedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = message_id,
            poll = poll.toDomain(),
            newAnswer = newAnswer,
        )
    }

    /**
     * Transforms [VoteChangedEventDto] to [VoteChangedEvent].
     */
    private fun VoteChangedEventDto.toDomain(): VoteChangedEvent = with(domainMapping) {
        val pollVote = poll_vote.toDomain()
        val (channelType, channelId) = cid.cidToTypeAndId()
        val newPoll = poll.toDomain()
            .let { poll ->
                pollVote.takeIf { it.user?.id == currentUserIdProvider() }
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
            messageId = message_id,
            poll = newPoll,
            newVote = pollVote,
        )
    }

    /**
     * Transforms [VoteRemovedEventDto] to [VoteRemovedEvent].
     */
    private fun VoteRemovedEventDto.toDomain(): VoteRemovedEvent = with(domainMapping) {
        val removedVote = poll_vote.toDomain()
        val (channelType, channelId) = cid.cidToTypeAndId()
        val newPoll = poll.toDomain()
        return VoteRemovedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = message_id,
            poll = newPoll,
            removedVote = removedVote,
        )
    }

    /**
     * Transforms [DraftMessageUpdatedEventDto] to [DraftMessageUpdatedEvent].
     */
    private fun DraftMessageUpdatedEventDto.toDomain(): DraftMessageUpdatedEvent = with(domainMapping) {
        return DraftMessageUpdatedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            draftMessage = draft.toDomain(),
        )
    }

    /**
     * Transforms [DraftMessageDeletedEventDto] to [DraftMessageDeletedEvent].
     */
    private fun DraftMessageDeletedEventDto.toDomain(): DraftMessageDeletedEvent = with(domainMapping) {
        return DraftMessageDeletedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            draftMessage = draft.toDomain(),
        )
    }

    /**
     * Transforms [ReminderCreatedEventDto] to [ReminderCreatedEvent].
     */
    private fun ReminderCreatedEventDto.toDomain(): ReminderCreatedEvent = with(domainMapping) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        ReminderCreatedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = message_id,
            userId = user_id,
            reminder = reminder.toDomain(),
        )
    }

    /**
     * Transforms [ReminderUpdatedEventDto] to [ReminderUpdatedEvent].
     */
    private fun ReminderUpdatedEventDto.toDomain(): ReminderUpdatedEvent = with(domainMapping) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        ReminderUpdatedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = message_id,
            userId = user_id,
            reminder = reminder.toDomain(),
        )
    }

    /**
     * Transforms [ReminderDeletedEventDto] to [ReminderDeletedEvent].
     */
    private fun ReminderDeletedEventDto.toDomain(): ReminderDeletedEvent = with(domainMapping) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        ReminderDeletedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = message_id,
            userId = user_id,
            reminder = reminder.toDomain(),
        )
    }

    /**
     * Transforms [NotificationReminderDueEventDto] to [NotificationReminderDueEvent].
     */
    private fun NotificationReminderDueEventDto.toDomain(): NotificationReminderDueEvent = with(domainMapping) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        NotificationReminderDueEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = message_id,
            userId = user_id,
            reminder = reminder.toDomain(),
        )
    }

    private fun UserMessagesDeletedEventDto.toDomain(): UserMessagesDeletedEvent = with(domainMapping) {
        return UserMessagesDeletedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            channelType = channel_type,
            channelId = channel_id,
            user = user.toDomain(),
            hardDelete = hard_delete == true,
        )
    }

    /**
     * Transforms [AIIndicatorUpdatedEventDto] to [AIIndicatorUpdatedEvent].
     */
    private fun AIIndicatorUpdatedEventDto.toDomain(): AIIndicatorUpdatedEvent = with(domainMapping) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        return AIIndicatorUpdatedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            user = user.toDomain(),
            channelType = channelType,
            channelId = channelId,
            aiState = ai_state,
            messageId = message_id,
        )
    }

    /**
     * Transforms [AIIndicatorClearEventDto] to [AIIndicatorClearEvent].
     */
    private fun AIIndicatorClearEventDto.toDomain(): AIIndicatorClearEvent = with(domainMapping) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        return AIIndicatorClearEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user.toDomain(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
        )
    }

    /**
     * Transforms [AIIndicatorStopEventDto] to [AIIndicatorStopEvent].
     */
    private fun AIIndicatorStopEventDto.toDomain(): AIIndicatorStopEvent = with(domainMapping) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        return AIIndicatorStopEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            cid = cid,
            user = user.toDomain(),
            channelType = channelType,
            channelId = channelId,
        )
    }

    /**
     * Transforms [ConnectedEventDto] to [ConnectedEvent].
     */
    private fun ConnectedEventDto.toDomain(): ConnectedEvent = with(domainMapping) {
        ConnectedEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            me = me.toDomain(),
            connectionId = connection_id,
        )
    }

    private fun ConnectionErrorEventDto.toDomain(): ConnectionErrorEvent = ConnectionErrorEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        connectionId = connection_id,
        error = error.toDomain(),
    )

    private fun ConnectingEventDto.toDomain(): ConnectingEvent = ConnectingEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
    )

    private fun DisconnectedEventDto.toDomain(): DisconnectedEvent = DisconnectedEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
    )

    private fun ErrorEventDto.toDomain(): ErrorEvent = ErrorEvent(
        type = type,
        createdAt = created_at.date,
        rawCreatedAt = created_at.rawDate,
        error = error,
    )

    /**
     * Transforms [UnknownEventDto] to [UnknownEvent].
     */
    private fun UnknownEventDto.toDomain(): UnknownEvent = with(domainMapping) {
        UnknownEvent(
            type = type,
            createdAt = created_at.date,
            rawCreatedAt = created_at.rawDate,
            user = user?.toDomain(),
            rawData = rawData,
        )
    }
}
