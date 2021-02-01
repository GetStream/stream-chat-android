package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.ReactionDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.UserDto
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User

internal fun Message.toDto(): UpstreamMessageDto =
    UpstreamMessageDto(
        id = id,
        cid = cid,
        text = text,
        html = html,
        parent_id = parentId,
        command = command,
        user = user.toDto(),
        silent = silent,
        shadowed = shadowed,
        show_in_channel = showInChannel,
        mentioned_users = mentionedUsersIds,
        attachments = attachments.map(Attachment::toDto),
        quoted_message = replyTo?.toDto(),
        quoted_message_id = replyMessageId,
        pinned = pinned,
        pinned_at = pinnedAt,
        pin_expires = pinExpires,
        pinned_by = pinnedBy?.toDto(),
        thread_participants = threadParticipants.map(User::toDto),
        extraData = extraData,
    )

internal fun DownstreamMessageDto.toDomain(): Message =
    Message(
        id = id,
        cid = cid,
        type = type,
        text = text,
        html = html,
        parentId = parent_id,
        command = command,
        user = user.toDomain(),
        replyCount = reply_count,
        reactionCounts = reaction_counts.toMutableMap(),
        reactionScores = reaction_scores.toMutableMap(),
        latestReactions = latest_reactions.mapTo(mutableListOf(), ReactionDto::toDomain),
        ownReactions = own_reactions.mapTo(mutableListOf(), ReactionDto::toDomain),
        silent = silent,
        shadowed = shadowed,
        showInChannel = show_in_channel,
        mentionedUsers = mentioned_users.mapTo(mutableListOf(), UserDto::toDomain),
        i18n = i18n,
        threadParticipants = thread_participants.map(UserDto::toDomain),
        attachments = attachments.mapTo(mutableListOf(), AttachmentDto::toDomain),
        createdAt = created_at,
        updatedAt = updated_at,
        deletedAt = deleted_at,
        replyTo = quoted_message?.toDomain(),
        replyMessageId = quoted_message_id,
        pinned = pinned,
        pinnedAt = pinned_at,
        pinExpires = pin_expires,
        pinnedBy = pinned_by?.toDomain(),
        channelInfo = channel?.toDomain(),
        extraData = extraData.toMutableMap(),
    )
