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
import java.util.Date

/**
 * The DTO for a thread.
 * Corresponds to [ThreadStateResponse].
 *
 * @param active_participant_count The number of active participants.
 * @param channel The channel info.
 * @param channel_cid The channel CID.
 * @param created_at The date when the thread was created.
 * @param created_by The user who created the thread.
 * @param created_by_user_id The ID of the user who created the thread.
 * @param deleted_at The date when the thread was deleted.
 * @param draft The draft message in the thread.
 * @param last_message_at The date of the last message in the thread.
 * @param latest_replies The latest replies in the thread.
 * @param parent_message The parent message.
 * @param parent_message_id The parent message ID.
 * @param participant_count The number of participants in the thread.
 * @param read The read states of the thread.
 * @param reply_count The number of replies in the thread.
 * @param thread_participants The participants in the thread.
 * @param title The title of the thread.
 * @param updated_at The date when the thread was updated.
 * @param extraData Any additional data.
 */
@JsonClass(generateAdapter = true)
internal data class DownstreamThreadDto(
    val active_participant_count: Int?,
    val channel: DownstreamChannelDto?,
    val channel_cid: String,
    val created_at: Date,
    val created_by: DownstreamUserDto?,
    val created_by_user_id: String,
    val deleted_at: Date?,
    val draft: DownstreamDraftDto?,
    val last_message_at: Date,
    val latest_replies: List<DownstreamMessageDto>,
    val parent_message: DownstreamMessageDto,
    val parent_message_id: String,
    val participant_count: Int,
    val read: List<DownstreamChannelUserRead>?,
    val reply_count: Int?,
    val thread_participants: List<DownstreamThreadParticipantDto>?,
    val title: String,
    val updated_at: Date,
    val extraData: Map<String, Any>,
) : ExtraDataDto

/**
 * The DTO for a shortened thread info.
 * Corresponds to [ThreadResponse].
 *
 * @param channel_cid The channel CID.
 * @param channel The channel info.
 * @param parent_message_id The parent message ID.
 * @param parent_message The parent message.
 * @param created_by_user_id The ID of the user who created the thread.
 * @param created_by The user who created the thread.
 * @param reply_count The number of replies in the thread.
 * @param participant_count The number of participants in the thread.
 * @param active_participant_count The number of active participants.
 * @param thread_participants The participants in the thread.
 * @param last_message_at The date of the last message in the thread.
 * @param created_at The date when the thread was created.
 * @param updated_at The date when the thread was updated.
 * @param deleted_at The date when the thread was deleted.
 * @param title The title of the thread.
 * @param extraData Any additional data.
 */
@JsonClass(generateAdapter = true)
internal data class DownstreamThreadInfoDto(
    val channel_cid: String,
    val channel: DownstreamChannelDto?,
    val parent_message_id: String,
    val parent_message: DownstreamMessageDto?,
    val created_by_user_id: String,
    val created_by: DownstreamUserDto?,
    val reply_count: Int?,
    val participant_count: Int?,
    val active_participant_count: Int?,
    val thread_participants: List<DownstreamThreadParticipantDto>?,
    val last_message_at: Date?,
    val created_at: Date,
    val updated_at: Date,
    val deleted_at: Date?,
    val title: String,
    val extraData: Map<String, Any>,
) : ExtraDataDto

/**
 * The DTO for Thread Participant.
 *
 * @param user_id The ID of the user (thread participant).
 * @param user The user as the thread participant. (Note: It is not always delivered, sometimes we only get the ID of
 * the user - [user_id]).
 */
@JsonClass(generateAdapter = true)
internal data class DownstreamThreadParticipantDto(
    val user_id: String,
    val user: DownstreamUserDto?,
)
