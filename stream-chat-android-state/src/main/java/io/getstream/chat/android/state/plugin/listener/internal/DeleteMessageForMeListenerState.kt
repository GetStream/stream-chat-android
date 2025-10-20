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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.client.plugin.listeners.DeleteMessageForMeListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.result.Result

internal class DeleteMessageForMeListenerState(
    private val logicRegistry: LogicRegistry,
    private val clientState: ClientState,
) : DeleteMessageForMeListener {

    /**
     * Updates the state of the message as deleted for me.
     */
    override suspend fun onDeleteMessageForMeRequest(messageId: String) {
        logicRegistry.channelFromMessageId(messageId)
            ?.getMessage(messageId)
            ?.let { message ->
                val isNetworkAvailable = clientState.isNetworkAvailable
                val messageToBeUpdated = message.copy(
                    deletedForMe = true,
                    syncStatus = if (isNetworkAvailable) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED,
                )

                updateMessage(messageToBeUpdated)
            }
    }

    /**
     * Updates the message sync status based on the result of the delete for me operation,
     * handling both success and failure cases.
     */
    override suspend fun onDeleteMessageForMeResult(messageId: String, result: Result<Message>) {
        when (result) {
            is Result.Success -> {
                updateMessage(result.value.copy(syncStatus = SyncStatus.COMPLETED))
            }

            is Result.Failure -> {
                logicRegistry.channelFromMessageId(messageId)
                    ?.getMessage(messageId)
                    ?.let { message ->
                        val messageToBeUpdated = message.copy(
                            deletedForMe = true,
                            syncStatus = SyncStatus.SYNC_NEEDED,
                        )

                        updateMessage(messageToBeUpdated)
                    }
            }
        }
    }

    private fun updateMessage(message: Message) {
        logicRegistry.channelFromMessageId(message.id)?.upsertMessage(message)
        logicRegistry.getActiveQueryThreadsLogic().forEach { it.upsertMessage(message) }
        logicRegistry.threadFromMessageId(message.id)?.upsertMessage(message)
    }
}
