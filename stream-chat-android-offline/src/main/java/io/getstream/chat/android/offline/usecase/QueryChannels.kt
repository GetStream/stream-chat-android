package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import kotlinx.coroutines.launch

internal class QueryChannels(private val domainImpl: ChatDomainImpl) {
    /**
     * Queries offline storage and the API for channels matching the filter
     * Returns a queryChannelsController
     *
     * @param filter The filter object.
     * @param sort How to sort the channels (default is last_message_at).
     * @param limit The number of channels to retrieve.
     * @param messageLimit How many messages to retrieve per channel.
     * @param memberLimit The number of members per channel.
     *
     * @see io.getstream.chat.android.offline.querychannels.QueryChannelsController
     * @see io.getstream.chat.android.client.utils.FilterObject
     * @see io.getstream.chat.android.client.api.models.QuerySort
     * @see <a href="https://getstream.io/chat/docs/query_channels/?language=kotlin">Filter syntax</a>
     */
    @CheckResult
    operator fun invoke(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        limit: Int = 30,
        messageLimit: Int = 1,
        memberLimit: Int = 30,
    ): Call<QueryChannelsController> {
        val queryChannelsControllerImpl = domainImpl.queryChannels(filter, sort)
        return CoroutineCall(domainImpl.scope) {
            if (limit > 0) {
                domainImpl.scope.launch {
                    queryChannelsControllerImpl.query(
                        channelLimit = limit,
                        messageLimit = messageLimit,
                        memberLimit = memberLimit,
                    )
                }
            }
            Result(queryChannelsControllerImpl)
        }
    }
}
