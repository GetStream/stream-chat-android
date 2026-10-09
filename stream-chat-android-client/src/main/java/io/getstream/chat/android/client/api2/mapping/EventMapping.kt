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

@file:Suppress("TooManyFunctions")

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.ChatEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectingEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectionErrorEventDto
import io.getstream.chat.android.client.api2.model.dto.DisconnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.ErrorEventDto
import io.getstream.chat.android.client.api2.model.dto.GeneratedEventDto
import io.getstream.chat.android.client.api2.model.dto.HealthEventDto
import io.getstream.chat.android.client.api2.model.dto.UnknownEventDto
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
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.network.infrastructure.ExactDate
import io.getstream.chat.android.network.models.PollResponseData
import io.getstream.chat.android.network.models.PollVoteResponseData
import io.getstream.chat.android.network.models.WSEvent
import io.getstream.chat.android.network.models.AIIndicatorClearEvent as GeneratedAIIndicatorClearEvent
import io.getstream.chat.android.network.models.AIIndicatorStopEvent as GeneratedAIIndicatorStopEvent
import io.getstream.chat.android.network.models.AIIndicatorUpdateEvent as GeneratedAIIndicatorUpdateEvent
import io.getstream.chat.android.network.models.ChannelDeletedEvent as GeneratedChannelDeletedEvent
import io.getstream.chat.android.network.models.ChannelHiddenEvent as GeneratedChannelHiddenEvent
import io.getstream.chat.android.network.models.ChannelTruncatedEvent as GeneratedChannelTruncatedEvent
import io.getstream.chat.android.network.models.ChannelUpdatedEvent as GeneratedChannelUpdatedEvent
import io.getstream.chat.android.network.models.ChannelVisibleEvent as GeneratedChannelVisibleEvent
import io.getstream.chat.android.network.models.DraftDeletedEvent as GeneratedDraftDeletedEvent
import io.getstream.chat.android.network.models.DraftUpdatedEvent as GeneratedDraftUpdatedEvent
import io.getstream.chat.android.network.models.MemberAddedEvent as GeneratedMemberAddedEvent
import io.getstream.chat.android.network.models.MemberRemovedEvent as GeneratedMemberRemovedEvent
import io.getstream.chat.android.network.models.MemberUpdatedEvent as GeneratedMemberUpdatedEvent
import io.getstream.chat.android.network.models.MessageDeletedEvent as GeneratedMessageDeletedEvent
import io.getstream.chat.android.network.models.MessageDeliveredEvent as GeneratedMessageDeliveredEvent
import io.getstream.chat.android.network.models.MessageNewEvent as GeneratedMessageNewEvent
import io.getstream.chat.android.network.models.MessageReadEvent as GeneratedMessageReadEvent
import io.getstream.chat.android.network.models.MessageUpdatedEvent as GeneratedMessageUpdatedEvent
import io.getstream.chat.android.network.models.NotificationAddedToChannelEvent as GeneratedNotificationAddedToChannelEvent
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
import io.getstream.chat.android.network.models.ReactionDeletedEvent as GeneratedReactionDeletedEvent
import io.getstream.chat.android.network.models.ReactionNewEvent as GeneratedReactionNewEvent
import io.getstream.chat.android.network.models.ReactionUpdatedEvent as GeneratedReactionUpdatedEvent
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
    internal fun ChatEventDto.toDomain(): ChatEvent {
        return when (this) {
            is ConnectedEventDto -> toDomain()
            is ConnectionErrorEventDto -> toDomain()
            is ConnectingEventDto -> toDomain()
            is DisconnectedEventDto -> toDomain()
            is ErrorEventDto -> toDomain()
            is HealthEventDto -> toDomain()
            is UnknownEventDto -> toDomain()
            is GeneratedEventDto -> event.toDomain()
        }
    }

    /**
     * Transforms the generated [GeneratedChannelHiddenEvent] to [ChannelHiddenEvent].
     */
    private fun GeneratedChannelHiddenEvent.toDomain(): ChannelHiddenEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return ChannelHiddenEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            user = requireNotNull(user).toDomain(),
            channel = channel.toDomain(),
            clearHistory = clearHistory ?: false,
        )
    }

    /**
     * Transforms the generated [GeneratedChannelVisibleEvent] to [ChannelVisibleEvent].
     */
    private fun GeneratedChannelVisibleEvent.toDomain(): ChannelVisibleEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return ChannelVisibleEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            user = requireNotNull(user).toDomain(),
            channel = channel?.toDomain() ?: Channel(id = channelId, type = channelType),
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
     * Transforms the generated [GeneratedMessageDeletedEvent] to [MessageDeletedEvent].
     */
    private fun GeneratedMessageDeletedEvent.toDomain(): MessageDeletedEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return MessageDeletedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = user?.toDomain(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            message = message.toDomain(),
            hardDelete = hardDelete ?: false,
            channelMessageCount = channelMessageCount,
            deletedForMe = deletedForMe ?: false,
        )
    }

    /**
     * Transforms the generated [GeneratedMessageDeliveredEvent] to [MessageDeliveredEvent].
     */
    private fun GeneratedMessageDeliveredEvent.toDomain(): MessageDeliveredEvent = with(domainMapping) {
        MessageDeliveredEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = requireNotNull(user).toDomain(),
            cid = requireNotNull(cid),
            channelType = requireNotNull(channelType),
            channelId = requireNotNull(channelId),
            lastDeliveredAt = requireNotNull(lastDeliveredAt?.let(ExactDate::parseOrNull)).date,
            lastDeliveredMessageId = requireNotNull(lastDeliveredMessageId),
        )
    }

    /**
     * Transforms the generated [GeneratedMessageReadEvent] to [MessageReadEvent].
     */
    private fun GeneratedMessageReadEvent.toDomain(): MessageReadEvent = with(domainMapping) {
        MessageReadEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = requireNotNull(user).toDomain(),
            cid = requireNotNull(cid),
            channelType = requireNotNull(channelType),
            channelId = requireNotNull(channelId),
            thread = thread?.toDomain(),
            lastReadMessageId = lastReadMessageId,
            team = team,
        )
    }

    /**
     * Transforms the generated [GeneratedMessageUpdatedEvent] to [MessageUpdatedEvent].
     */
    private fun GeneratedMessageUpdatedEvent.toDomain(): MessageUpdatedEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return MessageUpdatedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = requireNotNull(user).toDomain(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            message = message.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedMessageNewEvent] to [NewMessageEvent].
     */
    private fun GeneratedMessageNewEvent.toDomain(): NewMessageEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        // The message carries no channel, so its channel info comes from the event.
        val channelInfo = ChannelInfo(
            cid = cid,
            id = channelId,
            type = channelType,
            memberCount = channelMemberCount ?: 0,
            name = channelCustom?.get("name") as? String,
            image = channelCustom?.get("image") as? String,
        )
        return NewMessageEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = requireNotNull(user).toDomain(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            message = message.toDomain(channelInfo),
            watcherCount = watcherCount ?: 0,
            totalUnreadCount = totalUnreadCount ?: 0,
            unreadChannels = unreadChannels ?: 0,
            channelMessageCount = channelMessageCount,
            groupedUnreadChannels = groupedUnreadChannels,
        )
    }

    /**
     * Transforms the generated [GeneratedNotificationAddedToChannelEvent] to [NotificationAddedToChannelEvent].
     */
    private fun GeneratedNotificationAddedToChannelEvent.toDomain(): NotificationAddedToChannelEvent =
        with(domainMapping) {
            val cid = requireNotNull(cid)
            val (channelType, channelId) = cid.cidToTypeAndId()
            return NotificationAddedToChannelEvent(
                type = type,
                createdAt = createdAt.date,
                rawCreatedAt = createdAt.raw,
                cid = cid,
                channelType = channelType,
                channelId = channelId,
                channel = channel.toDomain(),
                member = member.toDomain(),
                totalUnreadCount = totalUnreadCount ?: 0,
                unreadChannels = unreadChannels ?: 0,
            )
        }

    /**
     * Transforms the generated [GeneratedNotificationMarkReadEvent] to [NotificationMarkReadEvent], or to
     * [MarkAllReadEvent] when it carries no channel, which is how the backend sends a mark-all-read.
     */
    private fun GeneratedNotificationMarkReadEvent.toDomain(): ChatEvent = with(domainMapping) {
        val user = requireNotNull(user).toDomain()
        val cid = cid ?: return MarkAllReadEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = user,
            totalUnreadCount = totalUnreadCount,
            unreadChannels = unreadChannels,
            groupedUnreadChannels = groupedUnreadChannels,
        )
        NotificationMarkReadEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = user,
            cid = cid,
            channelType = requireNotNull(channelType),
            channelId = requireNotNull(channelId),
            totalUnreadCount = totalUnreadCount,
            unreadChannels = unreadChannels,
            threadId = threadId,
            thread = thread?.toDomain(),
            unreadThreads = unreadThreads,
            unreadThreadMessages = unreadThreadMessages,
            lastReadMessageId = lastReadMessageId,
            groupedUnreadChannels = groupedUnreadChannels,
        )
    }

    /**
     * Transforms the generated [GeneratedNotificationNewMessageEvent] to [NotificationMessageNewEvent].
     */
    private fun GeneratedNotificationNewMessageEvent.toDomain(): NotificationMessageNewEvent = with(domainMapping) {
        NotificationMessageNewEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = requireNotNull(cid),
            channelType = requireNotNull(channelType),
            channelId = requireNotNull(channelId),
            channel = channel.toDomain(),
            message = message.toDomain(channel.toChannelInfo()),
            totalUnreadCount = totalUnreadCount ?: 0,
            unreadChannels = unreadChannels ?: 0,
            groupedUnreadChannels = groupedUnreadChannels,
        )
    }

    /**
     * Transforms the generated [GeneratedThreadUpdatedEvent] to [ThreadUpdatedEvent].
     */
    private fun GeneratedThreadUpdatedEvent.toDomain(): ThreadUpdatedEvent = with(domainMapping) {
        ThreadUpdatedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = requireNotNull(cid),
            channelType = requireNotNull(channelType),
            channelId = requireNotNull(channelId),
            thread = requireNotNull(thread).toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedUserDeletedEvent] to [UserDeletedEvent].
     */
    private fun GeneratedUserDeletedEvent.toDomain(): UserDeletedEvent = with(domainMapping) {
        UserDeletedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = user.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedUserUpdatedEvent] to [UserUpdatedEvent].
     */
    private fun GeneratedUserUpdatedEvent.toDomain(): UserUpdatedEvent = with(domainMapping) {
        UserUpdatedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = user.toDomain(),
        )
    }

    /**
     * Transforms an event parsed with its generated model. The event adapter only produces the types
     * handled here, and rejects an event missing a field its domain event requires.
     */
    private fun WSEvent.toDomain(): ChatEvent = when (this) {
        is GeneratedUserWatchingStartEvent -> toDomain()
        is GeneratedUserWatchingStopEvent -> toDomain()
        is GeneratedUserPresenceChangedEvent -> toDomain()
        is GeneratedUserBannedEvent -> toDomain()
        is GeneratedUserUnbannedEvent -> toDomain()
        is GeneratedMemberAddedEvent -> toDomain()
        is GeneratedMemberRemovedEvent -> toDomain()
        is GeneratedMemberUpdatedEvent -> toDomain()
        is GeneratedTypingStartEvent -> toDomain()
        is GeneratedTypingStopEvent -> toDomain()
        is GeneratedNotificationInvitedEvent -> toDomain()
        is GeneratedNotificationInviteAcceptedEvent -> toDomain()
        is GeneratedNotificationInviteRejectedEvent -> toDomain()
        is GeneratedNotificationChannelDeletedEvent -> toDomain()
        is GeneratedNotificationChannelTruncatedEvent -> toDomain()
        is GeneratedNotificationThreadMessageNewEvent -> toDomain()
        is GeneratedNotificationMarkUnreadEvent -> toDomain()
        is GeneratedNotificationMutesUpdatedEvent -> toDomain()
        is GeneratedNotificationChannelMutesUpdatedEvent -> toDomain()
        is GeneratedPollClosedEvent -> toDomain()
        is GeneratedPollDeletedEvent -> toDomain()
        is GeneratedPollUpdatedEvent -> toDomain()
        is GeneratedPollVoteCastedEvent -> toDomain()
        is GeneratedPollVoteChangedEvent -> toDomain()
        is GeneratedPollVoteRemovedEvent -> toDomain()
        is GeneratedReminderCreatedEvent -> toDomain()
        is GeneratedReminderUpdatedEvent -> toDomain()
        is GeneratedReminderDeletedEvent -> toDomain()
        is GeneratedReminderNotificationEvent -> toDomain()
        is GeneratedNotificationNewMessageEvent -> toDomain()
        is GeneratedThreadUpdatedEvent -> toDomain()
        is GeneratedUserDeletedEvent -> toDomain()
        is GeneratedMessageNewEvent -> toDomain()
        is GeneratedMessageReadEvent -> toDomain()
        is GeneratedMessageDeliveredEvent -> toDomain()
        is GeneratedMessageDeletedEvent -> toDomain()
        is GeneratedMessageUpdatedEvent -> toDomain()
        is GeneratedNotificationMarkReadEvent -> toDomain()
        is GeneratedChannelUpdatedEvent -> toDomain()
        is GeneratedChannelTruncatedEvent -> toDomain()
        is GeneratedChannelDeletedEvent -> toDomain()
        is GeneratedChannelHiddenEvent -> toDomain()
        is GeneratedChannelVisibleEvent -> toDomain()
        is GeneratedNotificationRemovedFromChannelEvent -> toDomain()
        is GeneratedUserUpdatedEvent -> toDomain()
        is GeneratedUserMessagesDeletedEvent -> toDomain()
        is GeneratedDraftUpdatedEvent -> toDomain()
        is GeneratedDraftDeletedEvent -> toDomain()
        is GeneratedReactionDeletedEvent -> toDomain()
        is GeneratedReactionNewEvent -> toDomain()
        is GeneratedReactionUpdatedEvent -> toDomain()
        is GeneratedNotificationAddedToChannelEvent -> toDomain()
        is GeneratedAIIndicatorUpdateEvent -> toDomain()
        is GeneratedAIIndicatorClearEvent -> toDomain()
        is GeneratedAIIndicatorStopEvent -> toDomain()
        else -> error("No mapping for the generated ${getWSEventType()} event")
    }

    /**
     * Transforms the generated [GeneratedUserWatchingStartEvent] to [UserStartWatchingEvent].
     */
    private fun GeneratedUserWatchingStartEvent.toDomain(): UserStartWatchingEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return UserStartWatchingEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            watcherCount = watcherCount,
            channelType = channelType,
            channelId = channelId,
            user = user.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedUserWatchingStopEvent] to [UserStopWatchingEvent].
     */
    private fun GeneratedUserWatchingStopEvent.toDomain(): UserStopWatchingEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return UserStopWatchingEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            watcherCount = watcherCount,
            channelType = channelType,
            channelId = channelId,
            user = user.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedUserPresenceChangedEvent] to [UserPresenceChangedEvent].
     */
    private fun GeneratedUserPresenceChangedEvent.toDomain(): UserPresenceChangedEvent = with(domainMapping) {
        UserPresenceChangedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = user.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedUserBannedEvent] to [ChannelUserBannedEvent] when it carries a cid, or
     * to [GlobalUserBannedEvent] otherwise.
     */
    private fun GeneratedUserBannedEvent.toDomain(): ChatEvent = with(domainMapping) {
        when (val cid = cid) {
            null -> GlobalUserBannedEvent(
                type = type,
                user = user.toDomain(),
                createdAt = createdAt.date,
                rawCreatedAt = createdAt.raw,
            )
            else -> {
                val (channelType, channelId) = cid.cidToTypeAndId()
                ChannelUserBannedEvent(
                    type = type,
                    createdAt = createdAt.date,
                    rawCreatedAt = createdAt.raw,
                    cid = cid,
                    channelType = channelType,
                    channelId = channelId,
                    user = user.toDomain(),
                    expiration = expiration,
                    shadow = shadow ?: false,
                )
            }
        }
    }

    /**
     * Transforms the generated [GeneratedUserUnbannedEvent] to [ChannelUserUnbannedEvent] when it carries a cid,
     * or to [GlobalUserUnbannedEvent] otherwise.
     */
    private fun GeneratedUserUnbannedEvent.toDomain(): ChatEvent = with(domainMapping) {
        when (val cid = cid) {
            null -> GlobalUserUnbannedEvent(
                type = type,
                createdAt = createdAt.date,
                rawCreatedAt = createdAt.raw,
                user = user.toDomain(),
            )
            else -> {
                val (channelType, channelId) = cid.cidToTypeAndId()
                ChannelUserUnbannedEvent(
                    type = type,
                    createdAt = createdAt.date,
                    rawCreatedAt = createdAt.raw,
                    user = user.toDomain(),
                    cid = cid,
                    channelType = channelType,
                    channelId = channelId,
                )
            }
        }
    }

    /**
     * Transforms the generated [GeneratedMemberAddedEvent] to [MemberAddedEvent].
     */
    private fun GeneratedMemberAddedEvent.toDomain(): MemberAddedEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return MemberAddedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = requireNotNull(user).toDomain(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            member = member.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedMemberRemovedEvent] to [MemberRemovedEvent].
     */
    private fun GeneratedMemberRemovedEvent.toDomain(): MemberRemovedEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return MemberRemovedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = requireNotNull(user).toDomain(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            member = member.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedMemberUpdatedEvent] to [MemberUpdatedEvent].
     */
    private fun GeneratedMemberUpdatedEvent.toDomain(): MemberUpdatedEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return MemberUpdatedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = requireNotNull(user).toDomain(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            member = member.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedReactionDeletedEvent] to [ReactionDeletedEvent].
     */
    private fun GeneratedReactionDeletedEvent.toDomain(): ReactionDeletedEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return ReactionDeletedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = requireNotNull(user).toDomain(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            message = requireNotNull(message).toDomain(),
            reaction = requireNotNull(reaction).toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedReactionNewEvent] to [ReactionNewEvent].
     */
    private fun GeneratedReactionNewEvent.toDomain(): ReactionNewEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return ReactionNewEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = requireNotNull(user).toDomain(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            message = requireNotNull(message).toDomain(),
            reaction = requireNotNull(reaction).toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedReactionUpdatedEvent] to [ReactionUpdateEvent].
     */
    private fun GeneratedReactionUpdatedEvent.toDomain(): ReactionUpdateEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return ReactionUpdateEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = requireNotNull(user).toDomain(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            message = message.toDomain(),
            reaction = requireNotNull(reaction).toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedTypingStartEvent] to [TypingStartEvent].
     */
    private fun GeneratedTypingStartEvent.toDomain(): TypingStartEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return TypingStartEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = requireNotNull(user).toDomain(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            parentId = parentId,
            member = member?.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedTypingStopEvent] to [TypingStopEvent].
     */
    private fun GeneratedTypingStopEvent.toDomain(): TypingStopEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return TypingStopEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = requireNotNull(user).toDomain(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            parentId = parentId,
            member = member?.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedNotificationInvitedEvent] to [NotificationInvitedEvent].
     */
    private fun GeneratedNotificationInvitedEvent.toDomain(): NotificationInvitedEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return NotificationInvitedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            user = requireNotNull(user).toDomain(),
            member = member.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedNotificationInviteAcceptedEvent] to [NotificationInviteAcceptedEvent].
     */
    private fun GeneratedNotificationInviteAcceptedEvent.toDomain(): NotificationInviteAcceptedEvent =
        with(domainMapping) {
            val cid = requireNotNull(cid)
            val (channelType, channelId) = cid.cidToTypeAndId()
            return NotificationInviteAcceptedEvent(
                type = type,
                createdAt = createdAt.date,
                rawCreatedAt = createdAt.raw,
                cid = cid,
                channelType = channelType,
                channelId = channelId,
                user = requireNotNull(user).toDomain(),
                member = member.toDomain(),
                channel = channel.toDomain(),
            )
        }

    /**
     * Transforms the generated [GeneratedNotificationInviteRejectedEvent] to [NotificationInviteRejectedEvent].
     */
    private fun GeneratedNotificationInviteRejectedEvent.toDomain(): NotificationInviteRejectedEvent =
        with(domainMapping) {
            val cid = requireNotNull(cid)
            val (channelType, channelId) = cid.cidToTypeAndId()
            return NotificationInviteRejectedEvent(
                type = type,
                createdAt = createdAt.date,
                rawCreatedAt = createdAt.raw,
                cid = cid,
                channelType = channelType,
                channelId = channelId,
                user = requireNotNull(user).toDomain(),
                member = member.toDomain(),
                channel = channel.toDomain(),
            )
        }

    /**
     * Transforms the generated [GeneratedChannelUpdatedEvent] to [ChannelUpdatedByUserEvent] when a user made the
     * change, or to [ChannelUpdatedEvent] otherwise.
     */
    private fun GeneratedChannelUpdatedEvent.toDomain(): ChatEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        val channelInfo = channel.toChannelInfo()
        return when (val user = user) {
            null -> ChannelUpdatedEvent(
                type = type,
                createdAt = createdAt.date,
                rawCreatedAt = createdAt.raw,
                cid = cid,
                channelType = channelType,
                channelId = channelId,
                message = message?.toDomain(channelInfo),
                channel = channel.toDomain(),
            )
            else -> ChannelUpdatedByUserEvent(
                type = type,
                createdAt = createdAt.date,
                rawCreatedAt = createdAt.raw,
                cid = cid,
                channelType = channelType,
                channelId = channelId,
                user = user.toDomain(),
                message = message?.toDomain(channelInfo),
                channel = channel.toDomain(),
            )
        }
    }

    /**
     * Transforms the generated [GeneratedChannelTruncatedEvent] to [ChannelTruncatedEvent].
     */
    private fun GeneratedChannelTruncatedEvent.toDomain(): ChannelTruncatedEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return ChannelTruncatedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            user = user?.toDomain(),
            message = message?.toDomain(channel.toChannelInfo()),
            channel = channel.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedChannelDeletedEvent] to [ChannelDeletedEvent].
     */
    private fun GeneratedChannelDeletedEvent.toDomain(): ChannelDeletedEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return ChannelDeletedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            channel = channel.toDomain(),
            user = user?.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedNotificationRemovedFromChannelEvent] to [NotificationRemovedFromChannelEvent].
     */
    private fun GeneratedNotificationRemovedFromChannelEvent.toDomain(): NotificationRemovedFromChannelEvent =
        with(domainMapping) {
            val cid = requireNotNull(cid)
            val (channelType, channelId) = cid.cidToTypeAndId()
            return NotificationRemovedFromChannelEvent(
                type = type,
                createdAt = createdAt.date,
                rawCreatedAt = createdAt.raw,
                user = user?.toDomain(),
                cid = cid,
                channelType = channelType,
                channelId = channelId,
                channel = channel.toDomain(),
                member = member.toDomain(),
            )
        }

    /**
     * Transforms the generated [GeneratedNotificationChannelDeletedEvent] to [NotificationChannelDeletedEvent].
     */
    private fun GeneratedNotificationChannelDeletedEvent.toDomain(): NotificationChannelDeletedEvent =
        with(domainMapping) {
            val cid = requireNotNull(cid)
            val (channelType, channelId) = cid.cidToTypeAndId()
            return NotificationChannelDeletedEvent(
                type = type,
                createdAt = createdAt.date,
                rawCreatedAt = createdAt.raw,
                cid = cid,
                channelType = channelType,
                channelId = channelId,
                channel = channel.toDomain(),
                totalUnreadCount = totalUnreadCount ?: 0,
                unreadChannels = unreadChannels ?: 0,
                groupedUnreadChannels = groupedUnreadChannels,
            )
        }

    /**
     * Transforms the generated [GeneratedNotificationChannelTruncatedEvent] to [NotificationChannelTruncatedEvent].
     */
    private fun GeneratedNotificationChannelTruncatedEvent.toDomain(): NotificationChannelTruncatedEvent =
        with(domainMapping) {
            val cid = requireNotNull(cid)
            val (channelType, channelId) = cid.cidToTypeAndId()
            return NotificationChannelTruncatedEvent(
                type = type,
                createdAt = createdAt.date,
                rawCreatedAt = createdAt.raw,
                cid = cid,
                channelType = channelType,
                channelId = channelId,
                channel = channel.toDomain(),
                totalUnreadCount = totalUnreadCount ?: 0,
                unreadChannels = unreadChannels ?: 0,
                groupedUnreadChannels = groupedUnreadChannels,
            )
        }

    /**
     * Transforms the generated [GeneratedNotificationThreadMessageNewEvent] to [NotificationThreadMessageNewEvent].
     */
    private fun GeneratedNotificationThreadMessageNewEvent.toDomain(): NotificationThreadMessageNewEvent =
        with(domainMapping) {
            val cid = requireNotNull(cid)
            val (channelType, channelId) = cid.cidToTypeAndId()
            NotificationThreadMessageNewEvent(
                type = type,
                cid = cid,
                channelId = channelId,
                channelType = channelType,
                message = message.toDomain(channel.toChannelInfo()),
                channel = channel.toDomain(),
                createdAt = createdAt.date,
                rawCreatedAt = createdAt.raw,
                unreadThreads = requireNotNull(unreadThreads),
                unreadThreadMessages = requireNotNull(unreadThreadMessages),
            )
        }

    /**
     * Transforms the generated [GeneratedNotificationMarkUnreadEvent] to [NotificationMarkUnreadEvent].
     */
    private fun GeneratedNotificationMarkUnreadEvent.toDomain(): NotificationMarkUnreadEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return NotificationMarkUnreadEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = requireNotNull(user).toDomain(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            totalUnreadCount = totalUnreadCount ?: 0,
            unreadChannels = unreadChannels ?: 0,
            firstUnreadMessageId = requireNotNull(firstUnreadMessageId),
            lastReadMessageId = lastReadMessageId,
            lastReadMessageAt = requireNotNull(lastReadAt),
            unreadMessages = requireNotNull(unreadMessages),
            threadId = threadId,
            unreadThreads = unreadThreads ?: 0,
            groupedUnreadChannels = groupedUnreadChannels,
        )
    }

    /**
     * Transforms the generated [GeneratedNotificationMutesUpdatedEvent] to [NotificationMutesUpdatedEvent].
     */
    private fun GeneratedNotificationMutesUpdatedEvent.toDomain(): NotificationMutesUpdatedEvent = with(domainMapping) {
        NotificationMutesUpdatedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            me = me.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedNotificationChannelMutesUpdatedEvent] to
     * [NotificationChannelMutesUpdatedEvent].
     */
    private fun GeneratedNotificationChannelMutesUpdatedEvent.toDomain(): NotificationChannelMutesUpdatedEvent =
        with(domainMapping) {
            NotificationChannelMutesUpdatedEvent(
                type = type,
                createdAt = createdAt.date,
                rawCreatedAt = createdAt.raw,
                me = me.toDomain(),
            )
        }

    /**
     * Transforms the generated [GeneratedPollClosedEvent] to [PollClosedEvent].
     */
    private fun GeneratedPollClosedEvent.toDomain(): PollClosedEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return PollClosedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            poll = poll.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedPollDeletedEvent] to [PollDeletedEvent].
     */
    private fun GeneratedPollDeletedEvent.toDomain(): PollDeletedEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return PollDeletedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            poll = poll.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedPollUpdatedEvent] to [PollUpdatedEvent].
     */
    private fun GeneratedPollUpdatedEvent.toDomain(): PollUpdatedEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return PollUpdatedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            poll = poll.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedPollVoteCastedEvent] to [VoteCastedEvent], or to [AnswerCastedEvent]
     * when the vote is an answer.
     */
    private fun GeneratedPollVoteCastedEvent.toDomain(): ChatEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        if (pollVote.isAnswer == true) return answerCasted(type, createdAt, cid, messageId, poll, pollVote)
        val newVote = pollVote.toDomain()
        val (channelType, channelId) = cid.cidToTypeAndId()
        return VoteCastedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            poll = poll.toDomain().withOwnVote(newVote),
            newVote = newVote,
        )
    }

    /**
     * Transforms the generated [GeneratedPollVoteChangedEvent] to [VoteChangedEvent], or to
     * [AnswerCastedEvent] when the vote is an answer.
     */
    private fun GeneratedPollVoteChangedEvent.toDomain(): ChatEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        if (pollVote.isAnswer == true) return answerCasted(type, createdAt, cid, messageId, poll, pollVote)
        val newVote = pollVote.toDomain()
        val (channelType, channelId) = cid.cidToTypeAndId()
        return VoteChangedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            poll = poll.toDomain().withOwnVote(newVote),
            newVote = newVote,
        )
    }

    /**
     * Transforms the generated [GeneratedPollVoteRemovedEvent] to [VoteRemovedEvent].
     */
    private fun GeneratedPollVoteRemovedEvent.toDomain(): VoteRemovedEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return VoteRemovedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            poll = poll.toDomain(),
            removedVote = pollVote.toDomain(),
        )
    }

    @Suppress("LongParameterList")
    private fun answerCasted(
        type: String,
        createdAt: ExactDate,
        cid: String,
        messageId: String?,
        poll: PollResponseData,
        pollVote: PollVoteResponseData,
    ): AnswerCastedEvent = with(domainMapping) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        return AnswerCastedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            poll = poll.toDomain(),
            newAnswer = pollVote.toAnswerDomain(),
        )
    }

    /** A vote by the current user is added to the poll's votes and own votes, replacing one with the same id. */
    private fun Poll.withOwnVote(vote: Vote): Poll =
        vote.takeIf { it.user?.id == domainMapping.currentUserIdProvider() }
            ?.let {
                copy(
                    votes = (votes.associateBy { it.id } + (it.id to it)).values.toList(),
                    ownVotes = (ownVotes.associateBy { it.id } + (it.id to it)).values.toList(),
                )
            } ?: this

    /**
     * Transforms the generated [GeneratedDraftUpdatedEvent] to [DraftMessageUpdatedEvent].
     */
    private fun GeneratedDraftUpdatedEvent.toDomain(): DraftMessageUpdatedEvent = with(domainMapping) {
        DraftMessageUpdatedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            draftMessage = requireNotNull(draft).toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedDraftDeletedEvent] to [DraftMessageDeletedEvent].
     */
    private fun GeneratedDraftDeletedEvent.toDomain(): DraftMessageDeletedEvent = with(domainMapping) {
        DraftMessageDeletedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            draftMessage = requireNotNull(draft).toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedReminderCreatedEvent] to [ReminderCreatedEvent].
     */
    private fun GeneratedReminderCreatedEvent.toDomain(): ReminderCreatedEvent = with(domainMapping) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        ReminderCreatedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            userId = userId,
            reminder = reminder.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedReminderUpdatedEvent] to [ReminderUpdatedEvent].
     */
    private fun GeneratedReminderUpdatedEvent.toDomain(): ReminderUpdatedEvent = with(domainMapping) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        ReminderUpdatedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            userId = userId,
            reminder = reminder.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedReminderDeletedEvent] to [ReminderDeletedEvent].
     */
    private fun GeneratedReminderDeletedEvent.toDomain(): ReminderDeletedEvent = with(domainMapping) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        ReminderDeletedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            userId = userId,
            reminder = reminder.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedReminderNotificationEvent] to [NotificationReminderDueEvent].
     */
    private fun GeneratedReminderNotificationEvent.toDomain(): NotificationReminderDueEvent = with(domainMapping) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        NotificationReminderDueEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            userId = userId,
            reminder = reminder.toDomain(),
        )
    }

    /**
     * Transforms the generated [GeneratedUserMessagesDeletedEvent] to [UserMessagesDeletedEvent].
     */
    private fun GeneratedUserMessagesDeletedEvent.toDomain(): UserMessagesDeletedEvent = with(domainMapping) {
        UserMessagesDeletedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            user = user.toDomain(),
            hardDelete = hardDelete == true,
        )
    }

    /**
     * Transforms the generated [GeneratedAIIndicatorUpdateEvent] to [AIIndicatorUpdatedEvent].
     */
    private fun GeneratedAIIndicatorUpdateEvent.toDomain(): AIIndicatorUpdatedEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return AIIndicatorUpdatedEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            user = requireNotNull(user).toDomain(),
            channelType = channelType,
            channelId = channelId,
            aiState = aiState,
            messageId = messageId,
        )
    }

    /**
     * Transforms the generated [GeneratedAIIndicatorClearEvent] to [AIIndicatorClearEvent].
     */
    private fun GeneratedAIIndicatorClearEvent.toDomain(): AIIndicatorClearEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return AIIndicatorClearEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = requireNotNull(user).toDomain(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
        )
    }

    /**
     * Transforms the generated [GeneratedAIIndicatorStopEvent] to [AIIndicatorStopEvent].
     */
    private fun GeneratedAIIndicatorStopEvent.toDomain(): AIIndicatorStopEvent = with(domainMapping) {
        val cid = requireNotNull(cid)
        val (channelType, channelId) = cid.cidToTypeAndId()
        return AIIndicatorStopEvent(
            type = type,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            cid = cid,
            user = requireNotNull(user).toDomain(),
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
