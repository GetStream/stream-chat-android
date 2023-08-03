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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.isPermanent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.utils.internal.toMessageSyncDescription
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.log.StreamLog
import io.getstream.result.Error
import io.getstream.result.Result
import java.util.Date

private const val TAG = "Chat:SendMessageHandler"

/**
 * Implementation of [SendMessageListener] that deals with updates of state of the SDK.
 */
internal class SendMessageListenerState(private val logic: LogicRegistry) : SendMessageListener {

    /**
     * Side effect to be invoked when the original request is completed with a response. This method updates the state
     * of the SDK.
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
        handleLastSentMessageDate(result, message)

        val cid = "$channelType:$channelId"

        if (logic.getMessageById(message.id)?.syncStatus == SyncStatus.COMPLETED) return

        when (result) {
            is Result.Success -> handleSendMessageSuccess(cid, logic, result.value)
            is Result.Failure -> handleSendMessageFail(logic, message, result.value)
        }
    }

    /**
     * Updates the message object and local database with new message state after message is sent successfully.
     *
     * @param processedMessage [Message] returned from API response.
     * @return [Message] Updated message.
     */
    private fun handleSendMessageSuccess(
        cid: String,
        logic: LogicRegistry,
        processedMessage: Message,
    ) {
        processedMessage.enrichWithCid(cid)
            .copy(
                syncStatus = SyncStatus.COMPLETED,
                syncDescription = null,
            )
            .also { message ->
                logic.channelFromMessage(message)?.upsertMessage(message)
                logic.threadFromMessage(message)?.upsertMessage(message)
            }
    }

    /**
     * Updates the message object and local database with new message state after message wasn't sent due to error.
     *
     * @param message [Message] that were being sent.
     * @param error [Error] with the reason of failure.
     *
     * @return [Message] Updated message.
     */
    private fun handleSendMessageFail(
        logic: LogicRegistry,
        message: Message,
        error: Error,
    ) {
        val isPermanentError = error.isPermanent()
        val isMessageModerationFailed = error is Error.NetworkError &&
            error.serverErrorCode == ChatErrorCode.MESSAGE_MODERATION_FAILED.code
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
        ).also {
            logic.channelFromMessage(it)?.upsertMessage(it)
            logic.threadFromMessage(it)?.upsertMessage(it)
        }
    }

    /**
     * Updates the channel state container with the information about the last message send date.
     *
     * Note: Sometimes [NewMessageEvent] event arrives faster than [onMessageSendResult] is executed.
     * As a result, the sync status is already [SyncStatus.COMPLETED] and [handleSendMessageSuccess]
     * is never executed. That's why this method is executed as early as possible.
     *
     * @param result [Result] response from the original request.
     * @param message [Message] to be sent.
     */
    private fun handleLastSentMessageDate(
        result: Result<Message>,
        message: Message,
    ) {
        if (result is Result.Success) {
            logic.channelFromMessage(message)?.setLastSentMessageDate(result.value.createdAt)
        }
    }
}
