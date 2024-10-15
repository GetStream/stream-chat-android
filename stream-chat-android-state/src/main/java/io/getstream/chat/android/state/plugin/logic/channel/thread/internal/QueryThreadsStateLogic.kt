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

package io.getstream.chat.android.state.plugin.logic.channel.thread.internal

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.state.plugin.state.querythreads.internal.QueryThreadsMutableState

/**
 * Logic for managing the state of the threads list.
 *
 * @param mutableState Reference to the global [QueryThreadsMutableState].
 */
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
     * Appends the new page of [threads] to the current thread list.
     *
     * @param threads The new page of threads.
     */
    internal fun appendThreads(threads: List<Thread>) =
        mutableState.appendThreads(threads)

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
     * Updates the parent message of a thread.
     *
     * @param parent The new state of the thread parent message.
     * @return true if matching parent message was found and was updated, false otherwise.
     */
    internal fun updateParent(parent: Message): Boolean {
        val oldThreads = getThreads()
        val oldThread = oldThreads.find { it.parentMessageId == parent.id }
        oldThread ?: return false  // no matching parent message was found
        val newThread = oldThread.copy(
            parentMessage = parent,
            deletedAt = parent.deletedAt,
            updatedAt = parent.updatedAt,
            replyCount = parent.replyCount
        )
        val newThreads = oldThreads.map {
            if (it.parentMessageId == newThread.parentMessageId) {
                newThread
            } else {
                it
            }
        }
        mutableState.setThreads(newThreads)
        return true  // matching parent message was found and updated
    }

    /**
     * Inserts/updates the given reply into the appropriate thread.
     *
     * @param reply The reply to upsert.
     */
    internal fun upsertReply(reply: Message) {
        val oldThreads = getThreads()
        val thread = oldThreads.find { it.parentMessageId == reply.parentId }
        thread ?: return
        val oldReplies = thread.latestReplies
        val newReplies = if (oldReplies.any { it.id == reply.id }) {
            // update
            oldReplies.map {
                if (it.id == reply.id) {
                    reply
                } else {
                    it
                }
            }
        } else {
            // insert
            oldReplies + listOf(reply)
        }
        val sortedNewReplies = newReplies.sortedBy {
            it.createdAt ?: it.createdLocallyAt
        }
        val newThread = thread.copy(latestReplies = sortedNewReplies)
        val newThreads = oldThreads.map {
            if (it.parentMessageId == newThread.parentMessageId) {
                newThread
            } else {
                it
            }
        }
        mutableState.setThreads(newThreads)
    }
}
