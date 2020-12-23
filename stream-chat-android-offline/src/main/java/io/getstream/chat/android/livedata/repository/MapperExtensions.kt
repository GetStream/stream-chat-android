package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.entity.ReactionEntity

internal fun Reaction.toEntity(): ReactionEntity {
    val reactionEntity = ReactionEntity(messageId, fetchUserId(), type)
    reactionEntity.score = score
    reactionEntity.createdAt = createdAt
    reactionEntity.updatedAt = updatedAt
    reactionEntity.extraData = extraData
    reactionEntity.syncStatus = syncStatus
    return reactionEntity
}

internal fun ReactionEntity.toModel(getUser: (userId: String) -> User): Reaction = Reaction(
    messageId = messageId,
    type = type,
    score = score,
    user = getUser(userId),
    extraData = extraData,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    userId = userId,
)

internal fun MessageEntity.toModel(getUser: (userId: String) -> User): Message = Message(
    id = id,
    cid = cid,
    user = getUser(userId),
    text = text,
    attachments = attachments.toMutableList(),
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
    mentionedUsers = mentionedUsersId.map(getUser).toMutableList(),
)

internal fun Message.toEntity(): MessageEntity = MessageEntity(
    id = id,
    cid = cid,
    userId = user.id,
    text = text,
    attachments = attachments,
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
    latestReactions = latestReactions.map(Reaction::toEntity),
    ownReactions = ownReactions.map(Reaction::toEntity),
    mentionedUsersId = mentionedUsers.map(User::id)
)
