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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationThreadMessageNewEvent
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
                // TODO: What to do in this case??
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
            is MessageUpdatedEvent -> updateMessageInThread(event.message)
            is MessageDeletedEvent -> updateMessageInThread(event.message)
            is NotificationChannelDeletedEvent -> deleteThreadsFromChannel(event.cid)
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
        val participants = thread.threadParticipants.orEmpty()
        val newParticipants = if (participants.any { it.id == event.message.user.id }) {
            participants // User of the new message is already a participant
        } else {
            participants + listOf(event.message.user)
        }
        val participantsCount = newParticipants.size
        val lastMessageAt = event.message.createdAt
        val updatedAt = event.createdAt
        // Append new message at the end of latest replies
        val latestReplies = thread.latestReplies + listOf(event.message)
        val currentUserId = ChatClient.instance().getCurrentOrStoredUserId()
        val read = thread.read?.map {
            if (it.user.id == currentUserId) {
                it.copy(unreadMessages = it.unreadMessages + 1)
            } else {
                it
            }
        }
        val updatedThread = thread.copy(
            replyCount = thread.replyCount?.let { it + 1 },
            participantCount = participantsCount,
            threadParticipants = newParticipants,
            lastMessageAt = lastMessageAt ?: thread.lastMessageAt, // update if possible
            updatedAt = updatedAt,
            latestReplies = latestReplies,
            read = read,
        )
        val updatedThreads = threads.map {
            if (it.parentMessageId == thread.parentMessageId) {
                updatedThread
            } else {
                it
            }
        }
        stateLogic.setThreads(updatedThreads)
    }

    /**
     * Updates the thread in which the message has been updated.
     *
     * @param message The updated [Message].
     */
    private fun updateMessageInThread(message: Message) {
        val parentMessagedUpdated = updateParentMessage(message)
        if (!parentMessagedUpdated) {
            updateReplyMessage(message)
        }
    }

    /**
     * Updates the parent message of the thread (if the updated [message] is a parent message).
     */
    private fun updateParentMessage(message: Message): Boolean {
        val threads = stateLogic.getThreads()
        val affectedThread = threads.find { it.parentMessageId == message.id }
        affectedThread ?: return false // No thread was changed

        val updatedThread = affectedThread.copy(parentMessage = message, parentMessageId = message.id)
        val updatedThreads = threads.map { thread ->
            if (thread.parentMessageId == message.id) {
                updatedThread
            } else {
                thread
            }
        }
        stateLogic.setThreads(updatedThreads)
        return true // thread was changed
    }

    /**
     * Updates a reply message in a thread (if the updated [message] is a reply message).
     */
    private fun updateReplyMessage(message: Message) {
        val threads = stateLogic.getThreads()
        val affectedThread = threads.find { it.parentMessageId == message.parentId }
        affectedThread ?: return

        val updatedReplies = affectedThread.latestReplies.map {
            if (it.id == message.id) {
                message
            } else {
                it
            }
        }
        val updatedThread = affectedThread.copy(latestReplies = updatedReplies)
        val updatedThreads = threads.map {
            if (it.parentMessageId == updatedThread.parentMessageId) {
                updatedThread
            } else {
                it
            }
        }
        stateLogic.setThreads(updatedThreads)
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
