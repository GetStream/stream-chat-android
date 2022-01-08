package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi

@ExperimentalStreamChatApi
/**
 * Listener of [ChatClient.queryChannels] requests.
 */
public interface QueryChannelsListener {

    /**
     * Run precondition for the request. If it returns [Result.success] then the request is run otherwise it returns
     * [Result.error] and no request is made.
     */
    public suspend fun onQueryChannelsPrecondition(
        request: QueryChannelsRequest,
    ): Result<Unit> = Result.success(Unit)

    /**
     * Runs side effect before the request is launched.
     *
     * @param request [QueryChannelsRequest] which is going to be used for the request.
     */
    public suspend fun onQueryChannelsRequest(request: QueryChannelsRequest) {}

    /**
     * Runs this function on the [Result] of this [QueryChannelsRequest].
     */
    public suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {}
}
