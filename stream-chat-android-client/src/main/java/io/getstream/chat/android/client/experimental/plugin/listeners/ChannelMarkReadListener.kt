package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.utils.Result

/**
 * Listener for [ChatClient.markRead] requests.
 */
public interface ChannelMarkReadListener {

    /**
     * Run precondition for the request. If it returns [Result.isSuccess] then the request is run otherwise it returns
     * [Result.error] and no request is made.
     *
     * @param channelType Type of the channel to mark as read.
     * @param channelId Id of the channel to mark as read.
     *
     * @return [Result.success] if precondition passes, otherwise [Result.error].
     */
    public suspend fun onChannelMarkReadPrecondition(
        channelType: String,
        channelId: String,
    ): Result<Unit>
}
