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

package io.getstream.chat.android.internal.offline.repository

import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.query.pagination.AnyChannelPaginationRequest
import io.getstream.chat.android.internal.offline.randomMessageEntity
import io.getstream.chat.android.internal.offline.repository.domain.message.internal.DatabaseMessageRepository
import io.getstream.chat.android.internal.offline.repository.domain.message.internal.MessageDao
import io.getstream.chat.android.internal.offline.repository.domain.message.internal.PollDao
import io.getstream.chat.android.internal.offline.repository.domain.message.internal.ReplyMessageDao
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@ExperimentalCoroutinesApi
internal class MessageRepositoryTests {

    private val messageDao: MessageDao = mock()
    private val replyMessageDao: ReplyMessageDao = mock()
    private val pollDao: PollDao = mock()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `when selecting messages for channel, correct messages should be requested to DAO`() = runTest {
        val sut = DatabaseMessageRepository(
            this,
            messageDao,
            replyMessageDao,
            pollDao,
            ::randomUser,
            randomUser(id = "currentUserId"),
            emptySet(),
            100,
        )
        val createdAt = Date()
        val cid = randomString()
        val messageEntity = randomMessageEntity(createdAt = createdAt)

        val requestGreaterOrEqual = AnyChannelPaginationRequest(30).apply {
            messageFilterDirection = Pagination.GREATER_THAN_OR_EQUAL
        }

        val requestGreater = AnyChannelPaginationRequest(30).apply {
            messageFilterDirection = Pagination.GREATER_THAN
        }

        val requestLessThan = AnyChannelPaginationRequest(30).apply {
            messageFilterDirection = Pagination.LESS_THAN
        }

        val requestLessOrEqualThan = AnyChannelPaginationRequest(30).apply {
            messageFilterDirection = Pagination.LESS_THAN_OR_EQUAL
        }

        whenever(messageDao.select(requestGreaterOrEqual.messageFilterValue)) doReturn messageEntity
        whenever(messageDao.select(requestGreater.messageFilterValue)) doReturn messageEntity
        whenever(messageDao.select(requestLessThan.messageFilterValue)) doReturn messageEntity
        whenever(messageDao.select(requestLessOrEqualThan.messageFilterValue)) doReturn messageEntity

        whenever(messageDao.messagesForChannelEqualOrNewerThan(any(), any(), any())) doReturn listOf(messageEntity)
        whenever(messageDao.messagesForChannelNewerThan(any(), any(), any())) doReturn listOf(messageEntity)
        whenever(messageDao.messagesForChannelEqualOrOlderThan(any(), any(), any())) doReturn listOf(messageEntity)
        whenever(messageDao.messagesForChannelOlderThan(any(), any(), any())) doReturn listOf(messageEntity)

        sut.selectMessagesForChannel(cid, requestGreaterOrEqual)
        sut.selectMessagesForChannel(cid, requestGreater)
        sut.selectMessagesForChannel(cid, requestLessThan)
        sut.selectMessagesForChannel(cid, requestLessOrEqualThan)

        inOrder(messageDao).run {
            verify(messageDao).messagesForChannelEqualOrNewerThan(cid, requestGreaterOrEqual.messageLimit, createdAt)
            verify(messageDao).messagesForChannelNewerThan(cid, requestGreaterOrEqual.messageLimit, createdAt)
            verify(messageDao).messagesForChannelOlderThan(cid, requestLessOrEqualThan.messageLimit, createdAt)
            verify(messageDao).messagesForChannelEqualOrOlderThan(cid, requestLessThan.messageLimit, createdAt)
        }
    }

    @Test
    fun `when calling deletePoll, then deletePoll from PollDao is called`() = runTest {
        // given
        val repository = DatabaseMessageRepository(
            this,
            messageDao,
            replyMessageDao,
            pollDao,
            ::randomUser,
            randomUser(id = "currentUserId"),
            emptySet(),
        )
        val pollId = randomString()
        // when
        repository.deletePoll(pollId)
        // then
        verify(pollDao).deletePoll(pollId)
    }

    @Test
    fun `when calling selectAllUserMessages, then selectByUserId from MessageDao is called and entities are mapped to messages`() =
        runTest {
            // given
            val userId = randomString()
            val messageEntities = listOf(
                randomMessageEntity(userId = userId),
                randomMessageEntity(userId = userId),
                randomMessageEntity(userId = userId),
            )

            whenever(messageDao.selectByUserId(userId)) doReturn messageEntities

            val repository = DatabaseMessageRepository(
                this,
                messageDao,
                replyMessageDao,
                pollDao,
                ::randomUser,
                randomUser(id = "currentUserId"),
                emptySet(),
            )

            // when
            val result = repository.selectAllUserMessages(userId)

            // then
            verify(messageDao).selectByUserId(userId)
            assertEquals(messageEntities.size, result.size)
            result.forEachIndexed { index, message ->
                assertEquals(messageEntities[index].messageInnerEntity.id, message.id)
                assertEquals(messageEntities[index].messageInnerEntity.userId, message.user.id)
            }
        }

    @Test
    fun `when calling selectAllChannelUserMessages, then selectByCidAndUserId from MessageDao is called and entities are mapped to messages`() =
        runTest {
            // given
            val cid = randomString()
            val userId = randomString()
            val messageEntities = listOf(
                randomMessageEntity(cid = cid, userId = userId),
                randomMessageEntity(cid = cid, userId = userId),
            )

            whenever(messageDao.selectByCidAndUserId(cid, userId)) doReturn messageEntities

            val repository = DatabaseMessageRepository(
                this,
                messageDao,
                replyMessageDao,
                pollDao,
                ::randomUser,
                randomUser(id = "currentUserId"),
                emptySet(),
            )

            // when
            val result = repository.selectAllChannelUserMessages(cid, userId)

            // then
            verify(messageDao).selectByCidAndUserId(cid, userId)
            assertEquals(messageEntities.size, result.size)
            result.forEachIndexed { index, message ->
                assertEquals(messageEntities[index].messageInnerEntity.id, message.id)
                assertEquals(messageEntities[index].messageInnerEntity.cid, message.cid)
                assertEquals(messageEntities[index].messageInnerEntity.userId, message.user.id)
            }
        }

    @Test
    fun `when calling deleteMessages, then deleteMessages from MessageDao is called`() = runTest(testDispatcher) {
        // given
        val messages = listOf(
            randomMessage(id = "message1"),
            randomMessage(id = "message2"),
            randomMessage(id = "message3"),
        )
        val messageIds = messages.map { it.id }

        val repository = DatabaseMessageRepository(
            this,
            messageDao,
            replyMessageDao,
            pollDao,
            ::randomUser,
            randomUser(id = "currentUserId"),
            emptySet(),
        )

        // when
        repository.deleteMessages(messages)

        // then
        verify(messageDao).deleteMessages(messageIds)
    }
}
