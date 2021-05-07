package io.getstream.chat.android.offline.repository.domain.channel

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.repository.domain.channel.member.MemberEntity
import io.getstream.chat.android.offline.repository.domain.channel.member.toEntity
import io.getstream.chat.android.offline.repository.domain.channel.member.toModel
import io.getstream.chat.android.offline.repository.domain.channel.userread.ChannelUserReadEntity
import io.getstream.chat.android.offline.repository.domain.channel.userread.toEntity
import io.getstream.chat.android.offline.repository.domain.channel.userread.toModel
import io.getstream.chat.android.offline.repository.domain.message.MessageEntity
import io.getstream.chat.android.offline.repository.domain.message.toEntity
import java.util.Date

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
        lastMessageId = lastMessage?.messageInnerEntity?.id,
        lastMessageAt = lastMessageAt,
        createdByUserId = createdBy.id,
        team = team,
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
    extraData = extraData.toMutableMap(),
    lastMessageAt = lastMessageAt,
    syncStatus = syncStatus,
    hidden = hidden,
    hiddenMessagesBefore = hideMessagesBefore,
    members = members.values.map { it.toModel(getUser) },
    messages = listOfNotNull(lastMessageId?.let { getMessage(it) }),
    read = reads.values.map { it.toModel(getUser) },
    createdBy = getUser(createdByUserId),
    team = team,
)
