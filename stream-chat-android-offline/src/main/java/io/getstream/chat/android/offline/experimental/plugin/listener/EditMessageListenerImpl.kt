package io.getstream.chat.android.offline.experimental.plugin.listener

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.plugin.listeners.EditMessageListener
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.extensions.isPermanent
import java.util.Date

@ExperimentalStreamChatApi
internal class EditMessageListenerImpl(
    private val logic: LogicRegistry,
    private val globalState: GlobalState
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

        updateAndSaveMessages(messagesToEdit, channelLogic)
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

            updateAndSaveMessages(messages, channelLogic)
        } else {
            val channelLogic = channelLogicForMessage(originalMessage)
            val failedMessage = originalMessage.updateFailedMessage(result.error()).let(::listOf)

            updateAndSaveMessages(failedMessage, channelLogic)
        }
    }

    /**
     * Updates the messages locally and saves it at database.
     *
     * @param messages The list of messages to be updated in the SDK and to be saved in database.
     * @param channelLogic [ChannelLogic].
     */
    private suspend fun updateAndSaveMessages(messages: List<Message>, channelLogic: ChannelLogic) {
        channelLogic.upsertMessages(messages)
        channelLogic.storeMessageLocally(messages)
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
