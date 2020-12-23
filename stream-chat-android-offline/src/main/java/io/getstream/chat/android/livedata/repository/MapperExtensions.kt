package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.entity.ChannelEntity
import io.getstream.chat.android.livedata.entity.ChannelUserReadEntity
import io.getstream.chat.android.livedata.entity.MemberEntity
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.entity.ReactionEntity
import java.util.Date

internal fun Reaction.toEntity(): ReactionEntity {
    val reactionEntity = ReactionEntity(messageId, fetchUserId(), type)
    reactionEntity.score = score
    reactionEntity.createdAt = createdAt
    reactionEntity.updatedAt = updatedAt
    reactionEntity.extraData = extraData
    reactionEntity.syncStatus = syncStatus
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
    syncStatus = syncStatus,
    userId = userId,
)

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

internal fun ChannelUserRead.toEntity(): ChannelUserReadEntity = ChannelUserReadEntity(getUserId(), lastRead)

internal suspend fun ChannelUserReadEntity.toModel(getUser: suspend (userId: String) -> User): ChannelUserRead =
    ChannelUserRead(getUser(userId), lastRead)

internal fun Member.toEntity(): MemberEntity = MemberEntity(
    userId = getUserId(),
    role = role ?: user.role,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isInvited = isInvited ?: false,
    inviteAcceptedAt = inviteAcceptedAt,
    inviteRejectedAt = inviteRejectedAt,
    shadowBanned = shadowBanned,
)

internal suspend fun MemberEntity.toModel(getUser: suspend (userId: String) -> User): Member = Member(
    user = getUser(userId),
    role = role,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isInvited = isInvited,
    inviteAcceptedAt = inviteAcceptedAt,
    inviteRejectedAt = inviteRejectedAt,
    shadowBanned = shadowBanned,
)

internal fun Channel.toEntity(): ChannelEntity {
    var lastMessage: MessageEntity? = null
    var lastMessageAt: Date? = null
    messages.lastOrNull()?.let { message ->
        lastMessage = message.toEntity()
        lastMessageAt = message.createdAt
    }
    return ChannelEntity(
        type = type,
        channelId = id,
        cooldown = cooldown,
        frozen = frozen,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        extraData = extraData,
        syncStatus = syncStatus,
        hidden = hidden,
        hideMessagesBefore = hiddenMessagesBefore,
        members = members.map(Member::toEntity).associateBy(MemberEntity::userId).toMutableMap(),
        reads = read.map(ChannelUserRead::toEntity).associateBy(ChannelUserReadEntity::userId).toMutableMap(),
        lastMessage = lastMessage,
        lastMessageAt = lastMessageAt,
        createdByUserId = createdBy.id,
    )
}

internal suspend fun ChannelEntity.toModel(
    getUser: suspend (userId: String) -> User,
    getMessage: suspend (messageId: String) -> Message?,
): Channel = Channel(
    cooldown = cooldown,
    type = type,
    id = channelId,
    cid = cid,
    frozen = frozen,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    extraData = extraData,
    lastMessageAt = lastMessageAt,
    syncStatus = syncStatus,
    hidden = hidden,
    hiddenMessagesBefore = hideMessagesBefore,
    members = members.values.map { it.toModel(getUser) },
    messages = listOfNotNull(lastMessage?.toModel(getUser, getMessage)),
    read = reads.values.map { it.toModel(getUser) },
    createdBy = getUser(createdByUserId)
)
