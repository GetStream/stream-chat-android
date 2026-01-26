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

package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.utils.internal.ExactDate
import io.getstream.result.Error
import java.util.Date

internal sealed class ChatEventDto

@JsonClass(generateAdapter = true)
internal data class ChannelDeletedEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val channel: DownstreamChannelDto,
    val user: DownstreamUserDto?,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelHiddenEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
    val channel: DownstreamChannelDto,
    val clear_history: Boolean,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelTruncatedEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto?,
    val message: DownstreamMessageDto?,
    val channel: DownstreamChannelDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelUpdatedEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val message: DownstreamMessageDto?,
    val channel: DownstreamChannelDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelUpdatedByUserEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
    val message: DownstreamMessageDto?,
    val channel: DownstreamChannelDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelVisibleEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
    val channel: DownstreamChannelDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class HealthEventDto(
    val type: String,
    val created_at: ExactDate,
    val connection_id: String,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MemberAddedEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val member: DownstreamMemberDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MemberRemovedEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val member: DownstreamMemberDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MemberUpdatedEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val member: DownstreamMemberDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MessageDeletedEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto?,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val message: DownstreamMessageDto,
    val hard_delete: Boolean?,
    val channel_message_count: Int? = null,
    val deleted_for_me: Boolean? = null,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MessageDeliveredEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val last_delivered_at: ExactDate,
    val last_delivered_message_id: String,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MessageReadEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val thread: DownstreamThreadInfoDto? = null,
    val last_read_message_id: String?,
    val team: String? = null,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MessageUpdatedEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val message: DownstreamMessageDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NewMessageEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_member_count: Int?,
    val channel_custom: DownstreamChannelCustomDto?,
    val channel_type: String,
    val channel_id: String,
    val message: DownstreamMessageDto,
    val watcher_count: Int = 0,
    val total_unread_count: Int = 0,
    val unread_channels: Int = 0,
    val channel_message_count: Int? = null,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class DraftMessageUpdatedEventDto(
    val type: String,
    val created_at: ExactDate,
    val draft: DownstreamDraftDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class DraftMessageDeletedEventDto(
    val type: String,
    val created_at: ExactDate,
    val draft: DownstreamDraftDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationAddedToChannelEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val channel: DownstreamChannelDto,
    val member: DownstreamMemberDto,
    val total_unread_count: Int = 0,
    val unread_channels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationChannelDeletedEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val channel: DownstreamChannelDto,
    val total_unread_count: Int = 0,
    val unread_channels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationChannelMutesUpdatedEventDto(
    val type: String,
    val created_at: ExactDate,
    val me: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationChannelTruncatedEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val channel: DownstreamChannelDto,
    val total_unread_count: Int = 0,
    val unread_channels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationInviteAcceptedEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
    val member: DownstreamMemberDto,
    val channel: DownstreamChannelDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationInviteRejectedEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
    val member: DownstreamMemberDto,
    val channel: DownstreamChannelDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationInvitedEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
    val member: DownstreamMemberDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationMarkReadEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val total_unread_count: Int = 0,
    val unread_channels: Int = 0,
    val thread_id: String? = null,
    val thread: DownstreamThreadInfoDto? = null,
    val unread_threads: Int? = null,
    val unread_thread_messages: Int? = null,
    val last_read_message_id: String?,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationMarkUnreadEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val first_unread_message_id: String,
    val last_read_message_id: String?,
    val last_read_at: ExactDate,
    val unread_messages: Int,
    val total_unread_count: Int,
    val unread_channels: Int,
    val thread_id: String? = null,
    val unread_threads: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MarkAllReadEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
    val total_unread_count: Int = 0,
    val unread_channels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationMessageNewEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val channel: DownstreamChannelDto,
    val message: DownstreamMessageDto,
    val total_unread_count: Int = 0,
    val unread_channels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationThreadMessageNewEventDto(
    val type: String,
    val cid: String,
    val channel_id: String,
    val channel_type: String,
    val message: DownstreamMessageDto,
    val channel: DownstreamChannelDto,
    val created_at: ExactDate,
    val unread_threads: Int,
    val unread_thread_messages: Int,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationMutesUpdatedEventDto(
    val type: String,
    val created_at: ExactDate,
    val me: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationRemovedFromChannelEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto?,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val channel: DownstreamChannelDto,
    val member: DownstreamMemberDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ReactionDeletedEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val message: DownstreamMessageDto,
    val reaction: DownstreamReactionDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ReactionNewEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val message: DownstreamMessageDto,
    val reaction: DownstreamReactionDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ReactionUpdateEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val message: DownstreamMessageDto,
    val reaction: DownstreamReactionDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class TypingStartEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val parent_id: String?,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class TypingStopEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val parent_id: String?,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelUserBannedEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
    val expiration: Date?,
    val shadow: Boolean?,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class GlobalUserBannedEventDto(
    val type: String,
    val user: DownstreamUserDto,
    val created_at: ExactDate,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UserDeletedEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UserPresenceChangedEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UserStartWatchingEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val watcher_count: Int = 0,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UserStopWatchingEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val watcher_count: Int = 0,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelUserUnbannedEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class GlobalUserUnbannedEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UserUpdatedEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class PollUpdatedEventDto(
    val type: String,
    val cid: String,
    val message_id: String?,
    val created_at: ExactDate,
    val poll: DownstreamPollDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class PollDeletedEventDto(
    val type: String,
    val cid: String,
    val message_id: String?,
    val created_at: ExactDate,
    val poll: DownstreamPollDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class PollClosedEventDto(
    val type: String,
    val cid: String,
    val message_id: String?,
    val created_at: ExactDate,
    val poll: DownstreamPollDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class VoteCastedEventDto(
    val type: String,
    val cid: String,
    val message_id: String?,
    val created_at: ExactDate,
    val poll: DownstreamPollDto,
    val poll_vote: DownstreamVoteDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class AnswerCastedEventDto(
    val type: String,
    val cid: String,
    val message_id: String?,
    val created_at: ExactDate,
    val poll: DownstreamPollDto,
    val poll_vote: DownstreamVoteDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class VoteChangedEventDto(
    val type: String,
    val cid: String,
    val message_id: String?,
    val created_at: ExactDate,
    val poll: DownstreamPollDto,
    val poll_vote: DownstreamVoteDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class VoteRemovedEventDto(
    val type: String,
    val cid: String,
    val message_id: String?,
    val created_at: ExactDate,
    val poll: DownstreamPollDto,
    val poll_vote: DownstreamVoteDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ReminderCreatedEventDto(
    val type: String,
    val created_at: ExactDate,
    val message_id: String,
    val user_id: String,
    val cid: String,
    val reminder: DownstreamReminderDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ReminderUpdatedEventDto(
    val type: String,
    val created_at: ExactDate,
    val message_id: String,
    val user_id: String,
    val cid: String,
    val reminder: DownstreamReminderDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ReminderDeletedEventDto(
    val type: String,
    val created_at: ExactDate,
    val message_id: String,
    val user_id: String,
    val cid: String,
    val reminder: DownstreamReminderDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationReminderDueEventDto(
    val type: String,
    val created_at: ExactDate,
    val message_id: String,
    val user_id: String,
    val cid: String,
    val reminder: DownstreamReminderDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class AIIndicatorUpdatedEventDto(
    val type: String,
    val ai_state: String,
    val cid: String,
    val user: DownstreamUserDto,
    val created_at: ExactDate,
    val message_id: String,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class AIIndicatorClearEventDto(
    val type: String,
    val cid: String,
    val user: DownstreamUserDto,
    val created_at: ExactDate,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class AIIndicatorStopEventDto(
    val type: String,
    val cid: String,
    val user: DownstreamUserDto,
    val created_at: ExactDate,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UserMessagesDeletedEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto,
    val cid: String?,
    val channel_type: String?,
    val channel_id: String?,
    val hard_delete: Boolean?,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ConnectedEventDto(
    val type: String,
    val created_at: ExactDate,
    val me: DownstreamUserDto,
    val connection_id: String,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ConnectionErrorEventDto(
    val type: String,
    val created_at: ExactDate,
    val connection_id: String,
    val error: ErrorDto,
) : ChatEventDto()

/**
 * Special upstream event class, as we have to send this event
 * after connecting.
 */
@JsonClass(generateAdapter = true)
internal data class UpstreamConnectedEventDto(
    val type: String,
    val created_at: Date,
    val me: UpstreamUserDto,
    val connection_id: String,
)

@JsonClass(generateAdapter = true)
internal data class ConnectingEventDto(
    val type: String,
    val created_at: ExactDate,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class DisconnectedEventDto(
    val type: String,
    val created_at: ExactDate,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ErrorEventDto(
    val type: String,
    val created_at: ExactDate,
    val error: Error,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UnknownEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto?,
    val rawData: Map<*, *>,
) : ChatEventDto()
