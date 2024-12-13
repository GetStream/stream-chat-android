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

import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto
import io.getstream.chat.android.core.internal.StreamHandsOff
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserId
import java.util.Date

internal fun Message.toDto(): UpstreamMessageDto =
    UpstreamMessageDto(
        attachments = attachments.map(Attachment::toDto),
        cid = cid,
        command = command,
        html = html,
        id = id,
        mentioned_users = mentionedUsersIds,
        parent_id = parentId,
        pin_expires = pinExpires,
        pinned = pinned,
        pinned_at = pinnedAt,
        pinned_by = pinnedBy?.toDto(),
        quoted_message_id = replyMessageId,
        shadowed = shadowed,
        show_in_channel = showInChannel,
        silent = silent,
        text = text,
        thread_participants = threadParticipants.map(User::toDto),
        extraData = extraData,
    )

internal fun DownstreamMessageDto.toDomain(currentUserId: UserId?): Message =
    Message(
        attachments = attachments.mapTo(mutableListOf(), AttachmentDto::toDomain),
        channelInfo = channel?.toDomain(),
        cid = cid,
        command = command,
        createdAt = created_at,
        deletedAt = deleted_at,
        html = html,
        i18n = i18n,
        id = id,
        latestReactions = latest_reactions.toDomain(currentUserId, id),
        mentionedUsers = mentioned_users.mapTo(mutableListOf()) { it.toDomain(currentUserId) },
        ownReactions = own_reactions.toDomain(currentUserId, id),
        parentId = parent_id,
        pinExpires = pin_expires,
        pinned = pinned,
        pinnedAt = pinned_at,
        pinnedBy = pinned_by?.toDomain(currentUserId),
        reactionCounts = reaction_counts.orEmpty().toMutableMap(),
        reactionScores = reaction_scores.orEmpty().toMutableMap(),
        reactionGroups = reaction_groups.orEmpty().mapValues { it.value.toDomain(it.key) },
        replyCount = reply_count,
        deletedReplyCount = deleted_reply_count,
        replyMessageId = quoted_message_id,
        replyTo = quoted_message?.toDomain(currentUserId),
        shadowed = shadowed,
        showInChannel = show_in_channel,
        silent = silent,
        text = text,
        threadParticipants = thread_participants.map { it.toDomain(currentUserId) },
        type = type,
        updatedAt = lastUpdateTime(),
        user = user.toDomain(currentUserId),
        moderationDetails = moderation_details?.toDomain(),
        moderation = moderation?.toDomain(),
        messageTextUpdatedAt = message_text_updated_at,
        poll = poll?.toDomain(currentUserId),
        extraData = extraData.toMutableMap(),
    )

/**
 * Map a list of [DownstreamReactionDto] to a list of [Reaction].
 * They are filtered by [messageId] and mapped to domain model.
 *
 * @param currentUserId the current user id
 * @param messageId the message id
 */
@StreamHandsOff(
    reason = "Backend response is including wrong reactions for the message, so we need to filter them manually.",

)
private fun List<DownstreamReactionDto>.toDomain(
    currentUserId: UserId?,
    messageId: String,
): List<Reaction> =
    filter { it.message_id == messageId }
        .map { it.toDomain(currentUserId) }

private fun DownstreamMessageDto.lastUpdateTime(): Date = listOfNotNull(
    updated_at,
    poll?.updated_at,
).maxBy { it.time }
