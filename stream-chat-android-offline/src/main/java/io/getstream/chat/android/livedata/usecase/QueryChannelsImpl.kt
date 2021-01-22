package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.QueryChannelsController
import kotlinx.coroutines.launch

public interface QueryChannels {
    /**
     * Queries offline storage and the API for channels matching the filter
     * Returns a queryChannelsController
     *
     * @param filter the filter object
     * @param sort how to sort the channels (default is last_message_at)
     * @param limit the number of channels to retrieve
     * @param messageLimit how many messages to retrieve per channel
     *
     * @see io.getstream.chat.android.livedata.controller.QueryChannelsController
     * @see io.getstream.chat.android.client.utils.FilterObject
     * @see io.getstream.chat.android.client.api.models.QuerySort
     * @see <a href="https://getstream.io/chat/docs/query_channels/?language=kotlin">Filter syntax</a>
     */
    @CheckResult
    public operator fun invoke(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        limit: Int = 30,
        messageLimit: Int = 1,
    ): Call<QueryChannelsController>
}

internal class QueryChannelsImpl(private val domainImpl: ChatDomainImpl) : QueryChannels {
    override operator fun invoke(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        limit: Int,
        messageLimit: Int,
    ): Call<QueryChannelsController> {
        val queryChannelsControllerImpl = domainImpl.queryChannels(filter, sort)
        return CoroutineCall(domainImpl.scope) {
            if (limit > 0) {
                domainImpl.scope.launch {
                    queryChannelsControllerImpl.query(limit, messageLimit)
                }
            }
            Result(queryChannelsControllerImpl)
        }
    }
}
