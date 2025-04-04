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

package io.getstream.chat.android.state.plugin.logic.querythreads.internal

import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationMarkUnreadEvent
import io.getstream.chat.android.client.events.NotificationThreadMessageNewEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.QueryThreadsResult
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadInfo
import io.getstream.chat.android.models.ThreadParticipant
import io.getstream.chat.android.models.User
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.Date

@Suppress("LargeClass")
internal class QueryThreadsLogicTest {

    private val threadList = listOf(
        Thread(
            activeParticipantCount = 2,
            cid = "messaging:123",
            channel = null,
            parentMessageId = "mId1",
            parentMessage = Message(id = "mId1", cid = "messaging:123", text = "Thread parent", replyCount = 1),
            createdByUserId = "usrId1",
            createdBy = null,
            participantCount = 2,
            threadParticipants = listOf(
                ThreadParticipant(User(id = "usrId1")),
                ThreadParticipant(User(id = "usrId2")),
            ),
            lastMessageAt = Date(),
            createdAt = Date(),
            updatedAt = Date(),
            deletedAt = null,
            title = "Thread 1",
            latestReplies = listOf(
                Message(
                    id = "mId2",
                    cid = "messaging:123",
                    text = "Thread reply",
                    parentId = "mId1",
                ),
            ),
            read = emptyList(),
        ),
    )

    @Test
    fun `Given QueryThreadsLogic When checking request precondition and data is already loading Should return failure`() {
        // given
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.isLoading()) doReturn true
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        val result = logic.onQueryThreadsPrecondition(QueryThreadsRequest())
        // then
        result shouldBeInstanceOf Result.Failure::class
    }

    @Test
    fun `Given QueryThreadsLogic When checking request precondition for more data and more data is already loading Should return failure`() {
        // given
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.isLoadingMore()) doReturn true
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        val result = logic.onQueryThreadsPrecondition(QueryThreadsRequest(next = "nextCursor"))
        // then
        result shouldBeInstanceOf Result.Failure::class
    }

    @Test
    fun `Given QueryThreadsLogic When checking request precondition for new data and more data is already loading Should return success`() {
        // given
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.isLoadingMore()) doReturn true
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        val result = logic.onQueryThreadsPrecondition(QueryThreadsRequest())
        // then
        result shouldBeInstanceOf Result.Success::class
    }

    @Test
    fun `Given QueryThreadsLogic When checking request precondition for new data and no data is loading Should return success`() {
        // given
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.isLoadingMore()) doReturn false
        whenever(stateLogic.isLoading()) doReturn false
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        val result = logic.onQueryThreadsPrecondition(QueryThreadsRequest())
        // then
        result shouldBeInstanceOf Result.Success::class
    }

    @Test
    fun `Given QueryThreadsLogic When requesting new data Should update loading state and fetch offline data`() =
        runTest {
            // given
            val stateLogic = mock<QueryThreadsStateLogic>()
            val databaseLogic = mock<QueryThreadsDatabaseLogic>()
            whenever(databaseLogic.getLocalThreadsOrder()) doReturn listOf("mId1")
            whenever(databaseLogic.getLocalThreads(any())) doReturn threadList
            val logic = QueryThreadsLogic(stateLogic, databaseLogic)
            // when
            logic.onQueryThreadsRequest(QueryThreadsRequest())
            // then
            verify(stateLogic, times(1)).setLoading(true)
            verify(stateLogic, never()).setLoadingMore(any())
            verify(databaseLogic, times(1)).getLocalThreadsOrder()
            verify(databaseLogic, times(1)).getLocalThreads(listOf("mId1"))
            verify(stateLogic, times(1)).insertThreadsIfAbsent(threadList)
        }

    @Test
    fun `Given QueryThreadsLogic When requesting new data by force reload Should update loading state and clear current data`() =
        runTest {
            // given
            val stateLogic = mock<QueryThreadsStateLogic>()
            whenever(stateLogic.getUnseenThreadIds()) doReturn setOf("mId3")
            val databaseLogic = mock<QueryThreadsDatabaseLogic>()
            val logic = QueryThreadsLogic(stateLogic, databaseLogic)
            // when
            logic.onQueryThreadsRequest(QueryThreadsRequest())
            // then
            verify(stateLogic, times(1)).setLoading(true)
            verify(stateLogic, never()).setLoadingMore(any())
            verify(stateLogic, times(1)).clearThreads()
            verify(stateLogic, times(1)).clearUnseenThreadIds()
        }

    @Test
    fun `Given QueryThreadsLogic When requesting new data Should update loadingMore state`() = runTest {
        // given
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(databaseLogic.getLocalThreadsOrder()) doReturn emptyList()
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.onQueryThreadsRequest(QueryThreadsRequest(next = "nextCursor"))
        // then
        verify(stateLogic, never()).setLoading(true)
        verify(stateLogic, times(1)).setLoadingMore(any())
    }

    @Test
    fun `Given QueryThreadsLogic When handling new data result Should set threads and clear unseenThreadIds`() =
        runTest {
            // given
            val stateLogic = mock<QueryThreadsStateLogic>()
            val databaseLogic = mock<QueryThreadsDatabaseLogic>()
            val logic = QueryThreadsLogic(stateLogic, databaseLogic)
            // when
            val request = QueryThreadsRequest()
            val result = Result.Success(
                value = QueryThreadsResult(
                    threads = emptyList(),
                    prev = null,
                    next = "nextCursor",
                ),
            )
            logic.onQueryThreadsResult(result, request)
            // then
            verify(stateLogic, times(1)).setLoading(false)
            verify(stateLogic, times(1)).setLoadingMore(false)
            verify(stateLogic, times(1)).setThreads(emptyList())
            verify(stateLogic, times(1)).clearUnseenThreadIds()
            verify(stateLogic, times(1)).setNext("nextCursor")
            verify(stateLogic, never()).upsertThreads(any())
        }

    @Test
    fun `Given QueryThreadsLogic When handling more data result Should append threads`() = runTest {
        // given
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        val request = QueryThreadsRequest(next = "page2Cursor")
        val result = Result.Success(
            value = QueryThreadsResult(
                threads = emptyList(),
                prev = "page1Cursor",
                next = "page3Cursor",
            ),
        )
        logic.onQueryThreadsResult(result, request)
        // then
        verify(stateLogic, times(1)).setLoading(false)
        verify(stateLogic, times(1)).setLoadingMore(false)
        verify(stateLogic, times(1)).setNext("page3Cursor")
        verify(stateLogic, times(1)).upsertThreads(emptyList())
        verify(stateLogic, never()).setThreads(any())
        verify(stateLogic, never()).clearUnseenThreadIds()
    }

    @Test
    fun `Given QueryThreadsLogic When handling error result Should update loading state`() = runTest {
        // given
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        val request = QueryThreadsRequest()
        val result = Result.Failure(Error.GenericError("error"))
        logic.onQueryThreadsResult(result, request)
        // then
        verify(stateLogic, times(1)).setLoading(false)
        verify(stateLogic, times(1)).setLoadingMore(false)
        verify(stateLogic, never()).setNext(any())
        verify(stateLogic, never()).upsertThreads(any())
        verify(stateLogic, never()).setThreads(any())
        verify(stateLogic, never()).clearUnseenThreadIds()
    }

    @Test
    fun `Given QueryThreadsLogic When handling ChannelDeletedEvent Should update state by deleting affected threads`() {
        // given
        val event = NotificationChannelDeletedEvent(
            type = "notification.channel_deleted",
            createdAt = Date(),
            rawCreatedAt = "",
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            channel = Channel(),
            channelLastMessageAt = Date(),
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        val expectedUpdatedThreadList = emptyList<Thread>()
        verify(stateLogic, times(1)).setThreads(expectedUpdatedThreadList)
    }

    @Test
    fun `Given QueryThreadsLogic When handling ThreadMessageNew for existing thread Should do nothing`() {
        // given
        val event = NotificationThreadMessageNewEvent(
            type = "notification.thread_message_new",
            cid = "messaging:123",
            channelId = "123",
            channelType = "messaging",
            message = Message(id = "mId3", parentId = "mId1", text = "Text"),
            channel = Channel(),
            createdAt = Date(),
            rawCreatedAt = "",
            unreadThreads = 1,
            unreadThreadMessages = 2,
            channelLastMessageAt = Date(),
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verify(stateLogic, never()).addUnseenThreadId(any())
    }

    @Test
    fun `Given QueryThreadsLogic When handling ThreadMessageNew for new thread Should update unseenThreadIds`() {
        // given
        val event = NotificationThreadMessageNewEvent(
            type = "notification.thread_message_new",
            cid = "messaging:123",
            channelId = "123",
            channelType = "messaging",
            message = Message(id = "mId3", parentId = "mId4", text = "Text"),
            channel = Channel(),
            createdAt = Date(),
            rawCreatedAt = "",
            unreadThreads = 1,
            unreadThreadMessages = 2,
            channelLastMessageAt = Date(),
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verify(stateLogic, times(1)).addUnseenThreadId("mId4")
    }

    @Test
    fun `Given QueryThreadsLogic When handling NotificationMarkUnread for non thread Should do nothing`() {
        // given
        val event = NotificationMarkUnreadEvent(
            type = "notification.mark_unread",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = "usrId1"),
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            lastReadMessageId = "mId1",
            lastReadMessageAt = Date(),
            firstUnreadMessageId = "mId2",
            threadId = null,
            unreadMessages = 1,
            channelLastMessageAt = Date(),
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        doNothing().whenever(stateLogic).markThreadAsUnreadByUser(any(), any(), any())
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verify(stateLogic, never()).markThreadAsUnreadByUser(any(), any(), any())
    }

    @Test
    fun `Given QueryThreadsLogic When handling NotificationMarkUnread for thread Should mark unread via stateLogic`() {
        // given
        val event = NotificationMarkUnreadEvent(
            type = "notification.mark_unread",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = "usrId1"),
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            lastReadMessageId = null,
            lastReadMessageAt = Date(),
            firstUnreadMessageId = "mId1",
            threadId = "mId1",
            unreadMessages = 1,
            channelLastMessageAt = Date(),
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        doNothing().whenever(stateLogic).markThreadAsUnreadByUser(any(), any(), any())
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verify(stateLogic, times(1)).markThreadAsUnreadByUser(event.threadId!!, event.user, event.createdAt)
    }

    @Test
    fun `Given QueryThreadsLogic When handling MessageRead for thread Should mark read via stateLogic`() {
        // given
        val event = MessageReadEvent(
            type = "notification.thread_message_new",
            cid = "messaging:123",
            channelId = "123",
            channelType = "messaging",
            createdAt = Date(),
            rawCreatedAt = "",
            thread = ThreadInfo(
                activeParticipantCount = 2,
                cid = "messaging:123",
                createdAt = Date(),
                createdBy = null,
                createdByUserId = "usrId1",
                deletedAt = null,
                lastMessageAt = Date(),
                parentMessage = null,
                parentMessageId = "mId1",
                participantCount = 2,
                replyCount = 2,
                title = "Thread 1",
                updatedAt = Date(),
            ),
            user = User(id = "usrId2"),
            channelLastMessageAt = Date(),
            lastReadMessageId = null,
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verify(stateLogic, times(1)).markThreadAsReadByUser(event.thread!!, event.user, event.createdAt)
    }

    @Test
    fun `Given QueryThreadsLogic When handling MessageRead without thread Should do nothing`() {
        // given
        val event = MessageReadEvent(
            type = "notification.thread_message_new",
            cid = "messaging:123",
            channelId = "123",
            channelType = "messaging",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = "usrId2"),
            channelLastMessageAt = Date(),
            lastReadMessageId = null,
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verify(stateLogic, never()).markThreadAsReadByUser(any(), any(), any())
    }

    @Test
    fun `Given QueryThreadsLogic When handling MessageNew Should upsert reply`() {
        // given
        val event = NewMessageEvent(
            type = "reply",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = "usrId1"),
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            message = Message(id = "mId4", parentId = "mId1", text = "New reply"),
            channelLastMessageAt = Date(),
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verify(stateLogic, times(1)).upsertReply(event.message)
    }

    @Test
    fun `Given QueryThreadsLogic When handling MessageUpdated for parent Should update parent`() {
        // given
        val event = MessageUpdatedEvent(
            type = "reply",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = "usrId1"),
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            message = Message(id = "mId1", text = "Updated thread parent"),
            channelLastMessageAt = Date(),
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        whenever(stateLogic.updateParent(event.message)) doReturn true
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verify(stateLogic, times(1)).updateParent(event.message)
        verify(stateLogic, times(0)).upsertReply(event.message)
    }

    @Test
    fun `Given QueryThreadsLogic When handling MessageUpdated for reply Should upsert reply`() {
        // given
        val event = MessageUpdatedEvent(
            type = "reply",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = "usrId1"),
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            message = Message(id = "mId2", text = "Updated thread reply"),
            channelLastMessageAt = Date(),
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        whenever(stateLogic.updateParent(event.message)) doReturn false
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verify(stateLogic, times(1)).updateParent(event.message)
        verify(stateLogic, times(1)).upsertReply(event.message)
    }

    @Test
    fun `Given QueryThreadsLogic When handling MessageDeleted for parent Should update parent`() {
        // given
        val event = MessageDeletedEvent(
            type = "reply",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = "usrId1"),
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            message = Message(id = "mId1", text = "Deleted thread parent"),
            hardDelete = false,
            channelLastMessageAt = Date(),
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        whenever(stateLogic.updateParent(event.message)) doReturn true
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verify(stateLogic, times(1)).updateParent(event.message)
        verify(stateLogic, times(0)).upsertReply(event.message)
    }

    @Test
    fun `Given QueryThreadsLogic When handling MessageDeleted for reply Should upsert reply`() {
        // given
        val event = MessageDeletedEvent(
            type = "reply",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = "usrId1"),
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            message = Message(id = "mId2", text = "Deleted thread reply"),
            hardDelete = false,
            channelLastMessageAt = Date(),
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        whenever(stateLogic.updateParent(event.message)) doReturn false
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verify(stateLogic, times(1)).updateParent(event.message)
        verify(stateLogic, times(1)).upsertReply(event.message)
    }

    @Test
    fun `Given QueryThreadsLogic When handling ReactionNew for parent Should update parent`() {
        // given
        val event = ReactionNewEvent(
            type = "reply",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = "usrId1"),
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            message = Message(id = "mId1", text = "Thread parent"),
            reaction = Reaction(),
            channelLastMessageAt = Date(),
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        whenever(stateLogic.updateParent(event.message)) doReturn true
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verify(stateLogic, times(1)).updateParent(event.message)
        verify(stateLogic, times(0)).upsertReply(event.message)
    }

    @Test
    fun `Given QueryThreadsLogic When handling ReactionNew for reply Should upsert reply`() {
        // given
        val event = ReactionNewEvent(
            type = "reply",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = "usrId1"),
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            message = Message(id = "mId2", text = "Thread reply"),
            reaction = Reaction(),
            channelLastMessageAt = Date(),
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        whenever(stateLogic.updateParent(event.message)) doReturn false
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verify(stateLogic, times(1)).updateParent(event.message)
        verify(stateLogic, times(1)).upsertReply(event.message)
    }

    @Test
    fun `Given QueryThreadsLogic When handling ReactionUpdate for parent Should update parent`() {
        // given
        val event = ReactionUpdateEvent(
            type = "reply",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = "usrId1"),
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            message = Message(id = "mId1", text = "Thread parent"),
            reaction = Reaction(),
            channelLastMessageAt = Date(),
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        whenever(stateLogic.updateParent(event.message)) doReturn true
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verify(stateLogic, times(1)).updateParent(event.message)
        verify(stateLogic, times(0)).upsertReply(event.message)
    }

    @Test
    fun `Given QueryThreadsLogic When handling ReactionUpdate for reply Should upsert reply`() {
        // given
        val event = ReactionUpdateEvent(
            type = "reply",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = "usrId1"),
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            message = Message(id = "mId2", text = "Thread reply"),
            reaction = Reaction(),
            channelLastMessageAt = Date(),
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        whenever(stateLogic.updateParent(event.message)) doReturn false
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verify(stateLogic, times(1)).updateParent(event.message)
        verify(stateLogic, times(1)).upsertReply(event.message)
    }

    @Test
    fun `Given QueryThreadsLogic When handling ReactionDeleted for parent Should update parent`() {
        // given
        val event = ReactionDeletedEvent(
            type = "reply",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = "usrId1"),
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            message = Message(id = "mId1", text = "Thread parent"),
            reaction = Reaction(),
            channelLastMessageAt = Date(),
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        whenever(stateLogic.updateParent(event.message)) doReturn true
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verify(stateLogic, times(1)).updateParent(event.message)
        verify(stateLogic, times(0)).upsertReply(event.message)
    }

    @Test
    fun `Given QueryThreadsLogic When handling ReactionDeleted for reply Should upsert reply`() {
        // given
        val event = ReactionDeletedEvent(
            type = "reply",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = "usrId1"),
            cid = "messaging:123",
            channelType = "messaging",
            channelId = "123",
            message = Message(id = "mId2", text = "Thread reply"),
            reaction = Reaction(),
            channelLastMessageAt = Date(),
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        whenever(stateLogic.updateParent(event.message)) doReturn false
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verify(stateLogic, times(1)).updateParent(event.message)
        verify(stateLogic, times(1)).upsertReply(event.message)
    }

    @Test
    fun `Given QueryThreadsLogic When handling unsupported event Should do nothing`() {
        // given
        val event = UnknownEvent(
            type = "reply",
            createdAt = Date(),
            rawCreatedAt = "",
            user = User(id = "usrId1"),
            rawData = emptyMap<Any, Any>(),
        )
        val stateLogic = mock<QueryThreadsStateLogic>()
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.handleEvents(listOf(event))
        // then
        verifyNoInteractions(stateLogic)
    }

    @Test
    fun `Given QueryThreadsLogic When calling getMessage Should get message via stateLogic`() {
        // given
        val stateLogic = mock<QueryThreadsStateLogic>()
        whenever(stateLogic.getMessage("mId1")) doReturn Message(id = "mId1")
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        val message = logic.getMessage("mId1")
        // then
        message `should be equal to` Message(id = "mId1")
        verify(stateLogic, times(1)).getMessage("mId1")
    }

    @Test
    fun `Given QueryThreadsLogic When calling upsertMessage for parent Should update parent`() {
        // given
        val messageToUpsert = Message(id = "mId1", text = "Updated thread parent")
        val stateLogic = mock<QueryThreadsStateLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        whenever(stateLogic.updateParent(messageToUpsert)) doReturn true
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.upsertMessage(messageToUpsert)
        // then
        verify(stateLogic, times(1)).updateParent(messageToUpsert)
        verify(stateLogic, never()).upsertReply(any())
    }

    @Test
    fun `Given QueryThreadsLogic When calling upsertMessage for reply Should upsert reply`() {
        // given
        val messageToUpsert = Message(id = "mId4", parentId = "mId1", text = "New reply")
        val stateLogic = mock<QueryThreadsStateLogic>()
        whenever(stateLogic.getThreads()) doReturn threadList
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        logic.upsertMessage(messageToUpsert)
        // then
        verify(stateLogic, times(1)).updateParent(messageToUpsert)
        verify(stateLogic, times(1)).upsertReply(messageToUpsert)
    }

    @Test
    fun `Given QueryThreadsLogic When calling deleteMessage Should delete message via stateLogic`() {
        // given
        val stateLogic = mock<QueryThreadsStateLogic>()
        doNothing().whenever(stateLogic).deleteMessage(any())
        val databaseLogic = mock<QueryThreadsDatabaseLogic>()
        val logic = QueryThreadsLogic(stateLogic, databaseLogic)
        // when
        val messageToDelete = Message(id = "mId1")
        logic.deleteMessage(messageToDelete)
        // then
        verify(stateLogic, times(1)).deleteMessage(messageToDelete)
    }
}
