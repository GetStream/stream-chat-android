package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi

@ExperimentalStreamChatApi
/**
 * Listener of [ChatClient.queryChannel] requests.
 */
public interface GetMessageListener {

    /**
     * Runs this function on the result of the request.
     */
    public suspend fun onGetMessageResult(
        result: Result<Message>,
        cid: String,
        messageId: String,
        olderMessagesOffset: Int = 0,
        newerMessagesOffset: Int = 0,
    )
}
