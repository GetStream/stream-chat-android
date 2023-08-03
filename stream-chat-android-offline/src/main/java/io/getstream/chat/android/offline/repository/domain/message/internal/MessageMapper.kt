/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.offline.repository.domain.message.internal

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageSyncDescription
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.AttachmentEntity
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.toEntity
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.toModel
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.toReplyEntity
import io.getstream.chat.android.offline.repository.domain.message.channelinfo.internal.toEntity
import io.getstream.chat.android.offline.repository.domain.message.channelinfo.internal.toModel
import io.getstream.chat.android.offline.repository.domain.reaction.internal.toEntity
import io.getstream.chat.android.offline.repository.domain.reaction.internal.toModel

internal suspend fun MessageEntity.toModel(
    getUser: suspend (userId: String) -> User,
    getReply: suspend (messageId: String) -> Message?,
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
        syncDescription = buildMessageSyncDescription(),
        shadowed = shadowed,
        latestReactions = (latestReactions.map { it.toModel(getUser) }).toMutableList(),
        ownReactions = (ownReactions.map { it.toModel(getUser) }).toMutableList(),
        mentionedUsers = remoteMentionedUserIds.map { getUser(it) }.toMutableList(),
        mentionedUsersIds = mentionedUsersId.toMutableList(),
        replyTo = replyToId?.let { getReply(it) },
        replyMessageId = replyToId,
        threadParticipants = threadParticipantsIds.map { getUser(it) },
        showInChannel = showInChannel,
        silent = silent,
        channelInfo = channelInfo?.toModel(),
        pinned = pinned,
        pinnedAt = pinnedAt,
        pinExpires = pinExpires,
        pinnedBy = pinnedByUserId?.let { getUser(it) },
        skipEnrichUrl = skipEnrichUrl,
        skipPushNotification = skipPushNotification,
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
        syncType = syncDescription?.type,
        syncContent = syncDescription?.content?.toEntity(),
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
        remoteMentionedUserIds = mentionedUsers.map(User::id),
        mentionedUsersId = mentionedUsersIds,
        replyToId = replyTo?.id ?: replyMessageId,
        threadParticipantsIds = threadParticipants.map(User::id),
        showInChannel = showInChannel,
        silent = silent,
        channelInfo = channelInfo?.toEntity(),
        pinned = pinned,
        pinnedAt = pinnedAt,
        pinExpires = pinExpires,
        pinnedByUserId = pinnedBy?.id,
        skipPushNotification = skipPushNotification,
        skipEnrichUrl = skipEnrichUrl,
    ),
    attachments = attachments.mapIndexed { index, attachment -> attachment.toEntity(id, index) },
    latestReactions = latestReactions.map(Reaction::toEntity),
    ownReactions = ownReactions.map(Reaction::toEntity),
)

internal suspend fun ReplyMessageEntity.toModel(
    getUser: suspend (userId: String) -> User,
): Message {
    val entity = this
    return this.replyMessageInnerEntity.run {
        Message(
            id = id,
            cid = cid,
            user = getUser(userId),
            text = text,
            html = html,
            attachments = entity.attachments.map { it.toModel() }.toMutableList(),
            type = type,
            replyCount = replyCount,
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            updatedAt = updatedAt,
            updatedLocallyAt = updatedLocallyAt,
            deletedAt = deletedAt,
            parentId = parentId,
            command = command,
            syncStatus = syncStatus,
            shadowed = shadowed,
            latestReactions = mutableListOf(),
            ownReactions = mutableListOf(),
            mentionedUsers = remoteMentionedUserIds.map { getUser(it) }.toMutableList(),
            mentionedUsersIds = mentionedUsersId.toMutableList(),
            replyTo = null,
            replyMessageId = null,
            threadParticipants = threadParticipantsIds.map { getUser(it) },
            showInChannel = showInChannel,
            silent = silent,
            pinned = pinned,
            pinnedAt = pinnedAt,
            pinExpires = pinExpires,
            pinnedBy = pinnedByUserId?.let { getUser(it) },
        )
    }
}

internal fun Message.toReplyEntity(): ReplyMessageEntity =
    ReplyMessageEntity(
        replyMessageInnerEntity = ReplyMessageInnerEntity(
            id = id,
            cid = cid,
            userId = user.id,
            text = text,
            html = html,
            syncStatus = syncStatus,
            syncType = syncDescription?.type,
            type = type,
            replyCount = replyCount,
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            updatedAt = updatedAt,
            updatedLocallyAt = updatedLocallyAt,
            deletedAt = deletedAt,
            parentId = parentId,
            command = command,
            shadowed = shadowed,
            remoteMentionedUserIds = mentionedUsers.map(User::id),
            mentionedUsersId = mentionedUsersIds,
            threadParticipantsIds = threadParticipants.map(User::id),
            showInChannel = showInChannel,
            silent = silent,
            pinned = pinned,
            pinnedAt = pinnedAt,
            pinExpires = pinExpires,
            pinnedByUserId = pinnedBy?.id,
        ),
        attachments = attachments.mapIndexed { index, attachment -> attachment.toReplyEntity(id, index) },
    )

private fun MessageEntity.buildMessageSyncDescription(): MessageSyncDescription? = with(messageInnerEntity) {
    if (syncType == null || syncContent == null) {
        return null
    }
    return MessageSyncDescription(
        syncType,
        syncContent.toModel(),
    )
}
