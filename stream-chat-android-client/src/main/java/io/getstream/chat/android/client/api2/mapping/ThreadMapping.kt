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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadInfoDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamThreadParticipantDto
import io.getstream.chat.android.models.ChannelTransformer
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadInfo
import io.getstream.chat.android.models.ThreadParticipant
import io.getstream.chat.android.models.UserId

/**
 * Transforms [DownstreamThreadDto] into [Thread]
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
internal fun DownstreamThreadDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): Thread =
    Thread(
        activeParticipantCount = active_participant_count ?: 0,
        cid = channel_cid,
        channel = channel?.toDomain(
            currentUserId = currentUserId,
            eventChatLastMessageAt = null,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        parentMessageId = parent_message_id,
        parentMessage = parent_message.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        createdByUserId = created_by_user_id,
        createdBy = created_by?.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        participantCount = participant_count,
        threadParticipants = thread_participants.orEmpty().map {
            it.toDomain(
                currentUserId = currentUserId,
                channelTransformer = channelTransformer,
                messageTransformer = messageTransformer,
            )
        },
        lastMessageAt = last_message_at,
        createdAt = created_at,
        updatedAt = updated_at,
        deletedAt = deleted_at,
        title = title,
        latestReplies = latest_replies.map {
            it.toDomain(
                currentUserId = currentUserId,
                channelTransformer = channelTransformer,
                messageTransformer = messageTransformer,
            )
        },
        read = read.orEmpty().map {
            it.toDomain(
                currentUserId = currentUserId,
                lastReceivedEventDate = last_message_at,
                channelTransformer = channelTransformer,
                messageTransformer = messageTransformer,
            )
        },
    )

/**
 * Transforms [DownstreamThreadInfoDto] into [ThreadInfo]
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
internal fun DownstreamThreadInfoDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): ThreadInfo =
    ThreadInfo(
        activeParticipantCount = active_participant_count ?: 0,
        cid = channel_cid,
        createdAt = created_at,
        createdBy = created_by?.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        createdByUserId = created_by_user_id,
        deletedAt = deleted_at,
        lastMessageAt = last_message_at,
        parentMessage = parent_message?.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        parentMessageId = parent_message_id,
        participantCount = participant_count ?: 0,
        replyCount = reply_count ?: 0,
        title = title,
        updatedAt = updated_at,
    )

/**
 * Transforms [DownstreamThreadParticipantDto] into [ThreadParticipant]
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
internal fun DownstreamThreadParticipantDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): ThreadParticipant = ThreadParticipant(
    user = user.toDomain(
        currentUserId = currentUserId,
        channelTransformer = channelTransformer,
        messageTransformer = messageTransformer,
    ),
)
