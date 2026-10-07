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
import io.getstream.chat.android.network.models.ChannelMemberResponse
import io.getstream.chat.android.network.models.DraftResponse
import io.getstream.chat.android.network.models.OwnUserResponse
import io.getstream.chat.android.network.models.ReactionResponse
import io.getstream.chat.android.network.models.UserResponseCommonFields
import io.getstream.chat.android.network.models.UserResponsePrivacyFields
import io.getstream.chat.android.network.models.WSEvent
import io.getstream.result.Error
import java.util.Date

internal sealed class ChatEventDto

@JsonClass(generateAdapter = true)
internal data class ChannelHiddenEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
    val channel: DownstreamChannelDto,
    // Events replayed by /sync on backends before v239.47.0 omit it.
    val clear_history: Boolean = false,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelVisibleEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
    // Events replayed by /sync on backends before v239.47.0 omit it.
    val channel: DownstreamChannelDto?,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class HealthEventDto(
    val type: String,
    val created_at: ExactDate,
    val connection_id: String,
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
    val grouped_unread_channels: Map<String, Int>? = null,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class DraftMessageUpdatedEventDto(
    val type: String,
    val created_at: ExactDate,
    val draft: DraftResponse,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class DraftMessageDeletedEventDto(
    val type: String,
    val created_at: ExactDate,
    val draft: DraftResponse,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationAddedToChannelEventDto(
    val type: String,
    val created_at: ExactDate,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val channel: DownstreamChannelDto,
    val member: ChannelMemberResponse,
    val total_unread_count: Int = 0,
    val unread_channels: Int = 0,
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
    val reaction: ReactionResponse,
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
    val reaction: ReactionResponse,
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
    val reaction: ReactionResponse,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UserUpdatedEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: UserResponsePrivacyFields,
) : ChatEventDto()

/**
 * An event parsed with its generated model, which the event mapping turns into the domain event.
 */
internal data class GeneratedEventDto(val event: WSEvent) : ChatEventDto()

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
    val user: UserResponseCommonFields,
    val cid: String?,
    val channel_type: String?,
    val channel_id: String?,
    val hard_delete: Boolean?,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ConnectedEventDto(
    val type: String,
    val created_at: ExactDate,
    val me: OwnUserResponse,
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
