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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionGroupDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamReactionDto
import io.getstream.chat.android.models.ChannelTransformer
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionGroup
import io.getstream.chat.android.models.UserId

internal fun Reaction.toDto(): UpstreamReactionDto =
    UpstreamReactionDto(
        created_at = createdAt,
        message_id = messageId,
        score = score,
        type = type,
        updated_at = updatedAt,
        user = user?.toDto(),
        user_id = userId,
        extraData = extraData,
    )

/**
 * Transforms [DownstreamReactionDto] to [Reaction].
 *
 * @param currentUserId the current user id.
 * @param channelTransformer the channel transformer to transform the channel.
 * @param messageTransformer the message transformer to transform the channel's messages.
 */
internal fun DownstreamReactionDto.toDomain(
    currentUserId: UserId?,
    channelTransformer: ChannelTransformer,
    messageTransformer: MessageTransformer,
): Reaction =
    Reaction(
        createdAt = created_at,
        messageId = message_id,
        score = score,
        type = type,
        updatedAt = updated_at,
        user = user?.toDomain(
            currentUserId = currentUserId,
            channelTransformer = channelTransformer,
            messageTransformer = messageTransformer,
        ),
        userId = user_id,
        extraData = extraData.toMutableMap(),
    )

internal fun DownstreamReactionGroupDto.toDomain(type: String): ReactionGroup =
    ReactionGroup(
        type = type,
        count = count,
        sumScore = sum_scores,
        firstReactionAt = first_reaction_at,
        lastReactionAt = last_reaction_at,
    )
