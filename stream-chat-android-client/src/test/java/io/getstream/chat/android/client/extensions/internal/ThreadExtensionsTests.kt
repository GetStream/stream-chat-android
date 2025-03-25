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

import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadInfo
import io.getstream.chat.android.models.ThreadParticipant
import io.getstream.chat.android.models.User
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import java.util.Date

internal class ThreadExtensionsTests {

    private val now = Date()
    private val user1 = User(id = "user1", name = "User 1")
    private val user2 = User(id = "user2", name = "User 2")
    private val parentMessage = Message(
        id = "parent1",
        text = "Parent message",
        createdAt = now,
        user = user1,
    )
    private val replyMessage = Message(
        id = "reply1",
        text = "Reply message",
        parentId = "parent1",
        createdAt = now,
        user = user2,
    )
    private val threadParticipant1 = ThreadParticipant(user = user1)
    private val threadParticipant2 = ThreadParticipant(user = user2)
    private val channelUserRead1 = ChannelUserRead(
        user = user1,
        unreadMessages = 0,
        lastReceivedEventDate = now,
        lastReadMessageId = "parent1",
        lastRead = now,
    )
    private val channelUserRead2 = ChannelUserRead(
        user = user2,
        unreadMessages = 1,
        lastReceivedEventDate = now,
        lastReadMessageId = "",
        lastRead = now,
    )

    private val baseThread = Thread(
        activeParticipantCount = 2,
        cid = "channel1",
        channel = null,
        parentMessageId = "parent1",
        parentMessage = parentMessage,
        createdByUserId = "user1",
        createdBy = user1,
        participantCount = 2,
        threadParticipants = listOf(threadParticipant1, threadParticipant2),
        lastMessageAt = now,
        createdAt = now,
        updatedAt = now,
        deletedAt = null,
        title = "Test Thread",
        latestReplies = listOf(replyMessage),
        read = listOf(channelUserRead1, channelUserRead2),
    )

    @Test
    fun `updateParentOrReply should update parent message when message is parent`() {
        // given
        val updatedParent = parentMessage.copy(text = "Updated parent message")

        // when
        val result = baseThread.updateParentOrReply(updatedParent)

        // then
        result.parentMessage shouldBeEqualTo updatedParent
    }

    @Test
    fun `updateParentOrReply should update reply when message is reply`() {
        // given
        val updatedReply = replyMessage.copy(text = "Updated reply message")

        // when
        val result = baseThread.updateParentOrReply(updatedReply)

        // then
        result.latestReplies.first() shouldBeEqualTo updatedReply
    }

    @Test
    fun `updateParentOrReply should not update when message is unrelated`() {
        // given
        val unrelatedMessage = Message(
            id = "unrelated",
            text = "Unrelated message",
            createdAt = now,
            user = user1,
        )

        // when
        val result = baseThread.updateParentOrReply(unrelatedMessage)

        // then
        result shouldBeEqualTo baseThread
    }

    @Test
    fun `updateParent should update parent message and related fields`() {
        // given
        val updatedParent = parentMessage.copy(
            text = "Updated parent message",
            deletedAt = now,
            updatedAt = now,
        )

        // when
        val result = baseThread.updateParent(updatedParent)

        // then
        result.parentMessage shouldBeEqualTo updatedParent
        result.deletedAt shouldBeEqualTo now
        result.updatedAt shouldBeEqualTo now
    }

    @Test
    fun `updateParent should not update when parent message id does not match`() {
        // given
        val wrongParent = Message(
            id = "wrong_parent",
            text = "Wrong parent message",
            createdAt = now,
            user = user1,
        )

        // when
        val result = baseThread.updateParent(wrongParent)

        // then
        result shouldBeEqualTo baseThread
    }

    @Test
    fun `upsertReply should add new reply and update related fields`() {
        // given
        val newReply = Message(
            id = "reply2",
            text = "New reply message",
            parentId = "parent1",
            createdAt = now,
            user = user1,
        )

        // when
        val result = baseThread.upsertReply(newReply)

        // then
        result.latestReplies.size shouldBeEqualTo 2
        result.latestReplies.last() shouldBeEqualTo newReply
        result.lastMessageAt shouldBeEqualTo now
        result.updatedAt shouldBeEqualTo now
    }

    @Test
    fun `upsertReply should update existing reply and update related fields`() {
        // given
        val updatedReply = replyMessage.copy(text = "Updated reply message")

        // when
        val result = baseThread.upsertReply(updatedReply)

        // then
        result.latestReplies.size shouldBeEqualTo 1
        result.latestReplies.first() shouldBeEqualTo updatedReply
    }

    @Test
    fun `upsertReply should not update when reply parent id does not match`() {
        // given
        val wrongReply = Message(
            id = "reply2",
            text = "Wrong reply message",
            parentId = "wrong_parent",
            createdAt = now,
            user = user1,
        )

        // when
        val result = baseThread.upsertReply(wrongReply)

        // then
        result shouldBeEqualTo baseThread
    }

    @Test
    fun `markAsReadByUser should update read status and thread info`() {
        // given
        val threadInfo = ThreadInfo(
            activeParticipantCount = 3,
            cid = "channel1",
            createdAt = now,
            createdBy = user1,
            createdByUserId = "user1",
            deletedAt = null,
            lastMessageAt = now,
            parentMessage = parentMessage,
            parentMessageId = "parent1",
            participantCount = 3,
            replyCount = 2,
            title = "Updated Thread",
            updatedAt = now,
        )
        val updatedUser = user2.copy(name = "Updated User 2")
        val newDate = Date(now.time + 1000)

        // when
        val result = baseThread.markAsReadByUser(threadInfo, updatedUser, newDate)

        // then
        result.activeParticipantCount shouldBeEqualTo 3
        result.participantCount shouldBeEqualTo 3
        result.title shouldBeEqualTo "Updated Thread"
        result.read.find { it.user.id == updatedUser.id }?.unreadMessages shouldBeEqualTo 0
        result.read.find { it.user.id == updatedUser.id }?.lastReceivedEventDate shouldBeEqualTo newDate
    }

    @Test
    fun `markAsReadByUser should not update when thread info parent message id does not match`() {
        // given
        val wrongThreadInfo = ThreadInfo(
            activeParticipantCount = 3,
            cid = "channel1",
            createdAt = now,
            createdBy = user1,
            createdByUserId = "user1",
            deletedAt = null,
            lastMessageAt = now,
            parentMessage = parentMessage,
            parentMessageId = "wrong_parent",
            participantCount = 3,
            replyCount = 2,
            title = "Updated Thread",
            updatedAt = now,
        )

        // when
        val result = baseThread.markAsReadByUser(wrongThreadInfo, user2, now)

        // then
        result shouldBeEqualTo baseThread
    }

    @Test
    fun `markAsUnreadByUser should increment unread messages count`() {
        // given
        val updatedUser = user2.copy(name = "Updated User 2")
        val newDate = Date(now.time + 1000)

        // when
        val result = baseThread.markAsUnreadByUser(updatedUser, newDate)

        // then
        result.read.find { it.user.id == updatedUser.id }?.unreadMessages shouldBeEqualTo 2
        result.read.find { it.user.id == updatedUser.id }?.lastReceivedEventDate shouldBeEqualTo newDate
    }
}
