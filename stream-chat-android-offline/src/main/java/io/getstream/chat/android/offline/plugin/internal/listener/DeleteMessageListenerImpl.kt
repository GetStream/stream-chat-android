package io.getstream.chat.android.offline.plugin.internal.listener

import io.getstream.chat.android.client.experimental.plugin.listeners.DeleteMessageListener
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.internal.repository.domain.message.MessageRepository
import io.getstream.chat.android.offline.plugin.internal.logic.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import java.util.Date

/**
 * Listener for requests of message deletion and for message deletion results.
 */
internal class DeleteMessageListenerImpl(
    private val logic: LogicRegistry,
    private val globalState: GlobalState,
    private val messageRepository: MessageRepository,
) : DeleteMessageListener {

    /**
     * Method called when a request to delete a message in the API happens
     *
     * @param messageId
     */
    override suspend fun onMessageDeleteRequest(messageId: String) {
        messageRepository.selectMessage(messageId)?.let { message ->
            val isOnline = globalState.isOnline()

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
