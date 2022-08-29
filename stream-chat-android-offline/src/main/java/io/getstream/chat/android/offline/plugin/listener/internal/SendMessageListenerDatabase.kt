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
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.internal.toMessageSyncDescription
import io.getstream.logging.StreamLog
import java.util.Date

private const val TAG = "Chat:SendMessageHandlerDB"

/**
 * Implementation of [SendMessageListener] that deals with updates of the database of the SDK.
 */
internal class SendMessageListenerDatabase(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository,
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

        if (result.isSuccess) {
            handleSendMessageSuccess(cid, result.data())
        } else {
            handleSendMessageFail(message, result.error())
        }
    }

    private suspend fun handleSendMessageSuccess(
        cid: String,
        processedMessage: Message,
    ) {
        processedMessage.enrichWithCid(cid)
            .copy(
                syncStatus = SyncStatus.COMPLETED,
                syncDescription = null
            )
            .also { message ->
                userRepository.insertUsers(message.users())
                messageRepository.insertMessage(message, cache = false)
            }
    }

    private suspend fun handleSendMessageFail(
        message: Message,
        error: ChatError,
    ) {
        val isPermanentError = error.isPermanent()
        val isMessageModerationFailed = error is ChatNetworkError &&
            error.streamCode == ChatErrorCode.MESSAGE_MODERATION_FAILED.code

        StreamLog.w(TAG) {
            "[handleSendMessageFail] isPermanentError: $isPermanentError" +
                ", isMessageModerationFailed: $isMessageModerationFailed"
        }

        message.copy(
            syncStatus = if (isPermanentError) {
                SyncStatus.FAILED_PERMANENTLY
            } else {
                SyncStatus.SYNC_NEEDED
            },
            syncDescription = error.toMessageSyncDescription(),
            updatedLocallyAt = Date(),
        ).also { parsedMessage ->
            userRepository.insertUsers(parsedMessage.users())
            messageRepository.insertMessage(parsedMessage, cache = false)
        }
    }
}
