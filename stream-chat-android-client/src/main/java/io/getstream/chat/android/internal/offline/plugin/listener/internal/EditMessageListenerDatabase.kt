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
import io.getstream.chat.android.client.extensions.updateFailedMessage
import io.getstream.chat.android.client.extensions.updateMessageOnlineState
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.plugin.listeners.EditMessageListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.result.Result

/**
 * Implementation of [EditMessageListener] that deals with database read and write.
 *
 * @param userRepository [UserRepository]
 * @param messageRepository [MessageRepository]
 * @param clientState [ClientState]
 */
internal class EditMessageListenerDatabase(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository,
    private val clientState: ClientState,
) : EditMessageListener {

    /**
     * Method called when a message edit request happens. This method should be used to update the database.
     *
     * @param message [Message].
     */
    override suspend fun onMessageEditRequest(message: Message) {
        val isOnline = clientState.isNetworkAvailable
        val messagesToEdit = message.updateMessageOnlineState(isOnline)

        saveMessage(messagesToEdit)
    }

    /**
     * Method called when an edition in a message returns from the API. Updates the database accordingly.
     *
     * @param originalMessage [Message].
     * @param result the result of the API call.
     */
    override suspend fun onMessageEditResult(originalMessage: Message, result: Result<Message>) {
        val parsedMessage = when (result) {
            is Result.Success -> result.value.copy(syncStatus = SyncStatus.COMPLETED)
            is Result.Failure -> originalMessage.updateFailedMessage(result.value)
        }

        saveMessage(parsedMessage)
    }

    private suspend fun saveMessage(message: Message) {
        userRepository.insertUsers(message.users())
        messageRepository.insertMessage(message)
    }
}
