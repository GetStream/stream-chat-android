/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.repository.domain.threads.internal

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadParticipant
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.repository.domain.channel.userread.internal.toEntity
import io.getstream.chat.android.offline.repository.domain.channel.userread.internal.toModel

/**
 * Maps a domain [Thread] model to a database [ThreadEntity] model.
 */
internal fun Thread.toEntity() = ThreadEntity(
    parentMessageId = parentMessageId,
    cid = cid,
    createdByUserId = createdByUserId,
    activeParticipantCount = activeParticipantCount,
    participantCount = participantCount,
    threadParticipantIds = threadParticipants.map { it.user.id },
    lastMessageAt = lastMessageAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    title = title,
    read = read.map(ChannelUserRead::toEntity),
    latestReplyIds = latestReplies.map(Message::id),
    extraData = extraData,
)

/**
 * Maps a database [ThreadEntity] model to a domain [Thread] model.
 */
internal suspend fun ThreadEntity.toModel(
    getUser: suspend (userId: String) -> User,
    getMessage: suspend (messageId: String) -> Message?,
    getChannel: suspend (cid: String) -> Channel?,
    getDraftMessage: suspend (messageId: String) -> DraftMessage?,
) = Thread(
    parentMessageId = parentMessageId,
    parentMessage = getMessage(parentMessageId) ?: Message(),
    cid = cid,
    channel = getChannel(cid),
    createdByUserId = createdByUserId,
    createdBy = getUser(createdByUserId),
    activeParticipantCount = activeParticipantCount,
    participantCount = participantCount,
    threadParticipants = threadParticipantIds.map { userId ->
        ThreadParticipant(user = getUser(userId))
    },
    lastMessageAt = lastMessageAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    title = title,
    read = read.map { it.toModel(getUser) },
    latestReplies = latestReplyIds.mapNotNull { getMessage(it) },
    draft = getDraftMessage(parentMessageId),
    extraData = extraData,
)
