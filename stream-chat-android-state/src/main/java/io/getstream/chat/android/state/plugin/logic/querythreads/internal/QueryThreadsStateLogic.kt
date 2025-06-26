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

import io.getstream.chat.android.client.extensions.internal.markAsReadByUser
import io.getstream.chat.android.client.extensions.internal.markAsUnreadByUser
import io.getstream.chat.android.client.extensions.internal.updateParent
import io.getstream.chat.android.client.extensions.internal.upsertReply
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadInfo
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.state.plugin.state.querythreads.internal.QueryThreadsMutableState
import java.util.Date

/**
 * Logic for managing the state of the threads list.
 *
 * @param mutableState Reference to the global [QueryThreadsMutableState].
 */
@Suppress("TooManyFunctions")
internal class QueryThreadsStateLogic(
    private val mutableState: QueryThreadsMutableState,
    private val mutableGlobalState: MutableGlobalState,
) {

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
    internal fun setThreads(threads: List<Thread>) {
        upsertDraftMessages(threads)
        mutableState.setThreads(threads)
    }

    /**
     * Inserts all [Thread]s that aren't already loaded into the [mutableState].
     *
     * @param threads The batch of [Thread]s to insert (if they don't already exist).
     */
    internal fun insertThreadsIfAbsent(threads: List<Thread>) {
        upsertDraftMessages(threads)
        mutableState.insertThreadsIfAbsent(threads)
    }

    /**
     * Upsert a list of threads in the [mutableState].
     *
     * @param threads The threads to upsert.
     */
    internal fun upsertThreads(threads: List<Thread>) {
        upsertDraftMessages(threads)
        mutableState.upsertThreads(threads)
    }

    private fun upsertDraftMessages(threads: List<Thread>) {
        threads.forEach { thread ->
            thread.draft?.let { draft ->
                mutableGlobalState.updateDraftMessage(draft)
            }
        }
    }

    /**
     * Clears all [Thread]s in the [mutableState].
     */
    internal fun clearThreads() =
        mutableState.clearThreads()

    /**
     * Updates the identifier for the next page of threads in the [mutableState].
     *
     * @param next The next page identifier.
     */
    internal fun setNext(next: String?) =
        mutableState.setNext(next)

    /**
     * Retrieves the current unseen thread IDs from the [mutableState].
     */
    internal fun getUnseenThreadIds() = mutableState.unseenThreadIds.value

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
        val thread = mutableState.threadMap[parent.id] ?: return false
        val updatedThread = thread.updateParent(parent)
        mutableState.upsertThreads(listOf(updatedThread))
        return true
    }

    /**
     * Inserts/updates the given reply into the appropriate thread.
     *
     * @param reply The reply to upsert.
     */
    internal fun upsertReply(reply: Message) {
        val thread = reply.parentId?.let { threadId -> mutableState.threadMap[threadId] } ?: return
        val updatedThread = thread.upsertReply(reply)
        mutableState.upsertThreads(listOf(updatedThread))
    }

    /**
     * Marks the given thread as read by the given user.
     *
     * @param threadInfo The [ThreadInfo] holding info about the [Thread] which should be marked as read.
     * @param user The [User] for which the thread should be marked as read.
     * @param createdAt The [Date] of the 'mark read' event.
     */
    internal fun markThreadAsReadByUser(threadInfo: ThreadInfo, user: User, createdAt: Date) {
        val thread = mutableState.threadMap[threadInfo.parentMessageId] ?: return
        val updatedThread = thread.markAsReadByUser(threadInfo, user, createdAt)
        mutableState.upsertThreads(listOf(updatedThread))
    }

    /**
     * Marks the given thread as read by the given user.
     *
     * @param threadId The ID of the message which was marked as unread. (to be found in the thread)
     * @param user The [User] for which the thread should be marked as unread.
     */
    internal fun markThreadAsUnreadByUser(threadId: String, user: User, createdAt: Date) {
        val thread = mutableState.threadMap[threadId] ?: return
        val updatedThread = thread.markAsUnreadByUser(user, createdAt)
        mutableState.upsertThreads(listOf(updatedThread))
    }
}
