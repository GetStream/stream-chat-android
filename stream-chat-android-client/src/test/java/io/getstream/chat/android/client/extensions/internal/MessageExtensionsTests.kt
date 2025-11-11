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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomPollVote
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomUser
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.util.Date
import java.util.concurrent.TimeUnit

internal class MessageExtensionsTests {

    @Test
    fun `Collection updateUsers should update all messages in collection`() {
        // given
        val originalUser = randomUser(id = "user1", name = "Original Name")
        val updatedUser = randomUser(id = "user1", name = "Updated Name")
        val users = mapOf("user1" to updatedUser)

        val message1 = randomMessage(
            id = "message1",
            user = originalUser,
            latestReactions = listOf(randomReaction(userId = originalUser.id, user = originalUser)),
            replyTo = randomMessage(user = originalUser),
            mentionedUsers = listOf(originalUser),
            threadParticipants = listOf(originalUser),
            pinnedBy = originalUser,
        )

        val message2 = randomMessage(
            id = "message2",
            user = originalUser,
            latestReactions = listOf(randomReaction(userId = originalUser.id, user = originalUser)),
            replyTo = randomMessage(user = originalUser),
            mentionedUsers = listOf(originalUser),
            threadParticipants = listOf(originalUser),
            pinnedBy = originalUser,
        )

        val messages = listOf(message1, message2)

        // when
        val result = messages.updateUsers(users)

        // then
        result.size shouldBeEqualTo 2
        result[0].user shouldBeEqualTo updatedUser
        result[0].latestReactions.first().user shouldBeEqualTo updatedUser
        result[0].replyTo?.user shouldBeEqualTo updatedUser
        result[0].mentionedUsers.first() shouldBeEqualTo updatedUser
        result[0].threadParticipants.first() shouldBeEqualTo updatedUser
        result[0].pinnedBy shouldBeEqualTo updatedUser

        result[1].user shouldBeEqualTo updatedUser
        result[1].latestReactions.first().user shouldBeEqualTo updatedUser
        result[1].replyTo?.user shouldBeEqualTo updatedUser
        result[1].mentionedUsers.first() shouldBeEqualTo updatedUser
        result[1].threadParticipants.first() shouldBeEqualTo updatedUser
        result[1].pinnedBy shouldBeEqualTo updatedUser
    }

    @Test
    fun `Collection updateUsers should not modify messages when no users need updating`() {
        // given
        val user = randomUser(id = "user1", name = "User")
        val message1 = randomMessage(
            id = "message1",
            user = user,
            latestReactions = listOf(randomReaction(user = user)),
            replyTo = randomMessage(user = user),
            mentionedUsers = listOf(user),
            threadParticipants = listOf(user),
            pinnedBy = user,
            ownReactions = listOf(randomReaction(user = user)),
        )

        val message2 = randomMessage(
            id = "message2",
            user = user,
            latestReactions = listOf(randomReaction(user = user)),
            replyTo = randomMessage(user = user),
            mentionedUsers = listOf(user),
            threadParticipants = listOf(user),
            pinnedBy = user,
            ownReactions = listOf(randomReaction(user = user)),
        )

        val messages = listOf(message1, message2)

        // when
        val result = messages.updateUsers(emptyMap())

        // then
        result shouldBeEqualTo messages
    }

    @Test
    fun `Map updateUsers should update all messages in map`() {
        // given
        val originalUser = randomUser(id = "user1", name = "Original Name")
        val updatedUser = randomUser(id = "user1", name = "Updated Name")
        val users = mapOf("user1" to updatedUser)

        val message1 = randomMessage(
            id = "message1",
            user = originalUser,
            latestReactions = listOf(randomReaction(userId = originalUser.id, user = originalUser)),
            replyTo = randomMessage(user = originalUser),
            mentionedUsers = listOf(originalUser),
            threadParticipants = listOf(originalUser),
            pinnedBy = originalUser,
        )

        val message2 = randomMessage(
            id = "message2",
            user = originalUser,
            latestReactions = listOf(randomReaction(userId = originalUser.id, user = originalUser)),
            replyTo = randomMessage(user = originalUser),
            mentionedUsers = listOf(originalUser),
            threadParticipants = listOf(originalUser),
            pinnedBy = originalUser,
        )

        val messages = mapOf("message1" to message1, "message2" to message2)

        // when
        val result = messages.updateUsers(users)

        // then
        result.size shouldBeEqualTo 2
        result["message1"]?.user shouldBeEqualTo updatedUser
        result["message1"]?.latestReactions?.first()?.user shouldBeEqualTo updatedUser
        result["message1"]?.replyTo?.user shouldBeEqualTo updatedUser
        result["message1"]?.mentionedUsers?.first() shouldBeEqualTo updatedUser
        result["message1"]?.threadParticipants?.first() shouldBeEqualTo updatedUser
        result["message1"]?.pinnedBy shouldBeEqualTo updatedUser

        result["message2"]?.user shouldBeEqualTo updatedUser
        result["message2"]?.latestReactions?.first()?.user shouldBeEqualTo updatedUser
        result["message2"]?.replyTo?.user shouldBeEqualTo updatedUser
        result["message2"]?.mentionedUsers?.first() shouldBeEqualTo updatedUser
        result["message2"]?.threadParticipants?.first() shouldBeEqualTo updatedUser
        result["message2"]?.pinnedBy shouldBeEqualTo updatedUser
    }

    @Test
    fun `Map updateUsers should not modify messages when no users need updating`() {
        // given
        val user = randomUser(id = "user1", name = "User")
        val message1 = randomMessage(
            id = "message1",
            user = user,
            latestReactions = listOf(randomReaction(user = user)),
            replyTo = randomMessage(user = user),
            mentionedUsers = listOf(user),
            threadParticipants = listOf(user),
            pinnedBy = user,
            ownReactions = listOf(randomReaction(user = user)),
        )

        val message2 = randomMessage(
            id = "message2",
            user = user,
            latestReactions = listOf(randomReaction(user = user)),
            replyTo = randomMessage(user = user),
            mentionedUsers = listOf(user),
            threadParticipants = listOf(user),
            pinnedBy = user,
            ownReactions = listOf(randomReaction(user = user)),
        )

        val messages = mapOf("message1" to message1, "message2" to message2)

        // when
        val result = messages.updateUsers(emptyMap())

        // then
        result shouldBeEqualTo messages
    }

    @Test
    fun `updateUsers should update all user references in message`() {
        // given
        val originalUser = randomUser(id = "user1", name = "Original Name")
        val updatedUser = randomUser(id = "user1", name = "Updated Name")
        val users = mapOf("user1" to updatedUser)

        val message = randomMessage(
            id = "message1",
            user = originalUser,
            latestReactions = listOf(randomReaction(userId = originalUser.id, user = originalUser)),
            replyTo = randomMessage(user = originalUser),
            mentionedUsers = listOf(originalUser),
            threadParticipants = listOf(originalUser),
            pinnedBy = originalUser,
        )

        // when
        val result = message.updateUsers(users)

        // then
        result.user shouldBeEqualTo updatedUser
        result.latestReactions.first().user shouldBeEqualTo updatedUser
        result.replyTo?.user shouldBeEqualTo updatedUser
        result.mentionedUsers.first() shouldBeEqualTo updatedUser
        result.threadParticipants.first() shouldBeEqualTo updatedUser
        result.pinnedBy shouldBeEqualTo updatedUser
    }

    @Test
    fun `updateUsers should not modify message when no users need updating`() {
        // given
        val user = randomUser(id = "user1", name = "User")
        val message = randomMessage(
            id = "message1",
            user = user,
            latestReactions = listOf(randomReaction(user = user)),
            replyTo = randomMessage(user = user),
            mentionedUsers = listOf(user),
            threadParticipants = listOf(user),
            pinnedBy = user,
            ownReactions = listOf(randomReaction(user = user)),
        )

        // when
        val result = message.updateUsers(emptyMap())

        // then
        result shouldBe message
    }

    @Test
    fun `populateMentions should add mentioned users from text`() {
        // given
        val user1 = randomUser(id = "user1", name = "John")
        val user2 = randomUser(id = "user2", name = "Jane")
        val channel = randomChannel(
            id = "channel1",
            members = listOf(
                randomMember(user = user1),
                randomMember(user = user2),
            ),
        )
        val message = randomMessage(
            id = "message1",
            text = "Hello @john and @jane",
            mentionedUsers = emptyList(),
        )

        // when
        val result = message.populateMentions(channel)

        // then
        result.mentionedUsersIds shouldBeEqualTo listOf("user1", "user2")
    }

    @Test
    fun `populateMentions should preserve existing mentions`() {
        // given
        val user1 = randomUser(id = "user1", name = "John")
        val user2 = randomUser(id = "user2", name = "Jane")
        val channel = randomChannel(
            id = "channel1",
            members = listOf(
                randomMember(user = user1),
                randomMember(user = user2),
            ),
        )
        val message = randomMessage(
            id = "message1",
            text = "Hello @john",
            mentionedUsers = listOf(user2),
        )

        // when
        val result = message.populateMentions(channel)

        // then
        result.mentionedUsersIds.toSet() shouldBeEqualTo listOf("user1", "user2").toSet()
    }

    @Test
    fun `populateMentions should return same message when no mentions in text`() {
        // given
        val channel = randomChannel(id = "channel1", members = emptyList())
        val message = randomMessage(
            id = "message1",
            text = "Hello there",
            mentionedUsers = emptyList(),
        )

        // when
        val result = message.populateMentions(channel)

        // then
        result shouldBe message
    }

    @Test
    fun `message creation time comparisons should work correctly`() {
        // given
        val now = randomDate()
        val oneHourAgo = Date(now.time - TimeUnit.HOURS.toMillis(1))
        val oneHourLater = Date(now.time + TimeUnit.HOURS.toMillis(1))
        val message = randomMessage(
            id = "message1",
            createdAt = now,
            createdLocallyAt = null,
        )

        // when/then
        message.wasCreatedAfterOrAt(oneHourAgo) shouldBeEqualTo true
        message.wasCreatedAfterOrAt(now) shouldBeEqualTo true
        message.wasCreatedAfterOrAt(oneHourLater) shouldBeEqualTo false

        message.wasCreatedAfter(oneHourAgo) shouldBeEqualTo true
        message.wasCreatedAfter(now) shouldBeEqualTo false
        message.wasCreatedAfter(oneHourLater) shouldBeEqualTo false

        message.wasCreatedBefore(oneHourAgo) shouldBeEqualTo false
        message.wasCreatedBefore(now) shouldBeEqualTo false
        message.wasCreatedBefore(oneHourLater) shouldBeEqualTo true

        message.wasCreatedBeforeOrAt(oneHourAgo) shouldBeEqualTo false
        message.wasCreatedBeforeOrAt(now) shouldBeEqualTo true
        message.wasCreatedBeforeOrAt(oneHourLater) shouldBeEqualTo true
    }

    @Test
    fun `local message creation time comparisons should work correctly`() {
        // given
        val now = randomDate()
        val oneHourAgo = Date(now.time - TimeUnit.HOURS.toMillis(1))
        val oneHourLater = Date(now.time + TimeUnit.HOURS.toMillis(1))
        val message = randomMessage(
            id = "message1",
            createdAt = null,
            createdLocallyAt = now,
        )

        // when/then
        message.wasCreatedAfterOrAt(oneHourAgo) shouldBeEqualTo true
        message.wasCreatedAfterOrAt(now) shouldBeEqualTo true
        message.wasCreatedAfterOrAt(oneHourLater) shouldBeEqualTo false

        message.wasCreatedAfter(oneHourAgo) shouldBeEqualTo true
        message.wasCreatedAfter(now) shouldBeEqualTo false
        message.wasCreatedAfter(oneHourLater) shouldBeEqualTo false

        message.wasCreatedBefore(oneHourAgo) shouldBeEqualTo false
        message.wasCreatedBefore(now) shouldBeEqualTo false
        message.wasCreatedBefore(oneHourLater) shouldBeEqualTo true

        message.wasCreatedBeforeOrAt(oneHourAgo) shouldBeEqualTo false
        message.wasCreatedBeforeOrAt(now) shouldBeEqualTo true
        message.wasCreatedBeforeOrAt(oneHourLater) shouldBeEqualTo true
    }

    @Test
    fun `message without creation times comparisons should work correctly`() {
        // given
        val now = Date(10000000)
        val oneHourAgo = Date(now.time - TimeUnit.HOURS.toMillis(1))
        val oneHourLater = Date(now.time + TimeUnit.HOURS.toMillis(1))
        val message = randomMessage(
            id = "message1",
            createdAt = null,
            createdLocallyAt = null,
        )

        // when/then
        message.wasCreatedAfterOrAt(oneHourAgo) shouldBeEqualTo false
        message.wasCreatedAfterOrAt(now) shouldBeEqualTo false
        message.wasCreatedAfterOrAt(oneHourLater) shouldBeEqualTo false

        message.wasCreatedAfter(oneHourAgo) shouldBeEqualTo false
        message.wasCreatedAfter(now) shouldBeEqualTo false
        message.wasCreatedAfter(oneHourLater) shouldBeEqualTo false

        message.wasCreatedBefore(oneHourAgo) shouldBeEqualTo true
        message.wasCreatedBefore(now) shouldBeEqualTo true
        message.wasCreatedBefore(oneHourLater) shouldBeEqualTo true

        message.wasCreatedBeforeOrAt(oneHourAgo) shouldBeEqualTo true
        message.wasCreatedBeforeOrAt(now) shouldBeEqualTo true
        message.wasCreatedBeforeOrAt(oneHourLater) shouldBeEqualTo true
    }

    @Test
    fun `users should return all users associated with message`() {
        // given
        val user1 = randomUser(id = "user1", name = "User 1")
        val user2 = randomUser(id = "user2", name = "User 2")
        val user3 = randomUser(id = "user3", name = "User 3")
        val user4 = randomUser(id = "user4", name = "User 4")
        val user5 = randomUser(id = "user5", name = "User 5")
        val user6 = randomUser(id = "user6", name = "User 6")
        val user7 = randomUser(id = "user7", name = "User 7")

        val message = randomMessage(
            id = "message1",
            latestReactions = listOf(randomReaction(userId = user1.id, user = user1)),
            user = user2,
            replyTo = null,
            mentionedUsers = listOf(user3),
            ownReactions = listOf(randomReaction(userId = user4.id, user = user4)),
            threadParticipants = listOf(user5),
            pinnedBy = user6,
            poll = randomPoll(votes = listOf(randomPollVote(user = user7))),
        )

        // when
        val result = message.users()

        // then
        result shouldBeEqualTo listOf(user1, user2, user3, user4, user5, user6, user7)
    }

    @Test
    fun `shouldIncrementUnreadCount should return true for unread message from other user`() {
        // given
        val currentUserId = "user1"
        val otherUserId = "user2"
        val message = randomMessage(
            id = "message1",
            user = randomUser(id = otherUserId),
            createdAt = Date(),
            silent = false,
            shadowed = false,
        )
        val lastMessageAtDate = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1))

        // when
        val result = message.shouldIncrementUnreadCount(currentUserId, lastMessageAtDate, false)

        // then
        result shouldBeEqualTo true
    }

    @Test
    fun `shouldIncrementUnreadCount should return false for own message`() {
        // given
        val currentUserId = "user1"
        val message = randomMessage(
            id = "message1",
            user = randomUser(id = currentUserId),
            createdAt = Date(),
            silent = false,
            shadowed = false,
        )
        val lastMessageAtDate = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1))

        // when
        val result = message.shouldIncrementUnreadCount(currentUserId, lastMessageAtDate, false)

        // then
        result shouldBeEqualTo false
    }

    @Test
    fun `shouldIncrementUnreadCount should return false for silent message`() {
        // given
        val currentUserId = "user1"
        val otherUserId = "user2"
        val message = randomMessage(
            id = "message1",
            user = randomUser(id = otherUserId),
            createdAt = Date(),
            silent = true,
            shadowed = false,
        )
        val lastMessageAtDate = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1))

        // when
        val result = message.shouldIncrementUnreadCount(currentUserId, lastMessageAtDate, false)

        // then
        result shouldBeEqualTo false
    }

    @Test
    fun `shouldIncrementUnreadCount should return false for shadowed message`() {
        // given
        val currentUserId = "user1"
        val otherUserId = "user2"
        val message = randomMessage(
            id = "message1",
            user = randomUser(id = otherUserId),
            createdAt = Date(),
            silent = false,
            shadowed = true,
        )
        val lastMessageAtDate = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1))

        // when
        val result = message.shouldIncrementUnreadCount(currentUserId, lastMessageAtDate, false)

        // then
        result shouldBeEqualTo false
    }

    @Test
    fun `shouldIncrementUnreadCount should return false for muted channel`() {
        // given
        val currentUserId = "user1"
        val otherUserId = "user2"
        val message = randomMessage(
            id = "message1",
            user = randomUser(id = otherUserId),
            createdAt = Date(),
            silent = false,
            shadowed = false,
        )
        val lastMessageAtDate = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1))

        // when
        val result = message.shouldIncrementUnreadCount(currentUserId, lastMessageAtDate, true)

        // then
        result shouldBeEqualTo false
    }

    @Test
    fun `hasPendingAttachments should return true when message has in-progress attachment`() {
        // given
        val message = randomMessage(
            id = "message1",
            attachments = listOf(
                randomAttachment(uploadState = Attachment.UploadState.InProgress(0, 1024)),
            ),
        )

        // when
        val result = message.hasPendingAttachments()

        // then
        result shouldBeEqualTo true
    }

    @Test
    fun `hasPendingAttachments should return true when message has idle attachment`() {
        // given
        val message = randomMessage(
            id = "message1",
            attachments = listOf(
                Attachment(uploadState = Attachment.UploadState.Idle),
            ),
        )

        // when
        val result = message.hasPendingAttachments()

        // then
        result shouldBeEqualTo true
    }

    @Test
    fun `hasPendingAttachments should return false when message has no pending attachments`() {
        // given
        val message = randomMessage(
            id = "message1",
            attachments = listOf(
                Attachment(uploadState = Attachment.UploadState.Success),
            ),
        )

        // when
        val result = message.hasPendingAttachments()

        // then
        result shouldBeEqualTo false
    }

    @Test
    fun `containsUserMention should return true when user is mentioned`() {
        // given
        val user = randomUser(id = "user1", name = "User")
        val message = randomMessage(
            id = "message1",
            mentionedUsers = listOf(user),
        )

        // when
        val result = message.containsUserMention(user)

        // then
        result shouldBeEqualTo true
    }

    @Test
    fun `containsUserMention should return false when user is not mentioned`() {
        // given
        val user = randomUser(id = "user1", name = "User")
        val message = randomMessage(
            id = "message1",
            mentionedUsers = emptyList(),
        )

        // when
        val result = message.containsUserMention(user)

        // then
        result shouldBeEqualTo false
    }
}
