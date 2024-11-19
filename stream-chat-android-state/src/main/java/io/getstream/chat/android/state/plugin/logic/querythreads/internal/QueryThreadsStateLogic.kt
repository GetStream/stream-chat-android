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

import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadInfo
import io.getstream.chat.android.models.ThreadParticipant
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.plugin.state.querythreads.internal.QueryThreadsMutableState
import java.util.Date

/**
 * Logic for managing the state of the threads list.
 *
 * @param mutableState Reference to the global [QueryThreadsMutableState].
 */
@Suppress("TooManyFunctions")
internal class QueryThreadsStateLogic(private val mutableState: QueryThreadsMutableState) {

    /**
     * Retrieves the current state of the 'loading' indicator from the [mutableState].
     */
    internal fun isLoading() = mutableState.loading.value

    /**
     * Updates the loading state of the [mutableState].
     *
     * @param loading The new loading state.
     */
    internal fun setLoading(loading: Boolean) =
        mutableState.setLoading(loading)

    /**
     * Retrieves the current state of the 'loading more' indicator from the [mutableState].
     */
    internal fun isLoadingMore() = mutableState.loadingMore.value

    /**
     * Updates the loading more state of the [mutableState].
     *
     * @param loading The new loading more state.
     */
    internal fun setLoadingMore(loading: Boolean) =
        mutableState.setLoadingMore(loading)

    /**
     * Retrieves the current state of the thread list from the [mutableState].
     */
    internal fun getThreads() = mutableState.threads.value

    /**
     * Updates the thread state of the [mutableState].
     *
     * @param threads The new threads state.
     */
    internal fun setThreads(threads: List<Thread>) =
        mutableState.setThreads(threads)

    /**
     * Upsert a list of threads in the [mutableState].
     *
     * @param threads The threads to upsert.
     */
    internal fun upsertThreads(threads: List<Thread>) =
        mutableState.upsertThreads(threads)

    /**
     * Updates the identifier for the next page of threads in the [mutableState].
     *
     * @param next The next page identifier.
     */
    internal fun setNext(next: String?) =
        mutableState.setNext(next)

    /**
     * Adds a new thread to the set of unseen thread IDs in the [mutableState].
     *
     * @param id The ID of the new [Thread].
     */
    internal fun addUnseenThreadId(id: String) =
        mutableState.addUnseenThreadId(id)

    /**
     * Clears the set of unseen thread IDs in the [mutableState].
     */
    internal fun clearUnseenThreadIds() =
        mutableState.clearUnseenThreadIds()

    /**
     * Retrieves a message from the [mutableState] if it exists.
     */
    internal fun getMessage(messageId: String): Message? {
        val threads = mutableState.threadMap
        return threads[messageId]?.parentMessage
            ?: threads.flatMap { it.value.latestReplies }.find { it.id == messageId }
    }

    /**
     * Deletes a message from a [Thread] in the [mutableState].
     *
     * @param message The [Message] to delete.
     */
    internal fun deleteMessage(message: Message) {
        val threads = mutableState.threadMap
        if (message.parentId == null && threads.containsKey(message.id)) {
            // Message is a thread parent
            mutableState.deleteThread(message.id)
        } else if (message.parentId != null) {
            // Message is a potential thread reply
            mutableState.deleteMessageFromThread(message.parentId, message.id)
        }
    }

    /**
     * Updates the parent message of a thread.
     *
     * @param parent The new state of the thread parent message.
     * @return true if matching parent message was found and was updated, false otherwise.
     */
    internal fun updateParent(parent: Message): Boolean {
        val oldThreads = getThreads()
        var threadFound = false
        val newThreads = oldThreads.map {
            if (it.parentMessageId == parent.id) {
                threadFound = true
                it.copy(
                    parentMessage = parent,
                    deletedAt = parent.deletedAt,
                    updatedAt = parent.updatedAt ?: it.updatedAt,
                    replyCount = parent.replyCount,
                )
            } else {
                it
            }
        }
        mutableState.setThreads(newThreads)
        return threadFound
    }

    /**
     * Inserts/updates the given reply into the appropriate thread.
     *
     * @param reply The reply to upsert.
     */
    internal fun upsertReply(reply: Message) {
        if (reply.parentId == null) return
        val oldThreads = getThreads()
        val newThreads = oldThreads.map { thread ->
            if (thread.parentMessageId == reply.parentId) {
                upsertReplyInThread(thread, reply)
            } else {
                thread
            }
        }
        mutableState.setThreads(newThreads)
    }

    /**
     * Marks the given thread as read by the given user.
     *
     * @param threadInfo The [ThreadInfo] holding info about the [Thread] which should be marked as read.
     * @param user The [User] for which the thread should be marked as read.
     * @param createdAt The [Date] of the 'mark read' event.
     */
    internal fun markThreadAsReadByUser(threadInfo: ThreadInfo, user: User, createdAt: Date) {
        val updatedThreads = getThreads().map { thread ->
            if (threadInfo.parentMessageId == thread.parentMessageId) {
                val updatedRead = thread.read.map { read ->
                    if (read.user.id == user.id) {
                        read.copy(
                            user = user,
                            unreadMessages = 0,
                            lastReceivedEventDate = createdAt,
                        )
                    } else {
                        read
                    }
                }
                thread.copy(
                    activeParticipantCount = threadInfo.activeParticipantCount,
                    deletedAt = threadInfo.deletedAt,
                    lastMessageAt = threadInfo.lastMessageAt ?: thread.lastMessageAt,
                    parentMessage = threadInfo.parentMessage ?: thread.parentMessage,
                    participantCount = threadInfo.participantCount,
                    replyCount = threadInfo.replyCount,
                    title = threadInfo.title,
                    updatedAt = threadInfo.updatedAt,
                    read = updatedRead,
                )
            } else {
                thread
            }
        }
        setThreads(updatedThreads)
    }

    /**
     * Marks the given thread as read by the given user.
     *
     * @param threadId The ID of the message which was marked as unread. (to be found in the thread)
     * @param user The [User] for which the thread should be marked as unread.
     */
    internal fun markThreadAsUnreadByUser(threadId: String, user: User, createdAt: Date) {
        val thread = mutableState.threadMap[threadId] ?: return
        val updatedRead = thread.read.map { read ->
            if (read.user.id == user.id) {
                read.copy(
                    user = user,
                    // Update this value to what the backend returns (when implemented)
                    unreadMessages = read.unreadMessages + 1,
                    lastReceivedEventDate = createdAt,
                )
            } else {
                read
            }
        }
        val updatedThread = thread.copy(read = updatedRead)
        mutableState.upsertThreads(listOf(updatedThread))
    }

    private fun upsertReplyInThread(thread: Thread, reply: Message): Thread {
        val newReplies = upsertMessageInList(reply, thread.latestReplies)
        val isInsert = newReplies.size > thread.latestReplies.size
        val sortedNewReplies = newReplies.sortedBy {
            it.createdAt ?: it.createdLocallyAt
        }
        val replyCount = if (isInsert) {
            thread.replyCount + 1
        } else {
            thread.replyCount
        }
        val lastMessageAt = sortedNewReplies.lastOrNull()?.let { latestReply ->
            latestReply.createdAt ?: latestReply.createdLocallyAt
        }
        // The new message could be from a new thread participant
        val threadParticipants = if (isInsert) {
            upsertThreadParticipantInList(
                newParticipant = ThreadParticipant(user = reply.user, userId = reply.user.id),
                participants = thread.threadParticipants,
            )
        } else {
            thread.threadParticipants
        }
        val participantCount = threadParticipants.size
        // Update read counts (+1 for each non-sender of the message)
        val read = if (isInsert) {
            updateReadCounts(thread.read, reply)
        } else {
            thread.read
        }
        return thread.copy(
            replyCount = replyCount,
            lastMessageAt = lastMessageAt ?: thread.lastMessageAt,
            updatedAt = lastMessageAt ?: thread.updatedAt,
            participantCount = participantCount,
            threadParticipants = threadParticipants,
            latestReplies = sortedNewReplies,
            read = read,
        )
    }

    private fun upsertMessageInList(newMessage: Message, messages: List<Message>): List<Message> {
        // Insert
        if (messages.none { it.id == newMessage.id }) {
            return messages + listOf(newMessage)
        }
        // Update
        return messages.map { message ->
            if (message.id == newMessage.id) {
                newMessage
            } else {
                message
            }
        }
    }

    private fun upsertThreadParticipantInList(
        newParticipant: ThreadParticipant,
        participants: List<ThreadParticipant>,
    ): List<ThreadParticipant> {
        // Insert
        if (participants.none { it.userId == newParticipant.userId }) {
            return participants + listOf(newParticipant)
        }
        // Update
        return participants.map { participant ->
            if (participant.userId == newParticipant.userId) {
                newParticipant
            } else {
                participant
            }
        }
    }

    private fun updateReadCounts(read: List<ChannelUserRead>, reply: Message): List<ChannelUserRead> {
        return read.map { userRead ->
            if (userRead.user.id != reply.user.id) {
                userRead.copy(unreadMessages = userRead.unreadMessages + 1)
            } else {
                userRead
            }
        }
    }
}
