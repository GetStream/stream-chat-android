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

package io.getstream.chat.android.state.plugin.logic.querythreads.internal

import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadInfo
import io.getstream.chat.android.models.ThreadParticipant
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.state.plugin.state.querythreads.internal.QueryThreadsMutableState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

internal class QueryThreadsStateLogicTest {

    private val threadList = listOf(
        Thread(
            activeParticipantCount = 2,
            cid = "messaging:123",
            channel = null,
            parentMessageId = "mId1",
            parentMessage = Message(
                id = "mId1",
                cid = "messaging:123",
                text = "Thread parent",
                replyCount = 1,
            ),
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
            read = listOf(
                ChannelUserRead(
                    User(id = "usrId1"),
                    lastReceivedEventDate = Date(),
                    unreadMessages = 0,
                    lastRead = Date(),
                    lastReadMessageId = "mId2",
                ),
                ChannelUserRead(
                    user = User(id = "usrId2"),
                    lastReceivedEventDate = Date(),
                    unreadMessages = 1,
                    lastRead = Date(),
                    lastReadMessageId = null,
                ),
            ),
            draft = null,
        ),
    )
    val mutableGlobalState = MutableGlobalState("usrId1")

    @Test
    fun `Given QueryThreadsStateLogic When getting isLoading Should return isLoading from mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.loading) doReturn MutableStateFlow(true)
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        val isLoading = logic.isLoading()
        // then
        isLoading `should be equal to` true
        verify(mutableState, times(1)).loading
    }

    @Test
    fun `Given QueryThreadsStateLogic When calling setLoading Should update mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        doNothing().whenever(mutableState).setLoading(any())
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        logic.setLoading(true)
        // then
        verify(mutableState, times(1)).setLoading(true)
    }

    @Test
    fun `Given QueryThreadsStateLogic When getting isLoadingMore Should return isLoadingMore from mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.loadingMore) doReturn MutableStateFlow(true)
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        val isLoading = logic.isLoadingMore()
        // then
        isLoading `should be equal to` true
        verify(mutableState, times(1)).loadingMore
    }

    @Test
    fun `Given QueryThreadsStateLogic When calling setLoadingMore Should update mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        doNothing().whenever(mutableState).setLoadingMore(any())
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        logic.setLoadingMore(true)
        // then
        verify(mutableState, times(1)).setLoadingMore(true)
    }

    @Test
    fun `Given QueryThreadsStateLogic When getting threads Should return threads from mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threads) doReturn MutableStateFlow(emptyList())
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        val threads = logic.getThreads()
        // then
        threads `should be equal to` emptyList()
        verify(mutableState, times(1)).threads
    }

    @Test
    fun `Given QueryThreadsStateLogic When calling setThreads Should update mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        doNothing().whenever(mutableState).setThreads(any())
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        logic.setThreads(emptyList())
        // then
        verify(mutableState, times(1)).setThreads(emptyList())
    }

    @Test
    fun `Given QueryThreadsStateLogic When calling insertThreadsIfAbsent Should update mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        doNothing().whenever(mutableState).upsertThreads(any())
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        logic.insertThreadsIfAbsent(emptyList())
        // then
        verify(mutableState, times(1)).insertThreadsIfAbsent(emptyList())
    }

    @Test
    fun `Given QueryThreadsStateLogic When calling upsertThreads Should update mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        doNothing().whenever(mutableState).upsertThreads(any())
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        logic.upsertThreads(emptyList())
        // then
        verify(mutableState, times(1)).upsertThreads(emptyList())
    }

    @Test
    fun `Given QueryThreadsStateLogic When calling clearThreads Should update mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        doNothing().whenever(mutableState).upsertThreads(any())
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        logic.clearThreads()
        // then
        verify(mutableState, times(1)).clearThreads()
    }

    @Test
    fun `Given QueryThreadsStateLogic When calling setNext Should update mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        doNothing().whenever(mutableState).setNext(any())
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        logic.setNext("nextCursor")
        // then
        verify(mutableState, times(1)).setNext("nextCursor")
    }

    @Test
    fun `Given QueryThreadsStateLogic When getting unseenThreadIds Should return unseenThreadIds from mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.unseenThreadIds) doReturn MutableStateFlow(setOf("mId1"))
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        val unseenThreadIds = logic.getUnseenThreadIds()
        // then
        unseenThreadIds `should be equal to` setOf("mId1")
        verify(mutableState, times(1)).unseenThreadIds
    }

    @Test
    fun `Given QueryThreadsStateLogic When calling addUnseenThreadId Should update mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        doNothing().whenever(mutableState).addUnseenThreadId(any())
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        logic.addUnseenThreadId("threadId")
        // then
        verify(mutableState, times(1)).addUnseenThreadId("threadId")
    }

    @Test
    fun `Given QueryThreadsStateLogic When calling clearUnseenThreadIds Should update mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        doNothing().whenever(mutableState).clearUnseenThreadIds()
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        logic.clearUnseenThreadIds()
        // then
        verify(mutableState, times(1)).clearUnseenThreadIds()
    }

    @Test
    fun `Given QueryTestStateLogic When calling getMessage for parent message Should return message`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threadMap) doReturn mapOf("mId1" to threadList[0])
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        val message = logic.getMessage("mId1")
        // then
        message `should be equal to` threadList[0].parentMessage
    }

    @Test
    fun `Given QueryTestStateLogic When calling getMessage for reply message Should return message`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threadMap) doReturn mapOf("mId1" to threadList[0])
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        val message = logic.getMessage("mId2")
        // then
        message `should be equal to` threadList[0].latestReplies[0]
    }

    @Test
    fun `Given QueryTestStateLogic When calling getMessage for unknown id Should return null`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threadMap) doReturn mapOf("mId1" to threadList[0])
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        val message = logic.getMessage("mId3")
        // then
        message `should be` null
    }

    @Test
    fun `Given QueryThreadsStateLogic When calling deleteMessage for parent message Should delete thread`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threadMap) doReturn mapOf("mId1" to threadList[0])
        doNothing().whenever(mutableState).deleteThread(any())
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        val messageToDelete = threadList[0].parentMessage
        logic.deleteMessage(messageToDelete)
        // then
        verify(mutableState).deleteThread("mId1")
    }

    @Test
    fun `Given QueryThreadsStateLogic When calling deleteMessage for reply message Should delete reply`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threadMap) doReturn mapOf("mId1" to threadList[0])
        doNothing().whenever(mutableState).deleteMessageFromThread(any(), any())
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        val messageToDelete = threadList[0].latestReplies[0]
        logic.deleteMessage(messageToDelete)
        // then
        verify(mutableState).deleteMessageFromThread(threadId = "mId1", messageId = "mId2")
    }

    @Test
    fun `Given QueryThreadsStateLogic When calling deleteMessage for unknown message Should do nothing`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threadMap) doReturn mapOf("mId1" to threadList[0])
        doNothing().whenever(mutableState).deleteThread(any())
        doNothing().whenever(mutableState).deleteMessageFromThread(any(), any())
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        val messageToDelete = Message()
        logic.deleteMessage(messageToDelete)
        // then
        verify(mutableState, never()).deleteThread(any())
        verify(mutableState, never()).deleteMessageFromThread(any(), any())
    }

    @Test
    fun `Given QueryThreadsStateLogic When updating parent message which doesn't exist Should return false`() =
        runTest {
            // given
            val mutableState = mock<QueryThreadsMutableState>()
            whenever(mutableState.threads) doReturn MutableStateFlow(threadList)
            val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
            val parent = Message(
                id = "mId3",
                cid = "messaging:123",
                text = "Text",
            )
            // when
            val updated = logic.updateParent(parent)
            // then
            updated `should be equal to` false
        }

    @Test
    fun `Given QueryThreadsStateLogic When updating parent message which exists Should return true`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threadMap) doReturn threadList.associateBy(Thread::parentMessageId)
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        val parent = Message(
            id = "mId1",
            cid = "messaging:123",
            text = "Text",
            replyCount = 1,
        )
        // when
        val updated = logic.updateParent(parent)
        // then
        val expectedUpdatedThread = threadList[0].copy(
            parentMessage = parent,
            deletedAt = parent.deletedAt,
            updatedAt = parent.updatedAt ?: threadList[0].updatedAt,
        )
        val expectedUpdatedThreadList = listOf(expectedUpdatedThread)
        updated `should be equal to` true
        verify(mutableState, times(1)).upsertThreads(expectedUpdatedThreadList)
    }

    @Test
    fun `Given QueryThreadsStateLogic When upserting reply without parent Should do nothing`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threads) doReturn MutableStateFlow(threadList)
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        val reply = Message(
            id = "mId3",
            cid = "messaging:123",
            text = "Text",
            parentId = null,
        )
        // when
        logic.upsertReply(reply)
        // then
        verify(mutableState, never()).threads
        verify(mutableState, never()).setThreads(any())
    }

    @Test
    fun `Given QueryThreadsStateLogic When upserting reply in unknown thread Should do nothing`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threadMap) doReturn threadList.associateBy(Thread::parentMessageId)
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        val reply = Message(
            id = "mId3",
            cid = "messaging:123",
            text = "Text",
            parentId = "mId10",
        )
        // when
        logic.upsertReply(reply)
        // then
        verify(mutableState, times(1)).threadMap
        verify(mutableState, times(0)).upsertThreads(any())
    }

    @Test
    fun `Given QueryThreadsStateLogic When updating reply in existing thread Should update mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threadMap) doReturn threadList.associateBy(Thread::parentMessageId)
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        val reply = Message(
            id = "mId2",
            cid = "messaging:123",
            text = "Updated text",
            parentId = "mId1",
        )
        // when
        logic.upsertReply(reply)
        // then
        val expectedUpdatedThread = threadList[0].copy(latestReplies = listOf(reply))
        val expectedUpdatedThreadList = listOf(expectedUpdatedThread)
        verify(mutableState, times(1)).upsertThreads(expectedUpdatedThreadList)
    }

    @Test
    fun `Given QueryThreadsStateLogic When inserting reply in existing thread from new participant Should update mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threadMap) doReturn threadList.associateBy(Thread::parentMessageId)
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        val reply = Message(
            id = "mId3",
            cid = "messaging:123",
            text = "Updated text",
            parentId = "mId1",
            user = User(id = "usrId3"),
        )
        // when
        logic.upsertReply(reply)
        // then
        val expectedUpdatedThread = threadList[0].copy(
            latestReplies = threadList[0].latestReplies + listOf(reply),
            participantCount = 3,
            threadParticipants = threadList[0].threadParticipants + listOf(ThreadParticipant(User("usrId3"))),
            read = threadList[0].read.map { read ->
                read.copy(unreadMessages = read.unreadMessages + 1)
            },
        )
        val expectedUpdatedThreadList = listOf(expectedUpdatedThread)
        verify(mutableState, times(1)).upsertThreads(expectedUpdatedThreadList)
    }

    @Test
    fun `Given QueryThreadsStateLogic When inserting reply in existing thread from existing participant Should update mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threadMap) doReturn threadList.associateBy(Thread::parentMessageId)
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        val reply = Message(
            id = "mId3",
            cid = "messaging:123",
            text = "Updated text",
            parentId = "mId1",
            user = User(id = "usrId2"),
        )
        // when
        logic.upsertReply(reply)
        // then
        val expectedUpdatedThread = threadList[0].copy(
            latestReplies = threadList[0].latestReplies + listOf(reply),
            read = threadList[0].read.map { read ->
                if (read.user.id == "usrId2") {
                    read
                } else {
                    read.copy(unreadMessages = read.unreadMessages + 1)
                }
            },
        )
        val expectedUpdatedThreadList = listOf(expectedUpdatedThread)
        verify(mutableState, times(1)).upsertThreads(expectedUpdatedThreadList)
    }

    @Test
    fun `Given QueryThreadsStateLogic When marking unknown thread as read Should do nothing`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threadMap) doReturn threadList.associateBy(Thread::parentMessageId)
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        logic.markThreadAsReadByUser(
            threadInfo = ThreadInfo(
                activeParticipantCount = 2,
                cid = "messaging:123",
                createdAt = Date(),
                createdBy = null,
                createdByUserId = "usrId2",
                deletedAt = null,
                lastMessageAt = Date(),
                parentMessage = null,
                parentMessageId = "mId13", // not a loaded thread
                participantCount = 2,
                replyCount = 2,
                title = "Unknown thread",
                updatedAt = Date(),
            ),
            user = User(id = "userId1"),
            createdAt = Date(),
        )
        // then
        verify(mutableState, times(0)).upsertThreads(any())
    }

    @Test
    fun `Given QueryThreadsStateLogic When marking thread as read Should update mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threadMap) doReturn threadList.associateBy(Thread::parentMessageId)
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        val threadInfo = ThreadInfo(
            activeParticipantCount = 2,
            cid = "messaging:123",
            createdAt = Date(),
            createdBy = null,
            createdByUserId = "usrId1",
            deletedAt = null,
            lastMessageAt = Date(),
            parentMessage = null,
            parentMessageId = "mId1", // loaded thread
            participantCount = 2,
            replyCount = 1,
            title = "Thread 1",
            updatedAt = Date(),
        )
        val user = User(id = "usrId2")
        val createdAt = Date()
        logic.markThreadAsReadByUser(threadInfo, user, createdAt)
        // then
        val expectedUpdatedThread = threadList[0].copy(
            activeParticipantCount = threadInfo.activeParticipantCount,
            deletedAt = threadInfo.deletedAt,
            lastMessageAt = threadInfo.lastMessageAt ?: threadList[0].lastMessageAt,
            parentMessage = threadInfo.parentMessage ?: threadList[0].parentMessage,
            participantCount = threadInfo.participantCount,
            title = threadInfo.title,
            updatedAt = threadInfo.updatedAt,
            read = threadList[0].read.map { read ->
                if (read.user.id == user.id) {
                    read.copy(user = user, unreadMessages = 0, lastReceivedEventDate = createdAt)
                } else {
                    read
                }
            },
        )
        val expectedUpdatedThreadList = listOf(expectedUpdatedThread)
        verify(mutableState, times(1)).upsertThreads(expectedUpdatedThreadList)
    }

    @Test
    fun `Given QueryThreadsStateLogic When updating thread from event Should update title extraData and updatedAt`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threadMap) doReturn threadList.associateBy(Thread::parentMessageId)
        doNothing().whenever(mutableState).upsertThreads(any())
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        val newUpdatedAt = Date()
        val threadInfo = ThreadInfo(
            activeParticipantCount = 5,
            cid = "messaging:123",
            createdAt = Date(),
            createdBy = null,
            createdByUserId = "usrId1",
            deletedAt = null,
            lastMessageAt = Date(),
            parentMessage = null,
            parentMessageId = "mId1",
            participantCount = 5,
            replyCount = 10,
            title = "Updated Title",
            updatedAt = newUpdatedAt,
            extraData = mapOf("color" to "red"),
        )
        logic.updateThreadFromEvent(threadInfo)
        // then
        val expectedUpdatedThread = threadList[0].copy(
            title = "Updated Title",
            updatedAt = newUpdatedAt,
            extraData = mapOf("color" to "red"),
        )
        val expectedUpdatedThreadList = listOf(expectedUpdatedThread)
        verify(mutableState, times(1)).upsertThreads(expectedUpdatedThreadList)
    }

    @Test
    fun `Given QueryThreadsStateLogic When updating unknown thread from event Should do nothing`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threadMap) doReturn threadList.associateBy(Thread::parentMessageId)
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        val threadInfo = ThreadInfo(
            activeParticipantCount = 2,
            cid = "messaging:123",
            createdAt = Date(),
            createdBy = null,
            createdByUserId = "usrId1",
            deletedAt = null,
            lastMessageAt = Date(),
            parentMessage = null,
            parentMessageId = "mId999",
            participantCount = 2,
            replyCount = 1,
            title = "Unknown thread",
            updatedAt = Date(),
        )
        logic.updateThreadFromEvent(threadInfo)
        // then
        verify(mutableState, never()).upsertThreads(any())
    }

    @Test
    fun `Given QueryThreadsStateLogic When marking not existing thread as unread Should do nothing`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threadMap) doReturn threadList.associateBy(Thread::parentMessageId)
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        val threadId = "mId2"
        val user = User(id = "usrId2")
        val createdAt = Date()
        logic.markThreadAsUnreadByUser(threadId, user, createdAt)
        // then
        verify(mutableState, never()).upsertThreads(any())
    }

    @Test
    fun `Given QueryThreadsStateLogic When marking thread as unread Should update mutableState`() {
        // given
        val mutableState = mock<QueryThreadsMutableState>()
        whenever(mutableState.threadMap) doReturn threadList.associateBy(Thread::parentMessageId)
        doNothing().whenever(mutableState).upsertThreads(any())
        val logic = QueryThreadsStateLogic(mutableState, mutableGlobalState)
        // when
        val threadId = "mId1"
        val user = User(id = "usrId2")
        val createdAt = Date()
        logic.markThreadAsUnreadByUser(threadId, user, createdAt)
        // then
        val expectedUpdatedThread = threadList[0].copy(
            read = threadList[0].read.map { read ->
                if (read.user.id == user.id) {
                    read.copy(user = user, unreadMessages = read.unreadMessages + 1, lastReceivedEventDate = createdAt)
                } else {
                    read
                }
            },
        )
        val expectedUpdatedThreadList = listOf(expectedUpdatedThread)
        verify(mutableState, times(1)).upsertThreads(expectedUpdatedThreadList)
    }
}
