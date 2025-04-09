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

import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
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
    getPoll: suspend (pollId: String) -> Poll?,
): Message = with(messageInnerEntity) {
    Message(
        id = id,
        cid = cid,
        user = getUser(userId),
        text = text,
        html = html,
        attachments = attachments.map(AttachmentEntity::toModel),
        type = type,
        replyCount = replyCount,
        deletedReplyCount = deletedReplyCount,
        createdAt = createdAt,
        createdLocallyAt = createdLocallyAt,
        updatedAt = updatedAt,
        updatedLocallyAt = updatedLocallyAt,
        deletedAt = deletedAt,
        parentId = parentId,
        command = command,
        extraData = extraData,
        reactionCounts = reactionCounts,
        reactionScores = reactionScores.toMutableMap(),
        reactionGroups = reactionGroups.mapValues { it.value.toModel() },
        syncStatus = syncStatus,
        shadowed = shadowed,
        i18n = i18n,
        latestReactions = (latestReactions.map { it.toModel(getUser) }),
        ownReactions = (ownReactions.map { it.toModel(getUser) }),
        mentionedUsers = remoteMentionedUserIds.map { getUser(it) },
        mentionedUsersIds = mentionedUsersId,
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
        moderationDetails = moderationDetails?.toModel(),
        moderation = moderation?.toDomain(),
        messageTextUpdatedAt = messageTextUpdatedAt,
        restrictedVisibility = restrictedVisibility,
        poll = pollId?.let { getPoll(it) },
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
        deletedReplyCount = deletedReplyCount,
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
        reactionGroups = reactionGroups.mapValues { it.value.toEntity() },
        shadowed = shadowed,
        i18n = i18n,
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
        moderationDetails = moderationDetails?.toEntity(),
        moderation = moderation?.toEntity(),
        messageTextUpdatedAt = messageTextUpdatedAt,
        pollId = poll?.id,
    ),
    attachments = attachments.mapIndexed { index, attachment -> attachment.toEntity(id, index) },
    latestReactions = latestReactions.map(Reaction::toEntity),
    ownReactions = ownReactions.map(Reaction::toEntity),
)

internal suspend fun ReplyMessageEntity.toModel(
    getUser: suspend (userId: String) -> User,
    getPoll: suspend (pollId: String) -> Poll?,
): Message {
    val entity = this
    return this.replyMessageInnerEntity.run {
        Message(
            id = id,
            cid = cid,
            user = getUser(userId),
            text = text,
            html = html,
            attachments = entity.attachments.map { it.toModel() },
            type = type,
            replyCount = replyCount,
            deletedReplyCount = deletedReplyCount,
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            updatedAt = updatedAt,
            updatedLocallyAt = updatedLocallyAt,
            deletedAt = deletedAt,
            parentId = parentId,
            command = command,
            syncStatus = syncStatus,
            shadowed = shadowed,
            i18n = i18n,
            latestReactions = mutableListOf(),
            ownReactions = mutableListOf(),
            mentionedUsers = remoteMentionedUserIds.map { getUser(it) },
            mentionedUsersIds = mentionedUsersId,
            replyTo = null,
            replyMessageId = null,
            threadParticipants = threadParticipantsIds.map { getUser(it) },
            showInChannel = showInChannel,
            silent = silent,
            pinned = pinned,
            pinnedAt = pinnedAt,
            pinExpires = pinExpires,
            pinnedBy = pinnedByUserId?.let { getUser(it) },
            moderationDetails = moderationDetails?.toModel(),
            messageTextUpdatedAt = messageTextUpdatedAt,
            poll = pollId?.let { getPoll(it) },
            restrictedVisibility = restrictedVisibility,
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
            type = type,
            replyCount = replyCount,
            deletedReplyCount = deletedReplyCount,
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            updatedAt = updatedAt,
            updatedLocallyAt = updatedLocallyAt,
            deletedAt = deletedAt,
            parentId = parentId,
            command = command,
            shadowed = shadowed,
            i18n = i18n,
            remoteMentionedUserIds = mentionedUsers.map(User::id),
            mentionedUsersId = mentionedUsersIds,
            threadParticipantsIds = threadParticipants.map(User::id),
            showInChannel = showInChannel,
            silent = silent,
            pinned = pinned,
            pinnedAt = pinnedAt,
            pinExpires = pinExpires,
            pinnedByUserId = pinnedBy?.id,
            moderationDetails = moderationDetails?.toEntity(),
            pollId = poll?.id,
        ),
        attachments = attachments.mapIndexed { index, attachment -> attachment.toReplyEntity(id, index) },
    )

internal fun Poll.toEntity(): PollEntity = PollEntity(
    id = id,
    name = name,
    description = description,
    options = options.map { it.toEntity() },
    votes = votes.map { it.toEntity() },
    createdAt = createdAt,
    updatedAt = updatedAt,
    votingVisibility = votingVisibility.toEntity(),
    enforceUniqueVote = enforceUniqueVote,
    maxVotesAllowed = maxVotesAllowed,
    allowUserSuggestedOptions = allowUserSuggestedOptions,
    allowAnswers = allowAnswers,
    voteCountsByOption = voteCountsByOption,
    ownVotes = ownVotes.map { it.toEntity() },
    closed = closed,
    answers = answers.map { it.toEntity() },
)

internal fun Option.toEntity(): OptionEntity = OptionEntity(
    id = id,
    text = text,
)

internal fun Vote.toEntity(): VoteEntity = VoteEntity(
    id = id,
    optionId = optionId,
    pollId = pollId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    userId = user?.id,
)

private fun Answer.toEntity(): AnswerEntity = AnswerEntity(
    id = id,
    pollId = pollId,
    text = text,
    createdAt = createdAt,
    updatedAt = updatedAt,
    userId = user?.id,
)

private fun VotingVisibility.toEntity(): String = when (this) {
    VotingVisibility.ANONYMOUS -> "anonymous"
    VotingVisibility.PUBLIC -> "public"
}

internal suspend fun PollEntity.toModel(
    getUser: suspend (userId: String) -> User,
): Poll = Poll(
    id = id,
    name = name,
    description = description,
    options = options.map(OptionEntity::toModel),
    votes = votes.map { it.toModel(getUser) },
    createdAt = createdAt,
    updatedAt = updatedAt,
    votingVisibility = votingVisibility.toVotingVisibility(),
    enforceUniqueVote = enforceUniqueVote,
    maxVotesAllowed = maxVotesAllowed,
    allowUserSuggestedOptions = allowUserSuggestedOptions,
    allowAnswers = allowAnswers,
    voteCountsByOption = voteCountsByOption,
    ownVotes = ownVotes.map { it.toModel(getUser) },
    closed = closed,
    answers = answers.map { it.toModel(getUser) },
)

private fun OptionEntity.toModel(): Option = Option(
    id = id,
    text = text,
)

private suspend fun VoteEntity.toModel(
    getUser: suspend (userId: String) -> User,
): Vote = Vote(
    id = id,
    optionId = optionId,
    pollId = pollId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    user = userId?.let { getUser(it) },
)

private suspend fun AnswerEntity.toModel(
    getUser: suspend (userId: String) -> User,
): Answer = Answer(
    id = id,
    pollId = pollId,
    text = text,
    createdAt = createdAt,
    updatedAt = updatedAt,
    user = userId?.let { getUser(it) },
)

private fun String.toVotingVisibility(): VotingVisibility = when (this) {
    "public" -> VotingVisibility.PUBLIC
    "anonymous" -> VotingVisibility.ANONYMOUS
    else -> throw IllegalArgumentException("Unknown voting visibility: $this")
}

internal fun DraftMessage.toEntity(): DraftMessageEntity = DraftMessageEntity(
    id = id,
    cid = cid,
    parentId = parentId,
    mentionedUsersIds = mentionedUsersIds,
    silent = silent,
    showInChannel = showInChannel,
    replyMessageId = replyMessage?.id,
    text = text,
    extraData = extraData,
)

internal suspend fun DraftMessageEntity.toModel(
    getMessage: suspend (messageId: String) -> Message?,
): DraftMessage = DraftMessage(
    id = id,
    cid = cid,
    parentId = parentId,
    mentionedUsersIds = mentionedUsersIds,
    silent = silent,
    showInChannel = showInChannel,
    replyMessage = replyMessageId?.let { getMessage(it) },
    text = text,
    extraData = extraData,
)
