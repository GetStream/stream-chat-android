package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result

/**
 * Listener of [ChatClient.getMessage] requests.
 */
public interface GetMessageListener {

    /**
     * Register this side effect to run on the result of the request.
     *
     * @param result Result of this request.
     * @param cid CID of the channel.
     * @param messageId ID of the message which is fetched.
     * @param olderMessagesOffset Offset for older messages.
     * @param newerMessagesOffset Offset for newer messages.
     */
    public suspend fun onGetMessageResult(
        result: Result<Message>,
        cid: String,
        messageId: String,
        olderMessagesOffset: Int = 0,
        newerMessagesOffset: Int = 0,
    )

    /**
     * Returns a [Result] from this side effect when original request is failed.
     *
     * @param cid CID of the channel.
     * @param messageId ID of the message which is fetched.
     * @param olderMessagesOffset Offset for older messages.
     * @param newerMessagesOffset Offset for newer messages.
     */
    public suspend fun onGetMessageError(
        cid: String,
        messageId: String,
        olderMessagesOffset: Int = 0,
        newerMessagesOffset: Int = 0,
    ): Result<Message>
}
