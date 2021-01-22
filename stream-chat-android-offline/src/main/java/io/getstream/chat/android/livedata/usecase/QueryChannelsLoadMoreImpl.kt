package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.ChatDomainImpl

public interface QueryChannelsLoadMore {
    /**
     * Load more channels for this query
     * @param filter the filter for querying channels, see https://getstream.io/chat/docs/query_channels/?language=kotlin
     * @param sort the sort for the channels, by default will sort on last_message_at
     * @param limit the number of channels to retrieve
     * @param messageLimit how many messages to fetch per chanel
     * @see io.getstream.chat.android.client.utils.FilterObject
     * @see io.getstream.chat.android.client.api.models.QuerySort
     * @see <a href="https://getstream.io/chat/docs/query_channels/?language=kotlin">Filter syntax</a>
     */
    @CheckResult
    public operator fun invoke(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        limit: Int = 30,
        messageLimit: Int = 10,
    ): Call<List<Channel>>
}

internal class QueryChannelsLoadMoreImpl(private val domainImpl: ChatDomainImpl) : QueryChannelsLoadMore {
    override operator fun invoke(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        limit: Int,
        messageLimit: Int,
    ): Call<List<Channel>> {
        return CoroutineCall(domainImpl.scope) {
            val queryChannelsController = domainImpl.queryChannels(filter, sort)
            queryChannelsController.loadMore(limit, messageLimit)
        }
    }
}
