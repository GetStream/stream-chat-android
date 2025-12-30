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

package io.getstream.chat.android.internal.offline.plugin.listener.internal

import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.models.Message
import io.getstream.result.Result

/**
 * ThreadQueryListenerFull handles database read and updates. It updates the database once the requests for backend
 * is complete.
 *
 * @param messageRepository [MessageRepository] Optional to handle database updates related to messages
 * @param userRepository [UserRepository]  Optional to handle database updates related to user
 */
internal class ThreadQueryListenerDatabase(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
) : ThreadQueryListener {

    override suspend fun onGetRepliesRequest(parentId: String, limit: Int) {
        // Nothing to do.
    }

    override suspend fun onGetRepliesResult(result: Result<List<Message>>, parentId: String, limit: Int) {
        onResult(result)
    }

    override suspend fun onGetRepliesMoreRequest(parentId: String, firstId: String, limit: Int) {
        // Nothing to do.
    }

    override suspend fun onGetNewerRepliesRequest(parentId: String, limit: Int, lastId: String?) {
        // Nothing to do.
    }

    override suspend fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        parentId: String,
        firstId: String,
        limit: Int,
    ) {
        onResult(result)
    }

    override suspend fun onGetNewerRepliesResult(
        result: Result<List<Message>>,
        parentId: String,
        limit: Int,
        lastId: String?,
    ) {
        onResult(result)
    }

    private suspend fun onResult(result: Result<List<Message>>) {
        if (result is Result.Success) {
            val messages = result.value

            userRepository.insertUsers(messages.flatMap(Message::users))
            messageRepository.insertMessages(messages)
        }
    }
}
