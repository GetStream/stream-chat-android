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

import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationThreadMessageNewEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.QueryThreadsResult
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result

/**
 * Logic class for "Query Threads" operations.
 *
 * @param stateLogic The [QueryThreadsStateLogic] managing the global state of the threads list.
 */
internal class QueryThreadsLogic(private val stateLogic: QueryThreadsStateLogic) {

    private val logger by taggedLogger("Chat:QueryThreadsLogic")

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
        return if (stateLogic.isLoadingMore() && isNextPageRequest(request)) {
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
    internal fun onQueryThreadsRequest(request: QueryThreadsRequest) {
        if (isNextPageRequest(request)) {
            stateLogic.setLoadingMore(true)
        } else {
            stateLogic.setLoading(true)
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
    internal fun onQueryThreadsResult(result: Result<QueryThreadsResult>, request: QueryThreadsRequest) {
        val isNextPageRequest = isNextPageRequest(request)
        if (isNextPageRequest) {
            stateLogic.setLoadingMore(false)
        } else {
            stateLogic.setLoading(false)
        }
        when (result) {
            is Result.Success -> {
                if (isNextPageRequest) {
                    stateLogic.appendThreads(result.value.threads)
                } else {
                    stateLogic.setThreads(result.value.threads)
                    stateLogic.clearUnseenThreadIds()
                }
                stateLogic.setNext(result.value.next)
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

    private fun handleEvent(event: ChatEvent) {
        when (event) {
            is NotificationThreadMessageNewEvent -> addNewThreadMessage(event)
            is NotificationChannelDeletedEvent -> deleteThreadsFromChannel(event.cid)
            is MessageDeletedEvent -> updateParentOrReply(event.message)
            is MessageUpdatedEvent -> updateParentOrReply(event.message)
            is ReactionNewEvent -> updateParentOrReply(event.message)
            is ReactionUpdateEvent -> updateParentOrReply(event.message)
            is ReactionDeletedEvent -> updateParentOrReply(event.message)
            else -> Unit
        }
    }

    private fun isNextPageRequest(request: QueryThreadsRequest) = request.next != null

    private fun addNewThreadMessage(event: NotificationThreadMessageNewEvent) {
        val threads = stateLogic.getThreads()
        val thread = threads.find { it.parentMessageId == event.message.parentId }
        if (thread == null) {
            // Thread is not (yet) loaded, just update the state of unseenThreadIds
            event.message.parentId?.let { parentId ->
                stateLogic.addUnseenThreadId(parentId)
            }
            return
        }
        // Update the thread inline if it is already loaded
        stateLogic.upsertReply(reply = event.message)
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
     * Deletes all threads associated with the channel with [cid].
     * Use when the channel was deleted.
     */
    private fun deleteThreadsFromChannel(cid: String) {
        val threads = stateLogic.getThreads()
        val filteredThreads = threads.filterNot { it.cid == cid }
        stateLogic.setThreads(filteredThreads)
    }
}
