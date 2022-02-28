package io.getstream.chat.android.client.experimental.errorhandler.listeners

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.ReturnOnErrorCall
import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * Error handler for [io.getstream.chat.android.client.ChatClient.queryMembers] calls.
 */
@ExperimentalStreamChatApi
public interface QueryMembersErrorHandler : ErrorHandler {

    /**
     * Returns a [Result] from this side effect when original request is failed.
     *
     * @param originalCall The original call.
     * @param cid The full channel id, i.e. "messaging:123".
     * @param messageId The id of the message to which reaction belongs.
     *
     * @return result The replacement for the original result.
     */
    public fun onQueryMembersError(
        originalCall: Call<List<Member>>,
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySort<Member>,
        members: List<Member>,
    ): ReturnOnErrorCall<List<Member>>
}

@ExperimentalStreamChatApi
internal fun Call<List<Member>>.onQueryMembersError(
    errorHandlers: List<QueryMembersErrorHandler>,
    channelType: String,
    channelId: String,
    offset: Int,
    limit: Int,
    filter: FilterObject,
    sort: QuerySort<Member>,
    members: List<Member>,
): Call<List<Member>> {
    return errorHandlers.fold(this) { messageCall, errorHandler ->
        errorHandler.onQueryMembersError(messageCall, channelType, channelId, offset, limit, filter, sort, members)
    }
}
