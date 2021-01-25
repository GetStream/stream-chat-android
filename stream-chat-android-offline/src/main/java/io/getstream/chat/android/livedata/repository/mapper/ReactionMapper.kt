package io.getstream.chat.android.livedata.repository.mapper

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.entity.ReactionEntity

internal fun Reaction.toEntity(): ReactionEntity {
    val reactionEntity = ReactionEntity(messageId, fetchUserId(), type)
    reactionEntity.score = score
    reactionEntity.createdAt = createdAt
    reactionEntity.updatedAt = updatedAt
    reactionEntity.deletedAt = deletedAt
    reactionEntity.extraData = extraData
    reactionEntity.syncStatus = syncStatus
    reactionEntity.enforceUnique = enforceUnique
    return reactionEntity
}

internal suspend fun ReactionEntity.toModel(getUser: suspend (userId: String) -> User): Reaction = Reaction(
    messageId = messageId,
    type = type,
    score = score,
    user = getUser(userId),
    extraData = extraData,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    syncStatus = syncStatus,
    userId = userId,
    enforceUnique = enforceUnique
)
