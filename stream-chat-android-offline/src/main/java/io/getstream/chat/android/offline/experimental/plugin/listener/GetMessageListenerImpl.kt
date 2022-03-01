package io.getstream.chat.android.offline.experimental.plugin.listener

import io.getstream.chat.android.client.experimental.plugin.listeners.GetMessageListener
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry

internal class GetMessageListenerImpl(private val logic: LogicRegistry) : GetMessageListener {
    override suspend fun onGetMessageResult(
        result: Result<Message>,
        cid: String,
        messageId: String,
        olderMessagesOffset: Int,
        newerMessagesOffset: Int,
    ) {
        cid.cidToTypeAndId().let { (channelType, channelId) ->
            logic.channel(channelType, channelId)
                .onGetMessageResult(result, cid, messageId, olderMessagesOffset, newerMessagesOffset)
        }
    }

    override suspend fun onGetMessageError(
        cid: String,
        messageId: String,
        olderMessagesOffset: Int,
        newerMessagesOffset: Int,
    ): Result<Message> = cid.cidToTypeAndId().let { (channelType, channelId) ->
        logic.channel(channelType, channelId)
            .onGetMessageError(cid, messageId, olderMessagesOffset, newerMessagesOffset)
    }
}
