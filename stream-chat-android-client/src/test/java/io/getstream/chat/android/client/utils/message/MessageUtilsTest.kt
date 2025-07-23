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

package io.getstream.chat.android.client.utils.message

import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageModerationAction
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.ModerationAction
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomMessageModerationDetails
import io.getstream.chat.android.randomModeration
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class MessageUtilsTest {

    @Test
    fun `latestOrNull should return the latest message based on createdAt`() {
        val message1 = randomMessage(createdAt = Date(1000))
        val message2 = randomMessage(createdAt = Date(2000))
        val messages = listOf(message1, message2)
        messages.latestOrNull() shouldBeEqualTo message2
    }

    @Test
    fun `latestOrNull should return the latest message based on createdLocallyAt if createdAt is null`() {
        val message1 = randomMessage(createdAt = null, createdLocallyAt = Date(1000))
        val message2 = randomMessage(createdAt = null, createdLocallyAt = Date(2000))
        val messages = listOf(message1, message2)
        messages.latestOrNull() shouldBeEqualTo message2
    }

    @Test
    fun `latestOrNull should return null for an empty list`() {
        val messages = emptyList<Message>()
        messages.latestOrNull() shouldBeEqualTo null
    }

    @Test
    fun `createdAfter should return true if the message was created after the given message`() {
        val message1 = randomMessage(createdAt = Date(1000))
        val message2 = randomMessage(createdAt = Date(2000))
        message2.createdAfter(message1) shouldBeEqualTo true
    }

    @Test
    fun `createdAfter should return false if the message was created before the given message`() {
        val message1 = randomMessage(createdAt = Date(2000))
        val message2 = randomMessage(createdAt = Date(1000))
        message2.createdAfter(message1) shouldBeEqualTo false
    }

    @Test
    fun `createdAfter should return true if the message was created locally after the given message`() {
        val message1 = randomMessage(createdAt = null, createdLocallyAt = Date(1000))
        val message2 = randomMessage(createdAt = null, createdLocallyAt = Date(2000))
        message2.createdAfter(message1) shouldBeEqualTo true
    }

    @Test
    fun `createdAfter should return false if the message was created locally before the given message`() {
        val message1 = randomMessage(createdAt = null, createdLocallyAt = Date(2000))
        val message2 = randomMessage(createdAt = null, createdLocallyAt = Date(1000))
        message2.createdAfter(message1) shouldBeEqualTo false
    }

    @Test
    fun `validate message_isPinExpired`() = runTest {
        val message = randomMessage(
            pinned = true,
            pinExpires = Date(currentTime + 1000),
        )
        advanceTimeBy(2000)
        message.isPinExpired { currentTime } shouldBeEqualTo true
    }

    @Test
    fun `validate message_isPinned when pin expires`() = runTest {
        val message = randomMessage(
            pinned = true,
            pinExpires = Date(currentTime + 1000),
        )
        advanceTimeBy(2000)
        message.isPinned { currentTime } shouldBeEqualTo false
    }

    @Test
    fun `validate message_isPinned when message gets deleted`() = runTest {
        val message = randomMessage(
            pinned = true,
            deletedAt = Date(currentTime),
        )
        advanceTimeBy(2000)
        message.isPinned { currentTime } shouldBeEqualTo false
    }

    @Test
    fun `validate message_isPinned when message gets unpinned`() = runTest {
        val message = randomMessage(
            pinned = false,
        )
        message.isPinned { currentTime } shouldBeEqualTo false
    }

    @Test
    fun `isFailed should return true for failed message`() {
        val message = randomMessage(syncStatus = SyncStatus.FAILED_PERMANENTLY)
        message.isFailed() shouldBeEqualTo true
    }

    @Test
    fun `isFailed should return false for non-failed message`() {
        val message = randomMessage(syncStatus = SyncStatus.COMPLETED)
        message.isFailed() shouldBeEqualTo false
    }

    @Test
    fun `isErrorOrFailed should return true for error message`() {
        val message = randomMessage(type = MessageType.ERROR)
        message.isErrorOrFailed() shouldBeEqualTo true
    }

    @Test
    fun `isErrorOrFailed should return true for failed message`() {
        val message = randomMessage(syncStatus = SyncStatus.FAILED_PERMANENTLY)
        message.isErrorOrFailed() shouldBeEqualTo true
    }

    @Test
    fun `isErrorOrFailed should return false for regular message`() {
        val message = randomMessage(type = MessageType.REGULAR, syncStatus = SyncStatus.COMPLETED)
        message.isErrorOrFailed() shouldBeEqualTo false
    }

    @Test
    fun `isDeleted should return true for deleted message`() {
        val message = randomMessage(deletedAt = Date())
        message.isDeleted() shouldBeEqualTo true
    }

    @Test
    fun `isDeleted should return false for non-deleted message`() {
        val message = randomMessage(deletedAt = null)
        message.isDeleted() shouldBeEqualTo false
    }

    @Test
    fun `isPinnedAndNotDeleted should return true for pinned and not deleted message`() {
        val message = randomMessage(pinned = true, deletedAt = null)
        message.isPinnedAndNotDeleted() shouldBeEqualTo true
    }

    @Test
    fun `isPinnedAndNotDeleted should return false for non-pinned message`() {
        val message = randomMessage(pinned = false)
        message.isPinnedAndNotDeleted() shouldBeEqualTo false
    }

    @Test
    fun `isPinnedAndNotDeleted should return false for deleted message`() {
        val message = randomMessage(pinned = true, deletedAt = Date())
        message.isPinnedAndNotDeleted() shouldBeEqualTo false
    }

    @Test
    fun `isPinned should return true for valid pinned message`() {
        val message =
            randomMessage(pinned = true, deletedAt = null, pinExpires = Date(System.currentTimeMillis() + 1000))
        message.isPinned() shouldBeEqualTo true
    }

    @Test
    fun `isPinned should return false for expired pinned message`() {
        val message =
            randomMessage(pinned = true, deletedAt = null, pinExpires = Date(System.currentTimeMillis() - 1000))
        message.isPinned() shouldBeEqualTo false
    }

    @Test
    fun `isPinned should return false for deleted message`() {
        val message = randomMessage(pinned = true, deletedAt = Date())
        message.isPinned() shouldBeEqualTo false
    }

    @Test
    fun `isPinned should return false for non-pinned message`() {
        val message = randomMessage(pinned = false)
        message.isPinned() shouldBeEqualTo false
    }

    @Test
    fun `isPinExpired should return true for expired pin`() = runTest {
        val message = randomMessage(
            pinned = true,
            pinExpires = Date(currentTime + 1000),
        )
        advanceTimeBy(2000)
        message.isPinExpired { currentTime } shouldBeEqualTo true
    }

    @Test
    fun `isPinExpired should return false for non-expired pin`() = runTest {
        val message = randomMessage(
            pinned = true,
            pinExpires = Date(currentTime + 1000),
        )
        advanceTimeBy(500)
        message.isPinExpired { currentTime } shouldBeEqualTo false
    }

    @Test
    fun `isPinExpired should return false for null pinExpires`() = runTest {
        val message = randomMessage(
            pinned = true,
            pinExpires = null,
        )
        message.isPinExpired { currentTime } shouldBeEqualTo false
    }

    @Test
    fun `isRegular should return true for regular message`() {
        val message = randomMessage(type = MessageType.REGULAR)
        message.isRegular() shouldBeEqualTo true
    }

    @Test
    fun `isRegular should return false for non-regular message`() {
        val message = randomMessage(type = MessageType.EPHEMERAL)
        message.isRegular() shouldBeEqualTo false
    }

    @Test
    fun `isEphemeral should return true for ephemeral message`() {
        val message = randomMessage(type = MessageType.EPHEMERAL)
        message.isEphemeral() shouldBeEqualTo true
    }

    @Test
    fun `isEphemeral should return false for non-ephemeral message`() {
        val message = randomMessage(type = MessageType.REGULAR)
        message.isEphemeral() shouldBeEqualTo false
    }

    @Test
    fun `isSystem should return true for system message`() {
        val message = randomMessage(type = MessageType.SYSTEM)
        message.isSystem() shouldBeEqualTo true
    }

    @Test
    fun `isSystem should return false for non-system message`() {
        val message = randomMessage(type = MessageType.REGULAR)
        message.isSystem() shouldBeEqualTo false
    }

    @Test
    fun `isError should return true for error message`() {
        val message = randomMessage(type = MessageType.ERROR)
        message.isError() shouldBeEqualTo true
    }

    @Test
    fun `isError should return false for non-error message`() {
        val message = randomMessage(type = MessageType.REGULAR)
        message.isError() shouldBeEqualTo false
    }

    @Test
    fun `isGiphy should return true for giphy message`() {
        val message = randomMessage(command = AttachmentType.GIPHY)
        message.isGiphy() shouldBeEqualTo true
    }

    @Test
    fun `isGiphy should return false for non-giphy message`() {
        val message = randomMessage(command = null)
        message.isGiphy() shouldBeEqualTo false
    }

    @Test
    fun `hasAudioRecording should return true for message with audio recording attachment`() {
        val message = randomMessage(attachments = listOf(Attachment(type = AttachmentType.AUDIO_RECORDING)))
        message.hasAudioRecording() shouldBeEqualTo true
    }

    @Test
    fun `hasAudioRecording should return false for message without audio recording attachment`() {
        val message = randomMessage(attachments = listOf(Attachment(type = AttachmentType.IMAGE)))
        message.hasAudioRecording() shouldBeEqualTo false
    }

    @Test
    fun `isPoll should return true for message with poll`() {
        val message = randomMessage(poll = randomPoll())
        message.isPoll() shouldBeEqualTo true
    }

    @Test
    fun `isPoll should return false for message without poll`() {
        val message = randomMessage(poll = null)
        message.isPoll() shouldBeEqualTo false
    }

    @Test
    fun `isPollClosed should return true for closed poll`() {
        val message = randomMessage(poll = randomPoll(closed = true))
        message.isPollClosed() shouldBeEqualTo true
    }

    @Test
    fun `isPollClosed should return false for open poll`() {
        val message = randomMessage(poll = randomPoll(closed = false))
        message.isPollClosed() shouldBeEqualTo false
    }

    @Test
    fun `isPollClosed should return false for message without poll`() {
        val message = randomMessage(poll = null)
        message.isPollClosed() shouldBeEqualTo false
    }

    @Test
    fun `isGiphyEphemeral should return true for giphy ephemeral message`() {
        val message = randomMessage(command = AttachmentType.GIPHY, type = MessageType.EPHEMERAL)
        message.isGiphyEphemeral() shouldBeEqualTo true
    }

    @Test
    fun `isGiphyEphemeral should return false for non-giphy ephemeral message`() {
        val message = randomMessage(command = null, type = MessageType.EPHEMERAL)
        message.isGiphyEphemeral() shouldBeEqualTo false
    }

    @Test
    fun `isThreadStart should return true for message with thread participants`() {
        val message = randomMessage(threadParticipants = listOf(randomUser()))
        message.isThreadStart() shouldBeEqualTo true
    }

    @Test
    fun `isThreadStart should return false for message without thread participants`() {
        val message = randomMessage(threadParticipants = emptyList())
        message.isThreadStart() shouldBeEqualTo false
    }

    @Test
    fun `isThreadReply should return true for message with parentId`() {
        val message = randomMessage(parentId = "parentId")
        message.isThreadReply() shouldBeEqualTo true
    }

    @Test
    fun `isThreadReply should return false for message without parentId`() {
        val message = randomMessage(parentId = null)
        message.isThreadReply() shouldBeEqualTo false
    }

    @Test
    fun `belongsToThread should return true for message with thread participants`() {
        val message = randomMessage(threadParticipants = listOf(User()))
        message.belongsToThread() shouldBeEqualTo true
    }

    @Test
    fun `belongsToThread should return true for message with parentId`() {
        val message = randomMessage(parentId = "parentId")
        message.belongsToThread() shouldBeEqualTo true
    }

    @Test
    fun `belongsToThread should return false for message without thread participants and parentId`() {
        val message = randomMessage(threadParticipants = emptyList(), parentId = null)
        message.belongsToThread() shouldBeEqualTo false
    }

    @Test
    fun `isReply should return true for message with replyTo`() {
        val message = randomMessage(replyTo = randomMessage())
        message.isReply() shouldBeEqualTo true
    }

    @Test
    fun `isReply should return false for message without replyTo`() {
        val message = randomMessage(replyTo = null)
        message.isReply() shouldBeEqualTo false
    }

    @Test
    fun `hasSharedLocation should return true for message with shared location`() {
        val message = randomMessage()
        message.hasSharedLocation() shouldBeEqualTo true
    }

    @Test
    fun `hasSharedLocation should return false for message without shared location`() {
        val message = randomMessage(sharedLocation = null)
        message.hasSharedLocation() shouldBeEqualTo false
    }

    @Test
    fun `isMine should return true for message from current user`() {
        val currentUserId = "userId"
        val message = randomMessage(user = randomUser(id = currentUserId))
        message.isMine(currentUserId) shouldBeEqualTo true
    }

    @Test
    fun `isMine should return false for message from different user`() {
        val currentUserId = "userId"
        val message = randomMessage(user = randomUser(id = "differentUserId"))
        message.isMine(currentUserId) shouldBeEqualTo false
    }

    @Test
    fun `isModerationBounce should return true for message with moderationDetails bounce action`() {
        val message =
            randomMessage(moderationDetails = randomMessageModerationDetails(action = MessageModerationAction.bounce))
        message.isModerationBounce() shouldBeEqualTo true
    }

    @Test
    fun `isModerationBounce should return false for message without moderationDetails bounce action`() {
        val message =
            randomMessage(moderationDetails = randomMessageModerationDetails(action = MessageModerationAction.block))
        message.isModerationBounce() shouldBeEqualTo false
    }

    @Test
    fun `isModerationBounce should return true for message with moderation bounce action`() {
        val message =
            randomMessage(moderation = randomModeration(action = ModerationAction.bounce))
        message.isModerationBounce() shouldBeEqualTo true
    }

    @Test
    fun `isModerationBounce should return false for message without moderation bounce action`() {
        val message =
            randomMessage(moderation = randomModeration(action = ModerationAction.remove))
        message.isModerationBounce() shouldBeEqualTo false
    }

    @Test
    fun `isModerationBlock should return true for message with moderation block action`() {
        val message =
            randomMessage(moderationDetails = randomMessageModerationDetails(action = MessageModerationAction.block))
        message.isModerationBlock() shouldBeEqualTo true
    }

    @Test
    fun `isModerationBlock should return false for message without moderation block action`() {
        val message =
            randomMessage(moderationDetails = randomMessageModerationDetails(action = MessageModerationAction.bounce))
        message.isModerationBlock() shouldBeEqualTo false
    }

    @Test
    fun `isModerationBlock should return false for message without moderation object`() {
        val message =
            randomMessage(moderationDetails = null)
        message.isModerationBlock() shouldBeEqualTo false
    }

    @Test
    fun `isModerationFlag should return true for message with moderationDetails flag action`() {
        val message =
            randomMessage(moderationDetails = randomMessageModerationDetails(action = MessageModerationAction.flag))
        message.isModerationFlag() shouldBeEqualTo true
    }

    @Test
    fun `isModerationFlag should return false for message without moderationDetails flag action`() {
        val message =
            randomMessage(moderationDetails = randomMessageModerationDetails(action = MessageModerationAction.bounce))
        message.isModerationFlag() shouldBeEqualTo false
    }

    @Test
    fun `isModerationFlag should return true for message with moderation flag action`() {
        val message = randomMessage(moderation = randomModeration(action = ModerationAction.flag))
        message.isModerationFlag() shouldBeEqualTo true
    }

    @Test
    fun `isModerationFlag should return false for message without moderation flag action`() {
        val message = randomMessage(moderation = randomModeration(action = ModerationAction.bounce))
        message.isModerationFlag() shouldBeEqualTo false
    }

    @Test
    fun `isModerationError should return true for message with moderation error`() {
        val currentUserId = randomString()
        val message = randomMessage(
            user = randomUser(id = currentUserId),
            type = MessageType.ERROR,
            moderation = randomModeration(
                action = ModerationAction.bounce,
            ),
        )
        message.isModerationError(currentUserId) shouldBeEqualTo true
    }

    @Test
    fun `isModerationError should return false for message without moderation error`() {
        val currentUserId = randomString()
        val message = randomMessage(
            user = randomUser(id = currentUserId),
            type = MessageType.REGULAR,
            moderation = randomModeration(
                action = ModerationAction.bounce,
            ),
        )
        message.isModerationError(currentUserId) shouldBeEqualTo false
    }

    @Test
    fun `ensureId should return message with existing id`() {
        val messageId = randomString()
        val message = randomMessage(id = messageId)
        val result = message.ensureId(randomUser())
        result.id shouldBeEqualTo messageId
    }

    @Test
    fun `ensureId should generate id for message without id`() {
        val userId = randomString()
        val user = randomUser(id = userId)
        val message = randomMessage(id = "")
        val result = message.ensureId(user)
        result.id.startsWith(userId) shouldBeEqualTo true
    }

    @Test
    fun `ensureId should return draft message with existing id`() {
        val draftId = randomString()
        val draftMessage = randomDraftMessage(id = draftId)
        val result = draftMessage.ensureId(null)
        result.id shouldBeEqualTo draftId
    }

    @Test
    fun `ensureId should generate id for draft message without id`() {
        val userId = randomString()
        val user = User(id = userId)
        val draftMessage = randomDraftMessage(id = "")
        val result = draftMessage.ensureId(user)
        result.id.startsWith(userId) shouldBeEqualTo true
    }
}
