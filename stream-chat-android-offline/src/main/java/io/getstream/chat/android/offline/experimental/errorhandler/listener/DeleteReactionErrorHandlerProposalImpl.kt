package io.getstream.chat.android.offline.experimental.errorhandler.listener

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.ReturnOnErrorCall
import io.getstream.chat.android.client.call.onErrorReturn
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.errorhandler.listeners.DeleteReactionErrorHandlerProposal
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import kotlinx.coroutines.CoroutineScope

@ExperimentalStreamChatApi
internal class DeleteReactionErrorHandlerProposalImpl(
    private val scope: CoroutineScope,
    private val logic: LogicRegistry,
    private val globalState: GlobalState,
) : DeleteReactionErrorHandlerProposal {

    override fun onDeleteReactionError(
        originalCall: Call<Message>,
        cid: String?,
        messageId: String,
    ): ReturnOnErrorCall<Message> {
        return originalCall.onErrorReturn(scope) { originalError ->
            if (cid == null || globalState.isOnline()) {
                Result.error<Message>(originalError)
            }
            val (channelType, channelId) = cid!!.cidToTypeAndId()
            val cachedMessage = logic.channel(channelType = channelType, channelId = channelId).getMessage(messageId)

            if (cachedMessage != null) {
                Result.success(cachedMessage)
            } else {
                Result.error(ChatError(message = "Local message was not found."))
            }
        }
    }

    override val name: String
        get() = "DeleteReactionErrorHandlerProposalImpl"
}
