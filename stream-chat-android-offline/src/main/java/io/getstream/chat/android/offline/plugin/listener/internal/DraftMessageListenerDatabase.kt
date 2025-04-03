/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.errors.isPermanent
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.plugin.listeners.DraftMessageListener
import io.getstream.chat.android.models.DraftMessage
import io.getstream.result.Result
import io.getstream.result.onErrorSuspend
import io.getstream.result.onSuccessSuspend

internal class DraftMessageListenerDatabase(
    private val messageRepository: MessageRepository,
) : DraftMessageListener {

    /**
     * Method called when a request to create a draft message in the API happens
     *
     * @param result The result of the create draft message request
     * @param channelType The type of the channel
     * @param channelId The id of the channel
     * @param message The draft message to be created
     */
    override suspend fun onCreateDraftMessageResult(
        result: Result<DraftMessage>,
        channelType: String,
        channelId: String,
        message: DraftMessage,
    ) {
        result
            .onSuccessSuspend { draftMessage -> messageRepository.insertDraftMessage(draftMessage) }
            .onErrorSuspend { error ->
                message.takeUnless { error.isPermanent() }?.let { draftMessage ->
                    messageRepository.insertDraftMessage(draftMessage)
                }
            }
    }

    /**
     * Method called when a request to delete draft messages in the API happens
     *
     * @param result The result of the delete draft messages request
     * @param channelType The type of the channel
     * @param channelId The id of the channel
     * @param message The draft message to be deleted
     */
    override suspend fun onDeleteDraftMessagesResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        message: DraftMessage,
    ) {
        result
            .onSuccessSuspend { messageRepository.deleteDraftMessage(message) }
            .onErrorSuspend { error ->
                message.takeUnless { error.isPermanent() }?.let { draftMessage ->
                    messageRepository.deleteDraftMessage(draftMessage)
                }
            }
    }

    /**
     * Method called when a request to query draft messages in the API happens
     *
     * @param result The result of the query draft messages request
     * @param offset The offset of the query
     * @param limit The limit of the query
     */
    override suspend fun onQueryDraftMessagesResult(
        result: Result<List<DraftMessage>>,
        offset: Int?,
        limit: Int?,
    ) {
        result.onSuccessSuspend { draftMessages ->
            draftMessages.forEach { draftMessage ->
                messageRepository.insertDraftMessage(draftMessage)
            }
        }
    }
}
