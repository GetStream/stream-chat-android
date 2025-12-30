/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.internal.offline.repository.domain.message.internal

import io.getstream.chat.android.internal.offline.randomMessageEntity
import io.getstream.chat.android.internal.offline.randomReactionGroupEntity
import io.getstream.chat.android.internal.offline.repository.domain.message.attachment.internal.toEntity
import io.getstream.chat.android.internal.offline.repository.domain.message.channelinfo.internal.toEntity
import io.getstream.chat.android.internal.offline.repository.domain.message.channelinfo.internal.toModel
import io.getstream.chat.android.internal.offline.repository.domain.reaction.internal.toEntity
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@Suppress("LongMethod")
internal class MessageMapperTest {

    @Suppress("LongMethod")
    @Test
    fun `Should map MessageEntity to Message correctly`() = runTest {
        val user = randomUser()
        val reactionGroups = mapOf(
            randomString() to randomReactionGroupEntity(),
            randomString() to randomReactionGroupEntity(),
        )
        val messageEntity = randomMessageEntity(
            reactionGroups = reactionGroups,
        )
        val expectedReactionGroups = reactionGroups.mapValues { it.value.toModel() }

        val expectedMessage = Message(
            id = messageEntity.messageInnerEntity.id,
            cid = messageEntity.messageInnerEntity.cid,
            user = user,
            text = messageEntity.messageInnerEntity.text,
            html = messageEntity.messageInnerEntity.html,
            attachments = emptyList(),
            type = messageEntity.messageInnerEntity.type,
            replyCount = messageEntity.messageInnerEntity.replyCount,
            deletedReplyCount = messageEntity.messageInnerEntity.deletedReplyCount,
            createdAt = messageEntity.messageInnerEntity.createdAt,
            createdLocallyAt = messageEntity.messageInnerEntity.createdLocallyAt,
            updatedAt = messageEntity.messageInnerEntity.updatedAt,
            updatedLocallyAt = messageEntity.messageInnerEntity.updatedLocallyAt,
            deletedAt = messageEntity.messageInnerEntity.deletedAt,
            parentId = messageEntity.messageInnerEntity.parentId,
            command = messageEntity.messageInnerEntity.command,
            extraData = messageEntity.messageInnerEntity.extraData,
            reactionCounts = messageEntity.messageInnerEntity.reactionCounts,
            reactionScores = emptyMap(),
            reactionGroups = expectedReactionGroups,
            syncStatus = messageEntity.messageInnerEntity.syncStatus,
            shadowed = messageEntity.messageInnerEntity.shadowed,
            i18n = messageEntity.messageInnerEntity.i18n,
            latestReactions = emptyList(),
            ownReactions = emptyList(),
            mentionedUsers = emptyList(),
            mentionedUsersIds = messageEntity.messageInnerEntity.mentionedUsersId,
            replyTo = null,
            replyMessageId = messageEntity.messageInnerEntity.replyToId,
            threadParticipants = emptyList(),
            showInChannel = messageEntity.messageInnerEntity.showInChannel,
            silent = messageEntity.messageInnerEntity.silent,
            channelInfo = messageEntity.messageInnerEntity.channelInfo?.toModel(),
            pinned = messageEntity.messageInnerEntity.pinned,
            pinnedAt = messageEntity.messageInnerEntity.pinnedAt,
            pinExpires = messageEntity.messageInnerEntity.pinExpires,
            pinnedBy = user,
            skipEnrichUrl = messageEntity.messageInnerEntity.skipEnrichUrl,
            skipPushNotification = messageEntity.messageInnerEntity.skipPushNotification,
            moderationDetails = messageEntity.messageInnerEntity.moderationDetails?.toModel(),
            moderation = messageEntity.messageInnerEntity.moderation?.toDomain(),
            messageTextUpdatedAt = messageEntity.messageInnerEntity.messageTextUpdatedAt,
            restrictedVisibility = messageEntity.messageInnerEntity.restrictedVisibility,
            poll = null,
            reminder = messageEntity.messageInnerEntity.reminder?.toModel(),
            sharedLocation = messageEntity.messageInnerEntity.sharedLocation?.run {
                Location(
                    messageId = messageId,
                    cid = cid,
                    userId = userId,
                    endAt = endAt,
                    latitude = latitude,
                    longitude = longitude,
                    deviceId = deviceId,
                )
            },
            channelRole = messageEntity.messageInnerEntity.channelRole,
            deletedForMe = messageEntity.messageInnerEntity.deletedForMe,
        )

        val result = messageEntity.toModel(
            getUser = { user },
            getReply = { null },
            getPoll = { null },
        )

        assertEquals(expectedMessage, result)
    }

    @Test
    fun `Should map Message to MessageEntity correctly`() = runTest {
        val message = randomMessage(
            replyTo = randomMessage(),
        )

        val expectedMessageEntity = MessageEntity(
            messageInnerEntity = MessageInnerEntity(
                id = message.id,
                cid = message.cid,
                userId = message.user.id,
                text = message.text,
                html = message.html,
                syncStatus = message.syncStatus,
                type = message.type,
                replyCount = message.replyCount,
                deletedReplyCount = message.deletedReplyCount,
                createdAt = message.createdAt,
                createdLocallyAt = message.createdLocallyAt,
                updatedAt = message.updatedAt,
                updatedLocallyAt = message.updatedLocallyAt,
                deletedAt = message.deletedAt,
                parentId = message.parentId,
                command = message.command,
                extraData = message.extraData,
                reactionCounts = message.reactionCounts,
                reactionScores = message.reactionScores,
                reactionGroups = message.reactionGroups.mapValues { it.value.toEntity() },
                shadowed = message.shadowed,
                i18n = message.i18n,
                remoteMentionedUserIds = message.mentionedUsers.map(User::id),
                mentionedUsersId = message.mentionedUsersIds,
                replyToId = message.replyTo?.id,
                threadParticipantsIds = message.threadParticipants.map(User::id),
                showInChannel = message.showInChannel,
                silent = message.silent,
                channelInfo = message.channelInfo?.toEntity(),
                pinned = message.pinned,
                pinnedAt = message.pinnedAt,
                pinExpires = message.pinExpires,
                pinnedByUserId = message.pinnedBy?.id,
                skipPushNotification = message.skipPushNotification,
                skipEnrichUrl = message.skipEnrichUrl,
                moderationDetails = message.moderationDetails?.toEntity(),
                moderation = message.moderation?.toEntity(),
                messageTextUpdatedAt = message.messageTextUpdatedAt,
                pollId = message.poll?.id,
                reminder = message.reminder?.toEntity(),
                sharedLocation = message.sharedLocation?.run {
                    LocationEntity(
                        messageId = message.id,
                        cid = message.cid,
                        userId = message.user.id,
                        endAt = endAt,
                        latitude = latitude,
                        longitude = longitude,
                        deviceId = deviceId,
                    )
                },
                channelRole = message.channelRole,
                deletedForMe = message.deletedForMe,
            ),
            attachments = message.attachments.mapIndexed { index, attachment ->
                attachment.toEntity(
                    message.id,
                    index,
                )
            },
            latestReactions = message.latestReactions.map { it.toEntity() },
            ownReactions = message.ownReactions.map { it.toEntity() },
        )

        val resultMessageEntity: MessageEntity = message.toEntity()

        assertEquals(expectedMessageEntity, resultMessageEntity)
    }
}
