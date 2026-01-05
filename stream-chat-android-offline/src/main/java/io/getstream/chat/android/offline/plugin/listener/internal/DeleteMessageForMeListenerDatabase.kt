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

package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.plugin.listeners.DeleteMessageForMeListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.result.Result

internal class DeleteMessageForMeListenerDatabase(
    private val clientState: ClientState,
    private val messageRepository: MessageRepository,
) : DeleteMessageForMeListener {

    /**
     * Updates the message as deleted for me in the database,
     * setting the appropriate sync status according to network availability.
     */
    override suspend fun onDeleteMessageForMeRequest(messageId: String) {
        messageRepository.selectMessage(messageId)?.let { message ->
            val isNetworkAvailable = clientState.isNetworkAvailable
            val messageToBeUpdated = message.copy(
                deletedForMe = true,
                syncStatus = if (isNetworkAvailable) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED,
            )

            messageRepository.insertMessage(messageToBeUpdated)
        }
    }

    /**
     * Updates the message sync status based on the result of the delete for me operation,
     * handling both success and failure cases.
     */
    override suspend fun onDeleteMessageForMeResult(messageId: String, result: Result<Message>) {
        when (result) {
            is Result.Success -> {
                messageRepository.insertMessage(
                    result.value.copy(syncStatus = SyncStatus.COMPLETED),
                )
            }

            is Result.Failure -> {
                messageRepository.selectMessage(messageId)?.let { message ->
                    val updatedMessage = message.copy(
                        deletedForMe = true,
                        syncStatus = SyncStatus.SYNC_NEEDED,
                    )

                    messageRepository.insertMessage(updatedMessage)
                }
            }
        }
    }
}
