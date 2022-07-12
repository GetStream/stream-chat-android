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
import io.getstream.chat.android.client.errors.cause.MessageModerationDeletedException
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.MessageSyncType
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.plugin.listeners.DeleteMessageListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import java.util.Date

/**
 * Listener for requests of message deletion and for message deletion results.
 */
internal class DeleteMessageListenerImpl(
    private val logic: LogicRegistry,
    private val clientState: ClientState,
    private val messageRepository: MessageRepository,
) : DeleteMessageListener {

    /**
     * Checks if message can be safely deleted.
     *
     * @param messageId The message id to be deleted.
     */
    override suspend fun onMessageDeletePrecondition(messageId: String): Result<Unit> {
        return messageRepository.selectMessage(messageId)?.let { message ->

            val isModerationFailed = message.user.id == clientState.user.value?.id &&
                message.syncStatus == SyncStatus.FAILED_PERMANENTLY &&
                message.syncDescription?.type == MessageSyncType.FAILED_MODERATION

            val (channelType, channelId) = message.cid.cidToTypeAndId()
            val channelLogic = logic.channel(channelType, channelId)

            if (isModerationFailed) {
                channelLogic.deleteMessage(message)
                messageRepository.deleteChannelMessage(message)
                Result.error(
                    MessageModerationDeletedException(
                        "Message with failed moderation has been deleted locally: $messageId"
                    )
                )
            } else {
                Result.success(Unit)
            }
        } ?: Result.error(ChatError(message = "No message found with id: $messageId"))
    }

    /**
     * Method called when a request to delete a message in the API happens
     *
     * @param messageId
     */
    override suspend fun onMessageDeleteRequest(messageId: String) {
        messageRepository.selectMessage(messageId)?.let { message ->
            val isOnline = clientState.isOnline

            val (channelType, channelId) = message.cid.cidToTypeAndId()
            val channelLogic = logic.channel(channelType, channelId)

            val messagesToBeDeleted = message.copy(
                deletedAt = Date(),
                syncStatus = if (!isOnline) SyncStatus.SYNC_NEEDED else SyncStatus.IN_PROGRESS
            ).let(::listOf)

            channelLogic.updateAndSaveMessages(messagesToBeDeleted)
        }
    }

    /**
     * Method called when a request for message deletion return. Use it to update database, update messages or to present
     * an error to the user.
     *
     * @param result the result of the API call.
     */
    override suspend fun onMessageDeleteResult(originalMessageId: String, result: Result<Message>) {
        if (result.isSuccess) {
            val deletedMessage = result.data()
            deletedMessage.syncStatus = SyncStatus.COMPLETED

            val (channelType, channelId) = deletedMessage.cid.cidToTypeAndId()
            logic.channel(channelType, channelId)
                .updateAndSaveMessages(deletedMessage.let(::listOf))
        } else {
            messageRepository.selectMessage(originalMessageId)?.let { originalMessage ->
                val failureMessage = originalMessage.copy(
                    syncStatus = SyncStatus.SYNC_NEEDED,
                    updatedLocallyAt = Date(),
                )

                val (channelType, channelId) = failureMessage.cid.cidToTypeAndId()
                logic.channel(channelType, channelId)
                    .updateAndSaveMessages(failureMessage.let(::listOf))
            }
        }
    }
}
