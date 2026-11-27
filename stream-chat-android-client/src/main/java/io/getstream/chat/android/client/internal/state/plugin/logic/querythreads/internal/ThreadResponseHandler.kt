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

package io.getstream.chat.android.client.internal.state.plugin.logic.querythreads.internal

import io.getstream.chat.android.client.extensions.internal.applyThreadUpdatedEventChanges
import io.getstream.chat.android.client.extensions.internal.upsertReply
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.persistance.repository.write
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadInfo
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CancellationException

/**
 * Stores the threads returned by `getThread` and `partialUpdateThread`, so the loaded thread lists and the offline
 * copy show them at once instead of waiting for the next query or event.
 *
 * A stored or loaded thread with a later `updatedAt` than the response is kept, as the response can arrive after a
 * newer event. This holds for replies too: the server and [upsertReply] both move `updatedAt` to the newest reply.
 * A thread is never added to a loaded list, as it may not match the list's query.
 *
 * The stored thread is read, compared and written in one write of the store, so no event batch is stored in between.
 *
 * A failure here is logged and never changes the result of the request. The loaded lists are updated even when the
 * database write fails.
 */
internal class ThreadResponseHandler(
    private val logic: LogicRegistry,
    private val repos: RepositoryFacade,
) {

    private val logger by taggedLogger("Chat:ThreadResponse")

    /**
     * Stores the [thread] returned by `getThread` and replaces it in the loaded thread lists.
     */
    suspend fun onThreadFetched(thread: Thread) {
        safely("onThreadFetched:state") { logic.getActiveQueryThreadsLogic().forEach { it.replaceThread(thread) } }
        safely("onThreadFetched:db") {
            repos.write {
                val stored = repos.selectThread(thread.parentMessageId)
                if (stored?.updatedAt?.after(thread.updatedAt) == true) {
                    logger.d { "[onThreadFetched] stored ${thread.parentMessageId} is newer than the response" }
                } else {
                    repos.insertThreads(listOf(thread))
                }
            }
        }
    }

    /**
     * Applies the [threadInfo] returned by `partialUpdateThread` to the stored thread and the loaded thread lists.
     */
    suspend fun onThreadPartiallyUpdated(threadInfo: ThreadInfo) {
        safely("onThreadPartiallyUpdated:state") {
            logic.getActiveQueryThreadsLogic().forEach { it.applyThreadInfo(threadInfo) }
        }
        safely("onThreadPartiallyUpdated:db") {
            repos.write {
                // A thread not stored yet is left to the next query: the response has no replies or reads.
                val stored = repos.selectThread(threadInfo.parentMessageId)
                if (stored != null && !stored.updatedAt.after(threadInfo.updatedAt)) {
                    repos.insertThreads(listOf(stored.applyThreadUpdatedEventChanges(threadInfo)))
                }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private inline fun safely(operation: String, block: () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e(e) { "[$operation] failed: ${e.message}" }
        }
    }
}
