package io.getstream.chat.android.offline.repository.domain.message

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.repository.domain.message.attachment.AttachmentEntity
import io.getstream.chat.android.offline.repository.domain.message.attachment.toEntity
import io.getstream.chat.android.offline.repository.domain.message.attachment.toModel
import io.getstream.chat.android.offline.repository.domain.reaction.toEntity
import io.getstream.chat.android.offline.repository.domain.reaction.toModel

internal suspend fun MessageEntity.toModel(
    getUser: suspend (userId: String) -> User,
    getMessage: suspend (messageId: String) -> Message?,
): Message = with(messageInnerEntity) {
    Message(
        id = id,
        cid = cid,
        user = getUser(userId),
        text = text,
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
        threadParticipants = threadParticipantsIds.map { getUser(it) },
    )
}

internal fun Message.toEntity(): MessageEntity = MessageEntity(
    messageInnerEntity = MessageInnerEntity(
        id = id,
        cid = cid,
        userId = user.id,
        text = text,
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
    ),
    attachments = attachments.map { it.toEntity(id) },
    latestReactions = latestReactions.map(Reaction::toEntity),
    ownReactions = ownReactions.map(Reaction::toEntity),
)
