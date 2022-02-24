package io.getstream.chat.android.offline.experimental.plugin.listener

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.repository.RepositoryFacade
import java.util.Date

@ExperimentalStreamChatApi
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
        val channel = logic.channel(channelType, channelId)
        if (result.isSuccess) {
            handleSendMessageSuccess(channel, result.data())
        } else {
            handleSendMessageFail(channel, message, result.error())
        }
    }

    /**
     * Updates the message object and local database with new message state after message is sent successfully.
     *
     * @param processedMessage [Message] returned from API response.
     * @return [Message] Updated message.
     */
    private suspend fun handleSendMessageSuccess(channel: ChannelLogic, processedMessage: Message) {
        // Don't update latest message with this id if it is already synced.
        val latestUpdatedMessage = repos.selectMessage(processedMessage.id) ?: processedMessage
        if (latestUpdatedMessage.syncStatus == SyncStatus.COMPLETED) {
            return
        }
        latestUpdatedMessage.enrichWithCid(channel.cid)
            .copy(syncStatus = SyncStatus.COMPLETED)
            .also {
                repos.insertMessage(it)
                channel.upsertMessage(it)
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
    private suspend fun handleSendMessageFail(channel: ChannelLogic, message: Message, error: ChatError) {
        // Don't update latest message with this id if it is already synced.
        val latestUpdatedMessage = repos.selectMessage(message.id) ?: message
        if (latestUpdatedMessage.syncStatus == SyncStatus.COMPLETED) {
            return
        }
        message.copy(
            syncStatus = if (error.isPermanent()) {
                SyncStatus.FAILED_PERMANENTLY
            } else {
                SyncStatus.SYNC_NEEDED
            },
            updatedLocallyAt = Date(),
        )
            .also {
                repos.insertMessage(it)
                channel.upsertMessage(it)
            }
    }
}
