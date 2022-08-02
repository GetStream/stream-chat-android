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
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.internal.toMessageSyncDescription
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.logging.StreamLog
import java.util.Date

private const val TAG = "Chat:SendMessageHandler"

internal class SendMessageListenerImpl(
    private val logic: LogicRegistry,
    private val repos: RepositoryFacade,
) : SendMessageListener {

    override suspend fun onMessageSendResult(
        result: Result<Message>,
        channelType: String,
        channelId: String,
        message: Message,
    ) {
        val cid = "$channelType:$channelId"
        if (result.isSuccess) {
            handleSendMessageSuccess(cid, logic, result.data())
        } else {
            handleSendMessageFail(logic, message, result.error())
        }
    }

    /**
     * Updates the message object and local database with new message state after message is sent successfully.
     *
     * @param processedMessage [Message] returned from API response.
     * @return [Message] Updated message.
     */
    private suspend fun handleSendMessageSuccess(
        cid: String,
        logic: LogicRegistry,
        processedMessage: Message
    ) {
        // Don't update latest message with this id if it is already synced.
        val latestUpdatedMessage = repos.selectMessage(processedMessage.id)
        if (latestUpdatedMessage?.syncStatus == SyncStatus.COMPLETED) {
            return
        }
        processedMessage.enrichWithCid(cid)
            .copy(
                syncStatus = SyncStatus.COMPLETED,
                syncDescription = null
            )
            .also {
                repos.insertMessage(it)
                logic.channelFromMessage(it)?.upsertMessage(it)
                logic.threadFromMessage(it)?.upsertMessage(it)
            }
    }

    /**
     * Updates the message object and local database with new message state after message wasn't sent due to error.
     *
     * @param message [Message] that were being sent.
     * @param error [ChatError] with the reason of failure.
     *
     * @return [Message] Updated message.
     */
    private suspend fun handleSendMessageFail(
        logic: LogicRegistry,
        message: Message,
        error: ChatError
    ) {
        // Don't update latest message with this id if it is already synced.
        val latestUpdatedMessage = repos.selectMessage(message.id) ?: message
        if (latestUpdatedMessage.syncStatus == SyncStatus.COMPLETED) {
            return
        }
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
        )
            .also {
                repos.insertMessage(it)
                logic.channelFromMessage(it)?.upsertMessage(it)
                logic.threadFromMessage(it)?.upsertMessage(it)
            }
    }
}
