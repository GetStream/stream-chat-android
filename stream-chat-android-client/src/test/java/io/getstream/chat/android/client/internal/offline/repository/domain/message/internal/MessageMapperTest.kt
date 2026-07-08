/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.internal.offline.repository.domain.message.internal

import io.getstream.chat.android.client.internal.offline.randomMessageEntity
import io.getstream.chat.android.client.internal.offline.randomReactionGroupEntity
import io.getstream.chat.android.client.internal.offline.randomReminderInfoEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.attachment.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.attachment.internal.toReplyEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.channelinfo.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.channelinfo.internal.toModel
import io.getstream.chat.android.client.internal.offline.repository.domain.reaction.internal.toEntity
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageReminderInfo
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomMessageReminderInfo
import io.getstream.chat.android.randomPoll
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
            mentionedHere = messageEntity.messageInnerEntity.mentionedHere,
            mentionedChannel = messageEntity.messageInnerEntity.mentionedChannel,
            mentionedRoles = messageEntity.messageInnerEntity.mentionedRoles,
            mentionedGroups = messageEntity.messageInnerEntity.mentionedGroups,
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
                mentionedHere = message.mentionedHere,
                mentionedChannel = message.mentionedChannel,
                mentionedRoles = message.mentionedRoles,
                mentionedGroups = message.mentionedGroups,
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

    @Test
    fun `Should map Message to MessageEntity with replyMessageId when replyTo is null`() = runTest {
        val replyMessageId = randomString()
        val message = randomMessage(
            replyTo = null,
            replyMessageId = replyMessageId,
        )

        val result = message.toEntity()

        assertEquals(replyMessageId, result.messageInnerEntity.replyToId)
    }

    @Test
    fun `Should map MessageEntity to Message with reply and poll`() = runTest {
        val user = randomUser()
        val reply = randomMessage()
        val poll = randomPoll()
        val messageEntity = randomMessageEntity(
            replyToId = reply.id,
            pollId = poll.id,
        )

        val result = messageEntity.toModel(
            getUser = { user },
            getReply = { messageId -> reply.takeIf { messageId == reply.id } },
            getPoll = { pollId -> poll.takeIf { pollId == poll.id } },
        )

        assertEquals(reply, result.replyTo)
        assertEquals(poll, result.poll)
    }

    @Test
    fun `Should map Message to ReplyMessageEntity correctly`() = runTest {
        val message = randomMessage(
            attachments = listOf(randomAttachment()),
            mentionedUsers = listOf(randomUser()),
        ).copy(
            // Diverge from the fixture default (mentioned user ids) so a mapping swap between
            // remoteMentionedUserIds and mentionedUsersId fails the test.
            mentionedUsersIds = listOf(randomString()),
        )

        val expectedReplyMessageEntity = ReplyMessageEntity(
            replyMessageInnerEntity = ReplyMessageInnerEntity(
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
                shadowed = message.shadowed,
                i18n = message.i18n,
                remoteMentionedUserIds = message.mentionedUsers.map(User::id),
                mentionedUsersId = message.mentionedUsersIds,
                mentionedHere = message.mentionedHere,
                mentionedChannel = message.mentionedChannel,
                mentionedGroups = message.mentionedGroups,
                mentionedRoles = message.mentionedRoles,
                threadParticipantsIds = message.threadParticipants.map(User::id),
                showInChannel = message.showInChannel,
                silent = message.silent,
                pinned = message.pinned,
                pinnedAt = message.pinnedAt,
                pinExpires = message.pinExpires,
                pinnedByUserId = message.pinnedBy?.id,
                moderationDetails = message.moderationDetails?.toEntity(),
                pollId = message.poll?.id,
                reminder = message.reminder?.toEntity(),
                channelRole = message.channelRole,
            ),
            attachments = message.attachments.mapIndexed { index, attachment ->
                attachment.toReplyEntity(
                    message.id,
                    index,
                )
            },
        )

        val result = message.toReplyEntity()

        assertEquals(expectedReplyMessageEntity, result)
    }

    @Test
    fun `Should map ReplyMessageEntity to Message correctly`() = runTest {
        val author = randomUser()
        val mentionedUser = randomUser()
        val threadParticipant = randomUser()
        val pinnedByUser = randomUser()
        val usersById = listOf(author, mentionedUser, threadParticipant, pinnedByUser).associateBy(User::id)
        val innerEntity = ReplyMessageInnerEntity(
            id = randomString(),
            cid = randomCID(),
            userId = author.id,
            text = randomString(),
            html = randomString(),
            type = randomString(),
            syncStatus = SyncStatus.COMPLETED,
            replyCount = randomInt(),
            deletedReplyCount = randomInt(),
            createdAt = randomDate(),
            createdLocallyAt = randomDate(),
            updatedAt = randomDate(),
            updatedLocallyAt = randomDate(),
            deletedAt = randomDate(),
            remoteMentionedUserIds = listOf(mentionedUser.id),
            mentionedUsersId = listOf(randomString()),
            mentionedHere = randomBoolean(),
            mentionedChannel = randomBoolean(),
            mentionedRoles = listOf(randomString()),
            parentId = randomString(),
            command = randomString(),
            shadowed = randomBoolean(),
            i18n = mapOf(randomString() to randomString()),
            showInChannel = randomBoolean(),
            silent = randomBoolean(),
            pinned = randomBoolean(),
            pinnedAt = randomDate(),
            pinExpires = randomDate(),
            pinnedByUserId = pinnedByUser.id,
            threadParticipantsIds = listOf(threadParticipant.id),
            messageTextUpdatedAt = randomDate(),
            pollId = null,
            restrictedVisibility = listOf(randomString()),
            reminder = randomReminderInfoEntity(),
            channelRole = randomString(),
        )
        val replyMessageEntity = ReplyMessageEntity(
            replyMessageInnerEntity = innerEntity,
            attachments = emptyList(),
        )

        val expectedMessage = Message(
            id = innerEntity.id,
            cid = innerEntity.cid,
            user = author,
            text = innerEntity.text,
            html = innerEntity.html,
            attachments = emptyList(),
            type = innerEntity.type,
            replyCount = innerEntity.replyCount,
            deletedReplyCount = innerEntity.deletedReplyCount,
            createdAt = innerEntity.createdAt,
            createdLocallyAt = innerEntity.createdLocallyAt,
            updatedAt = innerEntity.updatedAt,
            updatedLocallyAt = innerEntity.updatedLocallyAt,
            deletedAt = innerEntity.deletedAt,
            parentId = innerEntity.parentId,
            command = innerEntity.command,
            syncStatus = innerEntity.syncStatus,
            shadowed = innerEntity.shadowed,
            i18n = innerEntity.i18n,
            latestReactions = mutableListOf(),
            ownReactions = mutableListOf(),
            mentionedUsers = listOf(mentionedUser),
            mentionedUsersIds = innerEntity.mentionedUsersId,
            mentionedHere = innerEntity.mentionedHere,
            mentionedChannel = innerEntity.mentionedChannel,
            mentionedGroups = innerEntity.mentionedGroups,
            mentionedRoles = innerEntity.mentionedRoles,
            replyTo = null,
            replyMessageId = null,
            threadParticipants = listOf(threadParticipant),
            showInChannel = innerEntity.showInChannel,
            silent = innerEntity.silent,
            pinned = innerEntity.pinned,
            pinnedAt = innerEntity.pinnedAt,
            pinExpires = innerEntity.pinExpires,
            pinnedBy = pinnedByUser,
            moderationDetails = null,
            messageTextUpdatedAt = innerEntity.messageTextUpdatedAt,
            poll = null,
            restrictedVisibility = innerEntity.restrictedVisibility,
            channelInfo = null,
            reminder = innerEntity.reminder?.toModel(),
            channelRole = innerEntity.channelRole,
        )

        val result = replyMessageEntity.toModel(
            getUser = usersById::getValue,
            getPoll = { null },
        )

        assertEquals(expectedMessage, result)
    }

    @Test
    fun `Should map DraftMessage to DraftMessageEntity correctly`() = runTest {
        val draftMessage = randomDraftMessage()

        val expectedDraftMessageEntity = DraftMessageEntity(
            id = draftMessage.id,
            cid = draftMessage.cid,
            parentId = draftMessage.parentId,
            mentionedUsersIds = draftMessage.mentionedUsersIds,
            silent = draftMessage.silent,
            showInChannel = draftMessage.showInChannel,
            replyMessageId = draftMessage.replyMessage?.id,
            text = draftMessage.text,
            command = draftMessage.command,
            args = draftMessage.args,
            extraData = draftMessage.extraData,
        )

        val result = draftMessage.toEntity()

        assertEquals(expectedDraftMessageEntity, result)
    }

    @Test
    fun `Should map DraftMessageEntity to DraftMessage correctly`() = runTest {
        val replyMessage = randomMessage()
        val draftMessageEntity = DraftMessageEntity(
            id = randomString(),
            cid = randomCID(),
            text = randomString(),
            parentId = randomString(),
            mentionedUsersIds = listOf(randomString()),
            silent = randomBoolean(),
            showInChannel = randomBoolean(),
            replyMessageId = replyMessage.id,
            command = randomString(),
            args = randomString(),
            extraData = mapOf(randomString() to randomString()),
        )

        val expectedDraftMessage = DraftMessage(
            id = draftMessageEntity.id,
            cid = draftMessageEntity.cid,
            parentId = draftMessageEntity.parentId,
            mentionedUsersIds = draftMessageEntity.mentionedUsersIds,
            silent = draftMessageEntity.silent,
            showInChannel = draftMessageEntity.showInChannel,
            replyMessage = replyMessage,
            text = draftMessageEntity.text,
            command = draftMessageEntity.command,
            args = draftMessageEntity.args,
            extraData = draftMessageEntity.extraData,
        )

        val result = draftMessageEntity.toModel { messageId ->
            replyMessage.takeIf { messageId == replyMessage.id }
        }

        assertEquals(expectedDraftMessage, result)
    }

    @Test
    fun `Should map MessageReminderInfo to ReminderInfoEntity correctly`() = runTest {
        val reminderInfo = randomMessageReminderInfo()

        val expectedReminderInfoEntity = ReminderInfoEntity(
            remindAt = reminderInfo.remindAt,
            createdAt = reminderInfo.createdAt,
            updatedAt = reminderInfo.updatedAt,
        )

        assertEquals(expectedReminderInfoEntity, reminderInfo.toEntity())
    }

    @Test
    fun `Should map ReminderInfoEntity to MessageReminderInfo correctly`() = runTest {
        val reminderInfoEntity = randomReminderInfoEntity()

        val expectedReminderInfo = MessageReminderInfo(
            remindAt = reminderInfoEntity.remindAt,
            createdAt = reminderInfoEntity.createdAt,
            updatedAt = reminderInfoEntity.updatedAt,
        )

        assertEquals(expectedReminderInfo, reminderInfoEntity.toModel())
    }
}
