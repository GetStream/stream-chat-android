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
import io.getstream.chat.android.client.api2.model.dto.ConnectingEventDto
import io.getstream.chat.android.client.api2.model.dto.DisconnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.ErrorEventDto
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
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.network.models.MessageNewEvent
import io.getstream.chat.android.network.models.WSClientEvent
import java.util.Date
import io.getstream.chat.android.network.models.AIIndicatorClearEvent as GeneratedAIIndicatorClearEvent
import io.getstream.chat.android.network.models.AIIndicatorStopEvent as GeneratedAIIndicatorStopEvent
import io.getstream.chat.android.network.models.AIIndicatorUpdateEvent as GeneratedAIIndicatorUpdateEvent
import io.getstream.chat.android.network.models.ChannelDeletedEvent as GeneratedChannelDeletedEvent
import io.getstream.chat.android.network.models.ConnectionErrorEvent as GeneratedConnectionErrorEvent
import io.getstream.chat.android.network.models.ChannelHiddenEvent as GeneratedChannelHiddenEvent
import io.getstream.chat.android.network.models.ChannelTruncatedEvent as GeneratedChannelTruncatedEvent
import io.getstream.chat.android.network.models.ChannelUpdatedEvent as GeneratedChannelUpdatedEvent
import io.getstream.chat.android.network.models.ChannelVisibleEvent as GeneratedChannelVisibleEvent
import io.getstream.chat.android.network.models.DraftDeletedEvent as GeneratedDraftDeletedEvent
import io.getstream.chat.android.network.models.HealthCheckEvent as GeneratedHealthCheckEvent
import io.getstream.chat.android.network.models.DraftUpdatedEvent as GeneratedDraftUpdatedEvent
import io.getstream.chat.android.network.models.MemberAddedEvent as GeneratedMemberAddedEvent
import io.getstream.chat.android.network.models.MemberRemovedEvent as GeneratedMemberRemovedEvent
import io.getstream.chat.android.network.models.MemberUpdatedEvent as GeneratedMemberUpdatedEvent
import io.getstream.chat.android.network.models.MessageDeletedEvent as GeneratedMessageDeletedEvent
import io.getstream.chat.android.network.models.MessageDeliveredEvent as GeneratedMessageDeliveredEvent
import io.getstream.chat.android.network.models.MessageReadEvent as GeneratedMessageReadEvent
import io.getstream.chat.android.network.models.MessageUpdatedEvent as GeneratedMessageUpdatedEvent
import io.getstream.chat.android.network.models.NotificationAddedToChannelEvent as GeneratedNotificationAddedToChannelEvent
import io.getstream.chat.android.network.models.NotificationChannelDeletedEvent as GeneratedNotificationChannelDeletedEvent
import io.getstream.chat.android.network.models.NotificationInviteAcceptedEvent as GeneratedNotificationInviteAcceptedEvent
import io.getstream.chat.android.network.models.NotificationInviteRejectedEvent as GeneratedNotificationInviteRejectedEvent
import io.getstream.chat.android.network.models.NotificationInvitedEvent as GeneratedNotificationInvitedEvent
import io.getstream.chat.android.network.models.NotificationChannelMutesUpdatedEvent as GeneratedNotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.network.models.NotificationChannelTruncatedEvent as GeneratedNotificationChannelTruncatedEvent
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
import io.getstream.chat.android.network.models.ReminderNotificationEvent as GeneratedReminderNotificationEvent
import io.getstream.chat.android.network.models.ThreadUpdatedEvent as GeneratedThreadUpdatedEvent
import io.getstream.chat.android.network.models.ReminderCreatedEvent as GeneratedReminderCreatedEvent
import io.getstream.chat.android.network.models.ReminderDeletedEvent as GeneratedReminderDeletedEvent
import io.getstream.chat.android.network.models.ReminderUpdatedEvent as GeneratedReminderUpdatedEvent
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

    private val streamDateFormatter = StreamDateFormatter("EventMapping")

    /** Generated-DTO mirror of [ChatEventDto.toDomain]; [rawCreatedAt] is peeked from the wire. */
    internal fun WSClientEvent.toDomain(rawCreatedAt: String?): ChatEvent = when (this) {
        is MessageNewEvent -> toDomain(rawCreatedAt)
        is GeneratedTypingStartEvent -> toDomain(rawCreatedAt)
        is GeneratedTypingStopEvent -> toDomain(rawCreatedAt)
        is GeneratedReactionDeletedEvent -> toDomain(rawCreatedAt)
        is GeneratedReactionNewEvent -> toDomain(rawCreatedAt)
        is GeneratedMessageUpdatedEvent -> toDomain(rawCreatedAt)
        is GeneratedMessageDeletedEvent -> toDomain(rawCreatedAt)
        is GeneratedMessageDeliveredEvent -> toDomain(rawCreatedAt)
        is GeneratedMemberAddedEvent -> toDomain(rawCreatedAt)
        is GeneratedMemberRemovedEvent -> toDomain(rawCreatedAt)
        is GeneratedMemberUpdatedEvent -> toDomain(rawCreatedAt)
        is GeneratedUserWatchingStartEvent -> toDomain(rawCreatedAt)
        is GeneratedUserWatchingStopEvent -> toDomain(rawCreatedAt)
        is GeneratedChannelHiddenEvent -> toDomain(rawCreatedAt)
        is GeneratedChannelVisibleEvent -> toDomain(rawCreatedAt)
        is GeneratedUserPresenceChangedEvent -> toDomain(rawCreatedAt)
        is GeneratedChannelDeletedEvent -> toDomain(rawCreatedAt)
        is GeneratedConnectionErrorEvent -> toDomain(rawCreatedAt)
        is GeneratedChannelTruncatedEvent -> toDomain(rawCreatedAt)
        is GeneratedChannelUpdatedEvent -> toDomain(rawCreatedAt)
        is GeneratedDraftUpdatedEvent -> toDomain(rawCreatedAt)
        is GeneratedDraftDeletedEvent -> toDomain(rawCreatedAt)
        is GeneratedUserUpdatedEvent -> toDomain(rawCreatedAt)
        is GeneratedUserBannedEvent -> toDomain(rawCreatedAt)
        is GeneratedUserUnbannedEvent -> toDomain(rawCreatedAt)
        is GeneratedNotificationMutesUpdatedEvent -> toDomain(rawCreatedAt)
        is GeneratedNotificationChannelMutesUpdatedEvent -> toDomain(rawCreatedAt)
        is GeneratedNotificationChannelDeletedEvent -> toDomain(rawCreatedAt)
        is GeneratedNotificationChannelTruncatedEvent -> toDomain(rawCreatedAt)
        is GeneratedNotificationAddedToChannelEvent -> toDomain(rawCreatedAt)
        is GeneratedNotificationRemovedFromChannelEvent -> toDomain(rawCreatedAt)
        is GeneratedNotificationInvitedEvent -> toDomain(rawCreatedAt)
        is GeneratedNotificationInviteAcceptedEvent -> toDomain(rawCreatedAt)
        is GeneratedNotificationInviteRejectedEvent -> toDomain(rawCreatedAt)
        is GeneratedNotificationNewMessageEvent -> toDomain(rawCreatedAt)
        is GeneratedNotificationThreadMessageNewEvent -> toDomain(rawCreatedAt)
        is GeneratedUserDeletedEvent -> toDomain(rawCreatedAt)
        is GeneratedUserMessagesDeletedEvent -> toDomain(rawCreatedAt)
        is GeneratedReminderCreatedEvent -> toDomain(rawCreatedAt)
        is GeneratedReminderUpdatedEvent -> toDomain(rawCreatedAt)
        is GeneratedReminderDeletedEvent -> toDomain(rawCreatedAt)
        is GeneratedAIIndicatorUpdateEvent -> toDomain(rawCreatedAt)
        is GeneratedAIIndicatorClearEvent -> toDomain(rawCreatedAt)
        is GeneratedAIIndicatorStopEvent -> toDomain(rawCreatedAt)
        is GeneratedHealthCheckEvent -> toDomain(rawCreatedAt)
        is GeneratedPollClosedEvent -> toDomain(rawCreatedAt)
        is GeneratedPollDeletedEvent -> toDomain(rawCreatedAt)
        is GeneratedPollUpdatedEvent -> toDomain(rawCreatedAt)
        is GeneratedPollVoteCastedEvent -> toDomain(rawCreatedAt)
        is GeneratedPollVoteChangedEvent -> toDomain(rawCreatedAt)
        is GeneratedPollVoteRemovedEvent -> toDomain(rawCreatedAt)
        is GeneratedNotificationMarkUnreadEvent -> toDomain(rawCreatedAt)
        is GeneratedReactionUpdatedEvent -> toDomain(rawCreatedAt)
        is GeneratedThreadUpdatedEvent -> toDomain(rawCreatedAt)
        is GeneratedReminderNotificationEvent -> toDomain(rawCreatedAt)
        is GeneratedMessageReadEvent -> toDomain(rawCreatedAt)
        is GeneratedNotificationMarkReadEvent -> toDomain(rawCreatedAt)
        else -> error("Unmapped generated event ${this::class.simpleName}")
    }

    /**
     * Transforms [ChatEventDto] to [ChatEvent].
     * This is a generic transformation method that can be used to transform any [ChatEventDto] to [ChatEvent].
     * The actual transformation is delegated to the specific transformation methods for each event type.
     * The specific transformation methods are defined below.
     */
    @Suppress("LongMethod")
    internal fun ChatEventDto.toDomain(): ChatEvent {
        return when (this) {
            is ConnectingEventDto -> toDomain()
            is DisconnectedEventDto -> toDomain()
            is ErrorEventDto -> toDomain()
            is UnknownEventDto -> toDomain()
        }
    }

    private fun GeneratedChannelDeletedEvent.toDomain(rawCreatedAt: String?): ChannelDeletedEvent = with(domainMapping) {
        ChannelDeletedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = cid.orEmpty(),
            channelType = channelType.orEmpty(),
            channelId = channelId.orEmpty(),
            channel = channel.toDomain(),
            user = user?.toDomain(),
        )
    }

    /**
     * Transforms [GeneratedChannelHiddenEvent] to [ChannelHiddenEvent].
     */
    private fun GeneratedChannelHiddenEvent.toDomain(rawCreatedAt: String?): ChannelHiddenEvent = with(domainMapping) {
        ChannelHiddenEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = cid.orEmpty(),
            channelType = channelType.orEmpty(),
            channelId = channelId.orEmpty(),
            user = user?.toDomain() ?: User(),
            channel = channel.toDomain(),
            clearHistory = clearHistory,
        )
    }

    private fun GeneratedChannelTruncatedEvent.toDomain(rawCreatedAt: String?): ChannelTruncatedEvent = with(domainMapping) {
        ChannelTruncatedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = cid.orEmpty(),
            channelType = channelType.orEmpty(),
            channelId = channelId.orEmpty(),
            user = user?.toDomain(),
            message = message?.toDomain(channel.toChannelInfo()),
            channel = channel.toDomain(),
        )
    }

    private fun GeneratedChannelUpdatedEvent.toDomain(rawCreatedAt: String?): ChatEvent = with(domainMapping) {
        val safeCid = cid.orEmpty()
        val safeType = channelType.orEmpty()
        val safeId = channelId.orEmpty()
        val domainChannel = channel.toDomain()
        val domainMessage = message?.toDomain(channel.toChannelInfo())
        val eventUser = user
        if (eventUser != null) {
            ChannelUpdatedByUserEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                cid = safeCid,
                channelType = safeType,
                channelId = safeId,
                user = eventUser.toDomain(),
                message = domainMessage,
                channel = domainChannel,
            )
        } else {
            ChannelUpdatedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                cid = safeCid,
                channelType = safeType,
                channelId = safeId,
                message = domainMessage,
                channel = domainChannel,
            )
        }
    }

    /**
     * Transforms [GeneratedChannelVisibleEvent] to [ChannelVisibleEvent].
     */
    private fun GeneratedChannelVisibleEvent.toDomain(rawCreatedAt: String?): ChannelVisibleEvent = with(domainMapping) {
        ChannelVisibleEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = cid.orEmpty(),
            channelType = channelType.orEmpty(),
            channelId = channelId.orEmpty(),
            user = user?.toDomain() ?: User(),
            channel = channel.toDomain(),
        )
    }

    private fun GeneratedHealthCheckEvent.toDomain(rawCreatedAt: String?): ChatEvent = with(domainMapping) {
        val ownUser = me
        if (ownUser != null) {
            ConnectedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                me = ownUser.toDomain(),
                connectionId = connectionId,
            )
        } else {
            HealthEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                connectionId = connectionId,
            )
        }
    }

    /**
     * Transforms [MemberAddedEventDto] to [MemberAddedEvent].
     */
    private fun GeneratedMemberAddedEvent.toDomain(rawCreatedAt: String?): MemberAddedEvent = with(domainMapping) {
        MemberAddedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            user = user?.toDomain() ?: User(),
            cid = cid.orEmpty(),
            channelType = channelType.orEmpty(),
            channelId = channelId.orEmpty(),
            member = member.toDomain(),
        )
    }

    private fun GeneratedMemberRemovedEvent.toDomain(rawCreatedAt: String?): MemberRemovedEvent = with(domainMapping) {
        MemberRemovedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            user = user?.toDomain() ?: User(),
            cid = cid.orEmpty(),
            channelType = channelType.orEmpty(),
            channelId = channelId.orEmpty(),
            member = member.toDomain(),
        )
    }

    private fun GeneratedMemberUpdatedEvent.toDomain(rawCreatedAt: String?): MemberUpdatedEvent = with(domainMapping) {
        MemberUpdatedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            user = user?.toDomain() ?: User(),
            cid = cid.orEmpty(),
            channelType = channelType.orEmpty(),
            channelId = channelId.orEmpty(),
            member = member.toDomain(),
        )
    }

    private fun GeneratedMessageDeletedEvent.toDomain(rawCreatedAt: String?): MessageDeletedEvent =
        with(domainMapping) {
            MessageDeletedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                user = user?.toDomain(),
                cid = cid.orEmpty(),
                channelType = channelType.orEmpty(),
                channelId = channelId.orEmpty(),
                message = message.toDomain(),
                hardDelete = hardDelete,
                channelMessageCount = channelMessageCount,
                deletedForMe = deletedForMe ?: false,
            )
        }

    /**
     * Transforms [MessageDeliveredEventDto] to [MessageDeliveredEvent].
     */
    private fun GeneratedMessageDeliveredEvent.toDomain(rawCreatedAt: String?): MessageDeliveredEvent =
        with(domainMapping) {
            MessageDeliveredEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                user = user?.toDomain() ?: User(),
                cid = cid.orEmpty(),
                channelType = channelType.orEmpty(),
                channelId = channelId.orEmpty(),
                lastDeliveredAt = lastDeliveredAt?.let(streamDateFormatter::parse) ?: Date(0),
                lastDeliveredMessageId = lastDeliveredMessageId.orEmpty(),
            )
        }

    // Defensive cid-based split mirroring the legacy adapter. Code-read of the backend suggests
    // `message.read` always carries `cid` (every Go constructor populates it from a real channel;
    // `markAllRead()` fires `notification.mark_read` instead), but we keep the cid-absent branch
    // rather than drop it on the strength of a grep alone.
    private fun GeneratedMessageReadEvent.toDomain(rawCreatedAt: String?): ChatEvent = with(domainMapping) {
        val channelCid = cid
        if (channelCid != null) {
            MessageReadEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                user = user?.toDomain() ?: User(),
                cid = channelCid,
                channelType = channelType.orEmpty(),
                channelId = channelId.orEmpty(),
                thread = thread?.toDomain(),
                lastReadMessageId = lastReadMessageId,
                team = team,
            )
        } else {
            MarkAllReadEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                user = user?.toDomain() ?: User(),
            )
        }
    }

    private fun GeneratedMessageUpdatedEvent.toDomain(rawCreatedAt: String?): MessageUpdatedEvent =
        with(domainMapping) {
            MessageUpdatedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                user = user?.toDomain() ?: User(),
                cid = cid.orEmpty(),
                channelType = channelType.orEmpty(),
                channelId = channelId.orEmpty(),
                message = message.toDomain(),
            )
        }

    private fun MessageNewEvent.toDomain(rawCreatedAt: String?): NewMessageEvent = with(domainMapping) {
        // build ChannelInfo from the event data, as it is not delivered within the `message` field
        val channelInfo = ChannelInfo(
            cid = cid,
            id = channelId,
            type = channelType,
            memberCount = channelMemberCount ?: 0,
            name = channelCustom?.get("name") as? String,
            image = channelCustom?.get("image") as? String,
        )
        NewMessageEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            user = user?.toDomain() ?: User(),
            cid = cid.orEmpty(),
            channelType = channelType.orEmpty(),
            channelId = channelId.orEmpty(),
            message = message.toDomain(channelInfo),
            watcherCount = watcherCount,
            totalUnreadCount = totalUnreadCount ?: 0,
            unreadChannels = unreadChannels ?: 0,
            channelMessageCount = channelMessageCount,
        )
    }

    private fun GeneratedNotificationAddedToChannelEvent.toDomain(rawCreatedAt: String?): NotificationAddedToChannelEvent =
        with(domainMapping) {
            NotificationAddedToChannelEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                cid = cid.orEmpty(),
                channelType = channelType.orEmpty(),
                channelId = channelId.orEmpty(),
                channel = channel!!.toDomain(),
                member = member.toDomain(),
                totalUnreadCount = totalUnreadCount,
                unreadChannels = unreadChannels,
            )
        }

    private fun GeneratedNotificationChannelDeletedEvent.toDomain(rawCreatedAt: String?): NotificationChannelDeletedEvent =
        with(domainMapping) {
            NotificationChannelDeletedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                cid = cid.orEmpty(),
                channelType = channelType.orEmpty(),
                channelId = channelId.orEmpty(),
                channel = channel.toDomain(),
                totalUnreadCount = totalUnreadCount ?: 0,
                unreadChannels = unreadChannels ?: 0,
            )
        }

    private fun GeneratedNotificationChannelMutesUpdatedEvent.toDomain(rawCreatedAt: String?): NotificationChannelMutesUpdatedEvent =
        with(domainMapping) {
            NotificationChannelMutesUpdatedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                me = me.toDomain(),
            )
        }

    private fun GeneratedNotificationChannelTruncatedEvent.toDomain(rawCreatedAt: String?): NotificationChannelTruncatedEvent =
        with(domainMapping) {
            NotificationChannelTruncatedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                cid = cid.orEmpty(),
                channelType = channelType.orEmpty(),
                channelId = channelId.orEmpty(),
                channel = channel.toDomain(),
                totalUnreadCount = totalUnreadCount ?: 0,
                unreadChannels = unreadChannels ?: 0,
            )
        }

    private fun GeneratedNotificationInviteAcceptedEvent.toDomain(rawCreatedAt: String?): NotificationInviteAcceptedEvent =
        with(domainMapping) {
            NotificationInviteAcceptedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                cid = cid.orEmpty(),
                channelType = channelType.orEmpty(),
                channelId = channelId.orEmpty(),
                user = user!!.toDomain(),
                member = member.toDomain(),
                channel = channel.toDomain(),
            )
        }

    private fun GeneratedNotificationInviteRejectedEvent.toDomain(rawCreatedAt: String?): NotificationInviteRejectedEvent =
        with(domainMapping) {
            NotificationInviteRejectedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                cid = cid.orEmpty(),
                channelType = channelType.orEmpty(),
                channelId = channelId.orEmpty(),
                user = user!!.toDomain(),
                member = member.toDomain(),
                channel = channel.toDomain(),
            )
        }

    private fun GeneratedNotificationInvitedEvent.toDomain(rawCreatedAt: String?): NotificationInvitedEvent =
        with(domainMapping) {
            NotificationInvitedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                cid = cid.orEmpty(),
                channelType = channelType.orEmpty(),
                channelId = channelId.orEmpty(),
                user = user!!.toDomain(),
                member = member.toDomain(),
            )
        }

    /**
     * Transforms [NotificationMarkReadEventDto] to [NotificationMarkReadEvent].
     */
    // Wire `notification.mark_read` covers two domain events: with `cid` -> NotificationMarkReadEvent
    // (per-channel mark), without `cid` -> MarkAllReadEvent (global, from `ChatClient.markAllRead()`).
    private fun GeneratedNotificationMarkReadEvent.toDomain(rawCreatedAt: String?): ChatEvent = with(domainMapping) {
        val channelCid = cid
        if (channelCid != null) {
            NotificationMarkReadEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                user = user?.toDomain() ?: User(),
                cid = channelCid,
                channelType = channelType.orEmpty(),
                channelId = channelId.orEmpty(),
                totalUnreadCount = totalUnreadCount,
                unreadChannels = unreadChannels,
                threadId = threadId,
                thread = thread?.toDomain(),
                unreadThreads = unreadThreads,
                unreadThreadMessages = unreadThreadMessages,
                lastReadMessageId = lastReadMessageId,
            )
        } else {
            MarkAllReadEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                user = user?.toDomain() ?: User(),
                totalUnreadCount = totalUnreadCount,
                unreadChannels = unreadChannels,
            )
        }
    }

    private fun GeneratedNotificationMarkUnreadEvent.toDomain(rawCreatedAt: String?): NotificationMarkUnreadEvent = with(domainMapping) {
        NotificationMarkUnreadEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            user = user?.toDomain() ?: User(),
            cid = cid.orEmpty(),
            channelType = channelType.orEmpty(),
            channelId = channelId.orEmpty(),
            totalUnreadCount = totalUnreadCount ?: 0,
            unreadChannels = unreadChannels ?: 0,
            firstUnreadMessageId = firstUnreadMessageId.orEmpty(),
            lastReadMessageId = lastReadMessageId,
            lastReadMessageAt = lastReadAt ?: java.util.Date(0),
            unreadMessages = unreadMessages ?: 0,
            threadId = threadId,
            unreadThreads = unreadThreads ?: 0,
        )
    }

    /**
     * Transforms [MarkAllReadEventDto] to [MarkAllReadEvent].
     */
    private fun GeneratedNotificationNewMessageEvent.toDomain(rawCreatedAt: String?): NotificationMessageNewEvent =
        with(domainMapping) {
            NotificationMessageNewEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                cid = cid.orEmpty(),
                channelType = channelType.orEmpty(),
                channelId = channelId.orEmpty(),
                channel = channel.toDomain(),
                message = message.toDomain(channel.toChannelInfo()),
                totalUnreadCount = totalUnreadCount ?: 0,
                unreadChannels = unreadChannels ?: 0,
            )
        }

    private fun GeneratedThreadUpdatedEvent.toDomain(rawCreatedAt: String?): ThreadUpdatedEvent = with(domainMapping) {
        ThreadUpdatedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = cid.orEmpty(),
            channelType = channelType.orEmpty(),
            channelId = channelId.orEmpty(),
            thread = thread!!.toDomain(),
        )
    }

    private fun GeneratedNotificationThreadMessageNewEvent.toDomain(rawCreatedAt: String?): NotificationThreadMessageNewEvent =
        with(domainMapping) {
            NotificationThreadMessageNewEvent(
                type = type,
                cid = cid.orEmpty(),
                channelId = channelId.orEmpty(),
                channelType = channelType.orEmpty(),
                message = message.toDomain(channel.toChannelInfo()),
                channel = channel.toDomain(),
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                unreadThreads = unreadThreads ?: 0,
                unreadThreadMessages = unreadThreadMessages ?: 0,
            )
        }

    private fun GeneratedNotificationMutesUpdatedEvent.toDomain(rawCreatedAt: String?): NotificationMutesUpdatedEvent = with(domainMapping) {
        NotificationMutesUpdatedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            me = me.toDomain(),
        )
    }

    private fun GeneratedNotificationRemovedFromChannelEvent.toDomain(rawCreatedAt: String?): NotificationRemovedFromChannelEvent =
        with(domainMapping) {
            NotificationRemovedFromChannelEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                user = user?.toDomain(),
                cid = cid.orEmpty(),
                channelType = channelType.orEmpty(),
                channelId = channelId.orEmpty(),
                channel = channel.toDomain(),
                member = member.toDomain(),
            )
        }

    private fun GeneratedReactionDeletedEvent.toDomain(rawCreatedAt: String?): ReactionDeletedEvent =
        with(domainMapping) {
            ReactionDeletedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                user = user?.toDomain() ?: User(),
                cid = cid.orEmpty(),
                channelType = channelType.orEmpty(),
                channelId = channelId.orEmpty(),
                message = message?.toDomain() ?: Message(),
                reaction = reaction?.toDomain() ?: Reaction(),
            )
        }

    private fun GeneratedReactionNewEvent.toDomain(rawCreatedAt: String?): ReactionNewEvent = with(domainMapping) {
        ReactionNewEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            user = user?.toDomain() ?: User(),
            cid = cid.orEmpty(),
            channelType = channelType.orEmpty(),
            channelId = channelId.orEmpty(),
            message = message?.toDomain() ?: Message(),
            reaction = reaction?.toDomain() ?: Reaction(),
        )
    }

    private fun GeneratedReactionUpdatedEvent.toDomain(rawCreatedAt: String?): ReactionUpdateEvent = with(domainMapping) {
        ReactionUpdateEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            user = user?.toDomain() ?: User(),
            cid = cid.orEmpty(),
            channelType = channelType.orEmpty(),
            channelId = channelId.orEmpty(),
            message = message.toDomain(),
            reaction = reaction?.toDomain() ?: Reaction(),
        )
    }

    private fun GeneratedTypingStartEvent.toDomain(rawCreatedAt: String?): TypingStartEvent = with(domainMapping) {
        TypingStartEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            user = user?.toDomain() ?: User(),
            cid = cid.orEmpty(),
            channelType = channelType.orEmpty(),
            channelId = channelId.orEmpty(),
            parentId = parentId,
        )
    }

    private fun GeneratedTypingStopEvent.toDomain(rawCreatedAt: String?): TypingStopEvent = with(domainMapping) {
        TypingStopEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            user = user?.toDomain() ?: User(),
            cid = cid.orEmpty(),
            channelType = channelType.orEmpty(),
            channelId = channelId.orEmpty(),
            parentId = parentId,
        )
    }

    private fun GeneratedUserBannedEvent.toDomain(rawCreatedAt: String?): ChatEvent = with(domainMapping) {
        val safeCid = cid
        if (!safeCid.isNullOrBlank()) {
            ChannelUserBannedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                cid = safeCid,
                channelType = channelType.orEmpty(),
                channelId = channelId.orEmpty(),
                user = user.toDomain(),
                expiration = expiration,
                shadow = shadow ?: false,
            )
        } else {
            GlobalUserBannedEvent(
                type = type,
                user = user.toDomain(),
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
            )
        }
    }

    private fun GeneratedUserDeletedEvent.toDomain(rawCreatedAt: String?): UserDeletedEvent = with(domainMapping) {
        UserDeletedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            user = user.toDomain(),
        )
    }

    private fun GeneratedUserPresenceChangedEvent.toDomain(rawCreatedAt: String?): UserPresenceChangedEvent = with(domainMapping) {
        UserPresenceChangedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            user = user.toDomain(),
        )
    }

    private fun GeneratedUserWatchingStartEvent.toDomain(rawCreatedAt: String?): UserStartWatchingEvent = with(domainMapping) {
        UserStartWatchingEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = cid.orEmpty(),
            watcherCount = watcherCount,
            channelType = channelType.orEmpty(),
            channelId = channelId.orEmpty(),
            user = user.toDomain(),
        )
    }

    private fun GeneratedUserWatchingStopEvent.toDomain(rawCreatedAt: String?): UserStopWatchingEvent = with(domainMapping) {
        UserStopWatchingEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = cid.orEmpty(),
            watcherCount = watcherCount,
            channelType = channelType.orEmpty(),
            channelId = channelId.orEmpty(),
            user = user.toDomain(),
        )
    }

    private fun GeneratedUserUnbannedEvent.toDomain(rawCreatedAt: String?): ChatEvent = with(domainMapping) {
        val safeCid = cid
        if (!safeCid.isNullOrBlank()) {
            ChannelUserUnbannedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                user = user.toDomain(),
                cid = safeCid,
                channelType = channelType.orEmpty(),
                channelId = channelId.orEmpty(),
            )
        } else {
            GlobalUserUnbannedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                user = user.toDomain(),
            )
        }
    }

    private fun GeneratedUserUpdatedEvent.toDomain(rawCreatedAt: String?): UserUpdatedEvent = with(domainMapping) {
        UserUpdatedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            user = user.toDomain(),
        )
    }

    private fun GeneratedPollClosedEvent.toDomain(rawCreatedAt: String?): PollClosedEvent = with(domainMapping) {
        val safeCid = cid.orEmpty()
        val (channelType, channelId) = safeCid.cidToTypeAndId()
        PollClosedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = safeCid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            poll = poll.toDomain(),
        )
    }

    private fun GeneratedPollDeletedEvent.toDomain(rawCreatedAt: String?): PollDeletedEvent = with(domainMapping) {
        val safeCid = cid.orEmpty()
        val (channelType, channelId) = safeCid.cidToTypeAndId()
        PollDeletedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = safeCid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            poll = poll.toDomain(),
        )
    }

    private fun GeneratedPollUpdatedEvent.toDomain(rawCreatedAt: String?): PollUpdatedEvent = with(domainMapping) {
        val safeCid = cid.orEmpty()
        val (channelType, channelId) = safeCid.cidToTypeAndId()
        PollUpdatedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = safeCid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            poll = poll.toDomain(),
        )
    }

    private fun GeneratedPollVoteCastedEvent.toDomain(rawCreatedAt: String?): ChatEvent = with(domainMapping) {
        val safeCid = cid.orEmpty()
        val (channelType, channelId) = safeCid.cidToTypeAndId()
        if (pollVote.isAnswer == true) {
            AnswerCastedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                cid = safeCid,
                channelType = channelType,
                channelId = channelId,
                messageId = messageId,
                poll = poll.toDomain(),
                newAnswer = pollVote.toAnswerDomain(),
            )
        } else {
            val vote = pollVote.toDomain()
            val newPoll = poll.toDomain().let { p ->
                vote.takeIf { it.user?.id == currentUserIdProvider() }
                    ?.let {
                        p.copy(
                            votes = (p.votes.associateBy { it.id } + (it.id to it)).values.toList(),
                            ownVotes = (p.ownVotes.associateBy { it.id } + (it.id to it)).values.toList(),
                        )
                    } ?: p
            }
            VoteCastedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                cid = safeCid,
                channelType = channelType,
                channelId = channelId,
                messageId = messageId,
                poll = newPoll,
                newVote = vote,
            )
        }
    }

    private fun GeneratedPollVoteChangedEvent.toDomain(rawCreatedAt: String?): ChatEvent = with(domainMapping) {
        val safeCid = cid.orEmpty()
        val (channelType, channelId) = safeCid.cidToTypeAndId()
        if (pollVote.isAnswer == true) {
            AnswerCastedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                cid = safeCid,
                channelType = channelType,
                channelId = channelId,
                messageId = messageId,
                poll = poll.toDomain(),
                newAnswer = pollVote.toAnswerDomain(),
            )
        } else {
            val vote = pollVote.toDomain()
            val newPoll = poll.toDomain().let { p ->
                vote.takeIf { it.user?.id == currentUserIdProvider() }
                    ?.let {
                        p.copy(
                            votes = (p.votes.associateBy { it.id } + (it.id to it)).values.toList(),
                            ownVotes = (p.ownVotes.associateBy { it.id } + (it.id to it)).values.toList(),
                        )
                    } ?: p
            }
            VoteChangedEvent(
                type = type,
                createdAt = createdAt,
                rawCreatedAt = rawCreatedAt.orEmpty(),
                cid = safeCid,
                channelType = channelType,
                channelId = channelId,
                messageId = messageId,
                poll = newPoll,
                newVote = vote,
            )
        }
    }

    private fun GeneratedPollVoteRemovedEvent.toDomain(rawCreatedAt: String?): VoteRemovedEvent = with(domainMapping) {
        val safeCid = cid.orEmpty()
        val (channelType, channelId) = safeCid.cidToTypeAndId()
        VoteRemovedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = safeCid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            poll = poll.toDomain(),
            removedVote = pollVote.toDomain(),
        )
    }

    private fun GeneratedDraftUpdatedEvent.toDomain(rawCreatedAt: String?): DraftMessageUpdatedEvent = with(domainMapping) {
        DraftMessageUpdatedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            draftMessage = draft!!.toDomain(),
        )
    }

    private fun GeneratedDraftDeletedEvent.toDomain(rawCreatedAt: String?): DraftMessageDeletedEvent = with(domainMapping) {
        DraftMessageDeletedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            draftMessage = draft!!.toDomain(),
        )
    }

    private fun GeneratedReminderCreatedEvent.toDomain(rawCreatedAt: String?): ReminderCreatedEvent = with(domainMapping) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        ReminderCreatedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            userId = userId,
            reminder = reminder!!.toDomain(),
        )
    }

    private fun GeneratedReminderUpdatedEvent.toDomain(rawCreatedAt: String?): ReminderUpdatedEvent = with(domainMapping) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        ReminderUpdatedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            userId = userId,
            reminder = reminder!!.toDomain(),
        )
    }

    private fun GeneratedReminderDeletedEvent.toDomain(rawCreatedAt: String?): ReminderDeletedEvent = with(domainMapping) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        ReminderDeletedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            userId = userId,
            reminder = reminder!!.toDomain(),
        )
    }

    private fun GeneratedReminderNotificationEvent.toDomain(rawCreatedAt: String?): NotificationReminderDueEvent = with(domainMapping) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        NotificationReminderDueEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            messageId = messageId,
            userId = userId,
            reminder = reminder!!.toDomain(),
        )
    }

    private fun GeneratedUserMessagesDeletedEvent.toDomain(rawCreatedAt: String?): UserMessagesDeletedEvent = with(domainMapping) {
        UserMessagesDeletedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = cid,
            channelType = channelType,
            channelId = channelId,
            user = user.toDomain(),
            hardDelete = hardDelete == true,
        )
    }

    private fun GeneratedAIIndicatorUpdateEvent.toDomain(rawCreatedAt: String?): AIIndicatorUpdatedEvent = with(domainMapping) {
        val safeCid = cid.orEmpty()
        val (parsedType, parsedId) = safeCid.cidToTypeAndId()
        AIIndicatorUpdatedEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = safeCid,
            user = user?.toDomain() ?: User(),
            channelType = channelType ?: parsedType,
            channelId = channelId ?: parsedId,
            aiState = aiState,
            messageId = messageId,
        )
    }

    private fun GeneratedAIIndicatorClearEvent.toDomain(rawCreatedAt: String?): AIIndicatorClearEvent = with(domainMapping) {
        val safeCid = cid.orEmpty()
        val (parsedType, parsedId) = safeCid.cidToTypeAndId()
        AIIndicatorClearEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            user = user?.toDomain() ?: User(),
            cid = safeCid,
            channelType = channelType ?: parsedType,
            channelId = channelId ?: parsedId,
        )
    }

    private fun GeneratedAIIndicatorStopEvent.toDomain(rawCreatedAt: String?): AIIndicatorStopEvent = with(domainMapping) {
        val safeCid = cid.orEmpty()
        val (parsedType, parsedId) = safeCid.cidToTypeAndId()
        AIIndicatorStopEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            cid = safeCid,
            user = user?.toDomain() ?: User(),
            channelType = channelType ?: parsedType,
            channelId = channelId ?: parsedId,
        )
    }

    private fun GeneratedConnectionErrorEvent.toDomain(rawCreatedAt: String?): ConnectionErrorEvent =
        ConnectionErrorEvent(
            type = type,
            createdAt = createdAt,
            rawCreatedAt = rawCreatedAt.orEmpty(),
            connectionId = connectionId,
            error = error.toDomain(),
        )

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
