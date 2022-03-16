package io.getstream.chat.android.client.experimental.errorhandler

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.ReturnOnErrorCall
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Error handler for [io.getstream.chat.android.client.ChatClient.createChannel] calls.
 */
@InternalStreamChatApi
public interface CreateChannelErrorHandler : ErrorHandler {

    /**
     * Returns a [Result] from this side effect when original request fails.
     *
     * @param originalCall The original call.
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param memberIds The list of members' ids.
     * @param extraData Map of key-value pairs that let you store extra data
     *
     * @return result The replacement for the original result.
     */
    public fun onCreateChannelError(
        originalCall: Call<Channel>,
        channelType: String,
        channelId: String,
        memberIds: List<String>,
        extraData: Map<String, Any>,
    ): ReturnOnErrorCall<Channel>
}

internal fun Call<Channel>.onCreateChannelError(
    errorHandlers: List<CreateChannelErrorHandler>,
    channelType: String,
    channelId: String,
    memberIds: List<String>,
    extraData: Map<String, Any>,
): Call<Channel> {
    return errorHandlers.fold(this) { createChannelCall, errorHandler ->
        errorHandler.onCreateChannelError(
            originalCall = createChannelCall,
            channelId = channelId,
            channelType = channelType,
            memberIds = memberIds,
            extraData = extraData
        )
    }
}
