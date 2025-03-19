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

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.core.internal.StreamHandsOff
import java.util.Date

/**
 * See [io.getstream.chat.android.client.parser2.adapters.UpstreamMessageDtoAdapter] for
 * special [extraData] handling.
 */
@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
        "content of [extraData] map",
)
@JsonClass(generateAdapter = true)
internal data class UpstreamMessageDto(
    val attachments: List<AttachmentDto>,
    val cid: String,
    val command: String?,
    val html: String,
    val id: String,
    val type: String,
    val mentioned_users: List<String>,
    val parent_id: String?,
    val pin_expires: Date?,
    val pinned: Boolean?,
    val pinned_at: Date?,
    val pinned_by: UpstreamUserDto?,
    val quoted_message_id: String?,
    val shadowed: Boolean,
    val show_in_channel: Boolean,
    val silent: Boolean,
    val text: String,
    val thread_participants: List<UpstreamUserDto>,
    val restricted_visibility: List<String>,
    val extraData: Map<String, Any>,
) : ExtraDataDto

/**
 * See [io.getstream.chat.android.client.parser2.adapters.DownstreamMessageDtoAdapter] for
 * special [extraData] handling.
 */
@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
        "content of [extraData] map",
)
@JsonClass(generateAdapter = true)
internal data class DownstreamMessageDto(
    val attachments: List<AttachmentDto>,
    val channel: ChannelInfoDto?,
    val cid: String,
    val command: String?,
    val created_at: Date,
    val deleted_at: Date?,
    val html: String,
    val i18n: Map<String, String> = emptyMap(),
    val id: String,
    val latest_reactions: List<DownstreamReactionDto>,
    val mentioned_users: List<DownstreamUserDto>,
    val own_reactions: List<DownstreamReactionDto>,
    val parent_id: String?,
    val pin_expires: Date?,
    val pinned: Boolean = false,
    val pinned_at: Date?,
    val message_text_updated_at: Date?,
    val pinned_by: DownstreamUserDto?,
    val quoted_message: DownstreamMessageDto?,
    val quoted_message_id: String?,
    val reaction_counts: Map<String, Int>?,
    val reaction_scores: Map<String, Int>?,
    val reaction_groups: Map<String, DownstreamReactionGroupDto>?,
    val reply_count: Int,
    val deleted_reply_count: Int,
    val shadowed: Boolean = false,
    val show_in_channel: Boolean = false,
    val silent: Boolean,
    val text: String,
    val thread_participants: List<DownstreamUserDto> = emptyList(),
    val type: String,
    val updated_at: Date,
    val user: DownstreamUserDto,
    val moderation_details: DownstreamModerationDetailsDto? = null, // Used for Moderation V1
    val moderation: DownstreamModerationDto? = null, // Used for Moderation V2
    val poll: DownstreamPollDto? = null,

    val extraData: Map<String, Any>,
) : ExtraDataDto

@JsonClass(generateAdapter = true)
internal data class DownstreamDraftDto(
    val message: DownstreamDraftMessageDto,
    val channel_cid: String,
    val quoted_message: DownstreamMessageDto? = null,
    val parent_message: DownstreamMessageDto? = null,
)

@JsonClass(generateAdapter = true)
internal data class DownstreamDraftMessageDto(
    val id: String,
    val text: String,
    val attachments: List<AttachmentDto>? = null,
    val mentioned_users: List<DownstreamUserDto>? = null,
    val silent: Boolean = false,
    val show_in_channel: Boolean = false,

    val extraData: Map<String, Any>? = null,
)