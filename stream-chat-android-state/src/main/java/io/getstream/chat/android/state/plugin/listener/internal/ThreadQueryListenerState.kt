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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.state.plugin.logic.channel.thread.internal.ThreadLogic
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result

/**
 * ThreadQueryListenerState handles both state in the SDK. It uses, if available, the database
 * to update the state.
 *
 * @param logic [LogicRegistry] Optional class to handle state updates
 * @param messageRepository [MessageRepository] Optional to handle database updates related to messages
 */
internal class ThreadQueryListenerState(
    private val logic: LogicRegistry,
    private val messageRepository: MessageRepository,
) : ThreadQueryListener {

    private val logger by taggedLogger("Chat:ThreadQueryListener")

    override suspend fun onGetRepliesPrecondition(messageId: String, limit: Int): Result<Unit> {
        val loadingMoreMessage = logic.thread(messageId).isLoadingMessages()

        return if (loadingMoreMessage) {
            val errorMsg = "already loading messages for this thread, ignoring the load requests."
            logger.i { errorMsg }
            Result.Failure(Error.GenericError(errorMsg))
        } else {
            Result.Success(Unit)
        }
    }

    override suspend fun onGetRepliesRequest(messageId: String, limit: Int) {
        val threadLogic = logic.thread(messageId)

        threadLogic.setLoading(true)
        val messages = messageRepository.selectMessagesForThread(messageId, limit).toMutableList()

        // If the parent message is not inside the thread, it is necessary to add it.
        if (threadLogic.getMessage(messageId) == null) {
            messageRepository.selectMessage(messageId)?.let(messages::add)
        }

        if (messages.isNotEmpty()) {
            threadLogic.upsertMessages(messages)
        }
    }

    override suspend fun onGetRepliesResult(result: Result<List<Message>>, messageId: String, limit: Int) {
        val threadLogic = logic.thread(messageId)
        threadLogic.setLoading(false)
        onResult(threadLogic, result, limit)
    }

    override suspend fun onGetRepliesMorePrecondition(messageId: String, firstId: String, limit: Int): Result<Unit> {
        val loadingMoreMessage = logic.thread(messageId).isLoadingOlderMessages()

        return if (loadingMoreMessage) {
            val errorMsg = "already loading messages for this thread, ignoring the load more requests."
            logger.i { errorMsg }
            Result.Failure(Error.GenericError(errorMsg))
        } else {
            Result.Success(Unit)
        }
    }

    override suspend fun onGetRepliesMoreRequest(messageId: String, firstId: String, limit: Int) {
        logic.thread(messageId).setLoadingOlderMessages(true)
    }

    override suspend fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        messageId: String,
        firstId: String,
        limit: Int,
    ) {
        val threadLogic = logic.thread(messageId)

        threadLogic.setLoadingOlderMessages(false)
        onResult(threadLogic, result, limit)
    }

    private fun onResult(threadLogic: ThreadLogic?, result: Result<List<Message>>, limit: Int) {
        if (result is Result.Success) {
            val newMessages = result.value
            threadLogic?.run {
                upsertMessages(newMessages)
                setEndOfOlderMessages(newMessages.size < limit)
                updateOldestMessageInThread(newMessages)
            }
        }
    }
}
