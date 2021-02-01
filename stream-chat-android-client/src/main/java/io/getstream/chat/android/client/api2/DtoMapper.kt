package io.getstream.chat.android.client.api2

import io.getstream.chat.android.client.api2.model.dto.AttachmentDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.ReactionDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.UserDto
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User

internal class DtoMapper {

    fun toDto(message: Message?): UpstreamMessageDto? = message?.run {
        UpstreamMessageDto(
            id = id,
            cid = cid,
            text = text,
            html = html,
            parent_id = parentId,
            command = command,
            user = toDto(user)!!,
            silent = silent,
            shadowed = shadowed,
            show_in_channel = showInChannel,
            mentioned_users = mentionedUsersIds,
            attachments = attachments.map { toDto(it)!! },
            quoted_message = toDto(replyTo),
            quoted_message_id = replyMessageId,
            pinned = pinned,
            pinned_at = pinnedAt,
            pin_expires = pinExpires,
            pinned_by = toDto(pinnedBy),
            thread_participants = threadParticipants.map { toDto(it)!! },
            extraData = extraData,
        )
    }

    fun toDomain(message: DownstreamMessageDto?): Message? = message?.run {
        return Message(
            id = id,
            cid = cid,
            type = type,
            text = text,
            html = html,
            parentId = parent_id,
            command = command,
            user = toDomain(user)!!,
            replyCount = reply_count,
            reactionCounts = reaction_counts.toMutableMap(),
            reactionScores = reaction_scores.toMutableMap(),
            latestReactions = latest_reactions.mapTo(mutableListOf()) { toDomain(it)!! },
            ownReactions = own_reactions.mapTo(mutableListOf(), { toDomain(it)!! }),
            silent = silent,
            shadowed = shadowed,
            showInChannel = show_in_channel,
            mentionedUsers = mentioned_users.mapTo(mutableListOf()) { toDomain(it)!! },
            i18n = i18n,
            threadParticipants = thread_participants.map { toDomain(it)!! },
            attachments = attachments.mapTo(mutableListOf()) { toDomain(it)!! },
            createdAt = created_at,
            updatedAt = updated_at,
            deletedAt = deleted_at,
            replyTo = toDomain(quoted_message),
            replyMessageId = quoted_message_id,
            pinned = pinned,
            pinnedAt = pinned_at,
            pinExpires = pin_expires,
            pinnedBy = toDomain(pinned_by),
            channelInfo = channel,
            extraData = extraData.toMutableMap(),
        )
    }

    fun toDto(user: User?): UserDto? = user?.run {
        UserDto(
            id = id,
            role = role,
            invisible = invisible,
            banned = banned,
        )
    }

    fun toDomain(user: UserDto?): User? = user?.run {
        return User(
            id = id,
            role = role,
            invisible = invisible,
            banned = banned,
        )
    }

    fun toDto(attachment: Attachment?): AttachmentDto? = attachment?.run {
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
    }

    fun toDomain(attachment: AttachmentDto?): Attachment? = attachment?.run {
        return Attachment(
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
    }

    fun toDto(reaction: Reaction?): ReactionDto? = reaction?.run {
        ReactionDto(
            message_id = messageId,
            type = type,
            score = score,
            user = toDto(user),
            user_id = userId,
            created_at = createdAt,
            updated_at = updatedAt,
        )
    }

    fun toDomain(reaction: ReactionDto?): Reaction? = reaction?.run {
        Reaction(
            messageId = message_id,
            type = type,
            score = score,
            user = toDomain(user),
            userId = user_id,
            createdAt = created_at,
            updatedAt = updated_at,
        )
    }
}
