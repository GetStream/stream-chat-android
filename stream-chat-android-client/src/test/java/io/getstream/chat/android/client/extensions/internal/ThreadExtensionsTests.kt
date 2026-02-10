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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadInfo
import io.getstream.chat.android.models.ThreadParticipant
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.VotingVisibility
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeNull
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
        draft = null,
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
    fun `updateParent should preserve existing poll when updated parent has no poll`() {
        // given
        val originalPoll = Poll(
            id = "poll1",
            name = "Test Poll",
            description = "Test Description",
            options = listOf(
                Option(id = "option1", text = "Option 1"),
                Option(id = "option2", text = "Option 2"),
            ),
            votingVisibility = VotingVisibility.PUBLIC,
            enforceUniqueVote = true,
            maxVotesAllowed = 1,
            allowUserSuggestedOptions = false,
            allowAnswers = false,
            voteCountsByOption = emptyMap(),
            voteCount = 0,
            votes = emptyList(),
            ownVotes = emptyList(),
            createdAt = now,
            updatedAt = now,
            closed = false,
            answersCount = 0,
            answers = emptyList(),
            createdBy = null,
        )
        val threadWithPoll = baseThread.copy(
            parentMessage = parentMessage.copy(poll = originalPoll),
        )
        val updatedParent = parentMessage.copy(
            text = "Updated parent message",
            poll = null,
        )

        // when
        val result = threadWithPoll.updateParent(updatedParent)

        // then
        result.parentMessage.poll.shouldNotBeNull()
        result.parentMessage.poll shouldBeEqualTo originalPoll
    }

    @Test
    fun `updateParent should use new poll when updated parent has poll`() {
        // given
        val originalPoll = Poll(
            id = "poll1",
            name = "Original Poll",
            description = "Original Description",
            options = listOf(
                Option(id = "option1", text = "Option 1"),
            ),
            votingVisibility = VotingVisibility.PUBLIC,
            enforceUniqueVote = true,
            maxVotesAllowed = 1,
            allowUserSuggestedOptions = false,
            allowAnswers = false,
            voteCountsByOption = emptyMap(),
            voteCount = 0,
            votes = emptyList(),
            ownVotes = emptyList(),
            createdAt = now,
            updatedAt = now,
            closed = false,
            answersCount = 0,
            answers = emptyList(),
            createdBy = null,
        )
        val newPoll = Poll(
            id = "poll2",
            name = "Updated Poll",
            description = "Updated Description",
            options = listOf(
                Option(id = "option1", text = "Option 1"),
                Option(id = "option2", text = "Option 2"),
            ),
            votingVisibility = VotingVisibility.ANONYMOUS,
            enforceUniqueVote = false,
            maxVotesAllowed = 2,
            allowUserSuggestedOptions = true,
            allowAnswers = true,
            voteCountsByOption = mapOf("option1" to 5),
            voteCount = 0,
            votes = emptyList(),
            ownVotes = emptyList(),
            createdAt = now,
            updatedAt = now,
            closed = true,
            answersCount = 0,
            answers = emptyList(),
            createdBy = null,
        )
        val threadWithPoll = baseThread.copy(
            parentMessage = parentMessage.copy(poll = originalPoll),
        )
        val updatedParent = parentMessage.copy(
            text = "Updated parent message",
            poll = newPoll,
        )

        // when
        val result = threadWithPoll.updateParent(updatedParent)

        // then
        result.parentMessage.poll.shouldNotBeNull()
        result.parentMessage.poll shouldBeEqualTo newPoll
    }

    @Test
    fun `updateParent should add poll when original parent has no poll but updated parent has poll`() {
        // given
        val newPoll = Poll(
            id = "poll1",
            name = "New Poll",
            description = "New Description",
            options = listOf(
                Option(id = "option1", text = "Option 1"),
                Option(id = "option2", text = "Option 2"),
            ),
            votingVisibility = VotingVisibility.PUBLIC,
            enforceUniqueVote = true,
            maxVotesAllowed = 1,
            allowUserSuggestedOptions = false,
            allowAnswers = false,
            voteCountsByOption = emptyMap(),
            voteCount = 0,
            votes = emptyList(),
            ownVotes = emptyList(),
            createdAt = now,
            updatedAt = now,
            closed = false,
            answersCount = 0,
            answers = emptyList(),
            createdBy = null,
        )
        val updatedParent = parentMessage.copy(
            text = "Updated parent message",
            poll = newPoll,
        )

        // when
        val result = baseThread.updateParent(updatedParent)

        // then
        result.parentMessage.poll.shouldNotBeNull()
        result.parentMessage.poll shouldBeEqualTo newPoll
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
    fun `applyThreadInfoUpdate should update title extraData and updatedAt`() {
        // given
        val newUpdatedAt = Date(now.time + 5000)
        val threadInfo = ThreadInfo(
            activeParticipantCount = 5,
            cid = "channel1",
            createdAt = now,
            createdBy = user1,
            createdByUserId = "user1",
            deletedAt = null,
            lastMessageAt = Date(now.time + 3000),
            parentMessage = parentMessage.copy(text = "Updated parent"),
            parentMessageId = "parent1",
            participantCount = 5,
            replyCount = 10,
            title = "Updated Title",
            updatedAt = newUpdatedAt,
            extraData = mapOf("color" to "blue", "priority" to 1),
        )

        // when
        val result = baseThread.applyThreadUpdatedEventChanges(threadInfo)

        // then
        result.title shouldBeEqualTo "Updated Title"
        result.updatedAt shouldBeEqualTo newUpdatedAt
        result.extraData shouldBeEqualTo mapOf("color" to "blue", "priority" to 1)
    }

    @Test
    fun `applyThreadInfoUpdate should not update non-updatable fields`() {
        // given
        val threadInfo = ThreadInfo(
            activeParticipantCount = 99,
            cid = "channel1",
            createdAt = now,
            createdBy = user1,
            createdByUserId = "user1",
            deletedAt = Date(now.time + 9000),
            lastMessageAt = Date(now.time + 3000),
            parentMessage = parentMessage.copy(text = "Different parent"),
            parentMessageId = "parent1",
            participantCount = 99,
            replyCount = 99,
            title = "New Title",
            updatedAt = Date(now.time + 5000),
            extraData = mapOf("key" to "value"),
        )

        // when
        val result = baseThread.applyThreadUpdatedEventChanges(threadInfo)

        // then
        // These fields should NOT be overwritten
        result.activeParticipantCount shouldBeEqualTo baseThread.activeParticipantCount
        result.participantCount shouldBeEqualTo baseThread.participantCount
        result.lastMessageAt shouldBeEqualTo baseThread.lastMessageAt
        result.parentMessage shouldBeEqualTo baseThread.parentMessage
        result.deletedAt shouldBeEqualTo baseThread.deletedAt
        result.latestReplies shouldBeEqualTo baseThread.latestReplies
        result.read shouldBeEqualTo baseThread.read
    }

    @Test
    fun `applyThreadInfoUpdate should not update when thread info parent message id does not match`() {
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
            parentMessageId = "wrong_parent",
            participantCount = 3,
            replyCount = 2,
            title = "Updated Title",
            updatedAt = now,
            extraData = mapOf("key" to "value"),
        )

        // when
        val result = baseThread.applyThreadUpdatedEventChanges(threadInfo)

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
