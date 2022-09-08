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

package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.onSuccessSuspend
import io.getstream.chat.android.offline.plugin.logic.channel.thread.internal.ThreadLogic
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.logging.StreamLog

/**
 * ThreadQueryListenerFull handles both state and database. It uses, if available, the database
 * to update, if available, the state.
 *
 * @param logic [LogicRegistry] Optional class to handle state updates
 * @param messageRepository [MessageRepository] Optional to handle database updates related to messages
 * @param userRepository [UserRepository]  Optional to handle database updates related to user
 * @param getRemoteMessage Returns a remote message from backend side.
 */
internal class ThreadQueryListenerFull(
    private val logic: LogicRegistry?,
    private val messageRepository: MessageRepository?,
    private val userRepository: UserRepository?,
    private val getRemoteMessage: suspend (messageId: String) -> Result<Message>
) : ThreadQueryListener {

    private val logger = StreamLog.getLogger("Chat:ThreadQueryListener")

    override suspend fun onGetRepliesPrecondition(messageId: String, limit: Int): Result<Unit> {
        val loadingMoreMessage = logic?.thread(messageId)?.isLoadingMessages()

        return if (loadingMoreMessage == true) {
            val errorMsg = "already loading messages for this thread, ignoring the load requests."
            logger.i { errorMsg }
            Result(ChatError(errorMsg))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun onGetRepliesRequest(messageId: String, limit: Int) {
        val threadLogic = logic?.thread(messageId)

        threadLogic?.setLoading(true)
        val messages = messageRepository?.selectMessagesForThread(messageId, limit)
        val parentMessage = threadLogic?.getMessage(messageId) ?: messages?.firstOrNull { it.id == messageId }

        if (parentMessage != null && messages?.isNotEmpty() == true) {
            threadLogic?.upsertMessages(messages)
        } else {
            val result = getRemoteMessage(messageId)
            if (result.isSuccess) {
                val message = result.data()
                threadLogic?.upsertMessage(result.data())
                userRepository?.insertUsers(message.users())
                messageRepository?.insertMessage(message)
            }
        }
    }

    override suspend fun onGetRepliesResult(result: Result<List<Message>>, messageId: String, limit: Int) {
        val threadLogic = logic?.thread(messageId)
        threadLogic?.setLoading(false)
        onResult(threadLogic, result, limit)
    }

    override suspend fun onGetRepliesMorePrecondition(messageId: String, firstId: String, limit: Int): Result<Unit> {
        val loadingMoreMessage = logic?.thread(messageId)?.isLoadingOlderMessages()

        return if (loadingMoreMessage == true) {
            val errorMsg = "already loading messages for this thread, ignoring the load more requests."
            logger.i { errorMsg }
            Result(ChatError(errorMsg))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun onGetRepliesMoreRequest(messageId: String, firstId: String, limit: Int) {
        logic?.thread(messageId)?.setLoadingOlderMessages(true)
    }

    override suspend fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        messageId: String,
        firstId: String,
        limit: Int,
    ) {
        val threadLogic = logic?.thread(messageId)

        threadLogic?.setLoadingOlderMessages(false)
        onResult(threadLogic, result, limit)
    }

    private suspend fun onResult(threadLogic: ThreadLogic?, result: Result<List<Message>>, limit: Int) {
        if (result.isSuccess) {
            val newMessages = result.data()
            threadLogic?.run {
                upsertMessages(newMessages)
                setEndOfOlderMessages(newMessages.size < limit)
                updateOldestMessageInThread(newMessages)
            }
        }

        result.onSuccessSuspend { messages ->
            userRepository?.insertUsers(messages.flatMap(Message::users))
            messageRepository?.insertMessages(messages)
        }
    }
}
