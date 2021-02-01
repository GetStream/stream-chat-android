package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.api2.model.dto.ChannelInfoDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.ReactionDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.UserDto
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.ChannelInfo
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
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

internal fun User.toDto(): UserDto =
    UserDto(
        id = id,
        role = role,
        invisible = invisible,
        banned = banned,
    )

internal fun UserDto.toDomain(): User =
    User(
        id = id,
        role = role,
        invisible = invisible,
        banned = banned,
    )

internal fun Attachment.toDto(): AttachmentDto =
    AttachmentDto(
        author_name = authorName,
        title_link = titleLink,
        thumb_url = thumbUrl,
        image_url = imageUrl,
        asset_url = assetUrl,
        og_scrape_url = ogUrl,
        mime_type = mimeType,
        file_size = fileSize,
        title = title,
        text = text,
        type = type,
        image = image,
        url = url,
        name = name,
        fallback = fallback,
    )

internal fun AttachmentDto.toDomain(): Attachment =
    Attachment(
        authorName = author_name,
        titleLink = title_link,
        thumbUrl = thumb_url,
        imageUrl = image_url,
        assetUrl = asset_url,
        ogUrl = og_scrape_url,
        mimeType = mime_type,
        fileSize = file_size,
        title = title,
        text = text,
        type = type,
        image = image,
        url = url,
        name = name,
        fallback = fallback,
    )

internal fun Reaction.toDto(): ReactionDto =
    ReactionDto(
        message_id = messageId,
        type = type,
        score = score,
        user = user?.toDto(),
        user_id = userId,
        created_at = createdAt,
        updated_at = updatedAt,
    )

internal fun ReactionDto.toDomain(): Reaction =
    Reaction(
        messageId = message_id,
        type = type,
        score = score,
        user = user?.toDomain(),
        userId = user_id,
        createdAt = created_at,
        updatedAt = updated_at,
    )

internal fun ChannelInfoDto.toDomain(): ChannelInfo =
    ChannelInfo(
        cid = cid,
        id = id,
        type = type,
        memberCount = member_count,
        name = name,
    )
