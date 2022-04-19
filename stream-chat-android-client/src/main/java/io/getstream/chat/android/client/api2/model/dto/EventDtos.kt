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

package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.errors.ChatError
import java.util.Date

internal sealed class ChatEventDto

@JsonClass(generateAdapter = true)
internal data class ChannelDeletedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "channel") val channel: DownstreamChannelDto,
    @Json(name = "user") val user: DownstreamUserDto?,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelHiddenEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "clear_history") val clearHistory: Boolean,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelTruncatedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "user") val user: DownstreamUserDto?,
    @Json(name = "message") val message: DownstreamMessageDto?,
    @Json(name = "channel") val channel: DownstreamChannelDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelUpdatedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "message") val message: DownstreamMessageDto?,
    @Json(name = "channel") val channel: DownstreamChannelDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelUpdatedByUserEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "message") val message: DownstreamMessageDto?,
    @Json(name = "channel") val channel: DownstreamChannelDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelVisibleEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "user") val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class HealthEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "connection_id") val connectionId: String,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MemberAddedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "member") val member: DownstreamMemberDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MemberRemovedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "member") val member: DownstreamMemberDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MemberUpdatedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "member") val member: DownstreamMemberDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MessageDeletedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto?,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "message") val message: DownstreamMessageDto,
    @Json(name = "hard_delete") val hardDelete: Boolean?
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MessageReadEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MessageUpdatedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "message") val message: DownstreamMessageDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NewMessageEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "message") val message: DownstreamMessageDto,
    @Json(name = "watcher_count") val watcherCount: Int = 0,
    @Json(name = "total_unread_count") val totalUnreadCount: Int = 0,
    @Json(name = "unread_channels") val unreadChannels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationAddedToChannelEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "channel") val channel: DownstreamChannelDto,
    @Json(name = "total_unread_count") val totalUnreadCount: Int = 0,
    @Json(name = "unread_channels") val unreadchannels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationChannelDeletedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "channel") val channel: DownstreamChannelDto,
    @Json(name = "total_unread_count") val totalUnreadCount: Int = 0,
    @Json(name = "unread_channels") val unreadChannels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationChannelMutesUpdatedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "me") val me: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationChannelTruncatedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "channel") val channel: DownstreamChannelDto,
    @Json(name = "total_unread_count") val totalUnreadCount: Int = 0,
    @Json(name = "unread_channels") val unreadChannels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationInviteAcceptedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "member") val member: DownstreamMemberDto,
    @Json(name = "channel") val channel: DownstreamChannelDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationInviteRejectedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "member") val member: DownstreamMemberDto,
    @Json(name = "channel") val channel: DownstreamChannelDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationInvitedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "member") val member: DownstreamMemberDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationMarkReadEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "total_unread_count") val totalUnreadCount: Int = 0,
    @Json(name = "unread_channels") val unreadChannels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MarkAllReadEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "total_unread_count") val totalUnreadCount: Int = 0,
    @Json(name = "unread_channels") val unreadChannels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationMessageNewEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "channel") val channel: DownstreamChannelDto,
    @Json(name = "message") val message: DownstreamMessageDto,
    @Json(name = "total_unread_count") val totalUnreadCount: Int = 0,
    @Json(name = "unread_channels") val unreadChannels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationMutesUpdatedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "me") val me: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationRemovedFromChannelEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto?,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "channel") val channel: DownstreamChannelDto,
    @Json(name = "member") val member: DownstreamMemberDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ReactionDeletedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "message") val message: DownstreamMessageDto,
    @Json(name = "reaction") val reaction: DownstreamReactionDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ReactionNewEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "message") val message: DownstreamMessageDto,
    @Json(name = "reaction") val reaction: DownstreamReactionDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ReactionUpdateEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "message") val message: DownstreamMessageDto,
    @Json(name = "reaction") val reaction: DownstreamReactionDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class TypingStartEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "parent_id") val parentId: String?,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class TypingStopEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "parent_id") val parentId: String?,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelUserBannedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "expiration") val expiration: Date?,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class GlobalUserBannedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "created_at") val createdAt: Date,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UserDeletedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UserPresenceChangedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UserStartWatchingEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "cid") val cid: String,
    @Json(name = "watcher_count") val watcherCount: Int = 0,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "user") val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UserStopWatchingEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "cid") val cid: String,
    @Json(name = "watcher_count") val watcherCount: Int = 0,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "user") val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelUserUnbannedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
    @Json(name = "cid") val cid: String,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "channel_id") val channelId: String,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class GlobalUserUnbannedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UserUpdatedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ConnectedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "me") val me: DownstreamUserDto,
    @Json(name = "connection_id") val connectionId: String,
) : ChatEventDto()

/**
 * Special upstream event class, as we have to send this event
 * after connecting.
 */
@JsonClass(generateAdapter = true)
internal data class UpstreamConnectedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "me") val me: UpstreamUserDto,
    @Json(name = "connection_id") val connectionId: String,
)

@JsonClass(generateAdapter = true)
internal data class ConnectingEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "createdAt") val createdAt: Date,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class DisconnectedEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "createdAt") val createdAt: Date,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ErrorEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "createdAt") val createdAt: Date,
    @Json(name = "error") val error: ChatError,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UnknownEventDto(
    @Json(name = "type") val type: String,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "user") val user: DownstreamUserDto?,
    @Json(name = "rawData") val rawData: Map<*, *>,
) : ChatEventDto()
