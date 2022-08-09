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
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.plugin.listeners.EditMessageListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.internal.toMessageSyncDescription
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import java.util.Date

internal class EditMessageListenerImpl(
    private val logic: LogicRegistry,
    private val clientState: ClientState,
) : EditMessageListener {

    /**
     * Method called when a message edit request happens. This method should be used to update messages locally and
     * update the cache.
     *
     * @param message [Message].
     */
    override suspend fun onMessageEditRequest(message: Message) {
        val isOnline = clientState.isOnline
        val messagesToEdit = message.updateMessageOnlineState(isOnline).let(::listOf)

        logic.channelFromMessage(message)?.updateAndSaveMessages(messagesToEdit)
        logic.threadFromMessage(message)?.updateAndSaveMessages(messagesToEdit)
    }

    /**
     * Method called when an edition in a message returns from the API.
     *
     * @param result the result of the API call.
     */
    override suspend fun onMessageEditResult(originalMessage: Message, result: Result<Message>) {
        if (result.isSuccess) {
            val message = result.data()
            val messages = message.copy(syncStatus = SyncStatus.COMPLETED).let(::listOf)

            logic.channelFromMessage(message)?.updateAndSaveMessages(messages)
            logic.threadFromMessage(message)?.updateAndSaveMessages(messages)
        } else {
            val failedMessage = originalMessage.updateFailedMessage(result.error()).let(::listOf)

            logic.channelFromMessage(originalMessage)?.updateAndSaveMessages(failedMessage)
            logic.threadFromMessage(originalMessage)?.updateAndSaveMessages(failedMessage)
        }
    }

    /**
     * Updates a message that whose request (Edition/Delete/Reaction update...) has failed.
     *
     * @param chatError [ChatError].
     */
    private fun Message.updateFailedMessage(chatError: ChatError): Message {
        return this.copy(
            syncStatus = if (chatError.isPermanent()) {
                SyncStatus.FAILED_PERMANENTLY
            } else {
                SyncStatus.SYNC_NEEDED
            },
            syncDescription = chatError.toMessageSyncDescription(),
            updatedLocallyAt = Date(),
        )
    }

    /**
     * Update the online state of a message.
     *
     * @param isOnline [Boolean].
     */
    private fun Message.updateMessageOnlineState(isOnline: Boolean): Message {
        return this.copy(
            syncStatus = if (isOnline) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED,
            updatedLocallyAt = Date()
        )
    }
}
