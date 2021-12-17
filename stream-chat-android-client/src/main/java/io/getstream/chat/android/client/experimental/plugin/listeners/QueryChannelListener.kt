package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi

@ExperimentalStreamChatApi
/**
 * Listener of [ChatClient.queryChannel] requests.
 */
public interface QueryChannelListener {
    /**
     * Run precondition for the request. If it returns [Result.isSuccess] then the request is run otherwise it returns
     * [Result.error] and no request is made.
     */
    public suspend fun onQueryChannelPrecondition(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Result<Unit> = Result.success(Unit)

    /**
     * Runs side effect before the request is launched.
     *
     * @param channelType Type of the requested channel.
     * @param channelId Id of the requested channel.
     * @param request [QueryChannelRequest] which is going to be used for the request.
     */
    public suspend fun onQueryChannelRequest(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) { }

    /**
     * Runs this function on the result of the request.
     */
    public suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
    }
}
