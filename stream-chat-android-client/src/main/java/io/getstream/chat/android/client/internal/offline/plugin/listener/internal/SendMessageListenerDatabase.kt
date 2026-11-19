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

package io.getstream.chat.android.client.internal.offline.plugin.listener.internal

import io.getstream.chat.android.client.errors.isDuplicateMessageError
import io.getstream.chat.android.client.errors.sendFailureSyncStatus
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.log.StreamLog
import io.getstream.result.Error
import io.getstream.result.Result
import java.util.Date

private const val TAG = "Chat:SendMessageHandlerDB"

/**
 * Implementation of [SendMessageListener] that deals with updates of the database of the SDK.
 */
internal class SendMessageListenerDatabase(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository,
    private val clientState: ClientState,
) : SendMessageListener {

    /**
     * Side effect to be invoked when the original request is completed with a response. This method updates the
     * database of the SDK.
     *
     * @param result [Result] response from the original request.
     * @param channelType The type of the channel in which message is sent.
     * @param channelId The id of the the channel in which message is sent.
     * @param message [Message] to be sent.
     */
    override suspend fun onMessageSendResult(
        result: Result<Message>,
        channelType: String,
        channelId: String,
        message: Message,
    ) {
        val cid = "$channelType:$channelId"
        if (messageRepository.selectMessage(message.id)?.syncStatus == SyncStatus.COMPLETED) return

        when (result) {
            is Result.Success -> handleSendMessageSuccess(cid, result.value)
            is Result.Failure -> if (result.value.isDuplicateMessageError()) {
                // The message was delivered by an earlier attempt whose response was lost. Its reply never
                // arrived, so the local date stands in until the next channel query brings the server copy.
                handleSendMessageSuccess(cid, message.copy(createdAt = message.createdAt ?: message.createdLocallyAt))
            } else {
                handleSendMessageFailure(message, result.value)
            }
        }
    }

    private suspend fun handleSendMessageSuccess(
        cid: String,
        processedMessage: Message,
    ) {
        processedMessage.enrichWithCid(cid)
            .copy(syncStatus = SyncStatus.COMPLETED)
            .also { message ->
                userRepository.insertUsers(message.users())
                messageRepository.insertMessage(message)
            }
    }

    private suspend fun handleSendMessageFailure(
        message: Message,
        error: Error,
    ) {
        val syncStatus = error.sendFailureSyncStatus(clientState)
        StreamLog.w(TAG) { "[handleSendMessageFailure] syncStatus: $syncStatus" }

        message.copy(
            syncStatus = syncStatus,
            updatedLocallyAt = Date(),
        ).also { parsedMessage ->
            userRepository.insertUsers(parsedMessage.users())
            messageRepository.insertMessage(parsedMessage)
        }
    }
}
