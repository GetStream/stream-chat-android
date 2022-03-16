package io.getstream.chat.android.offline.repository.domain.message.internal

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.AttachmentEntity
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.toEntity
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.toModel
import io.getstream.chat.android.offline.repository.domain.message.channelinfo.internal.toEntity
import io.getstream.chat.android.offline.repository.domain.message.channelinfo.internal.toModel
import io.getstream.chat.android.offline.repository.domain.reaction.internal.toEntity
import io.getstream.chat.android.offline.repository.domain.reaction.internal.toModel

internal suspend fun MessageEntity.toModel(
    getUser: suspend (userId: String) -> User,
    getMessage: suspend (messageId: String) -> Message?,
): Message = with(messageInnerEntity) {
    Message(
        id = id,
        cid = cid,
        user = getUser(userId),
        text = text,
        html = html,
        attachments = attachments.map(AttachmentEntity::toModel).toMutableList(),
        type = type,
        replyCount = replyCount,
        createdAt = createdAt,
        createdLocallyAt = createdLocallyAt,
        updatedAt = updatedAt,
        updatedLocallyAt = updatedLocallyAt,
        deletedAt = deletedAt,
        parentId = parentId,
        command = command,
        extraData = extraData.toMutableMap(),
        reactionCounts = reactionCounts.toMutableMap(),
        reactionScores = reactionScores.toMutableMap(),
        syncStatus = syncStatus,
        shadowed = shadowed,
        latestReactions = (latestReactions.map { it.toModel(getUser) }).toMutableList(),
        ownReactions = (ownReactions.map { it.toModel(getUser) }).toMutableList(),
        mentionedUsers = mentionedUsersId.map { getUser(it) }.toMutableList(),
        replyTo = replyToId?.let { getMessage(it) },
        replyMessageId = replyToId,
        threadParticipants = threadParticipantsIds.map { getUser(it) },
        showInChannel = showInChannel,
        silent = silent,
        channelInfo = channelInfo?.toModel(),
        pinned = pinned,
        pinnedAt = pinnedAt,
        pinExpires = pinExpires,
        pinnedBy = pinnedByUserId?.let { getUser(it) }
    )
}

internal fun Message.toEntity(): MessageEntity = MessageEntity(
    messageInnerEntity = MessageInnerEntity(
        id = id,
        cid = cid,
        userId = user.id,
        text = text,
        html = html,
        syncStatus = syncStatus,
        type = type,
        replyCount = replyCount,
        createdAt = createdAt,
        createdLocallyAt = createdLocallyAt,
        updatedAt = updatedAt,
        updatedLocallyAt = updatedLocallyAt,
        deletedAt = deletedAt,
        parentId = parentId,
        command = command,
        extraData = extraData,
        reactionCounts = reactionCounts,
        reactionScores = reactionScores,
        shadowed = shadowed,
        mentionedUsersId = mentionedUsers.map(User::id),
        replyToId = replyTo?.id ?: replyMessageId,
        threadParticipantsIds = threadParticipants.map(User::id),
        showInChannel = showInChannel,
        silent = silent,
        channelInfo = channelInfo?.toEntity(),
        pinned = pinned,
        pinnedAt = pinnedAt,
        pinExpires = pinExpires,
        pinnedByUserId = pinnedBy?.id,
    ),
    attachments = attachments.mapIndexed { index, attachment -> attachment.toEntity(id, index) },
    latestReactions = latestReactions.map(Reaction::toEntity),
    ownReactions = ownReactions.map(Reaction::toEntity),
)
