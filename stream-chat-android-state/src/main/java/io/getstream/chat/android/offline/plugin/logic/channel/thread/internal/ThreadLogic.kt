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

package io.getstream.chat.android.offline.plugin.logic.channel.thread.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.HasMessage
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.onSuccessSuspend
import io.getstream.chat.android.offline.plugin.state.channel.thread.internal.ThreadMutableState
import io.getstream.logging.StreamLog

/** Logic class for thread state management. Implements [ThreadQueryListener] as listener for LLC requests. */
internal class ThreadLogic(
    private val repos: RepositoryFacade,
    private val client: ChatClient,
    private val threadStateLogic: ThreadStateLogic,
) : ThreadQueryListener {

    private val mutableState: ThreadMutableState = threadStateLogic.writeThreadState()
    private val logger = StreamLog.getLogger("Chat:ThreadLogic")

    /** Runs side effect when a result is obtained. */
    private suspend fun onResult(result: Result<List<Message>>, limit: Int) {
        if (result.isSuccess) {
            val newMessages = result.data()
            upsertMessages(newMessages)
            mutableState.setEndOfOlderMessages(newMessages.size < limit)
            mutableState.setOldestInThread(
                newMessages.sortedBy { it.createdAt }
                    .firstOrNull()
                    ?: mutableState.oldestInThread.value
            )
        }
        result.onSuccessSuspend {
            repos.insertMessages(it)
        }
    }

    override suspend fun onGetRepliesPrecondition(messageId: String, limit: Int): Result<Unit> {
        return if (mutableState.loading.value) {
            val errorMsg = "already loading messages for this thread, ignoring the load requests."
            logger.i { errorMsg }
            Result(ChatError(errorMsg))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun onGetRepliesRequest(messageId: String, limit: Int) {
        mutableState.setLoading(true)
        val messages = repos.selectMessagesForThread(messageId, limit)
        val parentMessage = messages.firstOrNull { it.id == messageId }
        if (parentMessage != null) {
            upsertMessages(messages)
            Result.success(Unit)
        } else {
            val result = client.getMessage(messageId).await()
            if (result.isSuccess) {
                upsertMessage(result.data())
                repos.insertMessage(result.data())
                Result.success(Unit)
            } else {
                Result(result.error())
            }
        }
    }

    override suspend fun onGetRepliesResult(result: Result<List<Message>>, messageId: String, limit: Int) {
        mutableState.setLoading(false)
        onResult(result, limit)
    }

    override suspend fun onGetRepliesMorePrecondition(messageId: String, firstId: String, limit: Int): Result<Unit> {
        return if (mutableState.loadingOlderMessages.value) {
            val errorMsg = "already loading messages for this thread, ignoring the load more requests."
            logger.i { errorMsg }
            Result(ChatError(errorMsg))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun onGetRepliesMoreRequest(
        messageId: String,
        firstId: String,
        limit: Int
    ) {
        mutableState.setLoadingOlderMessages(true)
    }

    override suspend fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        messageId: String,
        firstId: String,
        limit: Int
    ) {
        mutableState.setLoadingOlderMessages(false)
        onResult(result, limit)
    }

    internal fun deleteMessage(message: Message) {
        threadStateLogic.deleteMessage(message)
    }

    /**
     * Updates the messages locally and saves it at database.
     *
     * @param messages The list of messages to be updated in the SDK and to be saved in database.
     */
    internal suspend fun updateAndSaveMessages(messages: List<Message>) {
        threadStateLogic.upsertMessages(messages)
        storeMessageLocally(messages)
    }

    /**
     * Store the messages in the local cache.
     *
     * @param messages The messages to be stored. Check [Message].
     */
    private suspend fun storeMessageLocally(messages: List<Message>) {
        repos.insertMessages(messages)
    }

    internal fun upsertMessage(message: Message) = upsertMessages(listOf(message))

    internal fun upsertMessages(messages: List<Message>) = threadStateLogic.upsertMessages(messages)

    internal fun removeLocalMessage(message: Message) {
        threadStateLogic.removeLocalMessage(message)
    }

    internal fun handleEvents(events: List<HasMessage>) {
        for (event in events) {
            handleEvent(event)
        }
    }

    private fun handleEvent(event: HasMessage) {
        when (event) {
            is MessageUpdatedEvent -> {
                event.message.apply {
                    replyTo = mutableState.messages.value.firstOrNull { it.id == replyMessageId }
                }.let(::upsertMessage)
            }
            is NewMessageEvent,
            is MessageDeletedEvent,
            is NotificationMessageNewEvent,
            is ReactionNewEvent,
            is ReactionUpdateEvent,
            is ReactionDeletedEvent -> {
                upsertMessage(event.message)
            }
            else -> Unit
        }
    }
}
