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
import io.getstream.chat.android.client.experimental.plugin.listeners.EditMessageListener
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import java.util.Date

internal class EditMessageListenerImpl(
    private val logic: LogicRegistry,
    private val globalState: GlobalState,
) : EditMessageListener {

    /**
     * Method called when a message edit request happens. This method should be used to update messages locally and
     * update the cache.
     *
     * @param message [Message].
     */
    override suspend fun onMessageEditRequest(message: Message) {
        val (channelType, channelId) = message.cid.cidToTypeAndId()
        val channelLogic = logic.channel(channelType, channelId)

        val isOnline = globalState.isOnline()
        val messagesToEdit = message.updateMessageOnlineState(isOnline).let(::listOf)

        channelLogic.updateAndSaveMessages(messagesToEdit)
    }

    /**
     * Method called when an edition in a message returns from the API.
     *
     * @param result the result of the API call.
     */
    override suspend fun onMessageEditResult(originalMessage: Message, result: Result<Message>) {
        if (result.isSuccess) {
            val message = result.data()
            val channelLogic = channelLogicForMessage(message)
            val messages = message.copy(syncStatus = SyncStatus.COMPLETED).let(::listOf)

            channelLogic.updateAndSaveMessages(messages)
        } else {
            val channelLogic = channelLogicForMessage(originalMessage)
            val failedMessage = originalMessage.updateFailedMessage(result.error()).let(::listOf)

            channelLogic.updateAndSaveMessages(failedMessage)
        }
    }

    /**
     * Gets the channel logic for the channel of a message.
     *
     * @param message [Message].
     */
    private fun channelLogicForMessage(message: Message): ChannelLogic {
        val (channelType, channelId) = message.cid.cidToTypeAndId()
        return logic.channel(channelType, channelId)
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
