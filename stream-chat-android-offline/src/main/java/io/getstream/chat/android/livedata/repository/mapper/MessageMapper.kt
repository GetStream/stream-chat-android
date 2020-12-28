package io.getstream.chat.android.livedata.repository.mapper

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.entity.MessageEntity

internal suspend fun MessageEntity.toModel(
    getUser: suspend (userId: String) -> User,
    getMessage: suspend (messageId: String) -> Message?
): Message = Message(
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
    mentionedUsers = mentionedUsersId.map { getUser(it) }.toMutableList(),
    replyTo = replyToId?.let { getMessage(it) }
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
    mentionedUsersId = mentionedUsers.map(User::id),
    replyToId = replyTo?.id
)
