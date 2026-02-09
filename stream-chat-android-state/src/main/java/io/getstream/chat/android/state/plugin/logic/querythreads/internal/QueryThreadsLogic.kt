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

import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMarkUnreadEvent
import io.getstream.chat.android.client.events.NotificationThreadMessageNewEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.ThreadUpdatedEvent
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.QueryThreadsResult
import io.getstream.chat.android.models.Thread
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result

/**
 * Logic class for "Query Threads" operations.
 *
 * @param stateLogic The [QueryThreadsStateLogic] managing the global state of the threads list.
 * @param databaseLogic The [QueryThreadsDatabaseLogic] retrieving the (optional) offline database data.
 */
internal class QueryThreadsLogic(
    private val stateLogic: QueryThreadsStateLogic,
    private val databaseLogic: QueryThreadsDatabaseLogic,
) {

    private val logger by taggedLogger("Chat:QueryThreadsLogic")

    // Keeps track of the threads that we loaded from back-end in the current session
    private var threadIdsInSession: MutableList<String> = mutableListOf()

    /**
     * Run precondition for the request. If it returns [Result.Success] then the request is run otherwise it returns
     * [Result.Failure] and no request is made.
     *
     * @param request [QueryThreadsRequest] which is going to be used for the request.
     *
     * @return [Result.Success] if precondition passes otherwise [Result.Failure]
     */
    internal fun onQueryThreadsPrecondition(request: QueryThreadsRequest): Result<Unit> {
        if (stateLogic.isLoading()) {
            val errorMsg = "Already loading the threads, ignoring all other load requests."
            logger.d { errorMsg }
            return Result.Failure(Error.GenericError(errorMsg))
        }
        return if (stateLogic.isLoadingMore() && request.isNextPageRequest()) {
            val errorMsg = "Already loading the next page of threads, ignoring all other next page requests."
            logger.d { errorMsg }
            Result.Failure(Error.GenericError(errorMsg))
        } else {
            Result.Success(Unit)
        }
    }

    /**
     * Handles the actions that are needed to update the threads state before the attempt to load the threads
     * from the network.
     *
     * @param request The [QueryThreadsRequest] used to fetch the threads.
     */
    internal suspend fun onQueryThreadsRequest(request: QueryThreadsRequest) {
        val isNextPageRequest = request.isNextPageRequest()
        if (isNextPageRequest) {
            stateLogic.setLoadingMore(true)
        } else {
            stateLogic.setLoading(true)
        }
        if (isForceReload(request)) {
            stateLogic.clearThreads()
            stateLogic.clearUnseenThreadIds()
        } else if (!isNextPageRequest) {
            queryThreadsOffline(request)
        }
    }

    /**
     * Handles the actions that are needed to update the threads state after the loading of the threads from network was
     * completed.
     *
     * @param result The [Result] holding the [QueryThreadsResult] if the operation was successful, or an [Error] if the
     * operation failed.
     * @param request The [QueryThreadsRequest] used to fetch the threads.
     */
    internal suspend fun onQueryThreadsResult(result: Result<QueryThreadsResult>, request: QueryThreadsRequest) {
        stateLogic.setLoadingMore(false)
        stateLogic.setLoading(false)
        when (result) {
            is Result.Success -> {
                if (request.isNextPageRequest()) {
                    stateLogic.upsertThreads(result.value.threads)
                    threadIdsInSession += result.value.threads.map(Thread::parentMessageId)
                } else {
                    stateLogic.setThreads(result.value.threads)
                    stateLogic.clearUnseenThreadIds()
                    threadIdsInSession = result.value.threads.map(Thread::parentMessageId).toMutableList()
                }
                stateLogic.setNext(result.value.next)
                // Update local threads order
                databaseLogic.setLocalThreadsOrder(request.filter, request.sort, threadIdsInSession)
            }

            is Result.Failure -> {
                logger.i { "[queryThreadsResult] with request: $request failed." }
            }
        }
    }

    /**
     * Handles the given [List] of [ChatEvent]s by updating the threads state.
     *
     * @param events The [List] of [ChatEvent]s to handle.
     */
    internal fun handleEvents(events: List<ChatEvent>) = events.forEach(::handleEvent)

    /**
     * Retrieves a [Message] by its ID if it is stored in the Threads state.
     */
    internal fun getMessage(messageId: String): Message? =
        stateLogic.getMessage(messageId)

    /**
     * Upsert the given [Message] in a [Thread] if such exists.
     */
    internal fun upsertMessage(message: Message) = updateParentOrReply(message)

    /**
     * Upsert the given [Message] from a [Thread] if such exists.
     */
    internal fun deleteMessage(message: Message) =
        stateLogic.deleteMessage(message)

    private fun handleEvent(event: ChatEvent) {
        when (event) {
            // Destructive operation - remove the threads completely from the list
            is NotificationChannelDeletedEvent -> deleteThreadsFromChannel(event.cid)
            // Informs about a new thread (loaded, not loaded, or newly created thread)
            is NotificationThreadMessageNewEvent -> onNewThreadMessageNotification(event)
            // (Potentially) Informs about marking a thread as unread
            is NotificationMarkUnreadEvent -> markThreadAsUnread(event)
            // (Potentially) Informs about reading of a thread
            is MessageReadEvent -> markThreadAsRead(event)
            is NotificationMarkReadEvent -> markThreadAsRead(event)
            // Updates the thread when it is partially updated via the API (e.g. title or custom data changes)
            is ThreadUpdatedEvent -> stateLogic.updateThreadFromEvent(event.thread)
            // (Potentially) Updates/Inserts a message in a thread
            is NewMessageEvent -> updateParentOrReply(event.message)
            is MessageUpdatedEvent -> updateParentOrReply(event.message)
            is MessageDeletedEvent -> updateParentOrReply(event.message)
            is ReactionNewEvent -> updateParentOrReply(event.message)
            is ReactionUpdateEvent -> updateParentOrReply(event.message)
            is ReactionDeletedEvent -> updateParentOrReply(event.message)
            else -> Unit
        }
    }

    private fun QueryThreadsRequest.isNextPageRequest() = this.next != null

    private fun isForceReload(request: QueryThreadsRequest) =
        !request.isNextPageRequest() && stateLogic.getUnseenThreadIds().isNotEmpty()

    private fun onNewThreadMessageNotification(event: NotificationThreadMessageNewEvent) {
        val newMessageThreadId = event.message.parentId ?: return
        // Update the unseenThreadIsd if the relevant thread is not loaded (yet)
        val threads = stateLogic.getThreads()
        if (threads.none { it.parentMessageId == newMessageThreadId }) {
            stateLogic.addUnseenThreadId(newMessageThreadId)
        }
        // If the thread is loaded, it will be updated by message.new + message.updated events
    }

    /**
     * Updates the thread in which the message has been updated.
     *
     * @param message The updated [Message].
     */
    private fun updateParentOrReply(message: Message) {
        val parentUpdated = stateLogic.updateParent(parent = message)
        if (!parentUpdated) {
            stateLogic.upsertReply(reply = message)
        }
    }

    /**
     * Marks a given thread as read by a user, if the [MessageReadEvent] is delivered for a thread.
     *
     * @param event The [MessageReadEvent] informing about the read state change.
     */
    private fun markThreadAsRead(event: MessageReadEvent) {
        val threadInfo = event.thread ?: return
        stateLogic.markThreadAsReadByUser(
            threadInfo = threadInfo,
            user = event.user,
            createdAt = event.createdAt,
        )
    }

    private fun markThreadAsRead(event: NotificationMarkReadEvent) {
        val threadInfo = event.thread ?: return
        stateLogic.markThreadAsReadByUser(
            threadInfo = threadInfo,
            user = event.user,
            createdAt = event.createdAt,
        )
    }

    /**
     * Marks a given thread as unread by a user, if the [NotificationMarkUnreadEvent] is delivered for a thread.
     *
     * @param event The [NotificationMarkUnreadEvent] informing about the read state change.
     */
    private fun markThreadAsUnread(event: NotificationMarkUnreadEvent) {
        val threadId = event.threadId ?: return
        stateLogic.markThreadAsUnreadByUser(threadId, event.user, event.createdAt)
    }

    /**
     * Deletes all threads associated with the channel with [cid].
     * Use when the channel was deleted.
     */
    private fun deleteThreadsFromChannel(cid: String) {
        val threads = stateLogic.getThreads()
        val filteredThreads = threads.filterNot { it.cid == cid }
        stateLogic.setThreads(filteredThreads)
    }

    private suspend fun queryThreadsOffline(request: QueryThreadsRequest) {
        val filter = request.filter
        val sort = request.sort
        val localThreadsOrder = databaseLogic.getLocalThreadsOrder(filter, sort).take(request.limit)
        if (localThreadsOrder.isEmpty()) return
        val localThreads = databaseLogic.getLocalThreads(localThreadsOrder)
        if (localThreads.isEmpty()) return
        stateLogic.insertThreadsIfAbsent(localThreads)
    }
}
