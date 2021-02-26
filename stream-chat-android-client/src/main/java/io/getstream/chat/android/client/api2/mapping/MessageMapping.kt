package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User

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
        quoted_message = replyTo?.toDto(),
        quoted_message_id = replyMessageId,
        shadowed = shadowed,
        show_in_channel = showInChannel,
        silent = silent,
        text = text,
        thread_participants = threadParticipants.map(User::toDto),
        user = user.toDto(),
        extraData = extraData,
    )

internal fun DownstreamMessageDto.toDomain(): Message =
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
        latestReactions = latest_reactions.mapTo(mutableListOf(), DownstreamReactionDto::toDomain),
        mentionedUsers = mentioned_users.mapTo(mutableListOf(), DownstreamUserDto::toDomain),
        ownReactions = own_reactions.mapTo(mutableListOf(), DownstreamReactionDto::toDomain),
        parentId = parent_id,
        pinExpires = pin_expires,
        pinned = pinned,
        pinnedAt = pinned_at,
        pinnedBy = pinned_by?.toDomain(),
        reactionCounts = reaction_counts.toMutableMap(),
        reactionScores = reaction_scores.toMutableMap(),
        replyCount = reply_count,
        replyMessageId = quoted_message_id,
        replyTo = quoted_message?.toDomain(),
        shadowed = shadowed,
        showInChannel = show_in_channel,
        silent = silent,
        text = text,
        threadParticipants = thread_participants.map(DownstreamUserDto::toDomain),
        type = type,
        updatedAt = updated_at,
        user = user.toDomain(),
        extraData = extraData.toMutableMap(),
    )
