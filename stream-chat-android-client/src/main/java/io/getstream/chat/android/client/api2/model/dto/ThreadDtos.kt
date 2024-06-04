/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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
 *
 * @param channel_cid: The channel CID.
 * @param channel: The channel info.
 * @param parent_message_id: The parent message ID.
 * @param parent_message: The parent message.
 * @param created_by_user_id: The ID of the user who created the thread.
 * @param created_by: The user who created the thread.
 * @param reply_count: The number of replies in the thread.
 * @param participant_count: The number of participants in the thread.
 * @param thread_participants: The participants in the thread.
 * @param last_message_at: The date of the last message in the thread.
 * @param created_at: The date when the thread was created.
 * @param updated_at: The date when the thread was updated.
 * @param title: The title of the thread.
 * @param latest_replies: The latest replies in the thread.
 * @param read: The read states of the thread.
 */
@JsonClass(generateAdapter = true)
internal data class DownstreamThreadDto(
    val channel_cid: String,
    val channel: ChannelInfoDto,
    val parent_message_id: String,
    val parent_message: DownstreamMessageDto,
    val created_by_user_id: String,
    val created_by: DownstreamUserDto,
    val reply_count: Int,
    val participant_count: Int,
    val thread_participants: List<DownstreamThreadParticipantDto>,
    val last_message_at: Date,
    val created_at: Date,
    val updated_at: Date?,
    val title: String,
    val latest_replies: List<DownstreamMessageDto>,
    val read: List<DownstreamChannelUserRead>,
)

/**
 * The DTO for Thread Participant.
 *
 * @param channel_cid: The channel CID.
 *
 */
@JsonClass(generateAdapter = true)
internal data class DownstreamThreadParticipantDto(
    val channel_cid: String,
    val user_id: String,
    val user: DownstreamUserDto,
)
