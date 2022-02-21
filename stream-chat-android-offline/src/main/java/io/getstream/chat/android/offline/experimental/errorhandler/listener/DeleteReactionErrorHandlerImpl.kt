package io.getstream.chat.android.offline.experimental.errorhandler.listener

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.errorhandler.listeners.DeleteReactionErrorHandler
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry

/**
 * [DeleteReactionErrorHandler] implementation for [io.getstream.chat.android.offline.experimental.errorhandler.OfflineErrorHandler].
 * Checks if the change was done offline and can be synced.
 *
 * @param logic [LogicRegistry]
 * @param globalState [GlobalState] provided by the [io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin].
 */
@ExperimentalStreamChatApi
internal class DeleteReactionErrorHandlerImpl(
    private val logic: LogicRegistry,
    private val globalState: GlobalState,
) : DeleteReactionErrorHandler {

    /**
     * Replaces the original response error if the user is offline, [cid] is specified and the message exists in the cache.
     * This means that the message was updated locally but the API request failed due to lack of connection.
     * The request will be synced once user's connection is recovered.
     *
     * @param originalError The original error returned by the API.
     * @param cid The full channel id, i.e. "messaging:123".
     * @param messageId The id of the message to which reaction belongs.
     *
     * @return result The original or offline related result.
     */
    override fun onDeleteReactionError(originalError: ChatError, cid: String?, messageId: String): Result<Message> {
        if (cid == null || globalState.isOnline()) {
            return Result.error(originalError)
        }
        val (channelType, channelId) = cid.cidToTypeAndId()
        val cachedMessage = logic.channel(channelType = channelType, channelId = channelId).getMessage(messageId)

        return if (cachedMessage != null) {
            Result.success(cachedMessage)
        } else {
            Result.error(ChatError(message = "Local message was not found."))
        }
    }

    override val name: String
        get() = "DeleteReactionErrorHandlerImpl"
}
