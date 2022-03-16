package io.getstream.chat.android.offline.internal.utils

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.Result

/* Default filter to include FilterObject in a channel by its cid
*
* @param client - ChatClient to perform the filter
* @param cid - The cid of the channel of the filter
* @param filter - the filter to be included with the cid.
*/
internal object ChannelFilterRequest {
    suspend fun filter(client: ChatClient, cid: String, filter: FilterObject): Result<List<Channel>> =
        client.queryChannelsInternal(
            QueryChannelsRequest(
                filter = Filters.and(
                    filter,
                    Filters.eq("cid", cid)
                ),
                offset = 0,
                limit = 1,
                messageLimit = 0,
                memberLimit = 0,
            )
        ).await()
}
